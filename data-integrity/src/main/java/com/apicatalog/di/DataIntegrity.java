package com.apicatalog.di;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.proof.Ed25519Signature2020;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.trust.model.DataModel;
import com.apicatalog.trust.model.LexicalModel;
import com.apicatalog.trust.model.SemanticModel;
import com.apicatalog.trust.model.SemanticModel.C14nFactory;
import com.apicatalog.trust.model.SemanticModel.QuadConsumer;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.apicatalog.trust.proof.GraphProofReader;
import com.apicatalog.trust.proof.MapProofCursor;
import com.apicatalog.trust.proof.MapProofReader;

public class DataIntegrity {

    public static SemanticModelBuilder newSematicModelBuilder(String c14n) {
        return new SemanticModelBuilder(c14n);
    }

    public static LexicalModelBuilder newLexicalModelBuilder(String c14n) {
        return new LexicalModelBuilder(c14n);
    }

    public static class SemanticModelBuilder {

        private final String c14n;

        private C14nFactory c14nFactory;

        private GraphProofCursor.Factory factory;

        private BiConsumer<Map<String, Object>, QuadConsumer> tordf;

        private Map<String, CryptoSuite> cryptosuites;
        private Map<String, GraphProofReader> readers;

        private SemanticModelBuilder(String c14n) {
            this.c14n = c14n;
            this.readers = new LinkedHashMap<>();
        }

        public SemanticModelBuilder c14n(C14nFactory c14nFactory) {
            this.c14nFactory = c14nFactory;
            return this;
        }

        public SemanticModelBuilder tordf(BiConsumer<Map<String, Object>, QuadConsumer> tordf) {
            this.tordf = tordf;
            return this;
        }

        public SemanticModelBuilder processor(GraphProofCursor.Factory factory) {
            this.factory = factory;
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

        public DataModel build() {

            if (cryptosuites != null && !cryptosuites.isEmpty()) {
                readers.put(
                        DataIntegrityProof.TYPE_URI,
                        new DataIntegrityProof.GraphReader(cryptosuites));
            }

            if (readers.isEmpty()) {
                throw new IllegalStateException();
            }

            if (c14nFactory == null) {
                throw new IllegalStateException();
            }

            return new SemanticModel(factory, c14n, tordf, c14nFactory, readers);
        }
    }

    public static class LexicalModelBuilder {

        final private String c14n;

        private Function<Map<String, Object>, byte[]> canonize;

        private MapProofCursor.Factory factory;

        private Map<String, CryptoSuite> cryptosuites;
        private Map<String, MapProofReader> readers;

        private LexicalModelBuilder(String c14n) {
            this.c14n = c14n;
            this.readers = new LinkedHashMap<>();
        }

        public LexicalModelBuilder c14n(Function<Map<String, Object>, byte[]> canonize) {
            this.canonize = canonize;
            return this;
        }

        public LexicalModelBuilder processor(MapProofCursor.Factory factory) {
            this.factory = factory;
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

        public DataModel build() {
            if (cryptosuites != null && !cryptosuites.isEmpty()) {
                readers.put(
                        DataIntegrityProof.TYPE_NAME,
                        new DataIntegrityProof.MapReader(cryptosuites));
            }

            if (readers.isEmpty()) {
                throw new IllegalStateException();
            }

            if (canonize == null) {
                throw new IllegalStateException();
            }

            return new LexicalModel(factory, c14n, canonize, readers);
        }
    }

}
