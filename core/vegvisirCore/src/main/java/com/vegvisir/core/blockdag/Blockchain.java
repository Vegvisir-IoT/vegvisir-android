package com.vegvisir.core.blockdag;

import com.isaacsheff.charlotte.proto.CryptoId;
import com.isaacsheff.charlotte.proto.Hash;
import com.isaacsheff.charlotte.proto.Reference;
import com.vegvisir.core.datatype.proto.Block;
import java.util.List;

public abstract class Blockchain {
    /**
     * Store a list of block hashes, the concrete list type depends on implementation
     */
    List<Reference> _blocks;

    /**
     * The block dag owning this chain.
     */
    BlockDAG _dag;

    /**
     * The id of the node owning this chain. This is the node that can create new blocks(Transactions)
     * on this chain. All other nodes can ONLY append blocks that created by the @nodeID to this chain.
     */
    com.isaacsheff.charlotte.proto.CryptoId cryptoId;


    public Blockchain(BlockDAG dag, com.isaacsheff.charlotte.proto.CryptoId id) {
        _dag = dag;
        cryptoId = id;
    }

    /**
     * Create a new block by given transactions and parent blocks. This is different with appendBlocks in
     * 2 ways. First, this method should only be called by the host device of the chain. Other devices adding
     * blocks to the chain should call appendBlocks instead. Second, this call involves locks to serialize
     * the order of blocks.
     * If all transactions and parents are valid, this new block will be appended to the chain and saved in the
     * global block map.
     * @param transactions
     * @param parents
     * @return a new created block.
     */
    public abstract com.isaacsheff.charlotte.proto.Block createBlock(Iterable<Block.Transaction> transactions, Iterable<Reference> parents);


    /**
     * Append all blocks in @blocks to the current chain. If all blocks are already available in the chain, then
     * return null. Otherwise, return the last hash of the chain.
     * @param blocks
     * @return null if all blocks in @blocks are duplicate. Otherwise, the hash of the last appended block.
     */
    public abstract Reference appendBlocks(Iterable<com.isaacsheff.charlotte.proto.Block> blocks);


    /**
     * Append a block to the chain. This function assume the given block is valid.
     * @param block a valid block to be appended.
     * @return the hash of the given block.
     */
    public abstract Reference appendBlock(com.isaacsheff.charlotte.proto.Block block);


    /**
     * @return the this chain's owner's crypto id.
     */
    public CryptoId getCryptoId() {
        return cryptoId;
    }

    public String getCryptoIdStr() {
        return BlockUtil.cryptoId2Str(cryptoId);
    }


    /**
     * @return a list of all blocks' references on this chain so far.
     */
    public List<Reference> getBlockList() {
        return _blocks;
    }


//    /**
//     * @param index the index of the block in the list.
//     * @return a vector clock of the block in the given index position. If index is out of range,
//     * then return null.
//     */
//    public Block.VectorClock getVectorClock(int index) {
//        if (index >= _blocks.size())
//            return null;
//        return _dag.getBlock(_blocks.get(index)).getVegvisirBlock().getBlock().getClock();
//    }
//
//
//    /**
//     * @return the vector clock of the last block on this chain so far. This should never return
//     * null because all chains contains a reference to the genesis block.
//     */
//    public Block.VectorClock getLastVectorClock() {
//        return getVectorClock(_blocks.size()-1);
//    }
}
