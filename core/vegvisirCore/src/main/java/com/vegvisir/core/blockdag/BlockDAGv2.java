package com.vegvisir.core.blockdag;

import com.google.protobuf.InvalidProtocolBufferException;
import com.isaacsheff.charlotte.proto.Block;
import com.isaacsheff.charlotte.proto.Reference;

import com.isaacsheff.charlotte.proto.CryptoId;
import com.vegvisir.core.config.Config;

import java.sql.Ref;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import vegvisir.proto.Vector.VectorClock;

public class BlockDAGv2 extends BlockDAG {


    /**
     * A hash map mapping cryptoId -> blockchain.
     */
    private HashMap<String, BlockchainV1> blockchains;

    private final Set<Reference> leadingSet;

    private DataManager manager;

    public BlockDAGv2(Block genesisBlock, Config config, DataManager manager, NewBlockListener listener) {
        super(genesisBlock, config, listener);
        this.manager = manager;
        blockchains = new HashMap<>();
        blockchains.put(config.getDeviceID(), new BlockchainV1(this, config.getCryptoId()));
        leadingSet = new HashSet<>();
        Block oldGenesisBlock = manager.loadGenesisBlock();
        this.genesisBlock = oldGenesisBlock == null ? genesisBlock : oldGenesisBlock;
        blockStorage.putIfAbsent(BlockUtil.byRef(this.genesisBlock), this.genesisBlock);
        leadingSet.add(BlockUtil.byRef(this.genesisBlock));
        manager.saveGenesisBlock(this.genesisBlock);
    }

    /**
     * Verify all transactions and signature for the block. If all checks are passed, then append this block to the block dag.
     * TODO: check block transactions
     *
     * @param block
     * @return
     */
    @Override
    public boolean verifyBlock(com.vegvisir.core.datatype.proto.Block block) {
        if (block.hasGenesisBlock())
            return true;
        for (Reference r :  block.getUserBlock().getParentsList()) {
            if(!this.blockStorage.containsKey(r))
                return false;
        }
        return Config.checkSignature(block.getUserBlock().toByteArray(),
                block.getSignature());
    }


    /**
     * Add blocks to the dag. This will delegate to each blockchain to add blocks.
     * @param blocks a set of blocks to be appended.
     */
    public void addBlocks(Iterable<Block> blocks, boolean save) {
        for (Block block : blocks) {
            addBlock(block, save);
        }
    }

    public void addBlocks(Iterable<Block> blocks) {
        addBlocks(blocks, true);
    }

    @Override
    public Reference addBlock(Block block) {
        return addBlock(block, true);
    }

    public Reference addBlock(Block block, boolean save) {
        CryptoId blockId;
        Reference blockRef;
        com.vegvisir.core.datatype.proto.Block _block;
        try {
            _block = com.vegvisir.core.datatype.proto.Block.parseFrom(block.getBlock());
        } catch (InvalidProtocolBufferException ex) {
            return null;
        }
        if (!verifyBlock(_block))
            return null;
        blockId = _block.getUserBlock().getCryptoID();
        if (!blockchains.containsKey(BlockUtil.cryptoId2Str(blockId))) {
            if (validatePeer(blockId)) {
                addNewChain(blockId);
            } else {
                return null;
            }
        }
        synchronized (leadingSet) {
            putBlock(block);
            blockRef = blockchains.get(BlockUtil.cryptoId2Str(_block.getUserBlock().getCryptoID())).appendBlock(block);
            leadingSet.removeAll(_block.getUserBlock().getParentsList());
            leadingSet.add(blockRef);
        }
        if (save)
            manager.saveBlock(block);
        return blockRef;
    }

    //    /**
//     * Append blocks to chain with @cryptoId.
//     * @param blocks a set of blocks to be appended.
//     * @param cryptoId the id of the chain.
//     */
//    public void addBlocks(Iterable<Block> blocks, com.isaacsheff.charlotte.proto.CryptoId cryptoId) {
//        if (!blockchains.containsKey(cryptoId)) {
//            if (!validatePeer(cryptoId))
//                return;
//            addNewChain(cryptoId);
//        }
//        blockchains.get(cryptoId).appendBlocks(blocks);
//    }


