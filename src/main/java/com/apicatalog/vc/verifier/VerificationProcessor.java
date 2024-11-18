package com.apicatalog.vc.verifier;

import com.apicatalog.vc.method.resolver.VerificationKeyProvider;
import com.apicatalog.vc.processor.DocumentProcessor;
import com.apicatalog.vc.suite.SignatureSuite;

public class VerificationProcessor<T extends VerificationProcessor<T>> extends DocumentProcessor<T> {

    protected VerificationKeyProvider keyProvider;

    // TODO proof selector -> provider ! no, otherwise, only one provider!

    protected VerificationProcessor(final SignatureSuite... suites) {
        super(suites);
        this.keyProvider = null;
    }

    // TODO resolvers should be multilevel, per verifier, per proof type, e.g.
    // DidUrlMethodResolver could be different.
    @SuppressWarnings("unchecked")
    public T methodResolver(VerificationKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
        return (T) this;
    }

//    protected static final Collection<VerificationKeyProvider> defaultResolvers(DocumentLoader loader) {
//        Collection<VerificationKeyProvider> resolvers = new LinkedHashSet<>();
////        resolvers.add(new DidKeyMethodResolver(MulticodecDecoder.getInstance(Tag.Key)));
////        resolvers.add(HttpMethodResolver.getInstance(loader, Multikey.class, JsonWebKey.class));
//        return resolvers;
//    }
//
//    protected Optional<VerificationKey> resolveMethod(Proof proof) throws DocumentError {
//
//
//        // find the method id resolver
//        final Optional<VerificationKeyProvider> resolver = getMethodResolvers().stream()
////                .filter(r -> r.isAccepted(id))
//                .findFirst();
//
//        // try to resolve the method
//        if (resolver.isPresent()) {
//            return Optional.ofNullable(resolver.get().verificationKey(proof));
//        }
//
//        throw new DocumentError(ErrorType.Unknown, "VerificationMethod");
//    }

//    protected Collection<VerificationKeyProvider> getMethodResolvers() {
//        if (keyProvider == null) {
//            return defaultResolvers(getLoader());
//        }
//        return keyProvider;
//    }
}
