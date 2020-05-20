package com.vegvisir.pub_sub;



import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIDTest {

    @Test
    void getTransactionHeight() {
        TransactionID first = new TransactionID( "Albert", 2);
        TransactionID second = new TransactionID("Bob", 20);
        assertEquals(2, first.getTransactionHeight());
        assertEquals(20, second.getTransactionHeight());
        assertEquals(22, first.getTransactionHeight() + second.getTransactionHeight());

    }

    @Test
    void getDeviceID() {
        assertEquals("Albert", new TransactionID( "Albert", 2).getDeviceID());
    }
}