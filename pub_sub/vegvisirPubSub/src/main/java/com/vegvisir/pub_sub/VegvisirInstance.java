package com.vegvisir.pub_sub;

import com.vegvisir.core.datatype.proto.Block;

import java.util.List;
import java.util.Set;

public interface VegvisirInstance {


    /**
     * Register a delegator, which will handle new transactions for that application.
     * After the registration, new transactions will be forward to the delegator at most once.
     * If there already is delegator for that application, then this one replaces the old one.
     * However, transactions that already been sent to the old delegator will be processed by the
     * old one.
     * @param context a context object of the application.
     * @param delegator a delegator instance.
     * @return true if the @delegator is successfully registered.
     */
    public boolean registerApplicationDelegator(VegvisirApplicationContext context,
                                                VegvisirApplicationDelegator delegator);


    /**
     * Add a new transaction to the DAG. If the transaction is valid, then it will be added to the
     * block, either current one or next one depends on the transaction queue size. If the transaction
     * is valid, then this transaction will be pass to applyTransaction immediately to let application
     * update its states.
     * @param context a context object of the application.
     * @param topics a set of pub/sub topic that unique identify who are interested in this transaction.
     * @param payload a application defined data payload in byte array format.
     * @param dependencies a set of transactionIds that this transaction depends on.
     * @return true, if the transaction is valid.
     */
    public boolean addTransaction(VegvisirApplicationContext context,
                                  Set<String> topics,
                                  byte[] payload,
                                  Set<TransactionID> dependencies);


    /**
     * @return this device's ID in string format.
     */
    public String getThisDeviceID();


    public Set<String> getWitnessForTransaction(TransactionID id);
}
