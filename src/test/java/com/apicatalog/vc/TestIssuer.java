package com.apicatalog.vc;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.vc.issuer.StandardIssuer;

public class TestIssuer extends StandardIssuer {

    public TestIssuer(KeyPair keyPair, DocumentLoader loader) {
        super(keyPair, new TestSignatureSuite(), loader);
        // TODO Auto-generated constructor stub
    }

}
