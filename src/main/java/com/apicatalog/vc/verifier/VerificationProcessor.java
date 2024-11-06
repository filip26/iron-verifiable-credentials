package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jwk.JsonWebKey;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multikey.Multikey;
import com.apicatalog.vc.method.resolver.DidKeyMethodResolver;
import com.apicatalog.vc.method.resolver.HttpMethodResolver;
import com.apicatalog.vc.method.resolver.DeprecatedVerificationMethodResolver;
import com.apicatalog.vc.processor.DocumentProcessor;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

public class VerificationProcessor<T extends VerificationProcessor<T>> extends DocumentProcessor<T> {

    protected Collection<DeprecatedVerificationMethodResolver> methodResolvers;

    protected VerificationProcessor(final SignatureSuite... suites) {
        super(suites);
        this.methodResolvers = null;
    }

    // TODO resolvers should be multilevel, per verifier, per proof type, e.g.
    // DidUrlMethodResolver could be different.
    @SuppressWarnings("unchecked")
    public T methodResolvers(Collection<DeprecatedVerificationMethodResolver> resolvers) {
        this.methodResolvers = resolvers;
        return (T) this;
    }

    protected static final Collection<DeprecatedVerificationMethodResolver> defaultResolvers(DocumentLoader loader) {
        Collection<DeprecatedVerificationMethodResolver> resolvers = new LinkedHashSet<>();
        resolvers.add(new DidKeyMethodResolver(MulticodecDecoder.getInstance(Tag.Key)));
        resolvers.add(HttpMethodResolver.getInstance(loader, Multikey.class, JsonWebKey.class));
        return resolvers;
    }

    protected Optional<VerificationMethod> getMethod(final Proof proof) throws VerificationError, DocumentError {

        final VerificationMethod method = proof.method();

        if (method == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethod");
        }

        final String methodType = method.type();

        if (methodType != null
                && method instanceof VerificationKey
                && (((VerificationKey) method).publicKey() != null)) {
            //TODO accept method
            return Optional.of(method);
        }

        return resolveMethod(method.id(), proof);
    }

    protected Optional<VerificationMethod> resolveMethod(
            URI id,
            Proof proof) throws DocumentError {

        if (id == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethodId");
        }

        // find the method id resolver
        final Optional<DeprecatedVerificationMethodResolver> resolver = getMethodResolvers().stream()
                .filter(r -> r.isAccepted(id))
                .findFirst();

        // try to resolve the method
        if (resolver.isPresent()) {
            return Optional.ofNullable(resolver.get().resolve(id, proof));
        }

        throw new DocumentError(ErrorType.Unknown, "VerificationMethod");
    }

    protected Collection<DeprecatedVerificationMethodResolver> getMethodResolvers() {
        if (methodResolvers == null) {
            return defaultResolvers(getLoader());
        }
        return methodResolvers;
    }
}
