package com.vegvisir.core.blockdag;

import com.isaacsheff.charlotte.proto.Block;
import com.isaacsheff.charlotte.proto.Reference;
import com.vegvisir.core.config.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import vegvisir.proto.Vector;

public abstract class BlockDAG {

    /**
     * This stores all blocks, we may need to create a separate class for this in the future
     */
    protected ConcurrentMap<Reference, Block> blockStorage;

    /**
     * The genesisBlock, we might not need this in the future because of new data structure to model dag.
     */
    protected Block genesisBlock;

    /**
     * This config object contains all information about current nodes and we will use this to do cryptographic
     * related jobs as well, such as signing blocks, verify signatures and calculating hashes.
     */
    protected Config config;


    /**
     * A Listener which gets called when a new block arrived. This allows upper layers get notified for new blocks.
     */
    protected NewBlockListener newBlockListener;


    /**
     * This is constructor might need to be changed due to it contains genesis block.
     * @param genesisBlock
     * @param config
     */
    public BlockDAG(Block genesisBlock, Config config) {
        this.genesisBlock = genesisBlock;
        blockStorage = new ConcurrentHashMap<>();
        this.config = config;
//        blockStorage.put(BlockUtil.byRef(genesisBlock), genesisBlock);

    }

    public BlockDAG(Block genesisBlock, Config config, NewBlockListener listener) {
        this(genesisBlock, config);
        this.newBlockListener = listener;

    }


    /**
     * A default constructor.
     */
    public BlockDAG() {
        this(null, null);
    }


    /**
     * Only set genesis block once, if genesis block is not set at instantiation stage.
     * @param genesisBlock
     * @return true if set successfully.
     */
    public synchronized boolean setOnceGenesisBlock(Block genesisBlock) {
        if (this.genesisBlock != null) {
            this.genesisBlock = genesisBlock;
            return true;
        } else
            return false;
    }


    /**
     * Add a block to the block map. This is deprecated, using @putBlock instead.
     * @param block
     * @return
     */
    public Reference addBlock(Block block) {
        return BlockUtil.byRef(blockStorage.putIfAbsent(BlockUtil.byRef(block), block));
    }


    /**
     * put a block to the dag. If the block already exists, then return null, otherwise return the reference.
     * A reference is a protobuf object contains a hash.
     * @param block
     * @return the Reference of the block if the block is not duplicated otherwise null.
     */
    public Reference putBlock(Block block) {
        Reference ref = BlockUtil.byRef(block);
        if (blockStorage.putIfAbsent(ref, block) == null) {
            witness(block, config.getDeviceID());
            newBlockListener.onNewBlock(block);
            return ref;
        }
        return null;
    }


    /**
     * Verify all transactions and signature for the block. If all checks are passed, then append this block to the block dag.
     * TODO: check block transactions
     * @param block
     * @return
     */
    public abstract boolean verifyBlock(com.vegvisir.core.datatype.proto.Block block);


    /**
     * Get a particular block by its reference.
     * @param ref
     * @return
     */
    public Block getBlock(Reference ref) {
        return blockStorage.get(ref);
    }


    /**
     * @return the config object. This could be null.
     */
    public Config getConfig() {
        return config;
    }


    /**
     * Probably we don't want to call this method. It's here because of default constructor.
     * @param config
     */
    public void setConfig(Config config) {
        this.config = config;
    }


    /**
     * Helper method for version 0.1
     * @param blocks a collection of blocks to be append to the block dag.
     */
    public void addAllBlocks(Iterable<Block> blocks) {
        blocks.forEach(b -> {
            putBlock(b);
        });
    }


    /**
     * For version 0.1, we want this method help us to get all blocks.
     * @return all blocks on this node.
     */
    public Collection<Block> getAllBlocks() {
        return blockStorage.values();
    }


    /**
     * [V2 Features]
     * @return a vector clock represented the frontier set of current node.
     */
    public Vector.VectorClock computeFrontierSet() {
        return null;
    }


    public void setNewBlockListener(NewBlockListener newBlockListener) {
        this.newBlockListener = newBlockListener;
    }

    public abstract void createBlock(Iterable<com.vegvisir.core.datatype.proto.Block.Transaction> transactions,
                                     Iterable<Reference> parents);

    public Set<Reference> getFrontierBlocks() {
        return Collections.emptySet();
    }

    public abstract Set<String> computeWitness(Reference ref);

    public abstract void witness(Block block, String remoteId);


    public void save() {}

  
    /**
     * [V2 Feature]
     * @param remoteVC
     * @return
     */
    public Iterable<Block> findMissedBlocksByVectorClock(Vector.VectorClock remoteVC) {
        return null;
    }

    public void addLeadingBlock() {}

    public Set<Reference> getLeadingBlocks() {return Collections.emptySet();}

    public Reference  createBlock(String cryptoID, Iterable<com.vegvisir.core.datatype.proto.Block.Transaction> transactions, Iterable<Reference> parents) {
        return null;
    }

    public abstract void recoverBlocks();

}
