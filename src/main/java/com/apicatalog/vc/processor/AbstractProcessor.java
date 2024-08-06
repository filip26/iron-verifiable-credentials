package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.method.resolver.DidUrlMethodResolver;
import com.apicatalog.vc.method.resolver.HttpMethodResolver;
import com.apicatalog.vc.method.resolver.MethodResolver;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class AbstractProcessor<T extends AbstractProcessor<T>> {

    protected final SignatureSuite[] suites;

    protected DocumentLoader defaultLoader;
    protected boolean bundledContexts;
    protected URI base;

    protected ModelVersion modelVersion;

    protected Collection<MethodResolver> methodResolvers;

    protected AbstractProcessor(final SignatureSuite... suites) {
        this.suites = suites;

        // default values
        this.defaultLoader = null;
        this.bundledContexts = true;
        this.base = null;
        this.modelVersion = null;

        this.methodResolvers = defaultResolvers();
    }

    @SuppressWarnings("unchecked")
    public T loader(DocumentLoader loader) {
        this.defaultLoader = loader;
        return (T) this;
    }

    /**
     * Use well-known contexts that are bundled with the library instead of fetching
     * it online. <code>true</code> by default. Disabling might cause slower
     * processing.
     *
     * @param enable
     * @return the processor instance
     */
    @SuppressWarnings("unchecked")
    public T useBundledContexts(boolean enable) {
        this.bundledContexts = enable;
        return (T) this;
    }

    /**
     * If set, this overrides the input document's IRI.
     *
     * @param base
     * @return the processor instance
     */
    @SuppressWarnings("unchecked")
    public T base(URI base) {
        this.base = base;
        return (T) this;
    }

    // TODO resolvers should be multilevel, per verifier, per proof type, e.g.
    // DidUrlMethodResolver could be different.
    @SuppressWarnings("unchecked")
    public T methodResolvers(Collection<MethodResolver> resolvers) {
        this.methodResolvers = resolvers;
        return (T) this;
    }

    protected static final Collection<MethodResolver> defaultResolvers() {
        Collection<MethodResolver> resolvers = new LinkedHashSet<>();
        resolvers.add(new DidUrlMethodResolver(MultibaseDecoder.getInstance(), MulticodecDecoder.getInstance(Tag.Key)));
        resolvers.add(new HttpMethodResolver());
        return resolvers;
    }

    protected DocumentLoader getLoader() {

        DocumentLoader loader = defaultLoader;

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }
        return loader;
    }

    protected JsonObject fetch(final URI location, DocumentLoader loader) throws DocumentError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return json.asJsonObject();

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected JsonArray expand(final JsonObject document, DocumentLoader loader) throws VerificationError, DocumentError {

        try {
            // load the document
            return JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base).get();

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Optional<VerificationMethod> getMethod(final Proof proof, DocumentLoader loader) throws VerificationError, DocumentError {

        final VerificationMethod method = proof.method();

        if (method == null) {
            throw new DocumentError(ErrorType.Missing, "ProofVerificationMethod");
        }

        final URI methodType = method.type();

        if (methodType != null
                && method instanceof VerificationKey
                && (((VerificationKey) method).publicKey() != null)) {
            return Optional.of(method);
        }

        return resolveMethod(method.id(), proof, loader);
    }

    protected Optional<VerificationMethod> resolveMethod(
            URI id,
            Proof proof,
            DocumentLoader loader) throws DocumentError {

        if (id == null) {
            throw new DocumentError(ErrorType.Missing, "ProofVerificationId");
        }

        // find the method id resolver
        final Optional<MethodResolver> resolver = methodResolvers.stream()
                .filter(r -> r.isAccepted(id))
                .findFirst();

        // try to resolve the method
        if (resolver.isPresent()) {
            return Optional.ofNullable(resolver.get().resolve(id, loader, proof));
        }

        throw new DocumentError(ErrorType.Unknown, "ProofVerificationId");
    }

    protected SignatureSuite findSuite(Collection<String> proofTypes, JsonObject expandedProof) {
        for (final SignatureSuite suite : suites) {
            for (final String proofType : proofTypes) {
//                if (suite.isSupported(proofType, expandedProof)) {
//                    return suite;
//                }
            }
        }
        return null;
    }
}
