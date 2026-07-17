package com.apicatalog.di.sd;

import java.util.HexFormat;
import java.util.Map;

import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;

public record Keys(
        Multicodec codec,
        byte[] basePublicKey,
        byte[] basePrivateKey,
        byte[] proofPublicKey,
        byte[] proofPrivateKey,
        byte[] hmacKey) {

    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();

    static final MulticodecDecoder MULTICODEC = MulticodecDecoder.newInstance(
            KeyCodec.P256_PRIVATE,
            KeyCodec.P384_PRIVATE);

    public static Keys from(Map<String, Object> keysMap) {

        var baseKeys = (Map<String, String>) keysMap.get("baseKeyPair");

        var basePublicKey = MULTIBASE.decode(baseKeys.get("publicKeyMultibase"));
        var basePublicKeyCodec = MULTICODEC.getCodec(basePublicKey).orElseThrow();

        var basePrivateKey = MULTIBASE.decode(baseKeys.get("secretKeyMultibase"));
        var basePrivateKeyCodec = MULTICODEC.getCodec(basePrivateKey).orElseThrow();

        if (!basePublicKeyCodec.equals(basePrivateKeyCodec)) {
            throw new IllegalArgumentException();
        }

        var proofKeys = (Map<String, String>) keysMap.get("proofKeyPair");

        var proofPublicKey = MULTIBASE.decode(proofKeys.get("publicKeyMultibase"));
        var proofPrivateKey = MULTIBASE.decode(proofKeys.get("secretKeyMultibase"));
        var proofPrivateKeyCodec = MULTICODEC.getCodec(proofPrivateKey).orElseThrow();

        if (!basePrivateKeyCodec.equals(proofPrivateKeyCodec)) {
            throw new IllegalStateException("Unsupported");
        }

        return new Keys(
                basePrivateKeyCodec,
                basePublicKey,
                basePrivateKeyCodec.decode(basePrivateKey),
                proofPublicKey,
                proofPrivateKeyCodec.decode(proofPrivateKey),
                HexFormat.of().parseHex((String) keysMap.get("hmacKeyString")));
    }

}
