package com.vegvisir.pub_sub;

import com.google.protobuf.ByteString;
import com.vegvisir.core.datatype.proto.Block;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;


/**
 * This class is a virtual Vegvisir instance for application layer test purpose.
 * There no real reconciliation and networking happen in this class. Application developers can use
 * this class to test functionality of code above pub/sub layer.
 */
public class VirtualVegvisirInstance implements VegvisirInstance {


    /* Map device to its transaction height */
    private Map<String, Long> deviceToTransactionHeight;

    /* a transaction queue, this will simulate a new transaction arrives from the wire */
    private LinkedBlockingDeque<Block.Transaction> txQueue;

    /* The id of current device */
    private String deviceId = "DeviceA";

    /* The height of current device */
    private long height = 1;

    private Map<String, Set<String>> subscriptionList = new HashMap<>();

    /* delegator from upper application */
    private VegvisirApplicationDelegator delegator;

    private static VirtualVegvisirInstance instance = null;
    private static VirtualVegvisirInstance alternate= null;

    private static final Object instanceLock = new Object();

    private static Thread pollingThread;

    private Map<TransactionID, Set<String>> witnessMap;


    /**
     * We only need one instance of this class, i.e. singleton.
     * If this is the first call, instantiate an object and return.
     * @return the singleton object.
     */
    public static VirtualVegvisirInstance getInstance()
    {
        if (instance == null) {
            synchronized (instanceLock) {
                if (instance == null) {
                    instance = new VirtualVegvisirInstance();
                    instance.deviceToTransactionHeight = new HashMap<>();
                    instance.txQueue = new LinkedBlockingDeque<>();
                    instance.witnessMap = new HashMap<>();
                    pollingThread = new Thread(instance::poll);
                    pollingThread.start();
                }
            }
        }
        return instance;
    }

    /**
     * VirtualVegvisirInstance
     * @brief Creates a 2nd Instance of VirtualVegisir in the variable alternate
     * @param deviceName : String representation of name
     * @return VegvisirInstance
     */
    public static VirtualVegvisirInstance getInstance( String deviceName ){
        if (alternate == null){
            synchronized (instanceLock){
                if (alternate == null){
                    alternate = new VirtualVegvisirInstance( deviceName );
                    alternate.deviceToTransactionHeight = new HashMap<>();
                    alternate.txQueue = new LinkedBlockingDeque<>();
                    pollingThread = new Thread( alternate :: poll );
                    pollingThread.start();
                }
            }
        }
        return alternate;
    }


    private VirtualVegvisirInstance() {}


    /**
     * Public Constructor Overloaded
     */
    private VirtualVegvisirInstance( String aDevice ){
        this.deviceId = aDevice;
    }

    public void updateSubscriptionList( String deviceId, Set<String> applicableChannels){
        for (String topic : applicableChannels)
        {
            if( subscriptionList.containsKey( topic )){
                System.out.format("Key: %s here\n", topic);
                if (subscriptionList.get(topic).contains(deviceId) ){
                    continue;  // NO DUPLICATES
                }
                else{
                    subscriptionList.get(topic).add(deviceId);
                }
            }
            else{
                System.out.format("%s not present \n", topic);
                subscriptionList.put(topic, new HashSet<>(Arrays.asList(deviceId))) ;
            }
        }
        System.out.println(Arrays.asList( subscriptionList));
    }

    /**
     * Register a delegator, which will handle new transactions for that application.
     * After the registration, new transactions will be forward to the delegator at most once.
     * If there already is a delegator for that application, then this one replaces the old one.
     * However, transactions that already been sent to the old delegator will be processed by the
     * old one.
     *
     * @param context   a context object of the application.
     * @param delegator a delegator instance.
     * @return true if the @delegator is successfully registered.
     */
    @Override
    public boolean registerApplicationDelegator(VegvisirApplicationContext context,
                                                VegvisirApplicationDelegator delegator) {
        this.delegator = delegator;
        synchronized (this) {
            this.notifyAll();
        }
        return true;
    }


    /**
     * Polling the transaction queue for new transactions. If there is no delegator available, then
     * wait on this object until a delegator is assigned.
     */
    private void poll() {
        Random random = new Random();
        while (true) {
            try {
                if (delegator == null) {
                    synchronized (this) {
                        if (delegator == null)
                            this.wait();
                    }
                }
                Block.Transaction tx = txQueue.take();

                Set<TransactionID> deps = new HashSet<>();
                for (Block.Transaction.TransactionId id : tx.getDependenciesList()) {
                    deps.add(new TransactionID(id.getDeviceId(), id.getTransactionHeight()));
                }
                List<String> topics = tx.getTopicsList();
                delegator.applyTransaction(
                        new HashSet<String>(tx.getTopicsList()),
                        tx.getPayload().toByteArray(),
                        new TransactionID(tx.getTransactionId().getDeviceId(), tx.getTransactionId().getTransactionHeight()),
                        deps);
                if (random.nextBoolean())
                    delegator.onNewReconciliationFinished();
            } catch (InterruptedException ex) {
//                System.err.println("Interrupted transaction polling thread! Will exit.");
                break;
            }
        }
    }


