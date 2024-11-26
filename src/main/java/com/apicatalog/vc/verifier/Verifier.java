package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.cryptosuite.VerificationError;
import com.apicatalog.cryptosuite.VerificationError.VerificationErrorCode;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.jsonld.ContextAwareReaderProvider;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.VerifiableReaderProvider;
import com.apicatalog.vc.model.generic.GenericReader;
import com.apicatalog.vc.processor.Parameter;
import com.apicatalog.vc.processor.SuitesProcessor;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.status.StatusVerifier;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdi.VcdiVocab;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.JsonObject;

/**
 * A configurable set of suites and policies to verify. e.g. a set of allowed
 * signature suites, verification method resolvers, status resolvers, custom
 * validation logic, etc.
 */
public class Verifier extends SuitesProcessor<Verifier> {

//    private static final Logger LOGGER = Logger.getLogger(Verifier.class.getName());

    protected StatusVerifier statusVerifier;

    protected Verifier(final SignatureSuite... suites) {
        super(suites);

        this.readerProvider = defaultReaders(proofAdapter);

        this.statusVerifier = null;
    }

    protected static VerifiableReaderProvider defaultReaders(final ProofAdapter proofAdapter) {

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
     * @return {@link VerifiableDocument} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public VerifiableDocument verify(VerifiableDocument verifiable, Parameter<?>... parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(verifiable);
        return verify(verifiable, toMap(parameters));
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param document
     * @param parameters custom parameters, e.g. challenge token
     * @return {@link VerifiableDocument} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public VerifiableDocument verify(JsonObject document, Parameter<?>... parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(document);
        return verify(document, toMap(parameters), getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param document
     * @param parameters
     * @return {@link VerifiableDocument} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     * 
     */
    public VerifiableDocument verify(final JsonObject document, final Map<String, Object> parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(document);
        return verify(document, parameters, getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param location
     * @param parameters
     * @return {@link VerifiableDocument} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public VerifiableDocument verify(final URI location, final Parameter<?>... parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return verify(location, toMap(parameters), getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param location
     * @param parameters
     * @return {@link VerifiableDocument} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public VerifiableDocument verify(final URI location, final Map<String, Object> parameters) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return verify(location, parameters, getLoader());
    }

    protected VerifiableDocument verify(final URI location, final Map<String, Object> parameters, DocumentLoader loader) throws VerificationError, DocumentError {
        return verify(fetch(location), parameters, loader);
    }

    protected VerifiableDocument verify(final JsonObject document, final Map<String, Object> parameters, DocumentLoader loader) throws VerificationError, DocumentError {

        final VerifiableDocument verifiable = read(document, loader);

        if (verifiable != null) {
            return verify(verifiable, parameters);
        }
        throw new DocumentError(ErrorType.Unknown, "Model");
    }

    public VerifiableDocument verify(final VerifiableDocument verifiable, final Map<String, Object> parameters) throws VerificationError, DocumentError {

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

                if (keyProvider == null) {
                    throw new IllegalStateException("A verification method provider is not set.");
                }

                final VerificationKey verificationKey = keyProvider.keyFor(proof);

                if (verificationKey == null) {
                    throw new DocumentError(ErrorType.Unknown, VcdiVocab.VERIFICATION_METHOD);
                }

                // verify the proofs' signatures
                proof.verify(verificationKey);
            }

            // all good
            return verifiable;

        } catch (IllegalArgumentException e) {
            throw new DocumentError(e, ErrorType.Invalid);

//        } catch (NullPointerException e) {
//            throw new DocumentError(e, ErrorType.Missing);

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
//        if (statusVerifier != null && credential.status() != null && !credential.status().isEmpty()) {
//            for (final Status status : credential.status()) {
//                statusVerifier.verify(credential, status);
//            }
//        }
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
