package com.apicatalog.di;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.trust.lexical.LexicalModel;
import com.apicatalog.trust.lexical.MapAdapter;
import com.apicatalog.trust.lexical.MapProofCursor;
import com.apicatalog.trust.lexical.MapProofReader;
import com.apicatalog.trust.model.ProcessingModel.Vocab;
import com.apicatalog.trust.semantic.GraphAdapter;
import com.apicatalog.trust.semantic.GraphPayloadGenerator;
import com.apicatalog.trust.semantic.GraphProofCursor;
import com.apicatalog.trust.semantic.GraphProofReader;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;

public class DataIntegrity {

    public static final String VOCAB_PROOF_URI = "https://w3id.org/security#proof";
    public static final String VOCAB_PROOF_KEY = "proof";

    public static SemanticModelBuilder createSematicModel(String c14n) {
        return new SemanticModelBuilder(c14n);
    }

    public static LexicalModelBuilder createLexicalModel(String c14n) {
        return new LexicalModelBuilder(c14n);
    }

    public static class SemanticModelBuilder {

        private final String c14n;

        private Supplier<GraphCanonizer> c14nFactory;

        private String proofPredicate = DataIntegrity.VOCAB_PROOF_URI;

        private GraphAdapter.Factory processorFactory;
        private GraphProofCursor.Factory cursorFactory;
        private GraphPayloadGenerator.Factory payloadFactory;

        private BiConsumer<Object, QuadConsumer> tordf;
        private BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact;
        private Function<Map<String, Object>, Collection<Object>> expand;

        private Map<String, CryptoSuite> cryptosuites;
        private Map<String, GraphProofReader> readers;

        private SemanticModelBuilder(String c14n) {
            this.c14n = c14n;
            this.readers = new LinkedHashMap<>();
        }

        public SemanticModelBuilder proofPredicate(String uri) {
            this.proofPredicate = uri;
            return this;
        }

        public SemanticModelBuilder c14n(Supplier<GraphCanonizer> c14nFactory) {
            this.c14nFactory = c14nFactory;
            return this;
        }

        public SemanticModelBuilder expand(Function<Map<String, Object>, Collection<Object>> expand) {
            this.expand = expand;
            return this;
        }

        public SemanticModelBuilder compact(
                BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact) {
            this.compact = compact;
            return this;
        }

        public SemanticModelBuilder tordf(BiConsumer<Object, QuadConsumer> tordf) {
            this.tordf = tordf;
            return this;
        }

        public SemanticModelBuilder cursor(GraphProofCursor.Factory factory) {
            this.cursorFactory = factory;
            return this;
        }

        public SemanticModelBuilder processor(GraphAdapter.Factory factory) {
            this.processorFactory = factory;
            return this;
        }

        public SemanticModelBuilder payload(GraphPayloadGenerator.Factory factory) {
            this.payloadFactory = factory;
            return this;
        }

        public SemanticModelBuilder proof(Function<String, CryptoSuite> cryptosuite) {
            return proof(cryptosuite.apply(c14n));
        }

        public SemanticModelBuilder proof(CryptoSuite cryptosuite) {
            if (!c14n.equals(cryptosuite.c14n())) {
                throw new IllegalArgumentException();
            }
            if (cryptosuites == null) {
                cryptosuites = new HashMap<>();
            }
            cryptosuites.put(cryptosuite.id(), cryptosuite);
            return this;
        }

        public SemanticModelBuilder proof(String proofType, GraphProofReader reader) {
            readers.put(proofType, reader);
            return this;
        }

        // legacy support
        public SemanticModelBuilder Ed25519Signature2020() {
            proof(Ed25519Signature2020.TYPE_URI, Ed25519Signature2020.newReader());
            return this;
        }

        public SemanticModel build() {

            if (cryptosuites != null && !cryptosuites.isEmpty()) {
                readers.put(
                        DataIntegrityProof.TYPE_URI,
                        new DataIntegrityProof.GraphReader(cryptosuites));
            }

//            if (readers.isEmpty()) {
//                throw new IllegalStateException();
//            }

            if (c14nFactory == null) {
                throw new IllegalStateException();
            }

            return new SemanticModel(
                    new Vocab("@context", proofPredicate, null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    processorFactory,
                    cursorFactory,
                    payloadFactory,
                    c14n,
                    expand,
                    compact,
                    tordf,
                    c14nFactory,
                    readers);
        }
    }

    public static class LexicalModelBuilder {

        final private String c14n;

        private Function<Map<String, Object>, byte[]> canonize;

        private MapAdapter.Factory processorFactory;
        private MapProofCursor.Factory cursorFactory;

        private Map<String, CryptoSuite> cryptosuites;
        private Map<String, MapProofReader> readers;

        private String proofProperty = DataIntegrity.VOCAB_PROOF_KEY;

        private LexicalModelBuilder(String c14n) {
            this.c14n = c14n;
            this.readers = new LinkedHashMap<>();
        }

        public LexicalModelBuilder proofProperty(String name) {
            Objects.requireNonNull(name);
            proofProperty = name;
            return this;
        }

        public LexicalModelBuilder c14n(Function<Map<String, Object>, byte[]> canonize) {
            this.canonize = canonize;
            return this;
        }

        public LexicalModelBuilder cursor(MapProofCursor.Factory factory) {
            this.cursorFactory = factory;
            return this;
        }

        public LexicalModelBuilder processor(MapAdapter.Factory factory) {
            this.processorFactory = factory;
            return this;
        }

        public LexicalModelBuilder proof(Function<String, CryptoSuite> cryptosuite) {
            return proof(cryptosuite.apply(c14n));
        }

        public LexicalModelBuilder proof(CryptoSuite cryptosuite) {
            if (!c14n.equals(cryptosuite.c14n())) {
                throw new IllegalArgumentException();
            }
            if (cryptosuites == null) {
                cryptosuites = new HashMap<>();
            }
            cryptosuites.put(cryptosuite.id(), cryptosuite);
            return this;
        }

        public LexicalModel build() {
            if (cryptosuites != null && !cryptosuites.isEmpty()) {
                readers.put(
                        DataIntegrityProof.TYPE_NAME,
                        new DataIntegrityProof.MapReader(cryptosuites));
            }

//            if (readers.isEmpty()) {
//                throw new IllegalStateException();
//            }

            if (canonize == null) {
                throw new IllegalStateException();
            }

            return new LexicalModel(
                    new Vocab("@context", proofProperty, "id", "type"),
                    processorFactory,
                    cursorFactory,
                    c14n,
                    canonize,
                    readers);
        }
    }

}
