package com.vegvisir.core.reconciliation;

import com.vegvisir.network.datatype.proto.VegvisirProtocolMessage;
import vegvisir.proto.Handshake;

public interface ReconciliationProtocol {

    void onNewMessage(VegvisirProtocolMessage message);

    void onDisconnected();

    Handshake.ProtocolVersion getVersion();

    void startReconciliation() throws InterruptedException;
}
