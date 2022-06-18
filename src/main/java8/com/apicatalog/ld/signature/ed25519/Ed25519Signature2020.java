package com.apicatalog.ld.signature.ed25519;

import java.util.Objects;

import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;

public final class Ed25519Signature2020 extends SignatureSuite {

    private static final String TYPE = "https://w3id.org/security#Ed25519Signature2020";

    public Ed25519Signature2020() {
        super(
            TYPE,
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new TinkSignature(),
            new Ed25519SignatureAdapter()
            );
    }
    
    public static boolean isTypeOf(final String type) {
        return Objects.equals(TYPE, type);
    }
}