package com.vegvisir.gossip.adapter;

import com.vegvisir.network.datatype.proto.Payload;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface NetworkAdapter {

    /**
     * Push given @payload to the sending queue for peer with @peerId
     * @param peerId a unique id for the peer node
     * @param payload the actual data to be sent
     * @return true if peer is still connected.
     */
    public boolean sendBlock(String peerId, Payload payload);

    /**
     * Broadcast given @payload to all peers
     * @param payload data to be sent
     */
    public void broadCast(Payload payload);

    /**
     * Register a handler to handle new arrived payload from other peers.
     * @param handler the handle which takes peer id as the first argument and payload as the second argument and return nothing.
     */
    public void onReceiveBlock(BiConsumer<String, Payload> handler);


    /**
     * Register a handler for a lost of connection.
     * @param handler
     */
    public void onConnectionLost(Consumer<String> handler);

    /**
     * [BLOCKING] if there is no connection available.
     * @return a set of remote ids with which this node has been established connection.
     */
    public List<String> getAvailableConnections();


    /**
     * @return a set of strings which represent the id of nearby devices.
     */
    public List<String> getNearbyDevices();


    /**
     * Disconnect to a particular endpoint. Disconnecting should only happen after all data have been sent to the remote side.
     * @param endpoint
     */
    public void disconnect(String endpoint);
}

