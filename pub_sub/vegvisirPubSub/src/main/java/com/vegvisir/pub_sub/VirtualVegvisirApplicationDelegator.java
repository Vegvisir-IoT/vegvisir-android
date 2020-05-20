package com.vegvisir.pub_sub;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;



public class VirtualVegvisirApplicationDelegator implements VegvisirApplicationDelegator {


    /**
     * Vegvisir will call this function to init and run application.
     * @param instance a underlying Vegvisir instance for application use.
     */
    public void init(VegvisirInstance instance) { /* Not Enabled in Virtual Environment */}


    /**
     * An application implemented function. This function will get called whenever a new transaction
     * subscribed by this application arrives.
     *
     * @param topics  topics that this transaction is created for.
     * @param payload application specific data.
     * @param tx_id   A unique identifier for the transaction.
     * @param deps    which transactions this transaction depends on.
     */
    @Override
    public void applyTransaction(Set<String> topics, byte[] payload, TransactionID tx_id,
                                 Set<TransactionID> deps) {
    // For Now Apply transaction called for Topics Should be announced
        for( String channel : topics){
            System.out.format("Tx: Broadcast to this channel: %s ", channel);
        }
    }

    @Override
    public void onNewReconciliationFinished() {

    }
}


