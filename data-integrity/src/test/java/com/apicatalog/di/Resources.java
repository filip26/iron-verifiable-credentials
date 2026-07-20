package com.apicatalog.di;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;

import com.apicatalog.di.std.StandardGraphProcessor;
import com.apicatalog.di.std.StandardMapProcessor;
import com.apicatalog.di.suite.ECDSA2019;
import com.apicatalog.di.suite.EdDSA2022;
import com.apicatalog.di.suite.MLDSA2024;
import com.apicatalog.di.suite.SLHDSA2024;
import com.apicatalog.jcs.Jcs;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.api.RdfConsumerException;
import com.apicatalog.rdf.api.RdfQuadConsumer;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.rdf.nquads.NQuadsWriter;
import com.apicatalog.security.Digestor;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Emitter;
import com.apicatalog.tree.io.jakcson.Jackson2Parser;
import com.apicatalog.trust.lexical.LexicalModel;
import com.apicatalog.trust.lexical.MapProofCursor;
import com.apicatalog.trust.model.ProcessingModel;
import com.apicatalog.trust.semantic.GraphPayloadGenerator;
import com.apicatalog.trust.semantic.GraphProofCursor;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;
import com.fasterxml.jackson.core.JsonFactory;

class Resources {

    static LexicalModel LEXICAL_MODEL = DataIntegrity.createLexicalModel(ProcessingModel.C14N_JCS)
            .proofProperty(DataIntegrity.VOCAB_PROOF_KEY)
            .proof(EdDSA2022.withJCS())
            .proof(ECDSA2019.withJCS())
            .proof(MLDSA2024.get44withJCS())
            .proof(SLHDSA2024.get128withJCS())
            .c14n(Jcs::canonize)
            .processor(StandardMapProcessor::newInstance)
            .cursor(MapProofCursor::newInstance)
            .build();

    static SemanticModel SEMANTIC_MODEL = DataIntegrity.createSematicModel(ProcessingModel.C14N_RDFC)
            .proofPredicate(DataIntegrity.VOCAB_PROOF_URI)
            .proof(EdDSA2022.withRDFC())
            .proof(ECDSA2019.withRDFC())
            .proof(MLDSA2024.get44withRDFC())
            .proof(SLHDSA2024.get128withRDFC())
            .Ed25519Signature2020()
            .expand(Resources::expand)
            .tordf(Resources::toRDF)
            .c14n(Resources::createRDFC)
            .processor(StandardGraphProcessor::newInstance)
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

    static final void toRDF(Object document, final SemanticModel.QuadConsumer consumer) {
        try {
            // TODO temporary, remove with Titanium v2.x.x
            var bos = new ByteArrayOutputStream();
            try (var emitter = Jackson2Emitter.newEmitter(bos, JsonFactory.builder().build())) {
                Tree.write(document, emitter);
            }

            var toRdf = JsonLd.toRdf(JsonDocument.of(new ByteArrayInputStream(bos.toByteArray())))
                    .loader(ContextLoader.getInstance());

            // TODO remove with rdf-api 2.0.0
            toRdf.provide(new RdfQuadConsumer() {

                @Override
                public RdfQuadConsumer quad(String subject, String predicate, String object, String datatype,
                        String language,
                        String direction, String graph) {

                    consumer.accept(subject, predicate, object, datatype, language, direction, graph);
                    return this;
                }
            });

        } catch (IOException | JsonLdError e) {
            throw new IllegalStateException(e);
        }
    }

    static final Collection<Object> expand(Map<String, Object> document) {
        try {
            // TODO temporary, remove with Titanium v2.x.x
            var bos = new ByteArrayOutputStream();
            try (var emitter = Jackson2Emitter.newEmitter(bos, JsonFactory.builder().build())) {
                Tree.write(document, emitter);
            }

            var expanded = JsonLd.expand(JsonDocument.of(new ByteArrayInputStream(bos.toByteArray())))
                    .loader(ContextLoader.getInstance()).get();

            try (var parser = Jackson2Parser.newParser(new ByteArrayInputStream(expanded.toString().getBytes()),
                    JsonFactory.builder().build())) {
                return Tree.read(parser);
            }

        } catch (IOException | JsonLdError e) {
            throw new IllegalStateException(e);
        }
    }

    static final RdfcPrcessor createRDFC() {
        return new RdfcPrcessor(); // TODO reuse one instance across
    }

    static class RdfcPrcessor implements GraphCanonizer {

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final RdfCanon canon = RdfCanon.create(SHA_256);

        @Override
        public byte[] canonize() {

            bos.reset();

            canon.provide(line -> {
                try {
                    bos.write(line.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            return bos.toByteArray();
        }

        @Override
        public void canonize(QuadConsumer consumer) {
            try {
                canon.provide(((subject, predicate, object, datatype, language, direction, graph) -> {
                    consumer.accept(subject, predicate, object, datatype, language, direction, graph);
                    return null;
                }));
            } catch (RdfConsumerException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public QuadConsumer consumer() {
            // TODO remove with rdf-api 2.0.0
            return new SemanticModel.QuadConsumer() {
                @Override
                public void accept(
                        String subject,
                        String predicate,
                        String object,
                        String datatype,
                        String language,
                        String direction,
                        String graph) {

                    canon.quad(subject, predicate, object, datatype, language, direction, graph);
                }
            };
        }

        @Override
        public Map<String, String> labels() {
            return canon.mapping();
        }

        @Override
        public String toNQuad(String subject, String predicate, String object, String datatype, String language,
                String direction, String graph) {
            return NQuadsWriter.nquad(subject, predicate, object, datatype, language, direction, graph);
        }
    }
}
