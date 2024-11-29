package com.apicatalog.vc;

import java.util.Arrays;
import java.util.Random;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.cryptosuite.algorithm.SignatureAlgorithm;
import com.apicatalog.linkedtree.jsonld.io.JsonLdWriter;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.key.GenericMulticodecKey;
import com.apicatalog.multikey.GenericMultikey;
import com.apicatalog.multikey.Multikey;

/**
 * Simple XOR to test the rest.
 */
class TestAlgorithm implements SignatureAlgorithm {

    @Override
    public void verify(byte[] publicKey, byte[] signature, byte[] data) throws VerificationError {
        final byte[] result = new byte[publicKey.length];
        Arrays.fill(result, (byte) 0);

        for (int i = 0; i < data.length; i++) {
            result[i % publicKey.length] = (byte) ((result[i % publicKey.length] + data[i]) ^ publicKey[i % publicKey.length]);
        }

        if (!Arrays.equals(result, signature)) {
            throw new VerificationError(VerificationErrorCode.InvalidSignature);
        }
    }

    @Override
    public byte[] sign(byte[] privateKey, byte[] data) throws CryptoSuiteError {

        final byte[] result = new byte[privateKey.length];
        Arrays.fill(result, (byte) 0);

        for (int i = 0; i < data.length; i++) {
            result[i % privateKey.length] = (byte) ((result[i % privateKey.length] + data[i]) ^ privateKey[i % privateKey.length]);
        }

        return result;
    }

    @Override
    public KeyPair keygen() throws CryptoSuiteError {

        byte[] raw = new byte[32];

        new Random().nextBytes(raw);

        var publicKey = new GenericMulticodecKey(
                TestMulticodecKeyMapper.PUBLIC_KEY_CODEC,
                Multibase.BASE_58_BTC,
                raw);

        var privateKey = new GenericMulticodecKey(
                TestMulticodecKeyMapper.PRIVATE_KEY_CODEC,
                Multibase.BASE_58_BTC,
                raw);

        return GenericMultikey.of(null, null, publicKey, privateKey);
    }

    public static void main(String[] args) throws CryptoSuiteError {
        System.out.println(
                new JsonLdWriter()
                        .scan(Multikey.class)
                        .compact((new TestAlgorithm()).keygen()));
    }
}
