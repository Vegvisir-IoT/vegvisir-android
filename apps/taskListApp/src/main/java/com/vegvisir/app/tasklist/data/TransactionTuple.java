package com.vegvisir.app.tasklist.data;
import com.vegvisir.pub_sub.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TransactionTuple {
    public TransactionID transaction;
    public int transactionType; //0 is remove, 1 is low priority, 2 is medium priority
    // and 3 is high priority.

    public TransactionTuple(TransactionID tx_id, int tx_type) {
        transaction = tx_id;
        transactionType = tx_type;
    }


    public static Set<TransactionTuple> createSetFromPrevious (Set<TransactionTuple> prevSets,
                                                               Set<TransactionID> deps){
        Set<TransactionTuple> transactionSet = new HashSet<>();
        if (prevSets != null) {
            Iterator<TransactionTuple> itr = prevSets.iterator();
            while (itr.hasNext()) {
                TransactionTuple x = (TransactionTuple) ((Iterator) itr).next();

                if (!deps.contains(x.transaction)) {

                    transactionSet.add(x);
                }
            }
        }
        return transactionSet;
    }
}
