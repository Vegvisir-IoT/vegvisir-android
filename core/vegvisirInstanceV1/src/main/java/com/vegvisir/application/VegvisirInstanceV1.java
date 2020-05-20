package com.vegvisir.application;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.isaacsheff.charlotte.proto.Block;
import com.vegvisir.VegvisirCore;
import com.vegvisir.core.blockdag.DataManager;
import com.vegvisir.core.blockdag.NewBlockListener;
import com.vegvisir.core.blockdag.ReconciliationEndListener;
import com.vegvisir.core.config.Config;
import com.vegvisir.core.datatype.proto.Block.Transaction;
import com.vegvisir.core.reconciliation.VectorClockProtocol;
import com.vegvisir.gossip.adapter.NetworkAdapterManager;
import com.vegvisir.pub_sub.TransactionID;
import com.vegvisir.pub_sub.VegvisirApplicationContext;
import com.vegvisir.pub_sub.VegvisirApplicationDelegator;
import com.vegvisir.pub_sub.VegvisirInstance;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * The ADD ALL BLOCKS reconciliation protocol instance.
 */
public class VegvisirInstanceV1 implements VegvisirInstance, NewBlockListener, ReconciliationEndListener {

    /**
     * The block DAG instance.
     */
    private VegvisirCore core;

    /**
     * storing all incoming transactions.
     */
    private LinkedBlockingDeque<com.vegvisir.core.datatype.proto.Block.Transaction> transactionQueue;


    private ConcurrentHashMap<String, Set<String>> topic2app;

    private ConcurrentHashMap<String, VegvisirApplicationDelegator> app2handler;

    private ConcurrentHashMap<TransactionID, com.isaacsheff.charlotte.proto.Hash> tx2block;

    private KeyPair keyPair;

    private String deviceID;

    private DataManager dataManager;

    private int appCount = 0;
    private int backupCount = 0;


    private static boolean recovered = false;

    private static String PUB_FILENAME = "pub";
    private static String PRV_FILENAME = "prv";

    /**
     * The singleton instance.
     */
    private static VegvisirInstanceV1 instance;

    public static synchronized VegvisirInstance getInstance(Context applicationContext) {
        if (instance == null) {
            instance = new VegvisirInstanceV1(applicationContext);
        }
        return instance;
    }

    private VegvisirInstanceV1(Context ctx) {
        transactionQueue = new LinkedBlockingDeque<>();
        topic2app = new ConcurrentHashMap<>();
        app2handler = new ConcurrentHashMap<>();
        tx2block = new ConcurrentHashMap<>();
        keyPair = getKeyPair(ctx);
        deviceID = Config.pk2str(keyPair.getPublic());
        dataManager = VegvisirDataManager.getDataManager(ctx);
        backupCount = dataManager.loadAppCount();
        NetworkAdapterManager networkAdapterManager = new NetworkAdapterManager();
        networkAdapterManager.registerAdapter("GoogleNearBy", 100, new AndroidAdapter(ctx, deviceID));
        core = new VegvisirCore(networkAdapterManager,
//                VectorClockProtocol.class,
                dataManager,
                this,
                createGenesisBlock(keyPair),
                keyPair,
                deviceID
        );
        core.registerReconciliationEndListener(this);

        new Thread(this::pollTransactions).start();
        new Thread(core).start();
    }

    /**
     * Load existing key pairs. If there is no key pair available, then generate a new one and store
     * the pair to the files. This allows application to use the same key pair after reboot. Key pairs
     * are stored into the internal storage of this app.
     * @param ctx the application context for storing data.
     * @return a key pair.
     */
    private static synchronized KeyPair getKeyPair(Context ctx) {
        KeyPair keyPair;
        File pub = new File(ctx.getFilesDir(), PUB_FILENAME);
        File prv = new File(ctx.getFilesDir(), PRV_FILENAME);
        try {
            if (pub.exists() && prv.exists()) {
                InputStream stream = new FileInputStream(pub);
                ByteString bytes = ByteString.readFrom(stream);
                PublicKey publicKey = Config.bytes2pk(bytes.toByteArray());
                stream = new FileInputStream(prv);
                bytes = ByteString.readFrom(stream);
                PrivateKey privateKey = Config.bytes2prk(bytes.toByteArray());
                keyPair = new KeyPair(publicKey, privateKey);
                stream.close();
            } else {
                keyPair = Config.generateKeypair();
                OutputStream stream = new FileOutputStream(pub);
                stream.write(keyPair.getPublic().getEncoded());
                stream.close();
                stream = new FileOutputStream(prv);
                stream.write(keyPair.getPrivate().getEncoded());
                stream.close();
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex.getLocalizedMessage());

        } catch (IOException ex) {
            throw new RuntimeException(ex.getLocalizedMessage());
        }
        return keyPair;
    }


    /**
     * A thread runs forever for polling and applying new transactions.
     */
    private void pollTransactions() {
        while (true) {
            try{
                Transaction tx = transactionQueue.take();

                Set<TransactionID> deps = new HashSet<>();
                for (Transaction.TransactionId id : tx.getDependenciesList()) {
                    deps.add(new TransactionID(id.getDeviceId(), id.getTransactionHeight()));
                }
                List<String> topics = tx.getTopicsList();

                /* TODO: May want run this in separate threads to avoid being blocked by application code */
                getAppHandlers(new HashSet<>(topics)).forEach( (handler, _topics) -> {
                    handler.applyTransaction(
                            _topics,
                            tx.getPayload().toByteArray(),
                            new TransactionID(tx.getTransactionId().getDeviceId(), tx.getTransactionId().getTransactionHeight()),
                            deps);

                });
            } catch (InterruptedException ex) {
//                System.err.println("Interrupted transaction polling thread! Will exit.");
                break;
            }
        }
    }


