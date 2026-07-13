package com.apicatalog.di.sd;

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

import com.apicatalog.di.DataIntegrity;
import com.apicatalog.di.suite.ECDSASD2023;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.api.RdfConsumerException;
import com.apicatalog.rdf.api.RdfQuadConsumer;
import com.apicatalog.rdf.canon.RdfCanon;
import com.apicatalog.rdf.nquads.NQuadsWriter;
import com.apicatalog.tree.io.Tree;
import com.apicatalog.tree.io.jakcson.Jackson2Emitter;
import com.apicatalog.tree.io.jakcson.Jackson2Parser;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.model.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.model.SemanticModel.QuadConsumer;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.fasterxml.jackson.core.JsonFactory;

import jakarta.json.Json;

class Resources {

    static DataModel MODEL = DataIntegrity.newSematicModelBuilder(DataModel.C14N_RDFC)
            .proof(ECDSASD2023.getInstance())
            .expand(Resources::expand)
            .compact(Resources::compact)
            .tordf(Resources::toRDF)
            .c14n(Resources::newRDFC)
//TODO            .hmac()
            .processor(SDGraphProcessor::new)
            .processor(GraphProofCursor::new)
            .build();

    static final Map<String, MessageDigest> DIGEST_FACTORY;

    static {
        try {
            DIGEST_FACTORY = Map.of(
                    "SHA-256", MessageDigest.getInstance("SHA-256"),
                    "SHA-384", MessageDigest.getInstance("SHA-384"));

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

    static final Map<String, Object> compact(Collection<String> context, Map<String, Object> document) {
        try {
            // TODO temporary, remove with Titanium v2.x.x
            var bos = new ByteArrayOutputStream();
            try (var emitter = Jackson2Emitter.newEmitter(bos, JsonFactory.builder().build())) {
                Tree.write(document, emitter);
            }

            var ctx = Json.createArrayBuilder();
            context.forEach(ctx::add);
            var ctxx = ctx.build();

            var compacted = JsonLd.compact(JsonDocument.of(new ByteArrayInputStream(bos.toByteArray())),
                    JsonDocument.of(ctxx))
                    .loader(ContextLoader.getInstance())
                    .get();

            try (var parser = Jackson2Parser.newParser(new ByteArrayInputStream(compacted.toString().getBytes()),
                    JsonFactory.builder().build())) {
                return Tree.read(parser);
            }

        } catch (IOException | JsonLdError e) {
            throw new IllegalStateException(e);
        }
    }

    static final RdfcPrcessor newRDFC() {
        return new RdfcPrcessor(); // TODO reuse one instance across
    }

    static class RdfcPrcessor implements GraphCanonizer {

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final RdfCanon canon = RdfCanon.create(Resources.DIGEST_FACTORY.get("SHA-256"));

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

//            IO.println("C14N mapping > " + canon.mapping());

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
