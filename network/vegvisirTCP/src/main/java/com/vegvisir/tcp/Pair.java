package com.vegvisir.tcp;

public class Pair<K, V> {

    private K first;
    private V second;

    public Pair(K t1, V t2) {
        first = t1;
        second = t2;
    }

    public K getFirst() {
        return first;
    }

    public V getSecond() {
        return second;
    }
}
