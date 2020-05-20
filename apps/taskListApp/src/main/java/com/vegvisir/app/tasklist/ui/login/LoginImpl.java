package com.vegvisir.app.tasklist.ui.login;

import android.app.Activity;
import android.util.Log;

import com.vegvisir.app.tasklist.FourPSet;
import com.vegvisir.app.tasklist.TasklistApplication;
import com.vegvisir.app.tasklist.User;
import com.vegvisir.app.tasklist.data.TransactionTuple;
import com.vegvisir.app.tasklist.data.TwoPSetUser;
import com.vegvisir.pub_sub.TransactionID;
import com.vegvisir.pub_sub.VegvisirApplicationDelegator;
import com.vegvisir.pub_sub.VegvisirInstance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

/**
 * Ideally, all applications should implement this interface.
 */
public class LoginImpl implements VegvisirApplicationDelegator {

    private Activity loginActivity;

    public LoginImpl(Activity a) {
        this.loginActivity = a;
    }


    /**
     * Vegvisir will call this function to init and run application.
     * @param instance a underlying Vegvisir instance for application use.
     */
    public void init(VegvisirInstance instance) {

    }


    /**
     * An application implemented function. This function will get called whenever a new transaction
     * subscribed by this application arrives.
     * @param topics topics that this transaction is created for.
     * @param payload application specific data.
     * @param tx_id A unique identifier for the transaction.
     * @param deps which transactions this transaction depends on.
     */
    public synchronized void applyTransaction(
            Set<String> topics,
            byte[] payload,
            TransactionID tx_id,
            Set<TransactionID> deps) {

        String payloadString = new String(payload);
        int transactionType = Integer.parseInt(payloadString.substring(0,1));
        Set<TransactionTuple> updatedSet;
        String deviceId = tx_id.getDeviceID();

        TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();

        if (transactionType > 4 && transactionType < 8){
            int usernamePos = payloadString.indexOf(",");
            String username = payloadString.substring(1,usernamePos);
            String password = payloadString.substring(usernamePos + 1);

            Set<TransactionTuple> prevSets = thisApp.getDependencySets().get(username);

            updatedSet = TransactionTuple.createSetFromPrevious(prevSets, deps);

            TransactionTuple t = new TransactionTuple(tx_id, transactionType);
            updatedSet.add(t);
            thisApp.getDependencySets().put(username, updatedSet);

            thisApp.getLatestTransactions().put(deviceId, tx_id);

            for (TransactionID d : deps) {
                thisApp.getTopDeps().remove(d);
            }
            thisApp.getTopDeps().add(tx_id);

            TwoPSetUser regular = new TwoPSetUser();
            //regular.loadDependencies( deps, LoginActivity.twoPSets);
            regular.attachUser( transactionType, username, password);
            thisApp.getTwoPSets().put(tx_id, regular );

            TwoPSetUser userTop = new TwoPSetUser();
            userTop.loadDependencies( thisApp.getTopDeps(), thisApp.getTwoPSets() );

            thisApp.getTwoPSets().put(thisApp.getTop(), userTop);
            thisApp.getUsernames().clear();
            for (User u: userTop.filterRemove()){
                thisApp.getUsernames().put(u.getUsername(), u.getPassword());
            }
        }
        else {

            thisApp.getTransactionDeps().put(tx_id, deps);
            String item = payloadString.substring(1);
            if (transactionType > 7){
                int first = payloadString.indexOf(",");
                int second = payloadString.indexOf(",", first + 1);
                int y = Integer.parseInt(payloadString.substring(first+1,second));  //
                item = payloadString.substring(second+1);
                if (transactionType == 8){
                    transactionType = 0;
                }
                else if (transactionType == 9){
                    transactionType = 2;
                }
            }

            Set<TransactionTuple> prevSets = thisApp.getMainDependencySets().get(item);
            updatedSet = TransactionTuple.createSetFromPrevious(prevSets, deps);

            updatedSet.add( new TransactionTuple( tx_id, transactionType) );
            thisApp.getMainDependencySets().put(item, updatedSet);


            for (TransactionID d : deps) {
                thisApp.getMainTopDeps().remove(d);
            }

            thisApp.getMainTopDeps().add(tx_id);

            FourPSet reg4PSet = new FourPSet();
            reg4PSet.adjustForDependencies(deps, thisApp.getFourPSets());
            reg4PSet.updateBySetByType( transactionType, item);

            thisApp.getFourPSets().put(tx_id, reg4PSet);


            FourPSet top4PSet = new FourPSet();
            top4PSet.adjustForDependencies( thisApp.getMainTopDeps(), thisApp.getFourPSets() );

            thisApp.getFourPSets().put(thisApp.getMainTop(), top4PSet);

            thisApp.getItems().clear();
            thisApp.getPriorities().clear();

            updateByPriority( FourPSet.filterSetByList(top4PSet.getLowSet(),
                    Arrays.asList(top4PSet.getMediumSet(), top4PSet.getHighSet(),
                            top4PSet.getRemoveSet())), LoginActivity.Priority.Low, true);

            updateByPriority( FourPSet.filterSetByList(top4PSet.getMediumSet(),
                    Arrays.asList( top4PSet.getHighSet(), top4PSet.getRemoveSet())),
                    LoginActivity.Priority.Medium, true);

            updateByPriority( FourPSet.filterSetByList(top4PSet.getHighSet(),
                    Arrays.asList( top4PSet.getRemoveSet())), LoginActivity.Priority.High, true );

            ArrayList<String> removeList = new ArrayList<>();
            removeList.addAll(top4PSet.getRemoveSet());

            for(String s: removeList){
                if(!thisApp.getWitnessedItems().contains(s)){
                    thisApp.getItems().add(s);
                    thisApp.getPriorities().put(s, LoginActivity.Priority.Remove);
                }

            }

            thisApp.getItems().sort(new ItemComparator());


            if (!thisApp.getWitnessedTransactions().contains(tx_id) &&!thisApp.getNotWitnessedTransactions().contains(tx_id)){
                thisApp.getNotWitnessedTransactions().add(tx_id);
            }

            Log.i("nameofid", tx_id.toString());
            Log.i("witnessed", thisApp.getWitnessedItems().toString());
        }

    }

