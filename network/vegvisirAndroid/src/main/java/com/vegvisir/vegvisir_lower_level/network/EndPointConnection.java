package com.vegvisir.vegvisir_lower_level.network;

import android.content.Context;
import com.google.android.gms.tasks.Task;
import com.vegvisir.network.datatype.proto.Payload;
import com.vegvisir.vegvisir_lower_level.network.Exceptions.ConnectionNotAvailableException;
import com.vegvisir.vegvisir_lower_level.utils.Utils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Used for storing states for each connection
 */
public class EndPointConnection {

    private String endPointId;

    private String remoteID;

    private java.util.function.Function<Payload, Void> recvHandler;

    private BlockingDeque<Payload> recvQueue;

    private ByteStream stream;

    private Long wakeupTime;

    private Long connectedTime;

    private boolean connected;

    private Set<Task> sendingTasks;

    private final Object flushLock = new Object();

    private Boolean flushCondition = false;


    public EndPointConnection(String endPointId,
                              String remoteID,
                              Context context,
                              ByteStream stream) {
        this.remoteID = remoteID;
        this.endPointId = endPointId;
        this.stream = stream;
        this.recvQueue = new LinkedBlockingDeque<>();
        connectedTime = Utils.getTimeInMilliseconds();
        wakeupTime = Utils.getTimeInMilliseconds();
        sendingTasks = new HashSet<>();
    }

    /**
     * Send @payload to remote device.
     * @param payload
     * @throws ConnectionNotAvailableException
     */
    public void send(Payload payload) throws ConnectionNotAvailableException {
        if (isConnected()) {
            Task<Void> task = stream.send(endPointId, payload);
            sendingTasks.add(task);
            task.addOnCompleteListener((t) -> {
                sendingTasks.remove(t);
                synchronized (flushLock) {
                    if (sendingTasks.isEmpty() && this.flushCondition) {
                        flushLock.notify();
                        flushCondition = false;
                    }
                }
            });
            task.addOnCanceledListener(() -> {
                sendingTasks.remove(task);
                synchronized (flushLock) {
                    if (sendingTasks.isEmpty() && this.flushCondition) {
                        flushLock.notify();
                        flushCondition = false;
                    }
                }
            });
        }
        else
            throw new ConnectionNotAvailableException();
    }

    /**
     * Save new payload to receiving queue.
     * @param payload
     */
    public void onRecv(Payload payload) {
        recvQueue.push(payload);
    }

    /**
     * Wait until next payload available for this connection.
     * @return the arrived payload.
     */
    @Deprecated
    public Payload blockingRecv() throws InterruptedException {
        return recvQueue.take();
    }

    @Deprecated
    public Payload recv() {
        return recvQueue.remove();
    }

    /**
     * @return whether this connection is ignored or not.
     */
    public boolean isWakeup() {
        return Utils.getTimeInMilliseconds() > wakeupTime;
    }

    /**
     * Ignore any connections from this endpoint.
     * @param duration amount of time in milliseconds.
     */
    public void ignore(Long duration) {
        this.wakeupTime = duration + Utils.getTimeInMilliseconds();
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
        if (!this.connected) {
            recvQueue.add(Payload.newBuilder().build());
        }
    }

    /**
     * @return whether this connection is live.
     */
    public boolean isConnected() {
        return connected;
    }

//    @Deprecated
//    public com.vegvisir.network.datatype.proto.Connection toProtoConnection() {
//        return com.vegvisir.network.datatype.proto.Connection.newBuilder()
//                .setRemoteId(Identifier.newBuilder().setName(endPointId).build())
//                .setWakeupTime(Timestamp.newBuilder().setUtcTime(wakeupTime).build())
//                .setConnectedTime(Timestamp.newBuilder().setElapsedTime(connectedTime).build())
//                .build();
//    }

    public String getEndPointId() {
        return endPointId;
    }

    public String getRemoteID() {
        return remoteID;
    }

    public void waitUntilFlushAllData() {
        try {
            synchronized (flushLock) {
                if (!sendingTasks.isEmpty()) {
                    flushCondition = true;
                    flushLock.wait();
                }
            }
        } catch (InterruptedException ex) {
            synchronized (flushLock) {
                flushCondition = false;
            }
            ex.printStackTrace();
        }
    }
}
