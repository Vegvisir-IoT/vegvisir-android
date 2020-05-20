package com.vegvisir;

import com.google.protobuf.ByteString;
import com.vegvisir.core.blockdag.BlockDAG;
import com.vegvisir.core.blockdag.BlockDAGv1;
import com.vegvisir.core.blockdag.BlockDAGv2;
import com.vegvisir.core.blockdag.BlockUtil;
import com.vegvisir.core.blockdag.DataManager;
import com.vegvisir.core.blockdag.NewBlockListener;
import com.vegvisir.core.blockdag.ReconciliationEndListener;
import com.vegvisir.core.config.Config;
import com.vegvisir.core.reconciliation.ReconciliationDispatcher;
import com.vegvisir.core.reconciliation.SendAllProtocol;
import com.vegvisir.core.reconciliation.exceptions.VegvisirReconciliationException;
import com.vegvisir.gossip.*;
import com.vegvisir.gossip.adapter.NetworkAdapter;
import com.isaacsheff.charlotte.proto.Block;
import com.vegvisir.core.datatype.proto.Block.Transaction;
import com.vegvisir.gossip.adapter.NetworkAdapterManager;


import java.security.KeyPair;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Vegvisir reconciliation protocol version 0.1
 * We will use this class as a start point for vegivisir com.vegvisir.core.blockdag.
 */
public class VegvisirCore implements Runnable {


    /* Gossip layer for this Vegvisir block DAG instance */
    private Gossip gossipLayer;

    /* Block DAG containing real blocks */
    private final BlockDAGv2 dag;

    /* Protocol that this instance will use for reconciliation with peers */
//    private Class<? extends ReconciliationDispatcher> protocol;

    /* Config object contains device configuration such as key pairs */
    private Config config;

    private Map<String, ReconciliationDispatcher> disconnectHandlers;

    private ReconciliationEndListener reconciliationEndListener;


    /**
     * A sequence counter for transactions on this device.
     * TODO: move this to config?
     */
    private long transactionHeight;
    private final int BLOCK_SIZE = 1;

    private Set<Transaction> transactionBuffer;
    private DataManager manager;


    private ExecutorService service;

    private static final Logger logger = Logger.getLogger(VegvisirCore.class.getName());


    /**
     * Constructor for a Core instance. Core contains a block dag for storing all blocks; a gossip layer to disseminate new blocks.
     * And a protocol blueprint for doing reconciliation.
     * @param adapterManager an adapter for network layer. This could be an adapter for TCP or Google Nearby.
//     * @param protocol a reconciliation protocol class.
     * @param genesisBlock
     */
    public VegvisirCore(NetworkAdapterManager adapterManager,
//                        Class<? extends ReconciliationDispatcher> protocol,
                        DataManager manager,
                        NewBlockListener listener,
                        Block genesisBlock,
                        KeyPair keyPair,
                        String userid) {
        gossipLayer = new Gossip(adapterManager);
        this.manager = manager;
        transactionHeight = manager.loadTransactionHeight();

        if (keyPair == null)
            keyPair = Config.generateKeypair();

        if (userid == null || userid.isEmpty())
            userid = ByteString.copyFrom(keyPair.getPublic().getEncoded()).toString();

        config = new Config(userid, keyPair);

//        if (protocol.getName().equals(SendAllProtocol.class.getName()))
//            dag = new BlockDAGv1(genesisBlock, config, manager, listener);
//        else
        dag = new BlockDAGv2(genesisBlock, config, manager, listener);

//        this.protocol = protocol;
        service = Executors.newCachedThreadPool();
        transactionBuffer = new HashSet<>();
        disconnectHandlers = new HashMap<>();
        adapterManager.onConnectionLost(this::onLostConnection);
    }

//    public VegvisirCore(NetworkAdapter adapter, Class<ReconciliationDispatcher> protocol) {
//        this(adapter, protocol, null, null, null, null);
//    }
//
//    public VegvisirCore(NetworkAdapter adapter) {
//        this(adapter, SendAllProtocol.class, null, null, null);
//    }

//    public void updateProtocol(Class<? extends ReconciliationDispatcher> newProtocol)
//    {
//        /* Don't know whether this will cause a issue if a race condition happens */
//        this.protocol = newProtocol;
//    }

    /**
     * @return the block dag for this Core instance.
     */
    public BlockDAG getDag() {
        return dag;
    }

    @Override
    public void run() {

        /* Main loop for reconciliation */
        while (true) {
            String remoteId = waitingForNewConnection();
            if (remoteId != null) {
                /* A new instance of protocol is created for each new connection.
                 * As a result, reconciliation process is stateless after it finishes.
                 * This can make it easy to update protocol version */
                service.submit(() -> {
                    try {
                        gossipLayer.linkReconciliationInstanceWithConnection(remoteId, Thread.currentThread());
                        ReconciliationDispatcher dispatcher = new ReconciliationDispatcher(gossipLayer, remoteId, dag);
                        disconnectHandlers.put(remoteId, dispatcher);
                        dispatcher.reconcile();
                    } finally {
                        System.out.println("run: CATCH ALL REACHED");
                        gossipLayer.disconnect(remoteId);
                        if (disconnectHandlers.containsKey(remoteId)) {
                            disconnectHandlers.remove(remoteId);
                        }
                        if (reconciliationEndListener != null)
                            reconciliationEndListener.onReconciliationEnd();
                    }
                });
            }
        }
    }

    public void onLostConnection(String id) {
        if (disconnectHandlers.containsKey(id)) {
            disconnectHandlers.get(id).onDisconnected();
        }
    }

    public Set<String> findWitnessForBlock(com.isaacsheff.charlotte.proto.Hash bh) {
        return dag.computeWitness(BlockUtil.byRef(bh));
    }

    private String waitingForNewConnection() {
        return gossipLayer.randomPickAPeer();
    }


    public void registerNewBlockListener(NewBlockListener listener) {
        dag.setNewBlockListener(listener);
    }

    public void registerReconciliationEndListener(ReconciliationEndListener listener) {
        reconciliationEndListener = listener;
    }

    public synchronized boolean createTransaction(Collection<Transaction.TransactionId> deps,
                                     Set<String> topics,
                                     byte[] payload) {
        com.vegvisir.core.datatype.proto.Block.Transaction.Builder builder = com.vegvisir.core.datatype.proto.Block.Transaction.newBuilder();
        builder.addAllDependencies(deps)
                .addAllTopics(topics)
                .setPayload(ByteString.copyFrom(payload));
        com.vegvisir.core.datatype.proto.Block.Transaction.TransactionId id = com.vegvisir.core.datatype.proto.Block.Transaction.TransactionId.newBuilder()
                .setDeviceId(config.getDeviceID())
                .setTransactionHeight(getNIncTransactionHeight())
                .build();
        builder.setTransactionId(id);
        transactionBuffer.add(builder.build());
        if (transactionBuffer.size() >= BLOCK_SIZE) {
            dag.createBlock(transactionBuffer, getDag().getFrontierBlocks());
            transactionBuffer.clear();
        }
        return true;
    }

    private long getNIncTransactionHeight() {
        manager.updateTransactionHeight(transactionHeight+1);
        return transactionHeight++;

    }

    public void tryRecoverBlocks() {
        dag.recoverBlocks();
    }
}
