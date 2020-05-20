package com.vegvisir.core.reconciliation;

import com.vegvisir.core.blockdag.BlockDAG;
import com.vegvisir.core.blockdag.BlockDAGv2;
import com.vegvisir.core.blockdag.ReconciliationEndListener;
import com.vegvisir.core.config.Config;
import com.vegvisir.core.reconciliation.exceptions.VegvisirReconciliationException;
import com.vegvisir.gossip.Gossip;
import vegvisir.proto.Handshake;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The interface for reconciliation protocol, we may need to add functions here for newer version.
 */
public class ReconciliationDispatcher {


    protected final Object lock = new Object();

    /* remote side id */
    protected String remoteId;

    /* Gossip layer used to gossip messages */
    protected Gossip gossipLayer;

    /* The blockdag for the current node */
    protected BlockDAGv2 dag;

    protected ProtocolConfig config;

    protected Set<Handshake.ProtocolVersion> registeredProtocols;

    protected Map<Handshake.ProtocolVersion, ReconciliationProtocol> reconciliationProtocolHandlers;

    protected HandshakeHandler handshakeHandler;

    protected Thread dispatchThread;


    public ReconciliationDispatcher(Gossip gossipLayer, String remoteId, BlockDAGv2 dag) {
        this.gossipLayer = gossipLayer;
        this.remoteId = remoteId;
        this.dag = dag;
        this.registeredProtocols = new HashSet<>();
        reconciliationProtocolHandlers = new ConcurrentHashMap<>();
        this.config = new ProtocolConfig(remoteId, gossipLayer, registeredProtocols, dag);
        registerProtocol(Handshake.ProtocolVersion.SEND_ALL, new SendAllProtocol(config));
        registerProtocol(Handshake.ProtocolVersion.VECTOR, new VectorClockProtocol(config));
        handshakeHandler = new HandshakeHandler(config);
        dispatchThread = gossipLayer.setHandlerForPeerMessage(remoteId, this::dispatcherHandler);
    }


    public void reconcile() {
        try {
            handshakeHandler.startHandshake();
            if (config.isHandshakeSuccess()) {
                reconciliationProtocolHandlers.get(config.getRunningProtocol()).startReconciliation();
                config.waitForProtocolTermination();
            }
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        dispatchThread.interrupt();
    }

    public void onDisconnected() {

    }

    void dispatcherHandler(com.vegvisir.network.datatype.proto.Payload payload) {
        if (!payload.hasMessage()) {
            return;
        }

        switch (payload.getMessage().getMessageType()) {

            case HANDSHAKE:
                handshakeHandler.onNewMessage(payload.getMessage());
                break;

            case SEND_ALL:
                passToHandler(Handshake.ProtocolVersion.SEND_ALL, payload);
                break;

            case FRONTIER:
                passToHandler(Handshake.ProtocolVersion.FRONTIER, payload);
                break;

            case VECTOR_CLOCK:
                passToHandler(Handshake.ProtocolVersion.VECTOR, payload);
                break;

            case OTHERS:
                break;

            case END:
                config.remoteEndProtocol();
                break;

            case UNRECOGNIZED:
        }
    }

    private void passToHandler(Handshake.ProtocolVersion protocol, com.vegvisir.network.datatype.proto.Payload payload) {
        if (reconciliationProtocolHandlers.containsKey(protocol))
            reconciliationProtocolHandlers.get(protocol).onNewMessage(payload.getMessage());
    }

    /**
     * Because config object shares the same reference to the protocol set. Therefore,
     * a update of protocol here is also reflected at config object.
     * @param protocol
     */
    public void registerProtocol(Handshake.ProtocolVersion protocol, ReconciliationProtocol handler) {
        this.registeredProtocols.add(protocol);
        this.reconciliationProtocolHandlers.put(protocol, handler);
    }
}
