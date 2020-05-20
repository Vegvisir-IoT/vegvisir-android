package com.vegvisir.vegvisir_lower_level.network;

import androidx.core.util.Pair;

import com.vegvisir.vegvisir_lower_level.network.Exceptions.HandlerAlreadyExistsException;
import com.vegvisir.network.datatype.proto.Payload;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class PayloadHandler implements Runnable {

    private BlockingDeque<Pair<String, Payload>> payloads;
    private Handler handler;
    private Object handlerLock = new Object();
    private Thread runningThread;

    public PayloadHandler() {
        this.payloads = new LinkedBlockingDeque<>();
    }

    /**
     * Instantiate a payload handler with given handler function.
     * @param handler a method to be called when payload available.
     */
    public PayloadHandler(Handler handler) {
        this();
        this.handler = handler;
    }

    /**
     * Push new payload to the receiving queue.
     * @param remoteId who sent this payload
     * @param payload
     */
    public void onNewPayload(String remoteId, Payload payload) {
        payloads.add(new Pair<>(remoteId, payload));
    }

    @Deprecated
    public Pair<String, Payload> blockingRecv() throws InterruptedException, HandlerAlreadyExistsException {
        if (handler != null) {
            throw new HandlerAlreadyExistsException();
        }
        return payloads.take();
    }


    /**
     * Overwrite handler with a new one.
     * @param handler
     */
    public void setRecvHandler(Handler handler) {
        synchronized (handlerLock) {
            this.handler = handler;
            if (runningThread != null) {
                runningThread.interrupt();
            }
        }
    }

    /**
     * Remove a payload handler
     */
    public void removeRecvHandler() {
        synchronized (handlerLock) {
            this.handler = null;
        }
    }

    @Override
    public void run() {
        runningThread = Thread.currentThread();
        while (true) {
            if (handler == null) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ex) {
                    if (handler == null)
                        continue;
                }
            }
            /* handler already exists */
            try {
                Pair<String, Payload> input = payloads.take();
                synchronized (handlerLock) {
                    if (handler != null) {
                        handler.handle(input);
                    } else {
                        payloads.addFirst(input);
                    }
                }
            } catch (InterruptedException ex) {}
        }
    }


    class Builder {

       PayloadHandler handler;

       Builder() {
           handler = new PayloadHandler();
       }

    }

    public interface Handler {
        void handle(Pair<String, Payload> data);
    }
}
