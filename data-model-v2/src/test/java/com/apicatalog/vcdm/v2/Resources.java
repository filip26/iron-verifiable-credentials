package com.apicatalog.vcdm.v2;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Map;
import java.util.stream.Stream;

import com.apicatalog.di.DataIntegrity;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.proof.c14n.StaticRDFC;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.security.Digestor;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Parser;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.semantic.GraphAccessor;
import com.apicatalog.trust.semantic.GraphPayloadGenerator;
import com.apicatalog.trust.semantic.GraphProofCursor;
import com.apicatalog.trust.semantic.GraphUpdater;
import com.apicatalog.trust.semantic.SemanticModel;
import com.fasterxml.jackson.core.JsonFactory;

class Resources {

//    static LexicalModel VCDM20_LEXICAL_MODEL = DataIntegrity.newLexicalModel(Model.C14N_JCS)
//            .proofProperty(DataIntegrity.PROPERTY_PROOF)
//            .proof(EdDSA2022.withJCS())
//            .proof(ECDSA2019.withJCS())
//            .proof(MLDSA2024.get44withJCS())
//            .proof(SLHDSA2024.get128withJCS())
//            .c14n(DataIntegrityProof.TYPE_NAME, StaticJCS::canonize) // proof type specific c14n provider
////            .c14n(Jcs::canonize)
//            .accessor(PropertyMapAccessor::newInstance)
//            .cursor(PropertyProofCursor::newInstance)
//             .build();

    static SemanticModel VCDM20_SEMANTIC_MODEL = DataIntegrity.newSematicModel(Model.C14N_RDFC)
//            .document(VCDM2.CREDENTIAL_TYPE_URI, VCDM2.Credential, VCDM2.CredentialMapper::materialize)
            .proofPredicate(DataIntegrity.PREDICATE_PROOF)
            .cryptosuite(EdDSA2022.withRDFC())
            .cryptosuite(ECDSA2019.withRDFC())
            .cryptosuite(MLDSA2024.get44withRDFC())
            .cryptosuite(SLHDSA2024.get128withRDFC())
            .Ed25519Signature2020()
//            .expand(Resources::expand)
//            .tordf(Resources::toRDF)
            // proof type specific c14n provider
            .c14n(Ed25519Signature2020.TYPE_URI, Ed25519Signature2020::newStaticRDFC)
            .c14n(DataIntegrityProof.TYPE_URI, StaticRDFC::newInstance)
            // document and proof c14n provider
//            .c14n(Resources::createRDFC)
            .accessor(GraphAccessor::newInstance)
            .updater(GraphUpdater::new)
            .cursor(GraphProofCursor::newInstance)
            .payload(GraphPayloadGenerator::new)
            .build();

    static final Digestor.Factory DIGEST_FACTORY;

    static final MessageDigest SHA_256;

    static {
        try {
            SHA_256 = MessageDigest.getInstance("SHA-256");

            DIGEST_FACTORY = (Map.<String, Digestor>of(
                    Digestor.SHA_256, SHA_256::digest,
                    Digestor.SHA_384, MessageDigest.getInstance("SHA-384")::digest))::get;

        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static JsonFactory FACTORY = JsonFactory.builder().build();

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
