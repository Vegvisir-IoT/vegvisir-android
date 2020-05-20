package com.vegvisir.app.tasklist.data;

import com.vegvisir.app.tasklist.User;
import com.vegvisir.app.tasklist.ui.login.LoginActivity;
import com.vegvisir.pub_sub.TransactionID;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TwoPSetUser {
    private Set<User> addSet;
    private Set<User> removeSet;

    public TwoPSetUser() {
        addSet = new HashSet<>();
        removeSet = new HashSet<>();
    }

    public TwoPSetUser(Set<User> add, Set<User> remove) {
       addSet = add;
       removeSet = remove;
    }


    //HashSet<User> addSet = new HashSet<>();
    //HashSet<User> removeSet = new HashSet<>();
    // basic constructor

    public void loadDependencies(Set<TransactionID> deps, HashMap<TransactionID, TwoPSetUser> map) {
        for (TransactionID d : deps) {
            if (map.containsKey(d)) {
                addSet.addAll(map.get(d).getAddSet());
                removeSet.addAll(map.get(d).getRemoveSet());
            }
        }


    }

    /**
     * Helper Method to add user based to 2P Set
     * @param transactionType :: Integer
     * @param username  :: String representation of Username
     * @param password  :: String representation of password TODO Encryption
     */
    public void attachUser( int transactionType, String username, String password){
        if (transactionType == 5) {
            addSet.add(new User(username, password));
            removeSet.remove(username);
        } else {
            addSet.remove(username);
            removeSet.add(new User(username, password));
        }
    }

    /**
     * Helper function to remove return addSet without those items found in remove set
     * @return Set of User
     * @see User
     */
    public Set<User> filterRemove(){
        Set<User> returnSet = this.addSet;
        returnSet.removeAll(removeSet);
        return returnSet;
    }


    /*#######################
     # Getters & Setters   3
     ######################*/
    public Set<User> getAddSet(){     return this.addSet;    }

    public Set<User> getRemoveSet(){  return this.removeSet; }

    public String toString(){
        return "addset:" + addSet.toString() +", removeset:"+ removeSet.toString();
    }

}
