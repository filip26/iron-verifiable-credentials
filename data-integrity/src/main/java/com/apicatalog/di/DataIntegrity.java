package com.apicatalog.di;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import com.apicatalog.trust.semantic.Graph;
import com.apicatalog.trust.semantic.Graph.NodeMapper;
import com.apicatalog.trust.semantic.Graph.TypeMapping;
import com.apicatalog.trust.semantic.GraphProcessor;
import com.apicatalog.trust.semantic.GraphProofMapper;
import com.apicatalog.trust.semantic.SemanticModel;
import com.apicatalog.trust.semantic.SemanticModel.GraphCanonizer;

public class DataIntegrity {

    public static final String PREDICATE_PROOF = "https://w3id.org/security#proof";
    public static final String PROPERTY_PROOF = "proof";

    public static ModelBuilder newModelBuilder() {
        return new ModelBuilder();
    }

    @Deprecated
    public static LexicalModelBuilder newLexicalModel(String c14n) {
        return new LexicalModelBuilder(c14n);
    }

    public static class ModelBuilder {

//        private final String c14n;

//        private Supplier<GraphCanonizer> c14nFactory;

        private String proofPredicate = DataIntegrity.PREDICATE_PROOF;

        private GraphProcessor processor;
//        private SemanticModel.Accessor.Factory accessorFactory;
//        private GraphUpdater.Factory updaterFactory;
//        private GraphProofCursor.Factory cursorFactory;
//        private GraphPayloadGenerator.Factory payloadFactory;

//        private BiConsumer<Object, QuadConsumer> tordf;
//        private BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact;
//        private Function<Map<String, ?>, SequencedCollection<?>> expand;

        private Map<String, Supplier<GraphCanonizer>> proofC14n = Map.of();

        private Map<String, CryptoSuite> cryptosuites;

        @Deprecated
        private Function<Collection<String>, NodeMapper<?>> documentMapper;

        private Collection<TypeMapping> typeMapping;

        private Map<String, PropertyProofMapper> mapProofMappers;
        private Map<String, GraphProofMapper> graphProofMappers;

        private boolean ed25519Signature2020 = false;


        private String proofProperty = DataIntegrity.PROPERTY_PROOF;

        private ModelBuilder() {
//            this.c14n = c14n;
            // default processors
//            this.accessorFactory = GraphAccessor::newInstance;
//            this.updaterFactory = GraphUpdater::new;
//            this.cursorFactory = GraphProofCursor::newInstance;
//            this.payloadFactory = GraphPayloadGenerator::new;
        }

        public ModelBuilder proofPredicate(String uri) {
            this.proofPredicate = uri;
            return this;
        }
        
        public ModelBuilder proofProperty(String name) {
            this.proofProperty = name;
            return this;
        }

//        @Deprecated
//        public SemanticModelBuilder c14n(Supplier<GraphCanonizer> c14nFactory) {
//            this.c14nFactory = c14nFactory;
//            return this;
//        }
//
//        @Deprecated
//        public SemanticModelBuilder c14n(String proofType, Supplier<GraphCanonizer> c14nFactory) {
//            if (this.proofC14n.isEmpty()) {
//                this.proofC14n = new HashMap<>();
//            }
//            this.proofC14n.put(proofType, c14nFactory);
//            return this;
//        }
//
//        @Deprecated
//        public SemanticModelBuilder expand(Function<Map<String, ?>, SequencedCollection<?>> expand) {
//            this.expand = expand;
//            return this;
//        }
//
//        @Deprecated
//        public SemanticModelBuilder compact(
//                BiFunction<Collection<?>, Map<String, ?>, Map<String, ?>> compact) {
//            this.compact = compact;
//            return this;
//        }
//
//        @Deprecated
//        public SemanticModelBuilder tordf(BiConsumer<Object, QuadConsumer> tordf) {
//            this.tordf = tordf;
//            return this;
//        }
//
//        @Deprecated
//        public SemanticModelBuilder cursor(GraphProofCursor.Factory factory) {
//            this.cursorFactory = factory;
//            return this;
//        }
//
//        @Deprecated
//        public SemanticModelBuilder accessor(SemanticModel.Accessor.Factory factory) {
//            this.accessorFactory = factory;
//            return this;
//        }
//
//        @Deprecated
//        public SemanticModelBuilder updater(GraphUpdater.Factory factory) {
//            this.updaterFactory = factory;
//            return this;
//        }
//
//        @Deprecated
//        public SemanticModelBuilder payload(GraphPayloadGenerator.Factory factory) {
//            this.payloadFactory = factory;
//            return this;
//        }
//        