    /**
     * A helper method for gathering transaction handlers for a transaction with given topics.
     * The method returns a map mapping from handler to a set of topics that appear in both the
     * set of topics that the application is listening on and the set of transaction that
     * the transaction contains.
     * listening on.
     * @param topics a set of topics in the given transaction.
     * @return
     */
    private Map<VegvisirApplicationDelegator, Set<String>> getAppHandlers(Set<String> topics) {
        Map<VegvisirApplicationDelegator, Set<String>> delegatorTopics = new HashMap<>();
        for (String topic : topics) {
            if (!topic2app.containsKey(topic))
                continue;
            topic2app.get(topic).forEach(app -> {
                VegvisirApplicationDelegator delegator = app2handler.get(app);
                if (!delegatorTopics.containsKey(delegator)) {
                    delegatorTopics.put(delegator, new HashSet<>());
                }
                delegatorTopics.get(delegator).add(topic);
            });
        }
        return delegatorTopics;
    }

    /**
     * Register a delegator, which will handle new transactions for that application.
     * After the registration, new transactions will be forward to the delegator at most once.
     * If there already is delegator for that application, then this one replaces the old one.
     * However, transactions that already been sent to the old delegator will be processed by the
     * old one.
     *
     * @param context   a context object of the application.
     * @param delegator a delegator instance.
     * @return true if the @delegator is successfully registered.
     */
    @Override
    public synchronized boolean registerApplicationDelegator(final VegvisirApplicationContext context, VegvisirApplicationDelegator delegator) {
        if (context.getChannels() == null || context.getAppID() == null || context.getChannels().isEmpty())
            return false;
        context.getChannels().forEach(t -> {
            if (!topic2app.containsKey(t)) {
                topic2app.putIfAbsent(t, new HashSet<>());
            }
            topic2app.get(t).add(context.getAppID());
        });
        app2handler.put(context.getAppID(), delegator);
        appCount = app2handler.keySet().size();
        dataManager.updateAppCount(appCount);

        if (appCount == backupCount && !recovered) {
            recovered = true;
            core.tryRecoverBlocks();
        }
        return true;
    }

    /**
     * Add a new transaction to the DAG. If the transaction is valid, then it will be added to the
     * block, either current one or next one depends on the transaction queue size. If the transaction
     * is valid, then this transaction will be pass to applyTransaction immediately to let application
     * update its states.
     *
     * @param context      a context object of the application.
     * @param topics       a set of pub/sub topic that unique identify who are interested in this transaction.
     * @param payload      a application defined data payload in byte array format.
     * @param dependencies a set of transactionIds that this transaction depends on.
     * @return true, if the transaction is valid.
     */
    @Override
    public synchronized boolean addTransaction(VegvisirApplicationContext context, Set<String> topics, byte[] payload, Set<TransactionID> dependencies) {
        List<com.vegvisir.core.datatype.proto.Block.Transaction.TransactionId> deps = new ArrayList<>();
        for (TransactionID id : dependencies) {
            deps.add(com.vegvisir.core.datatype.proto.Block.Transaction.TransactionId.newBuilder().setTransactionHeight(id.getTransactionHeight()).setDeviceId(id.getDeviceID()).build());
        }
        core.createTransaction(deps, topics, payload);
        return true;
    }

    @Override
    public String getThisDeviceID() {
        return deviceID;
    }

    @Override
    public Set<String> getWitnessForTransaction(TransactionID id) {
        if (tx2block.containsKey(id))
            return core.findWitnessForBlock(tx2block.get(id));
        else
            throw new RuntimeException("No such transaction available in the blockchain " + id.toString());
    }

    /**
     * Called when a new block arrived.
     * Add all transaction in the block to the transaction queue.
     *
     * @param block a charlotte block.
     */
    @Override
    public void onNewBlock(Block block) {
        com.isaacsheff.charlotte.proto.Hash bh = Config.sha3(block);
        com.vegvisir.core.datatype.proto.Block _block;
        try {
            _block = com.vegvisir.core.datatype.proto.Block.parseFrom(block.getBlock());
        } catch (InvalidProtocolBufferException ex) {
            ex.printStackTrace();
            return;
        }
        _block.getUserBlock().getTransactionsList().parallelStream().forEach(transaction -> {
            tx2block.put(txIDFromProto(transaction), bh);
        });
        transactionQueue.addAll(_block.getUserBlock().getTransactionsList());
    }

    @Override
    public void onReconciliationEnd() {
        app2handler.values().forEach(VegvisirApplicationDelegator::onNewReconciliationFinished);
    }

    /**
     * Created a genesis block signed by given keypair.
     * @param keyPair a key pair used to sign the block.
     * @return a genesis block with empty content.
     */
    private Block createGenesisBlock(KeyPair keyPair) {
        com.vegvisir.core.datatype.proto.Block.GenesisBlock genesis =
                                com.vegvisir.core.datatype.proto.Block.GenesisBlock.newBuilder().
                                        build();
        return Block.newBuilder().
                setBlock(com.vegvisir.core.datatype.proto.Block.newBuilder()
                .setGenesisBlock(genesis).build().toByteString())
                .build();
    }

    private TransactionID txIDFromProto(Transaction tx) {
        return new TransactionID(tx.getTransactionId().getDeviceId(), tx.getTransactionId().getTransactionHeight());
    }

    private Transaction.TransactionId txID2Proto(TransactionID id) {
        return Transaction.TransactionId.newBuilder().setDeviceId(id.getDeviceID()).setTransactionHeight(id.getTransactionHeight())
                .build();
    }
}
