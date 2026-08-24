package com.apicatalog.trust.lexical;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import com.apicatalog.di.DataIntegrity;
import com.apicatalog.di.proof.DataIntegrityProof;
import com.apicatalog.di.suite.CryptoSuite;
import com.apicatalog.trust.model.Model.Vocab;

public class PropertyMapProcessor {

    public static Builder newBuilder(String c14n) {
        return new Builder(c14n);
    }
    
    public static class Builder {

        final private String c14n;

        private Function<Map<String, ?>, byte[]> canonize;

        private LexicalAccessor.Factory processorFactory;
        private PropertyProofCursor.Factory cursorFactory;

        private Map<String, Function<Map<String, ?>, byte[]>> proofC14n = Map.of();
        private Map<String, CryptoSuite> cryptosuites;
        private Map<String, PropertyProofMapper> readers;

        private String proofProperty = DataIntegrity.PROPERTY_PROOF;

        private Builder(String c14n) {
            this.c14n = c14n;
            this.readers = new LinkedHashMap<>();
        }

        public Builder proofProperty(String name) {
            Objects.requireNonNull(name);
            proofProperty = name;
            return this;
        }

        public Builder c14n(Function<Map<String, ?>, byte[]> canonize) {
            this.canonize = canonize;
            return this;
        }

        public Builder c14n(String proofType, Function<Map<String, ?>, byte[]> canonize) {
            if (this.proofC14n.isEmpty()) {
                this.proofC14n = new HashMap<>();
            }
            this.proofC14n.put(proofType, canonize);
            return this;
        }

        public Builder cursor(PropertyProofCursor.Factory factory) {
            this.cursorFactory = factory;
            return this;
        }

        public Builder accessor(LexicalAccessor.Factory factory) {
            this.processorFactory = factory;
            return this;
        }

//        public LexicalModelBuilder proof(Function<String, CryptoSuite> cryptosuite) {
//            return proof(cryptosuite.apply(c14n));
//        }

        // public LexicalModelBuilder proof(Predicate<Collection<?>> context,
        // CryptoSuite cryptosuite) {
        public Builder proof(CryptoSuite cryptosuite) {
            if (!c14n.equals(cryptosuite.c14n())) {
                throw new IllegalArgumentException();
            }
            if (cryptosuites == null) {
                cryptosuites = new HashMap<>();
            }
            cryptosuites.put(cryptosuite.id(), cryptosuite);
            return this;
        }

        public PropertyMapProcessor build() {

            return null;
//            if (canonize == null) {
//                throw new IllegalStateException();
//            }
//
//            if (cryptosuites != null && !cryptosuites.isEmpty()) {
//                readers.put(
//                        DataIntegrityProof.TYPE_NAME,
//                        new DataIntegrityProof.PropertyMapMapper(
//                                cryptosuites,
//                                proofC14n.getOrDefault(DataIntegrityProof.TYPE_NAME, canonize)));
//            }
//
////            if (readers.isEmpty()) {
////                throw new IllegalStateException();
////            }
//
//            return new LexicalModel(
//                    new Vocab("@context", proofProperty, "id", "type"),
//                    processorFactory,
//                    cursorFactory,
//                    canonize,
//                    readers);
        }
    }
}
