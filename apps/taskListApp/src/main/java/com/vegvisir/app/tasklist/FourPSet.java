package com.vegvisir.app.tasklist;

import com.vegvisir.app.tasklist.ui.login.LoginActivity;
import com.vegvisir.pub_sub.TransactionID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FourPSet {
    private Set<String> lowSet;
    private Set<String> mediumSet;
    private Set<String> highSet;
    private Set<String> removeSet;

    public FourPSet() {
        lowSet = new HashSet<>();
        mediumSet = new HashSet<>();
        highSet = new HashSet<>();
        removeSet = new HashSet<>();
    }

    public FourPSet(Set<String> low, Set<String> medium, Set<String> high, Set<String> remove) {
        lowSet = low;
        mediumSet = medium;
        highSet = high;
        removeSet = remove;
    }

    public void adjustForDependencies(Set<TransactionID> deps,
                                      HashMap<TransactionID, FourPSet> a4PSet){
        for ( TransactionID d : deps) {
            if (a4PSet.containsKey(d)) {
                lowSet.addAll( a4PSet.get(d).getLowSet() );
                mediumSet.addAll( a4PSet.get(d).getMediumSet() );
                highSet.addAll( a4PSet.get(d).getHighSet() );
                removeSet.addAll( a4PSet.get(d).getRemoveSet());
            }
        }
    }

    public void updateBySetByType(Integer transactionType, String item){

        if (transactionType == 1) {
            lowSet.add(item);
            mediumSet.remove(item);
            highSet.remove(item);
            removeSet.remove(item);
        }
        else if (transactionType == 2) {
            lowSet.remove(item);
            mediumSet.add(item);
            highSet.remove(item);
            removeSet.remove(item);
        }
        else if (transactionType == 3) {
            lowSet.remove(item);
            mediumSet.remove(item);
            highSet.add(item);
            removeSet.remove(item);
        }
        else if (transactionType == 0){
            lowSet.remove(item);
            mediumSet.remove(item);
            highSet.remove(item);
            removeSet.add(item);
        }
    }

    public static Set<String> filterSetByList(Set<String> primary, List<Set<String>> filters){
        for (Set<String> filter : filters){
            primary.removeAll(filter);
        }
        return primary;

    }
    public Set<String> getLowSet(){
        return this.lowSet;
    }

    public Set<String> getMediumSet(){
        return this.mediumSet;
    }

    public Set<String> getHighSet(){
        return this.highSet;
    }

    public Set<String> getRemoveSet(){
        return this.removeSet;
    }

    public String toString(){
        return "lowset:" + lowSet.toString() + ", mediumset:"+ mediumSet.toString() + ", highset:"+ highSet.toString() + ", removeset:"+ removeSet.toString();
    }


}
