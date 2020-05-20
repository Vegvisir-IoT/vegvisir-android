package com.vegvisir.pub_sub;


/**
 * A unique identifier for a transaction
 */
public class TransactionID {


    /* A ID for device who owns and created this transaction */
    private String deviceID;

    /* A transaction height is a counter value of how many transactions has been
    * created on the device that created this transaction */
    private long transactionHeight;


    public TransactionID(String deviceID, long transactionHeight) {
        this.deviceID = deviceID;
        this.transactionHeight = transactionHeight;
    }

    public long getTransactionHeight() {
        return transactionHeight;
    }

    public String getDeviceID() {
        return deviceID;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (o == null)
          return false;
      if (getClass() != o.getClass())
        return false;
      else{
        TransactionID t = (TransactionID) o;
        if (this.deviceID.equals(t.getDeviceID()) && (this.transactionHeight == t.getTransactionHeight())){
          return true;
        }
        return false;
      }
    }

    @Override
    public int hashCode() {
      return (this.deviceID.hashCode() ^ (new Long(this.transactionHeight).hashCode()));
    }

    @Override
    public String toString() {
        return (this.deviceID + " - " + this.transactionHeight);
    }

}