    /**
     * Add a new transaction to the DAG. If the transaction is valid, then it will be added to the
     * block, either current one or next one depends on the transaction queue size. If the transaction
     * is valid, then this transaction will be pass to applyTransaction immediately to let application
     * update its states.
     *
     * @param context      a context object of the application.
     * @param topics        a pub/sub topic that unique identify who are interested in this transaction.
     * @param payload      a application defined data payload in byte array format.
     * @param dependencies a list of transactionIds that this transaction depends on.
     * @return true, if the transaction is valid.
     */
    @Override
    public boolean addTransaction(VegvisirApplicationContext context,
                                  Set<String> topics,
                                  byte[] payload,
                                  Set<TransactionID> dependencies)
    {
        if (!deviceToTransactionHeight.containsKey(deviceId)) {
            deviceToTransactionHeight.put(deviceId, height);
        }
        return _addTransaction(this.deviceId, topics, payload, dependencies);
    }


    public boolean addTransactionByDevice(String deviceId,
                                          Set<String> topics,
                                          byte[] payload,
                                          Set<TransactionID> dependencies)
    {
        if (!deviceToTransactionHeight.containsKey(deviceId)) {
            deviceToTransactionHeight.put(deviceId, 1L);
        }
        return _addTransaction(deviceId, topics, payload, dependencies);
    }


    public boolean addTransactionByDeviceAndHeight(String deviceId,
                                                   long height,
                                                   Set<String> topics,
                                                   byte[] payload,
                                                   Set<TransactionID> dependencies)
    {
        if (!deviceToTransactionHeight.containsKey(deviceId)) {
            deviceToTransactionHeight.put(deviceId, height);
        }
        return _addTransaction(deviceId, height, topics, payload, dependencies);
    }


    private boolean _addTransaction(String deviceId,
                                    Set<String> topics,
                                    byte[] payload,
                                    Set<TransactionID> dependencies)
    {
        return _addTransaction(deviceId,
                deviceToTransactionHeight.get(deviceId),
                topics,
                payload,
                dependencies
        );
    }


    /**
     * Append a transaction to the transaction queue with given device id and height, then increase
     * the transaction height of @deviceId.
     * @param deviceId      a identifier for a device.
     * @param height        a natural number identifying the number of transaction has been created by that
     *                      device.
     * @param topics         a pub/sub topic that unique identify who are interested in this transaction.
     * @param payload       a application defined data payload in byte array format.
     * @param dependencies  a list of transactionIds that this transaction depends on.
     * @return true, if the transaction is valid.
     */
    private boolean _addTransaction(String deviceId,
                                    long height,
                                    Set<String> topics,
                                    byte[] payload,
                                    Set<TransactionID> dependencies)
    {
        List<Block.Transaction.TransactionId> deps = new ArrayList<>();
        for (TransactionID id : dependencies) {
            deps.add(Block.Transaction.TransactionId.newBuilder().setTransactionHeight(id.getTransactionHeight()).setDeviceId(id.getDeviceID()).build());
        }
        com.vegvisir.core.datatype.proto.Block.Transaction.Builder builder = com.vegvisir.core.datatype.proto.Block.Transaction.newBuilder();
        builder.addAllDependencies(deps)
                .addAllTopics(topics)
                .setPayload(ByteString.copyFrom(payload));
        com.vegvisir.core.datatype.proto.Block.Transaction.TransactionId id = com.vegvisir.core.datatype.proto.Block.Transaction.TransactionId.newBuilder()
                .setDeviceId(deviceId)
                .setTransactionHeight(height)
                .build();
        builder.setTransactionId(id);
        long nextHeight = height + 1;
        deviceToTransactionHeight.put(deviceId, nextHeight);
        txQueue.add(builder.build());
        return true;
    }


    public String getDeviceId() {
        return deviceId;
    }

    public Map<String, Set<String>> getSubscriptionList() {
        return subscriptionList;
    }

    public static Thread getPollingThread() {
        return pollingThread;
    }

    @Override
    public String getThisDeviceID() {
        return deviceId;
    }

    @Override
    public Set<String> getWitnessForTransaction(TransactionID id) {
        if (!witnessMap.containsKey(id))
            witnessMap.put(id, new HashSet<>(Arrays.asList(deviceId)));
        Set<String> witnesses = witnessMap.get(id);
        witnesses.add("Device_"+witnesses.size());
        return new HashSet<>(witnesses);
    }
}
