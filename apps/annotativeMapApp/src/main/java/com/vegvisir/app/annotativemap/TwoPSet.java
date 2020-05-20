package com.vegvisir.app.annotativemap;

import android.os.CpuUsageInfo;

import java.util.HashSet;
import java.util.Set;

public class TwoPSet {
    private Set<FullAnnotation> addSet;
    private Set<FullAnnotation> removeSet;

    public TwoPSet() {
        addSet = new HashSet<>();
        removeSet = new HashSet<>();
    }

    public TwoPSet(Set<FullAnnotation> add, Set<FullAnnotation> remove) {
        addSet = add;
        removeSet = remove;
    }

    public Set<FullAnnotation> getAddSet(){
        return this.addSet;
    }

    public Set<FullAnnotation> getRemoveSet(){
        return this.removeSet;
    }

    public String toString() {
        return "addSet: " + addSet.toString() + ", removeSet: " + removeSet.toString();
    }
}
