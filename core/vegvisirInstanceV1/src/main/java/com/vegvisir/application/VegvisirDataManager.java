package com.vegvisir.application;

import android.content.Context;

import com.esotericsoftware.kryo.ReferenceResolver;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.isaacsheff.charlotte.proto.Block;
import com.isaacsheff.charlotte.proto.Reference;
import com.vegvisir.core.blockdag.BlockUtil;
import com.vegvisir.core.blockdag.DataManager;
import com.vegvisir.core.config.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.paperdb.Book;
import io.paperdb.Paper;

public class VegvisirDataManager implements DataManager {

    private static DataManager manager;

    private static final Object lock = new Object();

    private static String DB_BLOCK = "VEGVISIR_DAG";
    private static String DB_META = "VEGVISIR_META";
    private static String DB_WITNESS = "VEGVISIR_WITNESS";

    private static String GENESIS_KEY = "genesis";
    private static String COUNTER_NAME = "counter";
    private static String APP_COUNT_NAME = "app_count";
    private static String TX_HEIGHT= "tx_height";

    private Book dagDB;
    private Book metaDB;
    private Book witnessDB;

    /**
     * get a singleton data manager object.
     * @return a single data manager object.
     */
    public static DataManager getDataManager(Context context) {
        if (manager == null) {
            synchronized (lock) {
                if (manager == null) {
                    manager = new VegvisirDataManager(context);
                }
            }
        }
        return manager;
    }

    private VegvisirDataManager(Context context) {
        Paper.init(context);
        dagDB = Paper.book(DB_BLOCK);
        metaDB = Paper.book(DB_META);
        witnessDB = Paper.book(DB_WITNESS);
    }

    @Override
    public void saveBlock(Block block) {
        synchronized (lock) {
            final long counter = metaDB.read(COUNTER_NAME, 0L);
            dagDB.write(String.valueOf(counter), block.toByteArray());
            metaDB.write(COUNTER_NAME, counter+1);
        }
    }

    @Override
    public Iterable<Block> loadBlockSet() {
        List<Block> blocks = new ArrayList<>();
        long counter = 0;
        synchronized (lock) {
            counter = metaDB.read(COUNTER_NAME, 0L);
        }
        for (long i = 0; i < counter; i ++) {
            byte[] _block = dagDB.read(String.valueOf(i));
            try {
                blocks.add(Block.parseFrom(_block));
            } catch (InvalidProtocolBufferException ex) {
                throw new RuntimeException(ex.getLocalizedMessage());
            }
        }
        return blocks;
    }

    @Override
    public void updateWitnessMap(Reference ref, Set<String> devices) {
        witnessDB.write(Utils.str2Hex(BlockUtil.ref2Str(ref)), devices);
    }

    @Override
    public Map<Reference, Set<String>> loadWitnessMap() {
        Map<Reference, Set<String>> witnessMap = new ConcurrentHashMap<>();
        witnessDB.getAllKeys().forEach(hexRefStr -> {
            String refStr = Utils.hex2str(hexRefStr);
            witnessMap.put(BlockUtil.refStr2Ref(refStr), witnessDB.read(hexRefStr));
        });
        return witnessMap;
    }

    @Override
    public void saveGenesisBlock(Block genesis) {
        metaDB.write(GENESIS_KEY, genesis);
    }

    @Override
    public Block loadGenesisBlock() {
        return metaDB.read(GENESIS_KEY, null);
    }

    @Override
    public int loadAppCount() {
        return metaDB.read(APP_COUNT_NAME, 0);
    }

    @Override
    public void updateAppCount(int c) {
        metaDB.write(APP_COUNT_NAME, c);
    }

    @Override
    public long loadTransactionHeight() {
        return metaDB.read(TX_HEIGHT, 1L);
    }

    @Override
    public void updateTransactionHeight(long height) {
        metaDB.write(TX_HEIGHT, height);
    }
}
