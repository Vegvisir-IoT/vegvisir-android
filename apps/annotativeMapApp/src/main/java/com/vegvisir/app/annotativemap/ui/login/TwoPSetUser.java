package com.vegvisir.app.annotativemap.ui.login;

import com.vegvisir.app.annotativemap.User;

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

    public Set<User> getAddSet(){
        return this.addSet;
    }

    public Set<User> getRemoveSet(){
        return this.removeSet;
    }

    public String toString(){
        return "addset:" + addSet.toString() +", removeset:"+ removeSet.toString();
    }
}
