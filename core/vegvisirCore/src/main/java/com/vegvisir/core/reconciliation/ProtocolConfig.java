package com.vegvisir.core.reconciliation;

import com.vegvisir.core.blockdag.BlockDAGv2;
import com.vegvisir.gossip.Gossip;
import com.vegvisir.network.datatype.proto.Payload;
import com.vegvisir.network.datatype.proto.VegvisirProtocolMessage;
import vegvisir.proto.Handshake;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProtocolConfig {

    private String remoteId;

    private Gossip gossipLayer;

    private BlockDAGv2 dag;

    private Set<Handshake.ProtocolVersion> protocols;

    private Handshake.ProtocolVersion runningProtocol;

    private boolean remoteEnd = false;

    private boolean hostEnd = false;

    private final Object lock = new Object();
    private boolean locked = false;

    public ProtocolConfig(String remoteId,
                          Gossip gossipLayer,
                          Set<Handshake.ProtocolVersion> protocols,
                          BlockDAGv2 dag) {
        this.remoteId = remoteId;
        this.gossipLayer = gossipLayer;
        this.protocols = protocols;
        this.dag = dag;
    }

    public void send(VegvisirProtocolMessage message) {
       Payload payload = Payload.newBuilder()
                .setMessage(message)
                .build();
        gossipLayer.sendToPeer(remoteId, payload);
    }

    public Set<Handshake.ProtocolVersion> getAvailableProtocols() {
        return protocols;
    }

    public void setRunningProtocol(Handshake.ProtocolVersion runningProtocol) {
        this.runningProtocol = runningProtocol;
    }

    public Handshake.ProtocolVersion getRunningProtocol() {
        return runningProtocol;
    }

    public boolean isHandshakeSuccess() {
        return runningProtocol != null;
    }

    public String getRemoteId() {
        return remoteId;
    }

    public BlockDAGv2 getDag() {
        return dag;
    }

    public void endProtocol() {
        synchronized (lock) {
            hostEnd = true;
            if (remoteEnd && locked) {
                lock.notifyAll();
            }
        }
        VegvisirProtocolMessage message = VegvisirProtocolMessage.newBuilder()
                .setMessageType(VegvisirProtocolMessage.MessageType.END)
                .build();
        send(message);
    }

    public void remoteEndProtocol() {
        synchronized (lock) {
            remoteEnd = true;
            if (hostEnd && locked) {
                lock.notifyAll();
            }
        }
    }

    public void waitForProtocolTermination() throws InterruptedException {
        synchronized (lock) {
            if (!remoteEnd || !hostEnd) {
                locked = true;
                lock.wait(2000);
            }
        }
    }
}
