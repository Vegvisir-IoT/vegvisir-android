package com.vegvisir.gossip;

import com.vegvisir.gossip.adapter.NetworkAdapter;
import com.vegvisir.gossip.adapter.NetworkAdapterManager;
import com.vegvisir.network.datatype.proto.Payload;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

public class Gossip {

    /* Key is peer id and value the meta data for that peer */
    private Map<String, GossipConnection> connections;

    NetworkAdapterManager adapterManager;

    Random rnd = new Random(new Date().getTime() + this.hashCode());

    /**
     * Constructor or Gossip layer
     * @param adapter an adapter for the underlying network layer. This could be an adapter for android, TCP or etc.
     */
    public Gossip(NetworkAdapterManager adapter) {
        connections = new HashMap<>();
        this.adapterManager = adapter;
        this.adapterManager.onReceiveBlock(this::onNewPayload);
        this.adapterManager.onConnectionLost(this::onLostConnection);
    }

    /**
     * [BLOCKING] Randomly choose a wake-up peer. This is a blocking method.
     * @return
     */
    public String randomPickAPeer() {
        List<String> view = adapterManager.getAvailableConnections(); // This call is blocking
        Collections.shuffle(view, rnd);
        String next;
        for (int i = 0; i < view.size(); i ++)
        {
            next = view.get(i);
            if (!connections.containsKey(next))
                connections.put(next, new GossipConnection(next));
            connections.get(next).setConnected();
            if (connections.get(next).isWakeup())
                return next;
        }
        return null;
    }

    /**
     * A view is a list of device ids that have been discovered by this node.
     * @return
     */
    public List<String> getNearbyView() {
        return adapterManager.getNearbyDevices();
    }

    /**
     * Send given payload to peer with id. Upper call from Blockchain
     * @param id
     * @param payload
     * @return true if remote side is still connected.
     */
    public boolean sendToPeer(String id, Payload payload) {
        System.err.println("Send data to " + id);
        boolean alive = adapterManager.sendBlock(id, payload);
        if (!alive)
            connections.get(id).disconnect();
        return alive;
    }

    /**
     * Upper call from blockchain.
     * Pass payload from @id to @handler, which takes payload as an argument.
     * @param id
     */
    public Payload receiveFromPeer(String id) throws InterruptedException {
        if (connections.containsKey(id)) {
            return connections.get(id).blockingGet();
        }
        return null;
    }

    /**
     * [NEW THREAD] This will dispatch a new thread to keep listening on new payloads.
     * The thread will be removed if remote side is disconnected.
     * @param id
     * @param handler
     */
    public Thread setHandlerForPeerMessage(String id, Consumer<Payload> handler) {
        Thread dispatchThread = new Thread(() -> {
            if (!connections.containsKey(id))
                return;
            while (true) {
                try {
                    handler.accept(connections.get(id).blockingGet());
                } catch (InterruptedException ex) {
                    return;
//                    if (!connections.get(id).isConnected())
//                        return;
                }
            }
        });
        dispatchThread.start();
        return dispatchThread;
    }

    /**
     * Set connection with @id to disconnected. This will also interrupt reconciliation instance thread that waiting for completing.
     * @param id
     */
    public void onLostConnection(String id) {
        if (connections.containsKey(id)) {
            connections.get(id).disconnect();
        }
    }

    /**
     * Disconnect with a particular endpoint with @id. This call will wait until all the data has been sent to the remote side.
     * @param id
     */
    public void disconnect(String id) {
        adapterManager.disconnect(id);

    }

    /**
     * Store reconciliation thread to connection object. We may need this in the future to interrupt reconciliation if connection is lost.
     * @param id
     * @param thread
     */
    public void linkReconciliationInstanceWithConnection(String id, Thread thread) {
        if (connections.containsKey(id))
            connections.get(id).setPollingThreads(thread);
    }

    /**
     * Handler when a new payload arrived from peer with id.
     * @param id
     * @param payload
     */
    private void onNewPayload(String id, Payload payload) {
        System.err.println("Received data from " + id);
        if (!connections.containsKey(id))
            connections.put(id, new GossipConnection(id));
        connections.get(id).recvPayload(payload);
    }
}
