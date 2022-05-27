package com.apicatalog.lds.ed25519;

import com.apicatalog.lds.SignatureSuite;
import com.apicatalog.lds.primitive.MessageDigest;
import com.apicatalog.lds.primitive.EdDsaSignature;
import com.apicatalog.lds.primitive.Urdna2015;

public class Ed25519Signature2020 extends SignatureSuite {

    public Ed25519Signature2020() {
        super(
                "https://w3id.org/security#Ed25519Signature2020",
                new Urdna2015(),
                new MessageDigest("SHA-256"),
                new EdDsaSignature("Ed25519")
                );
    }

}
