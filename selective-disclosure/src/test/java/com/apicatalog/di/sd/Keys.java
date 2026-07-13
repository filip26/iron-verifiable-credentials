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
        byte[] baseSecretKey,
        byte[] proofPublicKey,
        byte[] proofSecretKey,
        byte[] hmacKey) {

    static final MultibaseDecoder MULTIBASE = MultibaseDecoder.getInstance();

    static final MulticodecDecoder MULTICODEC = MulticodecDecoder.newInstance(
            KeyCodec.P256_PRIVATE,
            KeyCodec.P384_PRIVATE);

    public static Keys from(Map<String, Object> keysMap) {

        var baseKeys = (Map<String, String>) keysMap.get("baseKeyPair");

        var basePrivateKey = MULTIBASE.decode(baseKeys.get("secretKeyMultibase"));
        var basePrivateKeyCodec = MULTICODEC.getCodec(basePrivateKey).orElseThrow();

        var proofKeys = (Map<String, String>) keysMap.get("proofKeyPair");

        var proofPrivateKey = MULTIBASE.decode(proofKeys.get("secretKeyMultibase"));
        var proofPrivateKeyCodec = MULTICODEC.getCodec(proofPrivateKey).orElseThrow();

        if (!basePrivateKeyCodec.equals(proofPrivateKeyCodec)) {
            throw new IllegalStateException("Unsupported");
        }

        // FIXME
        return new Keys(
                basePrivateKeyCodec,
                null,
                basePrivateKeyCodec.decode(basePrivateKey),
                null,
                proofPrivateKeyCodec.decode(proofPrivateKey),
                HexFormat.of().parseHex((String) keysMap.get("hmacKeyString")));
    }

}
