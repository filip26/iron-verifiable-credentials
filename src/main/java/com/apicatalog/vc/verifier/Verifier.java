package com.apicatalog.vc.verifier;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.writer.NodeDebugWriter;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.processor.AbstractProcessor;
import com.apicatalog.vc.processor.Parameter;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.reader.VerifiableReaderResolver;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.status.StatusVerifier;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdi.VcdiVocab;
import com.apicatalog.vcdm.VcdmResolver;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

/**
 * A configurable set of suites and policies to verify. e.g. a set of allowed
 * signature suites, verification method resolvers, status resolvers, custom
 * validation logic, etc.
 */
public class Verifier extends AbstractProcessor<Verifier> {

    private static final Logger LOGGER = Logger.getLogger(Verifier.class.getName());

    protected StatusVerifier statusVerifier;

    protected final VerifiableReaderResolver verifiableAdapter;

    protected Verifier(final SignatureSuite... suites) {
        super(suites);

        this.verifiableAdapter = new VcdmResolver(suites);
        this.statusVerifier = null;
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
        return verify(verifiable, toMap(parameters), getLoader());
    }

    /**
     * A method to override with a custom verification logic that is called before
     * proofs' verification.
     * 
     * @param verifiable
     */
    protected void check(Verifiable verifiable) throws VerificationError {
    }

    /**
     * A method to override with a custom verification logic that is called before a
     * proof value is verified.
     * 
     * @param proof
     */
    protected void check(Proof proof) throws VerificationError {
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

    protected Verifiable verify(final URI location, final Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {
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

            return verify(json.asJsonObject(), params, loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable verify(final JsonObject document, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {

        // extract context
        final Collection<String> context;

        try {
            context = JsonLdContext.strings(document);
        } catch (IllegalArgumentException e) {
            throw new DocumentError(ErrorType.Invalid, "document");
        }

        final VerifiableReader reader = verifiableAdapter.resolveReader(context);

        if (reader == null) {
            LOGGER.log(Level.INFO, "An unknown document model {0}", context);
            throw new DocumentError(ErrorType.Unknown, "DocumentModel");
        }

        final Verifiable verifiable = reader.read(document, loader, base);

        if (verifiable == null) {
            throw new DocumentError(ErrorType.Invalid, "document");
        }
NodeDebugWriter.writeToStdOut(verifiable.ld());
NodeDebugWriter.writeToStdOut(verifiable.proofs().iterator().next().ld().root());
System.out.println(verifiable.proofs().size());
        return verify(verifiable, params, loader);
    }

    protected Verifiable verify(Verifiable verifiable, Map<String, Object> params, DocumentLoader loader) throws VerificationError, DocumentError {

        Objects.requireNonNull(verifiable);

        if (verifiable.proofs() == null || verifiable.proofs().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, "Proof");
        }

        // validate data model semantic
        if (verifiable.isCredential()) {
            validate(verifiable.asCredential());

        } else {
            verifiable.validate();
        }

        // sort the proofs in the verification order
        final ProofQueue queue = ProofQueue.create(verifiable.proofs());

        // verify the proofs' signatures
        Proof proof = queue.pop();

        while (proof != null) {

            // validate proof properties
            proof.validate(params);

            final ProofValue proofValue = proof.signature();

            if (proofValue == null) {
                throw new DocumentError(ErrorType.Missing, VcdiVocab.PROOF_VALUE);
            }

            final VerificationMethod verificationMethod = getMethod(proof, loader)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, VcdmVocab.PROOF, VcdiVocab.VERIFICATION_METHOD));

            if (verificationMethod instanceof VerificationKey verificationKey) {
                proof.verify(verificationKey);

            } else {
                throw new DocumentError(ErrorType.Unknown, VcdmVocab.PROOF, VcdiVocab.VERIFICATION_METHOD);
            }

            proof = queue.pop();
        }

        // all good
        return verifiable;
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
                // FIXME add 
//                statusVerifier.verify(credential, status);   
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
