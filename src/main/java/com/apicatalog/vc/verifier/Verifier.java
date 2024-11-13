package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.jsonld.ContextAwareReaderProvider;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.ProofAdapterProvider;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vc.model.generic.GenericReader;
import com.apicatalog.vc.processor.Parameter;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.status.StatusVerifier;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdi.VcdiVocab;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

/**
 * A configurable set of suites and policies to verify. e.g. a set of allowed
 * signature suites, verification method resolvers, status resolvers, custom
 * validation logic, etc.
 */
public class Verifier extends VerificationProcessor<Verifier> {

    private static final Logger LOGGER = Logger.getLogger(Verifier.class.getName());

    protected StatusVerifier statusVerifier;

    protected final VerifiableReader reader;

    protected Verifier(final SignatureSuite... suites) {
        super(suites);

        ProofAdapter proofAdapter = ProofAdapterProvider.of(suites);

        this.reader = defaultReaders(proofAdapter);

        this.statusVerifier = null;
    }

    protected static VerifiableReader defaultReaders(final ProofAdapter proofAdapter) {

        Vcdm11Reader vcdm11 = Vcdm11Reader.with(proofAdapter);

        return new ContextAwareReaderProvider()
                .with(VcdmVocab.CONTEXT_MODEL_V1, vcdm11)
                .with(VcdmVocab.CONTEXT_MODEL_V2, Vcdm20Reader.with(proofAdapter)
                        // add VCDM 1.1 credential support
                        .v11(vcdm11))
                .with(VcdiVocab.CONTEXT_MODEL_V2, GenericReader.with(proofAdapter));
    }

    /**
     * Set of accepted verification suites.
     * 
     * @param suites
     * @return
     */
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
     * @param verifiable
     * @param parameters custom parameters, e.g. challenge token
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable verify(Verifiable verifiable, Parameter<?>... parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(verifiable);
        return verify(verifiable, toMap(parameters));
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
    public Verifiable verify(final JsonObject document, final Map<String, Object> parameters) throws VerificationError, DocumentError {
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
    public Verifiable verify(final URI location, final Parameter<?>... parameters) throws VerificationError, DocumentError {
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
    public Verifiable verify(final URI location, final Map<String, Object> parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return verify(location, parameters, getLoader());
    }

    protected Verifiable verify(final URI location, final Map<String, Object> parameters, DocumentLoader loader) throws VerificationError, DocumentError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument
                    .getJsonContent()
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return verify(json.asJsonObject(), parameters, loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable verify(final JsonObject document, Map<String, Object> parameters, DocumentLoader loader) throws VerificationError, DocumentError {

//        // extract context
//        final Collection<String> context;
//
//        try {
//            context = JsonLdContext.strings(document);
//
//        } catch (IllegalArgumentException e) {
//            throw new DocumentError(e, ErrorType.Invalid, "Context");
//        }

//        final VerifiableReader reader = readerProvider.reader(document);
//
//        if (reader == null) {
////            LOGGER.log(Level.INFO, "An unknown document model {0}", context);
//            throw new DocumentError(ErrorType.Unknown, "DocumentModel");
//        }

        final Verifiable verifiable = reader.read(document, loader, base);

        if (verifiable == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        return verify(verifiable, parameters);
    }

    public Verifiable verify(final Verifiable verifiable, final Map<String, Object> parameters) throws VerificationError, DocumentError {

        if (verifiable.proofs() == null || verifiable.proofs().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, "Proof");
        }

        try {
            // validate data model semantic
            if (verifiable.isCredential()) {
                validate(verifiable.asCredential());

            } else {
                verifiable.validate();
            }

            // sort the proofs in the verification order
            for (final Proof proof : ProofQueue.sort(verifiable.proofs())) {

                // validate proof properties
                proof.validate(parameters == null
                        ? Collections.emptyMap()
                        : parameters);

                final ProofValue proofValue = proof.signature();

                if (proofValue == null) {
                    throw new DocumentError(ErrorType.Missing, VcdiVocab.PROOF_VALUE);
                }

                final VerificationMethod verificationMethod = resolveMethod(proof)
                        .orElseThrow(() -> new DocumentError(ErrorType.Missing, VcdiVocab.VERIFICATION_METHOD));

                if (verificationMethod instanceof VerificationKey verificationKey) {
                    // verify the proofs' signatures
                    proof.verify(verificationKey);

                } else {
                    throw new DocumentError(ErrorType.Unknown, VcdiVocab.VERIFICATION_METHOD);
                }
            }

            // all good
            return verifiable;

        } catch (IllegalArgumentException e) {
            throw new DocumentError(e, ErrorType.Invalid);

        } catch (NullPointerException e) {
            throw new DocumentError(e, ErrorType.Missing);

        } catch (UnsupportedOperationException e) {
            throw new DocumentError(e, ErrorType.Unknown);
        }
    }

    final void validate(final Credential credential) throws DocumentError, VerificationError {

        credential.validate();

        if (credential.isExpired()) {
            throw new VerificationError(VerificationErrorCode.Expired);
        }

        if (credential.isNotValidYet()) {
            throw new VerificationError(VerificationErrorCode.NotValidYet);
        }

        // status check
        if (statusVerifier != null && credential.status() != null && !credential.status().isEmpty()) {
            for (final Status status : credential.status()) {
                statusVerifier.verify(credential, status);
            }
        }
    }

    static final Map<String, Object> toMap(Parameter<?>... parameters) {
        return parameters != null && parameters.length > 0
                ? Stream.of(parameters)
                        .filter(p -> p.name() != null && p.value() != null)
                        .collect(Collectors.toMap(
                                Parameter::name,
                                Parameter::value))
                : Collections.emptyMap();
    }
}
