package com.vegvisir.util.profiling;

import java.util.Date;

public class VegvisirProfilingStats {

    private long bytesSoFar = 0;

    private int numOfReconciliation = 0;

    private Date startTime;

    private Date endTime;

    private long blocks = 0;

    private long receivedBytes = 0;

    public VegvisirProfilingStats() {

    }

    public void start() {
        startTime =  new Date();
    }

    public void addBytes(long bytes) {
        this.bytesSoFar += bytes;
    }

    public void incrementReconciliation() {
        numOfReconciliation ++;
    }

    public void end() {
        endTime = new Date();
    }

    public long getBytesSoFar() {
        return bytesSoFar;
    }

    public int getNumOfReconciliation() {
        return numOfReconciliation / 2;
    }

    public void addBlocks(int blocks) {
        this.blocks += blocks;
    }

    public long getBlocks() {
        return blocks;
    }

    public void setBlocks(long blocks) {
        this.blocks = blocks;
    }

    public void addReceivedBytes(long receivedBytes) {
        this.receivedBytes += receivedBytes;
    }

    public long getReceivedBytes() {
        return receivedBytes;
    }
}
