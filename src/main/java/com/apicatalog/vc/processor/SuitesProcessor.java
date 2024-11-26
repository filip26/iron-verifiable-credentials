package com.apicatalog.vc.processor;

import java.util.function.Function;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.method.resolver.VerificationKeyProvider;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.ProofAdapterProvider;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vc.model.VerifiableReaderProvider;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonObject;

public class SuitesProcessor<T extends SuitesProcessor<T>> extends DocumentProcessor<T> {

    protected VerifiableReaderProvider readerProvider;
    protected VerificationKeyProvider keyProvider;

    protected final ProofAdapter proofAdapter;

    protected SuitesProcessor(final SignatureSuite... suites) {
        super(suites);
        this.proofAdapter = ProofAdapterProvider.of(suites);
        this.keyProvider = null;
        this.readerProvider = null;
    }

    @SuppressWarnings("unchecked")
    public T methodResolver(VerificationKeyProvider keyProvider) {
        this.keyProvider = keyProvider;
        return (T) this;
    }
    
    @SuppressWarnings("unchecked")
    public T model(Function<ProofAdapter, VerifiableReaderProvider> provider) {
        this.readerProvider = provider.apply(proofAdapter);
        return (T)this;
    }

    protected VerifiableDocument read(final JsonObject document, DocumentLoader loader) throws DocumentError {
        final VerifiableReader reader = readerProvider.reader(document);

        if (reader != null) {
            final VerifiableModel model = reader.read(document, loader, base);

            if (model != null) {
                return reader.materialize(model, loader, base);
            }
        }
        throw new DocumentError(ErrorType.Unknown, "Model");
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
