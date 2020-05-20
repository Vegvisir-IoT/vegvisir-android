package com.vegvisir.core.blockdag;

import com.google.protobuf.InvalidProtocolBufferException;
import com.isaacsheff.charlotte.proto.Block;
import com.isaacsheff.charlotte.proto.Reference;
import com.vegvisir.core.config.Config;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class BlockDAGv1 extends BlockDAG {

    private Map<Reference, Set<String>> witnessMap;

    private Set<Reference> frontierReference;

    DataManager manager;

    public BlockDAGv1(Block genesisBlock) {
        super(genesisBlock, null);
    }

    public BlockDAGv1(Block genesisBlock, Config config, DataManager manager, NewBlockListener listener) {
        super(genesisBlock, config);
        this.newBlockListener = listener;
        this.manager = manager;
        frontierReference = new HashSet<>();
        witnessMap = manager.loadWitnessMap();
        Block oldGenesisBlock = manager.loadGenesisBlock();
        this.genesisBlock = oldGenesisBlock == null ? genesisBlock : oldGenesisBlock;
        blockStorage.putIfAbsent(BlockUtil.byRef(this.genesisBlock), this.genesisBlock);
        manager.saveGenesisBlock(this.genesisBlock);
    }

    public BlockDAGv1() {
        this(null);
    }

    /**
     * Verify all transactions and signature for the block. If all checks are passed, then append this block to the block dag.
     * TODO: check block transactions
     * @param block
     * @return
     */
    public boolean verifyBlock(com.vegvisir.core.datatype.proto.Block block) {
        if (!block.hasUserBlock()) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * Get a particular block by its reference.
     * @param ref
     * @return
     */
    public Block getBlock(Reference ref) {
        return blockStorage.get(ref);
    }


    /**
     * Helper method for version 0.1
     * @param blocks a collection of blocks to be append to the block dag.
     */
    @Override
    public synchronized void addAllBlocks(Iterable<Block> blocks) {
        blocks.forEach(b -> {
            putBlock(b);
            manager.saveBlock(b);
            try {
                frontierReference.removeAll(com.vegvisir.core.datatype.proto.Block.parseFrom(b.getBlock()).getUserBlock().getParentsList());
                frontierReference.add(BlockUtil.byRef(b));
            } catch (InvalidProtocolBufferException ex) {
                System.err.println(ex.getMessage());
            }
        });
    }


    @Override
    public void recoverBlocks() {
        manager.loadBlockSet().forEach(b -> {
            blockStorage.putIfAbsent(BlockUtil.byRef(b), b);
            newBlockListener.onNewBlock(b);
            try {
                frontierReference.removeAll(com.vegvisir.core.datatype.proto.Block.parseFrom(b.getBlock()).getUserBlock().getParentsList());
                frontierReference.add(BlockUtil.byRef(b));
            } catch (InvalidProtocolBufferException ex) {
                System.err.println(ex.getMessage());
            }
        });
    }


    /**
     * For version 0.1, we want this method help us to get all blocks.
     * @return all blocks on this node.
     */
    public Collection<Block> getAllBlocks() {
        return blockStorage.values();
    }

    @Override
    public void createBlock(Iterable<com.vegvisir.core.datatype.proto.Block.Transaction> transactions,
                            Iterable<Reference> parents) {
        com.vegvisir.core.datatype.proto.Block.UserBlock content = com.vegvisir.core.datatype.proto.Block.UserBlock.newBuilder().addAllParents(parents)
                .setUserid(getConfig().getNodeId())
                .setCryptoID(getConfig().getCryptoId())
                .setTimestamp(com.vegvisir.core.datatype.proto.Timestamp.newBuilder().setUtcTime(new Date().getTime()).build())
                .addAllTransactions(transactions)
                .build();
        com.isaacsheff.charlotte.proto.Block block = com.isaacsheff.charlotte.proto.Block.newBuilder()
                .setBlock(
                        com.vegvisir.core.datatype.proto.Block.newBuilder().setUserBlock(content)
                                .setSignature(getConfig().signProtoObject(content))
                                .build().toByteString()
                ).build();
        addAllBlocks(Collections.singleton(block));
    }

    @Override
    public Set<Reference> getFrontierBlocks() {
        return frontierReference;
    }

    @Override
    public Set<String> computeWitness(Reference ref) {
        return witnessMap.getOrDefault(ref, Collections.emptySet());
    }

    public void witness(Reference ref, String remoteId) {
        if (!witnessMap.containsKey(ref)) {
            witnessMap.put(ref, new HashSet<>());
            witnessMap.get(ref).add(config.getDeviceID());
        }
        witnessMap.get(ref).add(remoteId);
        manager.updateWitnessMap(ref, witnessMap.get(ref));
    }

    @Override
    public void witness(Block block, String remoteId) {
        witness(BlockUtil.byRef(block), remoteId);
    }


    void registerDataManager(DataManager manager) {
        this.manager = manager;
    }


}
