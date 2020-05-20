package com.vegvisir.core.blockdag;

public interface NewBlockListener {

    /**
     * Called when a new block arrived.
     * @param block a charlotte block.
     */
    void onNewBlock(com.isaacsheff.charlotte.proto.Block block);
}
