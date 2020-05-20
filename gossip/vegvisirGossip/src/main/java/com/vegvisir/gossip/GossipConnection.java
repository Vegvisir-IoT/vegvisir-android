package com.vegvisir.gossip;

import com.vegvisir.network.datatype.proto.Payload;

import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import com.vegvisir.core.datatype.proto.Location;

/**
 * This class stores basic information related to a peer node's connection.
 */
public class GossipConnection {

    private String id;

    private boolean connected;

    private Location location;

    /* After which this node should be re-connected */
    private Long timeToWakeup;

    private BlockingQueue<Payload> receivingQueue = new LinkedBlockingQueue();

    private Thread pollingThreads;

    public GossipConnection(String id) {
        this.id = id;
        timeToWakeup = 0L;
        connected = true;
    }

    public String getId() {
        return id;
    }

    public boolean isWakeup() {
        synchronized (timeToWakeup) {
            return timeToWakeup < new Date().getTime();
        }
    }

    public void ignore(int period) {
        synchronized (timeToWakeup) {
            timeToWakeup = new Date().getTime() + period;
        }
    }

    public void recvPayload(Payload payload) {
        this.receivingQueue.add(payload);
    }

    public Payload blockingGet() throws InterruptedException {
        return receivingQueue.take();
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        this.connected = false;
        clearPollingThread();
    }

    public void setConnected() {
        this.connected = true;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setPollingThreads(Thread pollingThreads) {
        synchronized (this) {
            this.pollingThreads = pollingThreads;
        }
    }

    public void clearPollingThread() {
        synchronized (this) {
            if (pollingThreads != null) {
                pollingThreads.interrupt();
            }
        }
    }

}
