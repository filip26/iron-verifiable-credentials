package com.apicatalog.di.sd;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.stream.Stream;

import com.apicatalog.di.DataIntegrity;
import com.apicatalog.di.suite.ECDSASD2023;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Parser;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.fasterxml.jackson.core.JsonFactory;

class Resources {

    static DataModel MODEL = DataIntegrity.newSematicModelBuilder(DataModel.C14N_RDFC)
            .proof(ECDSASD2023.getInstance())
            .expand(VerifierTest::expand)
            .compact(VerifierTest::compact)
            .tordf(VerifierTest::toRDF)
            .c14n(VerifierTest::newRDFC)
//TODO            .hmac()
            .processor(SDGraphProcessor::new)
            .processor(GraphProofCursor::new)
            .build();
    
    static final Map<String, MessageDigest> DIGEST_FACTORY;

    static {
        try {
            DIGEST_FACTORY = Map.of(
                    "SHA-256", MessageDigest.getInstance("SHA-256"),
                    "SHA-384", MessageDigest.getInstance("SHA-384")
                    );
            
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
    
    static JsonFactory FACTORY = JsonFactory.builder().build();
    
    static <T> Map<String, T> getMap(String name) throws IOException {
        try (var parser = Jackson2Parser.newParser(Resources.class.getResourceAsStream(name), FACTORY)) {
            return Tree.read(parser);
        }
    }

    static final Stream<String> stream() {
        return Stream.of(new File(Resources.class.getResource("").getPath()).listFiles())
                .filter(File::isFile)
                .map(File::getName);
    }
}
