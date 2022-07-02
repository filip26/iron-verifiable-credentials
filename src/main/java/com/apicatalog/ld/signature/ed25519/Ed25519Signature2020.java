package com.apicatalog.ld.signature.ed25519;

import java.net.URI;
import java.util.Objects;

import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.ld.signature.proof.VerificationMethod;

public final class Ed25519Signature2020 extends SignatureSuite {

    protected static final String BASE = "https://w3id.org/security#";
    protected static final String TYPE = "Ed25519Signature2020";

    public Ed25519Signature2020() {
        super(
            BASE + TYPE,
            new Urdna2015(),
            new MessageDigest("SHA-256"),
            new Ed25519Signature2020Provider(),
            new Ed25519Proof2020Adapter()
            );
    }

    public static boolean isTypeOf(final String type) {
        return Objects.equals(BASE + TYPE, type);
    }

    public static ProofOptions createOptions(VerificationMethod method, URI purpose) {
    return ProofOptions.create(BASE + TYPE, method, purpose);
    }
}