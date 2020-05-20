package com.vegvisir.core.reconciliation;

import com.vegvisir.network.datatype.proto.VegvisirProtocolMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import vegvisir.proto.Handshake;

public class HandshakeHandler {

    ProtocolConfig config;

    final Object lock;

    private boolean locked = false;

    public HandshakeHandler(ProtocolConfig config) {
        this.config = config;
        lock = new Object();
    }

    public void onNewMessage(VegvisirProtocolMessage message) {
        Handshake.HandshakeMessage _message = message.getHandshake();
        switch (_message.getType()) {
            case REQUEST: {
                Set<Handshake.ProtocolVersion> remoteProtocols = new HashSet<>(_message.getSpokenVersionsList());
                Set<Handshake.ProtocolVersion> protocols = config.getAvailableProtocols();
                remoteProtocols.retainAll(protocols);
                List<Handshake.ProtocolVersion> sorted = new ArrayList<>(remoteProtocols);
                Collections.sort(sorted);
                Collections.reverse(sorted);
                Set<Handshake.ProtocolVersion> resp = sorted.size() > 0 ? Collections.singleton(sorted.get(0)) : Collections.emptySet();
                Handshake.HandshakeMessage res = Handshake.HandshakeMessage.newBuilder()
                        .addAllSpokenVersions(resp)
                        .setType(Handshake.HandshakeMessage.Type.RESPONSE)
                        .build();
                config.send(VegvisirProtocolMessage.newBuilder().setMessageType(VegvisirProtocolMessage.MessageType.HANDSHAKE).setHandshake(res).build());
            }
                break;
            case RESPONSE:
                Set<Handshake.ProtocolVersion> remoteProtocols = new HashSet<>(_message.getSpokenVersionsList());
                Set<Handshake.ProtocolVersion> protocols = config.getAvailableProtocols();
                remoteProtocols.retainAll(protocols);
                if (remoteProtocols.size() == 1) {
                    config.setRunningProtocol(remoteProtocols.iterator().next());
                } else {
                    System.err.printf("No Common protocol was found");
                }
                synchronized (lock) {
                    if (locked)
                        lock.notifyAll();
                    else
                        locked = true;
                }
                break;
            case DUMMY_STATUS:
                break;
            case UNRECOGNIZED:
        }
    }

    public void startHandshake() throws InterruptedException {
        Set<Handshake.ProtocolVersion> protocols = config.getAvailableProtocols();
        Handshake.HandshakeMessage res = Handshake.HandshakeMessage.newBuilder()
                .addAllSpokenVersions(protocols)
                .setType(Handshake.HandshakeMessage.Type.REQUEST)
                .build();
        config.send(VegvisirProtocolMessage.newBuilder().setMessageType(VegvisirProtocolMessage.MessageType.HANDSHAKE).setHandshake(res).build());
        synchronized (lock) {
            if (!locked) {
                locked = true;
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    System.err.println("GOT Interrupt: "+ex.getLocalizedMessage());
                    locked = false;
                }
            }
        }
    }
}
