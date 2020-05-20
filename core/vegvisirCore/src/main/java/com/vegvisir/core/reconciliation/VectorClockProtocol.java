package com.vegvisir.core.reconciliation;

import com.isaacsheff.charlotte.proto.Block;
import com.vegvisir.common.datatype.proto.AddBlocks;
import com.vegvisir.core.blockdag.BlockDAGv2;
import com.vegvisir.network.datatype.proto.VegvisirProtocolMessage;

import java.util.List;

import vegvisir.proto.Handshake;
import vegvisir.proto.Vector;

public class VectorClockProtocol implements ReconciliationProtocol {


    private static final int TIMEOUT = 3000;

    private Vector.VectorClock remoteVector;

    private String remoteId;

    private ProtocolConfig config;

    BlockDAGv2 dag;

    private final Object lock = new Object();

    VectorClockProtocol(ProtocolConfig config) {
        this.config = config;
        dag = config.getDag();
    }

    @Override
    public void startReconciliation() throws InterruptedException {
        this.remoteId = config.getRemoteId();

        /*
         * Compute frontier set. Now this is a vector clock.
         */
        Vector.VectorClock clock = dag.computeFrontierSet();
        sendVectorClock(clock);
        synchronized (lock) {
            lock.wait(TIMEOUT);
        }
    }

    /**
     * Send this device's vector clock to the remote peer device.
     * @param clock
     */
    protected void sendVectorClock(Vector.VectorClock clock) {

        VegvisirProtocolMessage message = VegvisirProtocolMessage.newBuilder()
                .setMessageType(VegvisirProtocolMessage.MessageType.VECTOR_CLOCK)
                .setVector(Vector.VectorMessage.newBuilder()
                        .setType(Vector.VectorMessage.MessageType.LOCAL_VECTOR_CLOCK)
                        .setLocalView(clock)
                        .build())
                .build();
        System.err.println("MINE:\n" + clock.getBody().getClocksMap());
        config.send(message);
    }

    private void computeSendBlocks(Vector.VectorClock remoteVector) {
        System.err.println("REMOTE:\n" + remoteVector.getBody().getClocksMap());
        dag.updateVCForDevice(config.getRemoteId(), remoteVector);
        List<Block> blocks =
                dag.findMissedBlocksByVectorClock(remoteVector);

        /* Send blocks */
        int rest2Sent = blocks.size();
        for (Block b : blocks) {
            VegvisirProtocolMessage message = VegvisirProtocolMessage
                    .newBuilder().setMessageType(VegvisirProtocolMessage.MessageType.VECTOR_CLOCK)
                    .setVector(
                            Vector.VectorMessage.newBuilder()
                                    .setAdd(
                                            AddBlocks.newBuilder()
                                                    .addBlocksToAdd(b)
                                                    .build()
                                    )
                                    .setSendLimit(--rest2Sent)
                                    .setType(Vector.VectorMessage.MessageType.BLOCKS)
                                    .build()
                    )
                    .build();
            config.send(message);
        }
    }


    @Override
    public void onNewMessage(VegvisirProtocolMessage message) {

    /* Assume both ends using vector clock protocol */
        switch (message.getVector().getType()) {
            case BLOCKS:
                handleAddBlocks(message.getVector().getAdd().getBlocksToAddList());
                if (message.getVector().getSendLimit() == 0) {
                    config.endProtocol();
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
                break;
            case LOCAL_VECTOR_CLOCK:
                computeSendBlocks(message.getVector().getLocalView());
                break;
            case ALL_VECTOR_CLOCKS:
                break;
            case UNRECOGNIZED:
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public Handshake.ProtocolVersion getVersion() {
        return Handshake.ProtocolVersion.VECTOR;
    }

    protected void handleAddBlocks(Iterable<com.isaacsheff.charlotte.proto.Block> blocks) {
        dag.addAllBlocks(blocks);
    }
}
