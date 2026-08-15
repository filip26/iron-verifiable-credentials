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
import com.apicatalog.trust.lexical.LexicalAccessor;
import com.apicatalog.trust.lexical.LexicalModel;
import com.apicatalog.trust.lexical.PropertyProofCursor;
import com.apicatalog.trust.lexical.PropertyProofMapper;
import com.apicatalog.trust.model.Model.Vocab;
import com.apicatalog.trust.semantic.GraphPayloadGenerator;
import com.apicatalog.trust.semantic.GraphProofCursor;
import com.apicatalog.trust.semantic.GraphProofMapper;
import com.apicatalog.trust.semantic.SemanticAccessor;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;
import com.apicatalog.trust.semantic.SemanticModel.JsonLdOps;
import com.apicatalog.trust.semantic.SemanticModel.Primitives;
import com.apicatalog.trust.semantic.SemanticModel.QuadConsumer;
import com.apicatalog.trust.semantic.SemanticUpdater;

public class DataIntegrity {

    public static final String PREDICATE_PROOF = "https://w3id.org/security#proof";
    public static final String PROPERTY_PROOF = "proof";

    public static SemanticModelBuilder createSematicModel(String c14n) {
        return new SemanticModelBuilder(c14n);
    }

    public static LexicalModelBuilder createLexicalModel(String c14n) {
        return new LexicalModelBuilder(c14n);
    }

    public static class SemanticModelBuilder {

        private final String c14n;

        private Supplier<GraphCanonizer> c14nFactory;

        private String proofPredicate = DataIntegrity.PREDICATE_PROOF;

        private SemanticAccessor.Factory adapterFactory;
        private SemanticUpdater.Factory updaterFactory;
        private GraphProofCursor.Factory cursorFactory;
        private GraphPayloadGenerator.Factory payloadFactory;

        private BiConsumer<Object, QuadConsumer> tordf;
        private BiFunction<Collection<String>, Map<String, Object>, Map<String, Object>> compact;
        private Function<Map<String, Object>, Collection<Object>> expand;

        private Map<String, Supplier<GraphCanonizer>> proofC14n = Map.of();

        private Map<String, CryptoSuite> cryptosuites;

        private Map<String, GraphProofMapper> readers;

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

        public SemanticModelBuilder c14n(String proofType, Supplier<GraphCanonizer> c14nFactory) {
            if (this.proofC14n.isEmpty()) {
                this.proofC14n = new HashMap<>();
            }
            this.proofC14n.put(proofType, c14nFactory);
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

        public SemanticModelBuilder accessor(SemanticAccessor.Factory factory) {
            this.adapterFactory = factory;
            return this;
        }

        public SemanticModelBuilder updater(SemanticUpdater.Factory factory) {
            this.updaterFactory = factory;
            return this;
        }

        public SemanticModelBuilder payload(GraphPayloadGenerator.Factory factory) {
            this.payloadFactory = factory;
            return this;
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

        public SemanticModelBuilder proof(String proofType, GraphProofMapper reader) {
            readers.put(proofType, reader);
            return this;
        }

        // legacy support
        public SemanticModelBuilder Ed25519Signature2020() {
            proof(Ed25519Signature2020.TYPE_URI, Ed25519Signature2020.newReader());
            return this;
        }

        public SemanticModel build() {

            if (c14nFactory == null) {
                throw new IllegalStateException();
            }

            if (cryptosuites != null && !cryptosuites.isEmpty()) {
                readers.put(
                        DataIntegrityProof.TYPE_URI,
                        new DataIntegrityProof.GraphMapper(
                                cryptosuites,
                                proofC14n.getOrDefault(DataIntegrityProof.TYPE_URI, c14nFactory)));
            }

//            if (readers.isEmpty()) {
//                throw new IllegalStateException();
//            }

            return new SemanticModel(
                    new Vocab(
                            "@context",
                            proofPredicate,
                            null,
                            "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"),
                    new Primitives(
                            adapterFactory,
                            updaterFactory,
                            cursorFactory,
                            payloadFactory),
                    new JsonLdOps(
                            expand,
                            compact,
                            tordf),
                    c14nFactory,
                    readers);
        }
    }

    public static class LexicalModelBuilder {

        final private String c14n;

        private Function<Map<String, Object>, byte[]> canonize;

        private LexicalAccessor.Factory processorFactory;
        private PropertyProofCursor.Factory cursorFactory;

        private Map<String, Function<Map<String, Object>, byte[]>> proofC14n = Map.of();
        private Map<String, CryptoSuite> cryptosuites;
        private Map<String, PropertyProofMapper> readers;

        private String proofProperty = DataIntegrity.PROPERTY_PROOF;

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
        
        public LexicalModelBuilder c14n(String proofType, Function<Map<String, Object>, byte[]> canonize) {
            if (this.proofC14n.isEmpty()) {
                this.proofC14n = new HashMap<>();
            }
            this.proofC14n.put(proofType, canonize);
            return this;
        }


        public LexicalModelBuilder cursor(PropertyProofCursor.Factory factory) {
            this.cursorFactory = factory;
            return this;
        }

        public LexicalModelBuilder accessor(LexicalAccessor.Factory factory) {
            this.processorFactory = factory;
            return this;
        }

//        public LexicalModelBuilder proof(Function<String, CryptoSuite> cryptosuite) {
//            return proof(cryptosuite.apply(c14n));
//        }

        // public LexicalModelBuilder proof(Predicate<Collection<?>> context,
        // CryptoSuite cryptosuite) {
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
            
            if (canonize == null) {
                throw new IllegalStateException();
            }

            if (cryptosuites != null && !cryptosuites.isEmpty()) {                
                readers.put(
                        DataIntegrityProof.TYPE_NAME,
                        new DataIntegrityProof.PropertyMapMapper(
                                cryptosuites,
                                proofC14n.getOrDefault(DataIntegrityProof.TYPE_NAME, canonize)));
            }

//            if (readers.isEmpty()) {
//                throw new IllegalStateException();
//            }

            return new LexicalModel(
                    new Vocab("@context", proofProperty, "id", "type"),
                    processorFactory,
                    cursorFactory,
                    canonize,
                    readers);
        }
    }

}
