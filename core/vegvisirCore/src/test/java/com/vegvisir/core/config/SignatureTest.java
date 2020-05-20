package com.vegvisir.core.config;

import com.google.protobuf.ByteString;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;

public class SignatureTest {

    @Test
    public void signAndVerify() {
        KeyPair keyPair = Config.generateKeypair();
        byte[] data = ByteString.copyFromUtf8("ABDCDEFG").toByteArray();
        byte[] signedValue = Config.sign(keyPair.getPrivate(), data);
        Assertions.assertTrue(Config.checkSignature(data, keyPair.getPublic(), signedValue));
        System.out.println("Test passed");
    }
}
