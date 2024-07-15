package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.integrity.DataIntegrityVocab;
import com.apicatalog.vc.processor.AbstractProcessor;
import com.apicatalog.vc.processor.Parameter;
import com.apicatalog.vc.proof.EmbeddedProof;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.status.StatusVerifier;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public class Verifier extends AbstractProcessor<Verifier> {

    protected StatusVerifier statusVerifier;

    protected Verifier(final SignatureSuite... suites) {
        super(suites);

        this.statusVerifier = null;
    }

    public static Verifier with(final SignatureSuite... suites) {
        return new Verifier(suites);
    }

    /**
     * Set a credential status verifier. If not set then
     * <code>credentialStatus</code> is ignored if present.
     *
     * @param statusVerifier a custom status verifier instance
     * @return the verifier instance
     */
    public Verifier statusValidator(StatusVerifier statusVerifier) {
        this.statusVerifier = statusVerifier;
        return this;
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param document
     * @param parameters custom parameters, e.g. challenge token
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable verify(JsonObject document, Parameter<?>... parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(document);
        return verify(document, toMap(parameters), getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param document
     * @param parameters
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     * 
     */
    public Verifiable verify(JsonObject document, Map<String, Object> parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(document);
        return verify(document, parameters, getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param location
     * @param parameters
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable verify(URI location, Parameter<?>... parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return verify(location, toMap(parameters), getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param location
     * @param parameters
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable verify(URI location, Map<String, Object> parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return verify(location, parameters, getLoader());
    }

    protected Verifiable verify(final URI location, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return verify(json.asJsonObject(), params, loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable verify(final JsonObject document, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {

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

            return verifyExpanded(VerifiableReader.getVersion(document), context, expanded, params, loader);

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

        return verifyExpanded(version, context, verifiable.asJsonObject(), params, loader);
    }

    private Verifiable verifyExpanded(final ModelVersion version, JsonStructure context, final JsonObject expanded, Map<String, Object> params, DocumentLoader loader)
            throws VerificationError, DocumentError {

        // get a verifiable representation
        final Verifiable verifiable = reader.read(version, expanded);

        if (verifiable.isCredential()) {

            // data integrity and metadata validation
            validate(verifiable.asCredential());

            verifiable.proofs(verifyProofs(context, expanded, params, loader));

            return verifiable;

        } else if (verifiable.isPresentation()) {

            // verify presentation proofs
            verifiable.proofs(verifyProofs(context, expanded, params, loader));

            final Collection<Credential> credentials = new ArrayList<>();

            for (final JsonObject presentedCredentials : VerifiableReader.getCredentials(expanded)) {

                if (!VerifiableReader.isCredential(presentedCredentials)) {
                    throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS, Term.TYPE);
                }

                credentials.add(verifyExpanded(version, context, presentedCredentials, params, loader).asCredential());
            }

            verifiable.asPresentation().credentials(credentials);

            return verifiable;
        }
        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    protected Collection<Proof> verifyProofs(JsonStructure context, JsonObject expanded, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {

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

            if (signatureSuite == null) {
                throw new VerificationError(Code.UnsupportedCryptoSuite);
            }

            final Proof proof = signatureSuite.getProof(expandedProof, loader);

            if (proof == null) {
                throw new IllegalStateException("The suite [" + signatureSuite + "] returns null as a proof.");
            }
            proofs.add(proof);
        }

        // sort the proofs in the verification order
        final ProofQueue queue = ProofQueue.create(proofs);

        // verify the proofs' signatures
        Proof proof = queue.pop();

        while (proof != null) {

            proof.validate(params);

            final ProofValue proofValue = proof.signature();

            if (proofValue == null) {
                throw new DocumentError(ErrorType.Missing, "ProofValue");
            }

            VerificationMethod verificationMethod = getMethod(proof, loader)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, VcVocab.PROOF, DataIntegrityVocab.VERIFICATION_METHOD));

            if (!(verificationMethod instanceof VerificationKey)) {
                throw new DocumentError(ErrorType.Unknown, VcVocab.PROOF, DataIntegrityVocab.VERIFICATION_METHOD);
            }

            proof.verify(context, unsigned, (VerificationKey) verificationMethod);

            proof = queue.pop();
        }
        // all good
        return proofs;
    }

    final void validate(final Credential credential) throws DocumentError, VerificationError {

        credential.validate();

        // validation
        if (credential.isExpired()) {
            throw new VerificationError(Code.Expired);
        }

        if (credential.isNotValidYet()) {
            throw new VerificationError(Code.NotValidYet);
        }

        // status check
        if (statusVerifier != null && credential.status() != null && !credential.status().isEmpty()) {
            for (final Status status : credential.status()) {
                statusVerifier.verify(credential, status);   
            }
        }
    }

    protected static final Map<String, Object> toMap(Parameter<?>... parameters) {
        return parameters != null && parameters.length > 0
                ? Stream.of(parameters).filter(p -> p.value() != null).collect(Collectors.toMap(
                        Parameter::name,
                        Parameter::value))
                : Collections.emptyMap();
    }
}
