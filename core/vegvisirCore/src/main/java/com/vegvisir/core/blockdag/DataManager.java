package com.vegvisir.core.blockdag;

import com.isaacsheff.charlotte.proto.Block;
import com.isaacsheff.charlotte.proto.Reference;

import java.util.Map;
import java.util.Set;

public interface DataManager {

//    void save(String key, Object value);
//
//    void save(String table, String key, Object value);
//
//    Object load(String key, String value);
//
//    Map<String, Object> load(String table);

    void saveBlock(Block block);

    Iterable<Block> loadBlockSet();

    void updateWitnessMap(Reference ref, Set<String> devices);

    Map<Reference, Set<String>> loadWitnessMap();

    void saveGenesisBlock(Block genesis);

    Block loadGenesisBlock();

    void updateAppCount(int c);

    int loadAppCount();

    void updateTransactionHeight(long height);

    long loadTransactionHeight();
}
