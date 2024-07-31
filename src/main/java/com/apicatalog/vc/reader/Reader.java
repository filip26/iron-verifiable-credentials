package com.apicatalog.vc.reader;

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
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.jsonld.JsonLdPresentation;
import com.apicatalog.vc.jsonld.JsonLdVerifiable;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.processor.AbstractProcessor;
import com.apicatalog.vc.proof.EmbeddedProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public class Reader extends AbstractProcessor<Reader> {

    protected boolean failOnUnsupportedProof = true;
    
    protected Reader(final SignatureSuite... suites) {
        super(suites);
    }

    public static Reader with(final SignatureSuite... suites) {
        return new Reader(suites);
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param document
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable read(final JsonObject document) throws VerificationError, DocumentError {
        Objects.requireNonNull(document);
        return read(document, getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param location
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable read(final URI location) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return read(location, getLoader());
    }

    protected Verifiable read(final URI location, DocumentLoader loader) throws VerificationError, DocumentError {
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

    protected Verifiable read(final JsonObject document, DocumentLoader loader) throws VerificationError, DocumentError {

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
                    VerifiableReader.getVersion(document), 
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

    private Verifiable verifyExpanded(final ModelVersion version, JsonStructure context, JsonArray expanded, Map<String, Object> params, DocumentLoader loader)
            throws VerificationError, DocumentError {

        if (expanded == null || expanded.isEmpty() || expanded.size() > 1) {
            throw new DocumentError(ErrorType.Invalid);
        }

        final JsonValue verifiable = expanded.iterator().next();

        if (JsonUtils.isNotObject(verifiable)) {
            throw new DocumentError(ErrorType.Invalid);
        }

        return verifyExpanded(version, context, verifiable.asJsonObject(), loader);
    }

    private Verifiable verifyExpanded(final ModelVersion version, JsonStructure context, final JsonObject expanded, DocumentLoader loader)
            throws VerificationError, DocumentError {

        // get a verifiable representation
        final JsonLdVerifiable verifiable = null; //FIXMe reader.read(version, expanded);

        if (verifiable.isCredential()) {

            // data integrity and metadata validation
//TODO            validate(verifiable.asCredential());

//FIXME            verifiable.proofs(read(context, expanded, loader));

            return verifiable;

        } else if (verifiable.isPresentation()) {

            // verify presentation proofs
            verifiable.proofs(readProofs(context, expanded, loader));

            final Collection<Credential> credentials = new ArrayList<>();

            for (final JsonObject presentedCredentials : VerifiableReader.getCredentials(expanded)) {

                if (!VerifiableReader.isCredential(presentedCredentials)) {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS, Term.TYPE);
                }
//var params = new HashMap<>();
//FIXME                credentials.add(verifyExpanded(version, context, presentedCredentials, params, loader).asCredential());
            }

            ((JsonLdPresentation)verifiable.asPresentation()).credentials(credentials);

            return verifiable;
        }
        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    protected Collection<Proof> readProofs(JsonStructure context, JsonObject expanded, DocumentLoader loader) throws VerificationError, DocumentError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonObject> expandedProofs = EmbeddedProof.assertProof(expanded);

        // a data before issuance - no proof attached
        final JsonObject unsigned = EmbeddedProof.removeProofs(expanded);

        final Collection<Proof> proofs = new ArrayList<>(expandedProofs.size());

        // read attached proofs
        for (final JsonObject expandedProof : expandedProofs) {

            final Collection<String> proofTypes = LdType.strings(expandedProof);

            if (proofTypes == null || proofTypes.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, VcVocab.PROOF, Term.TYPE);
            }

            final SignatureSuite signatureSuite = findSuite(proofTypes, expandedProof);

            Proof proof = null;
            
            if (signatureSuite != null) {
                proof = signatureSuite.getProof(expandedProof, loader);
            }

            if (proof == null) {
                if (failOnUnsupportedProof) {
                    throw new VerificationError(Code.UnsupportedCryptoSuite);
                }
//FIXME                proof = new UnknownProof(expandedProof);
            }

            proofs.add(proof);
        }
        return proofs;
    }


}
