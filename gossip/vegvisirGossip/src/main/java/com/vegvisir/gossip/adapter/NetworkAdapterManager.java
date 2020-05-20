package com.vegvisir.gossip.adapter;

import com.vegvisir.network.datatype.proto.Payload;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NetworkAdapterManager implements NetworkAdapter {

    private Map<String, NetworkAdapter> adapters;
    private Map<String, Integer> priorities;
    private Map<String, Queue<List<String>>> connections;

    private NetworkAdapter current = null;

    private ExecutorService service;

    private boolean isRunning = true;


    public NetworkAdapterManager() {
        adapters = new HashMap<>();
        priorities = new HashMap<>();
        connections = new HashMap<>();
        service = Executors.newCachedThreadPool();
    }

    public void registerAdapter(String name, int priority, NetworkAdapter adapter) {
        adapters.put(name, adapter);
        priorities.put(name, priority);
        connections.put(name, new ConcurrentLinkedDeque<>());
        service.submit(() -> {
           pullingConnection(name);
        });
    }

    public NetworkAdapter getAdapter() {
        return current;
    }

    /**
     * Push given @payload to the sending queue for peer with @peerId
     *
     * @param peerId  a unique id for the peer node
     * @param payload the actual data to be sent
     * @return true if peer is still connected.
     */
    @Override
    public boolean sendBlock(String peerId, Payload payload) {
        return getAdapter().sendBlock(peerId, payload);
    }

    /**
     * Broadcast given @payload to all peers
     *
     * @param payload data to be sent
     */
    @Override
    public void broadCast(Payload payload) {
        getAdapter().broadCast(payload);
    }

    /**
     * Register a handler to handle new arrived payload from other peers.
     *
     * @param handler the handle which takes peer id as the first argument and payload as the second argument and return nothing.
     */
    @Override
    public void onReceiveBlock(BiConsumer<String, Payload> handler) {
        adapters.forEach((k, a) -> a.onReceiveBlock(handler));
    }

    /**
     * Register a handler for a lost of connection.
     *
     * @param handler
     */
    @Override
    public void onConnectionLost(Consumer<String> handler) {
        adapters.forEach((k, a) -> a.onConnectionLost(handler));
    }

    public List<String> getAvailableConnections() {

        String adapterName = null;
        int priority = 0;

        while (adapterName == null) {
            for (Map.Entry<String, Queue<List<String>>> entry : connections.entrySet()) {
                if (!entry.getValue().isEmpty()) {
                    if (adapterName == null || priority <= priorities.get(entry.getKey())) {
                        adapterName = entry.getKey();
                        priority = priorities.get(adapterName);
                    }
                }
            }
            if (adapterName == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    return null;
                }
            }
        }
        current = adapters.get(adapterName);
        return connections.get(adapterName).remove();
    }

    /**
     * @return a set of strings which represent the id of nearby devices.
     */
    @Override
    public List<String> getNearbyDevices() {
        return getAdapter().getNearbyDevices();
    }

    /**
     * Disconnect to a particular endpoint. Disconnecting should only happen after all data have been sent to the remote side.
     *
     * @param endpoint
     */
    @Override
    public void disconnect(String endpoint) {
        getAdapter().disconnect(endpoint);
    }

    private void pullingConnection(String name) {
        while (isRunning) {
            connections.get(name).add(adapters.get(name).getAvailableConnections());
        }
    }

    public void shutdown() {
        isRunning = false;
    }



}
