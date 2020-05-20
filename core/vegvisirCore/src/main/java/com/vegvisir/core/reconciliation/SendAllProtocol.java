package com.vegvisir.core.reconciliation;

import com.vegvisir.core.blockdag.BlockDAG;
import com.vegvisir.network.datatype.proto.VegvisirProtocolMessage;
import vegvisir.proto.Handshake;
import vegvisir.proto.Sendall;

/**
 * Reconciliation protocol implementation version 1.
 * In this version, we send all blocks to the remote side and that is.
 */
public class SendAllProtocol implements ReconciliationProtocol
{

    String remoteId;

    ProtocolConfig config;

    BlockDAG dag;

    final Object lock = new Object();

    private boolean locked = false;

    public SendAllProtocol(ProtocolConfig config) {
        this.config = config;
        this.dag = config.getDag();
        this.remoteId = config.getRemoteId();
    }

    @Override
    public void onNewMessage(VegvisirProtocolMessage message) {
        handleAddBlocks(message.getSendall().getAdd().getBlocksToAddList());
        config.endProtocol();
        synchronized (lock) {
            if (!locked) {
                locked = true;
                lock.notifyAll();
            }
        }

    }

    protected void handleAddBlocks(Iterable<com.isaacsheff.charlotte.proto.Block> blocks) {
        dag.addAllBlocks(blocks);
        blocks.forEach(b -> dag.witness(b, remoteId));
    }

    @Override
    public void startReconciliation() throws InterruptedException {
        sendAllBlocks();
        synchronized (lock) {
            if (!locked) {
                locked = true;
                lock.wait();
            }
        }
    }

    /**
     * Send all blocks to remote side.
     */
    protected void sendAllBlocks() {
         VegvisirProtocolMessage message = VegvisirProtocolMessage.newBuilder()
                 .setMessageType(VegvisirProtocolMessage.MessageType.SEND_ALL)
                 .setSendall(Sendall.SendallMessage.newBuilder()
                         .setAdd(com.vegvisir.common.datatype.proto.AddBlocks.newBuilder()
                                 .addAllBlocksToAdd(this.dag.getAllBlocks())
                                 .build()).build())
                 .build();
        config.send(message);
    }

    @Override
    public Handshake.ProtocolVersion getVersion() {
        return Handshake.ProtocolVersion.SEND_ALL;
    }

    @Override
    public void onDisconnected() {

    }
}
