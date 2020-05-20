package com.vegvisir.application;

import android.content.Context;

import androidx.core.util.Pair;

import com.vegvisir.gossip.adapter.NetworkAdapter;
import com.vegvisir.network.datatype.proto.Payload;
import com.vegvisir.vegvisir_lower_level.network.Exceptions.ConnectionNotAvailableException;
import com.vegvisir.vegvisir_lower_level.network.Network;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AndroidAdapter implements NetworkAdapter {

    /* Android google nearby abstract interface for sending and receiving messages */
    private Network network;

    private Deque<String> connectionHistory;

    private static final long INTERVAL = 1000L;


    public AndroidAdapter(Context context, String id) {
        network = new Network(context, id);
        connectionHistory = new ArrayDeque<>();
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
        try {
            network.send(peerId, payload);
            return true;
        } catch (ConnectionNotAvailableException ex) {
            return false;
        }
    }

    /**
     * Broadcast given @payload to all peers
     *
     * @param payload data to be sent
     */
    @Override
    public void broadCast(Payload payload) {
        /*TODO: Implement this in the future */
    }

    /**
     * Register a handler to handle new arrived payload from other peers.
     *
     * @param handler the handle which takes peer id as the first argument and payload as the second argument and return nothing.
     */
    @Override
    public void onReceiveBlock(BiConsumer<String, Payload> handler) {
        new Thread(() -> {
            for (;;) {
                /* Keep running to take new data */
                Pair<String, Payload> data = network.waitingData();
                handler.accept(data.first, data.second);
            }
        }).start();
    }

    /**
     * [NEW THREAD] Register a handler for a lost of connection.
     *
     * @param handler
     */
    @Override
    public void onConnectionLost(Consumer<String> handler) {
        new Thread(() -> {
            for (;;) {
                try {
                    handler.accept(network.getDisconnectedId());
                } catch (InterruptedException ex) {
                    return;
                }
            }
        });
    }

    /**
     * [BLOCKING] if there is no connection available.
     *
     * @return a set of remote ids with which this node has been established connection.
     */
    @Override
    public List<String> getAvailableConnections() {
        String remoteid = network.waitingConnection();
        connectionHistory.add(remoteid);
        return Arrays.asList(remoteid);
    }

    /**
     * @return a set of strings which represent the id of nearby devices.
     */
    @Override
    public List<String> getNearbyDevices() {
        return null;
    }

    /**
     * Disconnect to a particular endpoint. Disconnecting should only happen after all data have been sent to the remote side.
     *
     * @param remoteID
     */
    @Override
    public void disconnect(String remoteID) {
        network.disconnect(remoteID);
        network.ignore(remoteID, INTERVAL);

    }

    public Deque<String> getConnectionHistory() {
        return connectionHistory;
    }
}
