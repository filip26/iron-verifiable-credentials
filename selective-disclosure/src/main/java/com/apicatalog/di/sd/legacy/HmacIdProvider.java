package com.apicatalog.di.sd.legacy;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.apicatalog.multibase.Multibase;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfResource;

class HmacIdProvider {

    final Map<RdfResource, RdfResource> mapping = new HashMap<>();
    final Mac hmac;

    private HmacIdProvider(Mac hmac) {
        this.hmac = hmac;
    }

    public RdfResource getHmacId(final RdfResource resource) {

        RdfResource hmacId = mapping.get(resource);

        if (hmacId == null) {
            hmacId = Rdf.createBlankNode("_:" + Multibase.BASE_64_URL.encode(hmac.doFinal(resource.getValue().substring(2).getBytes(StandardCharsets.UTF_8))));
            hmac.reset();
            mapping.put(resource, hmacId);
        }
        return hmacId;
    }

    public static HmacIdProvider newInstance(final byte[] hmacKey) {
        final String type = "HmacSHA256";// getHmacType("P-256");
        final SecretKeySpec key = new SecretKeySpec(hmacKey, type);
        try {
            final Mac hmac = Mac.getInstance(type);
            hmac.init(key);
            return new HmacIdProvider(hmac);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public Map<RdfResource, RdfResource> mapping() {
        return mapping;
    }

//    public static byte[] generateKey(int length) throws CryptoSuiteError {
//        try {
//            byte[] key = new byte[length];
//
//            final SecureRandom random = SecureRandom.getInstance("NativePRNGNonBlocking");
//
//            random.nextBytes(key);
//
//            return key;
//        } catch (NoSuchAlgorithmException e) {
//            throw new CryptoSuiteError(CryptoSuiteErrorCode.KeyGenerator, e);
//        }
//    }
//    
//    protected static final String getHmacType(CurveType curveType) {
//        switch (curveType) {
//        case P256:
//            return "HmacSHA256";
//        case P384:
//            return "HmacSHA384";
//        }
//        throw new IllegalArgumentException("An unknown HMAC curve " + curveType);
//    }
}
