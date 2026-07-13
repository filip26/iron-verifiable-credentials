package com.apicatalog.di;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.stream.Stream;

import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Parser;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.processor.StandardGraphProcessor;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.apicatalog.trust.proof.MapProofCursor;
import com.fasterxml.jackson.core.JsonFactory;

class Resources {

    static DataModel LEXICAL_MODEL_1 = DataIntegrity.newLexicalModelBuilder(DataModel.C14N_JCS)
            .proof(EdDSA2022.withJCS())
            .proof(ECDSA2019.withJCS())
            .proof(MLDSA2024.get44withJCS())
            .proof(SLHDSA2024.get128withJCS())
            .c14n(Jcs::canonize)
            .processor(MapProofCursor::new)
            .build();

    static DataModel SEMANTIC_MODEL_1 = DataIntegrity.newSematicModelBuilder(DataModel.C14N_RDFC)
            .proof(EdDSA2022::get)
            .proof(ECDSA2019.withRDFC())
            .proof(MLDSA2024::get44)
            .proof(SLHDSA2024::get128s)
            .Ed25519Signature2020()
            .expand(VerifierTest::expand)
            .tordf(VerifierTest::toRDF)
            .c14n(VerifierTest::newRDFC)
            .processor(StandardGraphProcessor::new)
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