    public synchronized void onNewReconciliationFinished(){

        TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();
        for (TransactionID tid: thisApp.getNotWitnessedTransactions()){
            Set<String> witnesses = thisApp.getVirtual().getWitnessForTransaction(tid);
            if (witnesses.size() >= 3){
                thisApp.getWitnessedTransactions().add(tid);
                applyWitnessedTransaction(tid);
            }
            else{
                break;
            }
        }

        thisApp.getNotWitnessedTransactions().removeAll(thisApp.getWitnessedTransactions());
    }

    public void applyWitnessedTransaction(TransactionID tx_id){

        TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();
        if (!thisApp.getTransactionDeps().containsKey(tx_id)){
            Log.i("reaches", "here");
        }
        else{
            Set<TransactionID> deps = thisApp.getTransactionDeps().get(tx_id);

            for (TransactionID d : deps) {
                thisApp.getWitnessedTopDeps().remove(d);
            }

            thisApp.getWitnessedTopDeps().add(tx_id);

            FourPSet top4PSet = new FourPSet();
            top4PSet.adjustForDependencies( thisApp.getWitnessedTopDeps(), thisApp.getFourPSets() );

            thisApp.getFourPSets().put(thisApp.getWitnessedTop(), top4PSet);

            ArrayList<String> items = new ArrayList<>();
            items.addAll(thisApp.getWitnessedItems());

            for(String s: items){
                if (!thisApp.getWitnessedPriorities().get(s).equals(LoginActivity.Priority.Remove)){
                    thisApp.getWitnessedItems().remove(s);
                    thisApp.getWitnessedPriorities().remove(s);
                }
            }

//            thisApp.getWitnessedItems().clear();
//            thisApp.getWitnessedPriorities().clear();

            updateByPriority( FourPSet.filterSetByList(top4PSet.getLowSet(),
                    Arrays.asList(top4PSet.getMediumSet(), top4PSet.getHighSet(),
                            top4PSet.getRemoveSet())), LoginActivity.Priority.Low, false);

            updateByPriority( FourPSet.filterSetByList(top4PSet.getMediumSet(),
                    Arrays.asList( top4PSet.getHighSet(), top4PSet.getRemoveSet())),
                    LoginActivity.Priority.Medium, false);

            updateByPriority( FourPSet.filterSetByList(top4PSet.getHighSet(),
                    Arrays.asList( top4PSet.getRemoveSet())), LoginActivity.Priority.High, false );

            for (String s: top4PSet.getRemoveSet()){
                thisApp.getWitnessedItems().add(s);
                thisApp.getWitnessedPriorities().put(s, LoginActivity.Priority.Remove);
            }

        }

    }

    public void updateByPriority(Set<String> aSet, LoginActivity.Priority priority, boolean flag){

        TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();
        if (flag){
            for(String entry: aSet) {
                thisApp.getItems().add( entry );
                thisApp.getPriorities().put(entry, priority);
            }
        }
        else{
            for(String entry: aSet) {
                thisApp.getWitnessedItems().add( entry );
                thisApp.getWitnessedPriorities().put(entry, priority);
            }
        }

    }

    public class ItemComparator implements Comparator<String> {

        TasklistApplication thisApp = (TasklistApplication) loginActivity.getApplication();
        @Override
        public int compare(String s1,String s2) {
            LoginActivity.Priority p1 = thisApp.getPriorities().get(s1);
            LoginActivity.Priority p2 = thisApp.getPriorities().get(s2);
            if( p1 == p2)
                return s1.compareTo(s2);
            else
                return p1.compareTo(p2);

        }
    }
}

