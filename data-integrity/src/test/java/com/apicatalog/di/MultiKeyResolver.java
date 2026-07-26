package com.apicatalog.di;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.apicatalog.did.DidDocument;
import com.apicatalog.did.DidDocument.Relationship;
import com.apicatalog.did.primitive.MultiKey;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.VerificationMethod;
import com.apicatalog.multicodec.Multicodec;

public final class MultiKeyResolver {

    private final Map<String, Multicodec> codecs;
    private final Map<String, DidDocument.Resolver> documentResolvers;
    private final Map<String, VerificationMethod.Resolver> methodResolvers;

    public MultiKeyResolver(
            Map<String, Multicodec> codecs,
            Map<String, DidDocument.Resolver> documentResolvers,
            Map<String, VerificationMethod.Resolver> methodResolvers) {
        this.codecs = codecs;
        this.documentResolvers = documentResolvers;
        this.methodResolvers = methodResolvers;
    }

    public byte[] getPublicKey(String vm, String purpose, String algorithm, Instant timestamp) {

        var rel = Relationship.from(purpose);

        var codec = codecs.get(algorithm);

        if (codec == null) {
            throw new IllegalArgumentException();
        }

        var did = DidUrl.parse(vm);

        if (did.fragment() != null) {

            var methodResolver = methodResolvers.get(did.method());

            var method = methodResolver.resolveMethod(did, rel, Map.of()).orElseThrow();

            if (method instanceof MultiKey multikey) {

                if (!codec.isEncoded(multikey.publicKey())) {
                    throw new IllegalArgumentException();
                }

                if (multikey.revoked() != null && !timestamp.isBefore(multikey.revoked())) {
                    throw new IllegalArgumentException();
                }

                if (multikey.expires() != null && !timestamp.isBefore(multikey.expires())) {
                    throw new IllegalArgumentException();
                }

                return codec.decode(multikey.publicKey());
            }

        } else {

            var documentResolver = documentResolvers.get(did.method());

            var didDoc = documentResolver.resolve(did, Map.of());

            var methods = didDoc.document().methods(rel);

            if (methods == null || methods.isEmpty()) {
                throw new IllegalArgumentException();
            }

            for (var method : methods) {
                if (method instanceof MultiKey multikey
                        && codec.isEncoded(multikey.publicKey())
                        && (multikey.revoked() == null || timestamp.isBefore(multikey.revoked()))
                        && (multikey.expires() == null || timestamp.isBefore(multikey.expires()))) {
                    return codec.decode(multikey.publicKey());
                }
            }
        }

        throw new IllegalArgumentException();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<String, Multicodec> codecs;
        private final Map<String, DidDocument.Resolver> documentResolvers;
        private final Map<String, VerificationMethod.Resolver> methodResolvers;

        public Builder() {
            this.codecs = new HashMap<>();
            this.documentResolvers = new HashMap<>();
            this.methodResolvers = new HashMap<>();
        }

        public Builder codec(String algorithm, Multicodec codec) {
            codecs.put(algorithm, codec);
            return this;
        }

        public Builder documentResolver(String method, DidDocument.Resolver resolver) {
            documentResolvers.put(method, resolver);
            return this;
        }

        public Builder methodResolver(String method, VerificationMethod.Resolver resolver) {
            methodResolvers.put(method, resolver);
            return this;
        }

        public MultiKeyResolver build() {

            if (codecs.isEmpty() || documentResolvers.isEmpty() && methodResolvers.isEmpty()) {
                throw new IllegalStateException();
            }

            return new MultiKeyResolver(
                    Map.copyOf(codecs),
                    Map.copyOf(documentResolvers),
                    Map.copyOf(methodResolvers));
        }
    }
}
