package com.apicatalog.di;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.trust.model.GraphModel;
import com.apicatalog.trust.model.GraphModel.C14nFactory;
import com.apicatalog.trust.model.GraphModel.QuadConsumer;
import com.apicatalog.trust.model.Model;
import com.apicatalog.trust.model.TypeSpecificModel;
import com.apicatalog.trust.proof.GraphProofCursor;
import com.apicatalog.trust.proof.GraphProofReader;
import com.apicatalog.trust.proof.MapProofCursor;
import com.apicatalog.trust.proof.MapProofReader;

public class DataIntegrity {

    public static GraphModelBuilder newGraphModelBuilder(String c14n, C14nFactory c14nFactory) {
        return new GraphModelBuilder(c14n, c14nFactory);
    }

    public static TypeModelBuilder newTypeModelBuilder(String c14n) {
        return new TypeModelBuilder(c14n);
    }

    public static class GraphModelBuilder {

        private final String c14n;
        private final C14nFactory c14nFactory;

        private GraphProofCursor.Factory factory;

        private BiConsumer<Map<String, Object>, QuadConsumer> tordf;

        Map<String, CryptoSuite> cryptosuites;
        Map<String, GraphProofReader> readers;

        private GraphModelBuilder(String c14n, C14nFactory c14nFactory) {
            this.c14n = c14n;
            this.c14nFactory = c14nFactory;
            this.readers = new LinkedHashMap<>();
        }

        public GraphModelBuilder tordf(BiConsumer<Map<String, Object>, QuadConsumer> tordf) {
            this.tordf = tordf;
            return this;
        }

        public GraphModelBuilder processor(GraphProofCursor.Factory factory) {
            this.factory = factory;
            return this;
        }

        public GraphModelBuilder proof(CryptoSuite cryptosuite) {
            if (!c14n.equals(cryptosuite.c14n())) {
                throw new IllegalArgumentException();
            }
            if (cryptosuites == null) {
                cryptosuites = new HashMap<>();
            }
            cryptosuites.put(cryptosuite.id(), cryptosuite);
            return this;
        }

        public GraphModelBuilder proof(String proofType, GraphProofReader reader) {
            readers.put(proofType, reader);
            return this;
        }

        public GraphModelBuilder Ed25519Signature2020() {
//            proof(Ed25519Signature2020.TYPE_URI, Ed25519Signature2020::newReader);
            return this;
        }

        public Model build() {

            if (cryptosuites != null && !cryptosuites.isEmpty()) {
                readers.put(
                        DataIntegrityProof.TYPE.uri(),
                        new DataIntegrityProof.GraphReader(cryptosuites));
            }

            if (readers.isEmpty()) {
                throw new IllegalStateException();
            }

            return new GraphModel(factory, c14n, tordf, c14nFactory, readers);
        }
    }

    public static class TypeModelBuilder {

        final String c14n;

        MapProofCursor.Factory factory;

        Function<Map<String, Object>, byte[]> canonize;

        Collection<MapProofReader> readers;

        private TypeModelBuilder(String c14n) {
            this.c14n = c14n;
        }

        public TypeModelBuilder c14n(Function<Map<String, Object>, byte[]> canonize) {
            this.canonize = canonize;
            return this;
        }

        public TypeModelBuilder processor(MapProofCursor.Factory factory) {
            this.factory = factory;
            return this;
        }

        public TypeModelBuilder proof(CryptoSuite cryptosuite) {
            if (!c14n.equals(cryptosuite.c14n())) {
                throw new IllegalArgumentException();
            }
            proof(new DataIntegrityProof.MapReader(cryptosuite));
            return this;
        }

        public TypeModelBuilder proof(MapProofReader reader) {
            if (readers == null) {
                readers = new ArrayList<MapProofReader>();
            }
            readers.add(reader);
            return this;
        }

        public Model build() {
            return new TypeSpecificModel(factory, c14n, canonize, readers);
        }
    }

}
