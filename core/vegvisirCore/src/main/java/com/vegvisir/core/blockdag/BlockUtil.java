package com.vegvisir.core.blockdag;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.isaacsheff.charlotte.proto.Block;
import com.isaacsheff.charlotte.proto.Hash;
import com.isaacsheff.charlotte.proto.Reference;
import com.vegvisir.core.config.Config;

public class BlockUtil {

    /**
     * @param block
     * @return a Reference of given block.
     */
    public static Reference byRef(Block block) {
        return Reference.newBuilder().setHash(Config.sha3(block)).build();
    }


    /**
     * @param hash
     * @return a Reference wraps the given hash.
     */
    public static Reference byRef(com.isaacsheff.charlotte.proto.Hash hash) {
        return Reference.newBuilder().setHash(hash).build();
    }

    /**
     * get a string representation of given crypto id.
     * Currently this function return a UTF8 string from sha3 hash.
     * @param id
     * @return a string format of given id.
     */
    public static String cryptoId2Str(com.isaacsheff.charlotte.proto.CryptoId id) {
        if (id.hasHash())
            return id.getHash().getSha3().toStringUtf8();
        else
            return id.getPublicKey().getEllipticCurveP256().getByteString().toStringUtf8();
    }

    public static String ref2Str(Reference ref) {
        return hash2Str(ref.getHash());
    }

    public static String hash2Str(Hash hash) {
        return hash.getSha3().toStringUtf8();
    }

    public static Reference refStr2Ref(String refStr) {
        Hash hash = Hash.newBuilder().setSha3(ByteString.copyFromUtf8(refStr)).build();
        return byRef(hash);
    }

    public static com.isaacsheff.charlotte.proto.CryptoId str2cryptoId(String idstr) {
        return com.isaacsheff.charlotte.proto.CryptoId.newBuilder()
                .setHash(com.isaacsheff.charlotte.proto.Hash.newBuilder()
                        .setSha3(ByteString.copyFromUtf8(idstr)).build())
                .build();

    }

    public static com.vegvisir.core.datatype.proto.Block getVegvisirBlock(Block block) throws InvalidProtocolBufferException  {
        return com.vegvisir.core.datatype.proto.Block.parseFrom(block.getBlock());
    }
}