                @Deprecated
        public ModelBuilder document(Function<Collection<String>, NodeMapper<?>> mapper) {
            this.documentMapper = mapper;
            return this;
        }

       
        public ModelBuilder document(String type, NodeMapper<?> mapper) {
            if (this.typeMapping == null) {
                typeMapping = new ArrayList<>();
            }
            typeMapping.add(new TypeMapping(new String[] { type }, mapper));
            return this;
        }

        @Deprecated
        public ModelBuilder document(Set<String> types, NodeMapper<?> mapper) {
            if (this.typeMapping == null) {
                typeMapping = new ArrayList<>();
            }
            typeMapping.add(new TypeMapping(types.toArray(String[]::new), mapper));
            return this;
        }

        public ModelBuilder cryptosuite(CryptoSuite cryptosuite) {
//            if (!c14n.equals(cryptosuite.c14n())) {
//                throw new IllegalArgumentException();
//            }
            if (cryptosuites == null) {
                cryptosuites = new HashMap<>();
            }
            cryptosuites.put(cryptosuite.id(), cryptosuite);
            return this;
        }

        public ModelBuilder proof(String proofType, GraphProofMapper reader) {
            if (graphProofMappers == null) {
                graphProofMappers = new LinkedHashMap<>();
            }
            graphProofMappers.put(proofType, reader);
            return this;
        }

        // legacy support
        public ModelBuilder Ed25519Signature2020() {
            this.ed25519Signature2020 = true;
            return this;
        }

        public ModelBuilder processor(GraphProcessor processor) {
            Objects.requireNonNull(processor);
            this.processor = processor;
            return this;
        }

        public SemanticModel build() {

            if (processor == null) {
                throw new IllegalStateException();
            }

            if (cryptosuites != null && !cryptosuites.isEmpty()) {
                graphProofMappers.put(
                        DataIntegrityProof.TYPE_URI,
                        new DataIntegrityProof.GraphMapper(
                                cryptosuites,
                                proofC14n.getOrDefault(DataIntegrityProof.TYPE_URI, processor::newCanonizer)));
            }

            if (ed25519Signature2020) {
                graphProofMappers.put(
                        Ed25519Signature2020.TYPE_URI,
                        new Ed25519Signature2020.GraphMapper(
                                proofC14n.getOrDefault(Ed25519Signature2020.TYPE_URI, processor::newCanonizer)));
            }

//            if (readers.isEmpty()) {
//                throw new IllegalStateException();
//            }

            // FIXME
//            var processor = new GraphProcessor(
//                    accessorFactory,
//                    updaterFactory,
//                    cursorFactory,
//                    payloadFactory,
//                    expand,
//                    compact,
//                    tordf,
//                    c14nFactory
//                    );

            return new SemanticModel(
                    new Vocab(
                            "@context",
                            proofPredicate,
                            null,
                            Graph.PREDICATE_TYPE),
                    processor,
                    typeMapping != null && !typeMapping.isEmpty()
                            ? new Graph.TypeMappingMatcher(typeMapping)
                            : null,
                    documentMapper,
                    graphProofMappers);
        }
    }

    @Deprecated
    public static class LexicalModelBuilder {

        final private String c14n;

        private Function<Map<String, ?>, byte[]> canonize;

        private LexicalAccessor.Factory processorFactory;
        private PropertyProofCursor.Factory cursorFactory;

        private Map<String, Function<Map<String, ?>, byte[]>> proofC14n = Map.of();
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

        public LexicalModelBuilder c14n(Function<Map<String, ?>, byte[]> canonize) {
            this.canonize = canonize;
            return this;
        }

        public LexicalModelBuilder c14n(String proofType, Function<Map<String, ?>, byte[]> canonize) {
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
        public LexicalModelBuilder cryptosuite(CryptoSuite cryptosuite) {
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
