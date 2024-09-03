package com.apicatalog.vc.jsonld;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdType;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public class JsonLdVerifiableReader2  {

    protected final SignatureSuite[] suites;
    
    protected DocumentLoader defaultLoader;
    protected boolean bundledContexts;
    protected URI base;

    protected VcdmVersion modelVersion;

    protected JsonLdVerifiableReader2(final SignatureSuite... suites) {
        this.suites = suites;

        // default values
        this.defaultLoader = null;
        this.bundledContexts = true;
        this.base = null;
        this.modelVersion = null;

//        this.methodResolvers = defaultResolvers();
    }

    
    /**
     * Reads VC/VP document. 
     * 
     * @param document
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    public Verifiable read(final JsonObject document) throws DocumentError {
        Objects.requireNonNull(document);
        return read(document, getLoader());
    }

    /**
     * Reads VC/VP document. 
     * 
     * @param location
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    public Verifiable read(final URI location) throws DocumentError {
        Objects.requireNonNull(location);
        return read(location, getLoader());
    }

    protected Verifiable read(final URI location, DocumentLoader loader) throws DocumentError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return read(json.asJsonObject(), loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable read(final JsonObject document, DocumentLoader loader) throws DocumentError {

        try {

            // extract context
            final JsonStructure context = document.containsKey(Keywords.CONTEXT)
                    ? JsonUtils.toJsonArray(document.get(Keywords.CONTEXT))
                    : null;

            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base).get();

            return verifyExpanded(
//                    JsonLdVerifiable.getVersion(document),
                    null,
                    context,
                    expanded,
                    null,
                    null
// FIXME                   params, 
                    );

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private Verifiable verifyExpanded(final VcdmVersion version, JsonStructure context, JsonArray expanded, Map<String, Object> params, DocumentLoader loader)
            throws DocumentError {

        if (expanded == null || expanded.isEmpty() || expanded.size() > 1) {
            throw new DocumentError(ErrorType.Invalid);
        }

        final JsonValue verifiable = expanded.iterator().next();

        if (JsonUtils.isNotObject(verifiable)) {
            throw new DocumentError(ErrorType.Invalid);
        }

        return verifyExpanded(version, context, verifiable.asJsonObject(), loader);
    }

    private Verifiable verifyExpanded(final VcdmVersion version, JsonStructure context, final JsonObject expanded, DocumentLoader loader)
            throws DocumentError {
//
//        // get a verifiable representation
//        final JsonLdVerifiable verifiable = null; //FIXMe reader.read(version, expanded);
//
//        if (verifiable.isCredential()) {
//
//            // data integrity and metadata validation
////TODO            validate(verifiable.asCredential());
//
////FIXME            verifiable.proofs(read(context, expanded, loader));
//
//            return verifiable;
//
//        } else if (verifiable.isPresentation()) {
//
//            // verify presentation proofs
//            verifiable.proofs(readProofs(context, expanded, loader));
//
//            final Collection<Credential> credentials = new ArrayList<>();
//
////            for (final JsonObject presentedCredentials : VerifiableReader.getCredentials(expanded)) {
////
////                if (!VerifiableReader.isCredential(presentedCredentials)) {
////                    throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS, Term.TYPE);
////                }
//////var params = new HashMap<>();
//////FIXME                credentials.add(verifyExpanded(version, context, presentedCredentials, params, loader).asCredential());
////            }
//
//            ((JsonLdPresentation)verifiable.asPresentation()).credentials(credentials);
//
//            return verifiable;
//        }
        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    protected Collection<Proof> readProofs(JsonStructure context, JsonObject expanded, DocumentLoader loader) throws DocumentError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonObject> expandedProofs = EmbeddedProof.assertProof(expanded);

        // a data before issuance - no proof attached
//        final JsonObject unsigned = EmbeddedProof.removeProofs(expanded);

        final Collection<Proof> proofs = new ArrayList<>(expandedProofs.size());

        // read attached proofs
        for (final JsonObject expandedProof : expandedProofs) {

            final Collection<String> proofTypes = LdType.strings(expandedProof);

            if (proofTypes == null || proofTypes.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF, Term.TYPE);
            }

            final SignatureSuite signatureSuite = findSuite(proofTypes, expandedProof);

            Proof proof = null;
            
            if (signatureSuite != null) {
//                proof = signatureSuite.getProof(expandedProof, loader);
            }

            if (proof == null) {
//                if (failOnUnsupportedProof) {
//                    throw new VerificationError(Code.UnsupportedCryptoSuite);
//                }
//FIXME                proof = new UnknownProof(expandedProof);
            }

            proofs.add(proof);
        }
        return proofs;
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
//
//
//    public static JsonLdVerifiableReader with(TestSignatureSuite suite) {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