    /**
     * Add all bocks in @blocks to the dag. This is a alias of addBlocks in V2.
     * @param blocks a collection of blocks to be append to the block dag.
     */
    @Override
    public void addAllBlocks(Iterable<Block> blocks) {
        addBlocks(blocks);
    }

    public void loadAllBlocks(Iterable<Block> blocks) {
        addBlocks(blocks, false);
    }


    /**
     * Put a new chain to the blockchain map.
     * @param id the id of the node. This is also the key to be used.
     */
    protected synchronized void addNewChain(com.isaacsheff.charlotte.proto.CryptoId id) {
        if (!blockchains.containsKey(BlockUtil.cryptoId2Str(id)))
            blockchains.put(BlockUtil.cryptoId2Str(id), new BlockchainV1(this,  id));
    }


    /**
     * Check whether the given id is in the peer set. This call will delegate to a CRDT 2P set to
     * validate the given @id.
     * @param id the crypto id of peer node.
     * @return true if this is a valid peer, i.e. @id in the set of valid peers.
     */
    protected boolean validatePeer(com.isaacsheff.charlotte.proto.CryptoId id) {
        return true;
    }


    /**
     * A frontier set of each chain can be represented by a vector clock. Therefore, we just need
     * to return the vector clock of the last block of the blockchain for this node.
     * @return vector clock represented the frontier blocks of each blockchain.
     */
    @Override
    public VectorClock computeFrontierSet() {
        VectorClock.Body.Builder builder = VectorClock.Body.newBuilder();
        blockchains.entrySet().forEach(entry -> {
//            VectorClock.Value value = VectorClock.Value.newBuilder()
//                    .setIndex(entry.getValue().getBlockList().size())
//                    .setCryptoId(entry.getValue().getCryptoId()).build();
//            builder.putValues(entry.getKey(), value);
                    builder.putClocks(entry.getKey(), entry.getValue().getBlockList().size());
        });
        VectorClock.Body body = builder.build();
        return VectorClock.newBuilder()
                .setBody(body)
                .setSignature(config.signProtoObject(body))
                .build();
    }


    /**
     * Return a list of blocks with the correct dependencies order that should be sent to the remote
     * device.
     * @param remoteVC the vector clock from remote device.
     * @return a list of blocks to be sent.
     */
    public Iterable<Block> findMissedBlocksByVectorClock(VectorClock remoteVC) {
        /* finding the last common frontier set */
        Set<Reference> commonFrontierSet = new HashSet<>();
        VectorClock myClock = computeFrontierSet();
        long index = 0;
        for (Map.Entry<String, Long> entry: myClock.getBody().getClocksMap().entrySet()) {
            index = 0;
            if (remoteVC.getBody().getClocksMap().containsKey(entry.getKey())) {
                index = Math.min(remoteVC.getBody().getClocksOrDefault(entry.getKey(), 0L), entry.getValue());
            }
            if (index > 0) {
                commonFrontierSet.add(
                        blockchains.get(entry.getKey())
                                .getBlockList()
                                .get((int)index-1));
            }
        }
//        Blockchain thisChain = blockchains.get(BlockUtil.cryptoId2Str(this.config.getCryptoId()));
//        Reference leadingBlock = thisChain.getBlockList().get(thisChain.getBlockList().size()-1);
        return _findMissedBlocks(commonFrontierSet, leadingSet);
    }


