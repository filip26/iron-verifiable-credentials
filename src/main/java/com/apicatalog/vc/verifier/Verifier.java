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

import com.apicatalog.controller.method.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.VerificationErrorCode;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.model.Parameter;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.proof.ProofValue;
import com.apicatalog.vc.reader.ReaderResolver;
import com.apicatalog.vc.reader.VerifiableReader;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.status.StatusVerifier;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdi.VcdiVocab;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.io.VcdmResolver;
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

    protected final ReaderResolver readerResolver;

    protected Verifier(final SignatureSuite... suites) {
        super(suites);

        this.readerResolver = vcdmResolver(suites); 
        
        this.statusVerifier = null;
    }

    protected static ReaderResolver vcdmResolver(final SignatureSuite... suites) {
        var resolver = new VcdmResolver();
        resolver.v11(new Vcdm11Reader(suites));
        resolver.v20(new Vcdm20Reader(resolver, suites));
        return resolver;
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

//    /**
//     * A method to override with a custom verification logic that is called before
//     * proofs' verification.
//     * 
//     * @param verifiable
//     */
//    protected void check(Verifiable verifiable) throws VerificationError {
//    }
//
//    /**
//     * A method to override with a custom verification logic that is called before a
//     * proof value is verified.
//     * 
//     * @param proof
//     */
//    protected void check(Proof proof) throws VerificationError {
//    }

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
            throw new DocumentError(e, ErrorType.Invalid, "Context");
        }

        final VerifiableReader reader = readerResolver.resolveReader(context);

        if (reader == null) {
            LOGGER.log(Level.INFO, "An unknown document model {0}", context);
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        final Verifiable verifiable = reader.read(context, document, loader, base);

        if (verifiable == null) {
            throw new DocumentError(ErrorType.Invalid, "document");
        }

        return verify(verifiable, params);
    }

    public Verifiable verify(Verifiable verifiable, Map<String, Object> params) throws VerificationError, DocumentError {

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
            proof.validate(params == null
                    ? Collections.emptyMap()
                    : params);

            final ProofValue proofValue = proof.signature();

            if (proofValue == null) {
                throw new DocumentError(ErrorType.Missing, VcdiVocab.PROOF_VALUE);
            }

            final VerificationMethod verificationMethod = getMethod(proof)
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

    protected static final Map<String, Object> toMap(Parameter<?>... parameters) {
        return parameters != null && parameters.length > 0
                ? Stream.of(parameters).filter(p -> p.value() != null).collect(Collectors.toMap(
                        Parameter::name,
                        Parameter::value))
                : Collections.emptyMap();
    }
}