    /**
     * A BFS implementation that finds all blocks appended after the @commonFrontierSet with @leader
     * as leading block. The dependencies partial order is preserved in the returned list of blocks.
     * The leading block is the first element in the return list. If a block A -> block B("<" means
     * happens before), then, indexOf(Block A) > indexOf(Block B).
     *
     * @param commonFrontierSet
     * @param leaderSet
     * @return a list of blocks preserving the partial order of blocks based on their dependencies.
     */
    private List<Block> _findMissedBlocks(Set<Reference> commonFrontierSet, Set<Reference> leaderSet) {
        Deque<Reference> references = new ArrayDeque<>(leaderSet);
        Set<Reference> dupRefs = new HashSet<>();
        List<Block> blocks = new ArrayList<>();
        while (!references.isEmpty()) {
            Reference next = references.poll();
            if (next.equals(BlockUtil.byRef(genesisBlock)))
                continue;
            if (!dupRefs.add(next) || commonFrontierSet.contains(next))
                continue;
            Block nextBlock = this.getBlock(next);
            try {
                com.vegvisir.core.datatype.proto.Block _block = com.vegvisir.core.datatype.proto.Block.parseFrom(nextBlock.getBlock());
                references.addAll(_block.getUserBlock().getParentsList());
                blocks.add(nextBlock);
            } catch (InvalidProtocolBufferException ex) {
                System.err.println(ex.getMessage());
            }
        }
        Collections.reverse(blocks);
        return blocks;
    }


    @Override
    @Deprecated
    public void addLeadingBlock() {
        createBlock(BlockUtil.cryptoId2Str(config.getCryptoId()), Collections.emptyList(), Collections.emptyList());
    }

    protected Blockchain getMyChain() {
        return blockchains.get(BlockUtil.cryptoId2Str(config.getCryptoId()));
    }

    @Override
    @Deprecated
    public Set<Reference> getLeadingBlocks() {
        return leadingSet;
    }

    @Override
    public Reference createBlock(String cryptoID,
                                 Iterable<com.vegvisir.core.datatype.proto.Block.Transaction> transactions,
                                 Iterable<Reference> parents) {
        Reference ref;
        synchronized (leadingSet) {
            Set<Reference> _parents = new HashSet<>();
            parents.forEach(_parents::add);
            _parents.addAll(leadingSet);
            Block block = blockchains.get(cryptoID).createBlock(transactions, _parents);
            ref = addBlock(block);
        }
        return  ref;
    }

    @Override
    public void createBlock(Iterable<com.vegvisir.core.datatype.proto.Block.Transaction> transactions, Iterable<Reference> parents) {
        if (!parents.iterator().hasNext())
            throw new AssertionError("Parents set should not be empty");
        createBlock(getConfig().getDeviceID(), transactions, parents);
    }

    public void updateVCForDevice(String deviceID, VectorClock vc) {
        if (!blockchains.containsKey(deviceID))
            blockchains.putIfAbsent(deviceID, new BlockchainV1(this, BlockUtil.str2cryptoId(deviceID)));
        blockchains.get(deviceID).setLatestVC(vc);
    }

    /**
     * Compute how many witness for a particular block.
     * @param ref the reference for the block
     * @return a set of device ids who have seen the given block(reference).
     */
    @Override
    public Set<String> computeWitness(Reference ref) {
        Set<String> witnessDevices = new HashSet<>();
        Block block = blockStorage.get(ref);
        com.vegvisir.core.datatype.proto.Block _block;
        try {
            _block = com.vegvisir.core.datatype.proto.Block.parseFrom(block.getBlock());
        } catch (InvalidProtocolBufferException ex) {
            throw new RuntimeException(ex.getLocalizedMessage());
        }
        long height = _block.getUserBlock().getHeight();
        String id = BlockUtil.cryptoId2Str(_block.getUserBlock().getCryptoID());
        blockchains.entrySet().forEach(bc -> {
            Long vc = bc.getValue().getLatestVC().getBody().getClocksMap().getOrDefault(id, null);
            if (vc != null && vc >= height) {
                witnessDevices.add(bc.getKey());
            }
        });
        return witnessDevices;
    }

    @Override
    public void witness(Block block, String remoteId) {}

    @Override
    public void recoverBlocks() {
        loadAllBlocks(manager.loadBlockSet());
//        manager.loadBlockSet().forEach(b -> {
//            blockStorage.putIfAbsent(BlockUtil.byRef(b), b);
//            newBlockListener.onNewBlock(b);
//            leadingSet.removeAll(b.getVegvisirBlock().getBlock().getParentsList());
//            leadingSet.add(BlockUtil.byRef(b));
//        });
    }

    @Override
    public Set<Reference> getFrontierBlocks() {
        return leadingSet;
    }
}
