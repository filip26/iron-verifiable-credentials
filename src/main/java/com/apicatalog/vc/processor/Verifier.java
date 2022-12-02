package com.apicatalog.vc.processor;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdObject;
import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.schema.LdTerm;
import com.apicatalog.ld.signature.LinkedDataSignature;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.ld.signature.SignatureSuiteProvider;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.ld.signature.key.VerificationKey;
import com.apicatalog.ld.signature.method.DidUrlMethodResolver;
import com.apicatalog.ld.signature.method.HttpMethodResolver;
import com.apicatalog.ld.signature.method.MethodResolver;
import com.apicatalog.ld.signature.method.VerificationMethod;
import com.apicatalog.ld.signature.proof.EmbeddedProof;
import com.apicatalog.vc.VcSchema;
import com.apicatalog.vc.VcSchemaTag;
import com.apicatalog.vc.loader.StaticContextLoader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public final class Verifier extends Processor<Verifier> {

    protected final SignatureSuiteProvider suiteProvider;

    private final URI location;
    private final JsonObject document;

    private StatusVerifier statusVerifier = null;
    private SubjectVerifier subjectVerifier = null;

    private final Map<String, Object> params;

//    @Deprecated
//    private DidResolver didResolver = null;

    private Collection<MethodResolver> methodResolvers;

    public Verifier(URI location, final SignatureSuiteProvider suiteProvider) {
        this.location = location;
        this.document = null;
        this.suiteProvider = suiteProvider;
        this.methodResolvers = defaultResolvers();
        this.params = new LinkedHashMap<>(10);
    }

    public Verifier(JsonObject document, final SignatureSuiteProvider suiteProvider) {
        this.document = document;
        this.location = null;
        this.suiteProvider = suiteProvider;
        this.methodResolvers = defaultResolvers();
        this.params = new LinkedHashMap<>(10);
    }

    public static final Collection<MethodResolver> defaultResolvers() {
        Collection<MethodResolver> resolvers = new LinkedHashSet<>();
        resolvers.add(new DidUrlMethodResolver());
        resolvers.add(new HttpMethodResolver());
        return resolvers;
    }

    public Verifier ethodResolvers(Collection<MethodResolver> resolvers) {
        this.methodResolvers = resolvers;
        return this;
    }

    /**
     * Set a credential status verifier. If not set then
     * <code>credentialStatus</code> is ignored if present.
     *
     * @param statusVerifier a custom status verifier instance
     * @return the verifier instance
     */
    public Verifier statusVerifier(StatusVerifier statusVerifier) {
        this.statusVerifier = statusVerifier;
        return this;
    }

    /**
     * Set a credential subject verifier. If not set then
     * <code>credentialStatus</code> is not verified.
     *
     * @param subjectVerifier a custom subject verifier instance
     * @return the verifier instance
     */
    public Verifier subjectVerifier(SubjectVerifier subjectVerifier) {
        this.subjectVerifier = subjectVerifier;
        return this;
    }

//    public Verifier didResolver(final DidResolver didResolver) {
//        this.didResolver = didResolver;
//        return this;
//    }

    /**
     * Custom verifier parameters that can be consumed during validation.
     * 
     * @param name
     * @param value
     * @return
     */
    public Verifier param(final String name, final Object value) {
        params.put(name, value);
        return this;
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @throws VerificationError
     * @throws DocumentError
     */
    public void isValid() throws VerificationError, DocumentError {

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }

        if (document != null) {
            verify(document);
            return;
        }

        if (location != null) {
            verify(location);
            return;
        }

        throw new IllegalStateException();
    }

    private void verify(final URI location) throws VerificationError, DocumentError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(location).loader(loader).base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private void verify(final JsonObject document) throws VerificationError, DocumentError {
        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document)).loader(loader)
                    .base(base).get();

            verifyExpanded(expanded);

        } catch (JsonLdError e) {
            failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private void verifyExpanded(JsonArray expanded) throws VerificationError, DocumentError {

        if (expanded == null || expanded.isEmpty()) {
            throw new DocumentError(ErrorType.Invalid);
        }

        for (final JsonValue item : expanded) {
            if (JsonUtils.isNotObject(item)) {
                throw new DocumentError(ErrorType.Invalid);
            }
            verifyExpanded(item.asJsonObject());
        }
    }

    private void verifyExpanded(final JsonObject expanded) throws VerificationError, DocumentError {

        // get a verifiable representation
        final Verifiable veri1fiable = get(expanded);

        if (veri1fiable.isCredential()) {

            // data integrity and metadata validation
            validate(veri1fiable.asCredential(), statusVerifier, subjectVerifier);

            verifyProofs(expanded);

        } else if (veri1fiable.isPresentation()) {

            // verify embedded credentials
            for (final JsonObject expandedCredential : veri1fiable.asPresentation().getCredentials()) {

                final Verifiable credential = get(expandedCredential);

                if (!credential.isCredential()) {
                    throw new DocumentError(ErrorType.Invalid, VcSchema.VERIFIABLE_CREDENTIALS, LdTerm.TYPE);
                }

                // data integrity and metadata validation
                validate(credential.asCredential(), statusVerifier, subjectVerifier);

                verifyProofs(expandedCredential);
            }

            verifyProofs(expanded);

        } else {
            throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
        }
    }

    private void verifyProofs(JsonObject expanded) throws VerificationError, DocumentError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonValue> proofs = EmbeddedProof.assertProof(expanded);

        // a data before issuance - no proof attached
        final JsonObject data = EmbeddedProof.removeProof(expanded);

        // verify attached proofs' signatures
        for (final JsonValue embeddedProof : proofs) {

            if (JsonUtils.isNotObject(embeddedProof)) {
                throw new DocumentError(ErrorType.Invalid, VcSchema.PROOF);
            }

            final JsonObject proofObject = embeddedProof.asJsonObject();

            final Collection<String> proofType = JsonLdReader.getType(proofObject);

            if (proofType == null || proofType.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, VcSchema.PROOF, LdTerm.TYPE);
            }

            final SignatureSuite signatureSuite = proofType.stream()
                    .filter(suiteProvider::isSupported).findFirst()
                    .map(suiteProvider::getSignatureSuite)
                    .orElseThrow(() -> new VerificationError(Code.UnsupportedCryptoSuite));

            if (signatureSuite.getSchema() == null) {
                throw new IllegalStateException("The suite [" + signatureSuite.getProofType().id() + "] does not provide proof schema.");
            }

            // FIXMe run assertions validate(proof);

            final LdProperty<byte[]> proofValueProperty = signatureSuite.getSchema().property(VcSchemaTag.ProofValue.name());

            if (proofValueProperty == null) {
                throw new IllegalStateException("The proof schema does not define the proof value.");
            }

            final LdObject proof = signatureSuite.getSchema().read(proofObject);

            signatureSuite.getSchema().validate(proof, params);

            if (!proof.contains(proofValueProperty.term())) {
                throw new DocumentError(ErrorType.Missing, proofValueProperty.term());
            }

//            JsonArray jsonValue = proofObject.getJsonArray(proofValueName.id());
//
//            if (!jsonValue.stream().allMatch(ValueObject::isValueObject)
//                    || !jsonValue.stream()
//                            .map(JsonValue::asJsonObject)
//                            .map(o -> o.get(Keywords.VALUE))
//                            .allMatch(JsonUtils::isString)
//                    ) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValue");
//            }
//            
//            String proofValueType = jsonValue.getJsonObject(0).getString(Keywords.TYPE);

//            if (!signatureSuite.getProofValueAdapter().id().toString().equals(proofValueType)) {
//                throw new DocumentError(ErrorType.Invalid, "ProofValueType");
//            }

//            String encodedProofValue = jsonValue.getJsonObject(0).getString(Keywords.VALUE);

//            final byte[] proofValue = signatureSuite.getProofValueAdapter().decode(encodedProofValue);

            final byte[] proofValue = (byte[]) proof.value(proofValueProperty.term());

            if (proofValue == null || proofValue.length == 0) {
                throw new DocumentError(ErrorType.Missing, proofValueProperty.term());
            }

            // final Proof proof =
            // signatureSuite.getProofAdapter().deserialize(proofValue.asJsonObject());

            final LdProperty<VerificationMethod> methodProperty = signatureSuite.getSchema().property(VcSchemaTag.VerificationMethod.name());

            if (methodProperty == null) {
                throw new IllegalStateException("The proof schema does not define a verification method.");
            }

            VerificationMethod verificationMethod = getMethod(methodProperty, proofObject, signatureSuite)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, methodProperty.term()));

//            // if the verification is not a verification key
//            if ((!(proof.getMethod() instanceof VerificationKey)
//                    // or does not have public key
//                    || (((VerificationKey)proof.getMethod()).publicKey() == null)
//                    )   
//                && proof.getMethod().id() != null) {
//
//                // find the method id resolver
//                final Optional<MethodResolver> resolver = 
//                        methodResolvers.stream()
//                                    .filter(r -> r.isAccepted(proof.getMethod().id()))
//                                    .findFirst();
//                
//                // try to resolve the method                
//                if (resolver.isPresent()) {
//                    verificationMethod = resolver.get().resolve(proof.getMethod().id(), signatureSuite);
//                }
//            }

//            final VerificationMethod verificationMethod = 
//                    getMethod(
//                        proof.getMethod().id(),
//                        loader, 
//                        signatureSuite.getMethodAdapter()
//                        );

            if (!(verificationMethod instanceof VerificationKey)) {
                throw new DocumentError(ErrorType.Unknown, methodProperty.term());
            }

            // remote a proof value
            final JsonObject unsignedProof = Json.createObjectBuilder(proofObject)
                    .remove(proofValueProperty.term().id())
                    .build();

            final LinkedDataSignature signature = new LinkedDataSignature(signatureSuite.getCryptoSuite());

            // verify signature
            signature.verify(
                    data,
                    unsignedProof,
                    (VerificationKey) verificationMethod,
                    proofValue);
        }
        // all good
    }

    Optional<VerificationMethod> getMethod(final LdProperty<VerificationMethod> property, final JsonObject proofObject, final SignatureSuite suite) throws VerificationError, DocumentError {

        final JsonArray expanded = proofObject.getJsonArray(property.term().id());

        if (JsonUtils.isNull(expanded) || expanded.isEmpty()) {
            return Optional.empty();
        }

        for (final JsonValue methodValue : expanded) {

            if (JsonUtils.isNotObject(methodValue)) {
                throw new IllegalStateException(); // should never happen
            }

            final JsonObject methodObject = methodValue.asJsonObject();

            final Collection<String> types = JsonLdReader.getType(methodObject);

            if (types == null || types.isEmpty()) {
                return resolve(methodObject, suite, property);
            }

//            final MethodAdapter adapter = types.stream()
//                                            .map(suite::getMethodAdapter)
//                                            .filter(Objects::nonNull)
//                                            .findFirst()
//                                            .orElseThrow(() -> new DocumentError(ErrorType.Unknown, "VerificationMethod"));
//            
//            final VerificationMethod method = adapter.deserialize(methodObject);

            final VerificationMethod method = property.read(methodObject);

            if (method != null && method instanceof VerificationKey && (((VerificationKey) method).publicKey() != null)) {
                return Optional.of(method);
            }

            return resolve(method.id(), suite, property);
        }

        return Optional.empty();
    }

    Optional<VerificationMethod> resolve(JsonObject method, SignatureSuite suite, LdProperty<VerificationMethod> property) throws DocumentError, VerificationError {
        try {
            URI id = JsonLdReader
                    .getId(method)
                    .orElseThrow(() -> new DocumentError(ErrorType.Missing, property.term()));

            return resolve(id, suite, property);

        } catch (InvalidJsonLdValue e) {
            throw new DocumentError(e, ErrorType.Invalid, property.term());
        }

    }

    Optional<VerificationMethod> resolve(URI id, SignatureSuite suite, LdProperty<VerificationMethod> property) throws DocumentError {

        if (id == null) {
            throw new DocumentError(ErrorType.Invalid, property.term());
        }

        // find the method id resolver
        final Optional<MethodResolver> resolver = methodResolvers.stream()
                .filter(r -> r.isAccepted(id))
                .findFirst();

        // try to resolve the method
        if (resolver.isPresent()) {
            return Optional.ofNullable(resolver.get().resolve(id, loader, suite));
        }

        throw new DocumentError(ErrorType.Unknown, property.term());
    }

    // refresh/fetch verification method
//    final VerificationMethod getMethod(final URI id, final DocumentLoader loader, MethodAdapter keyAdapter) throws DocumentError, VerificationError {
//
//        if (DidUrl.isDidUrl(id)) {
//
//            DidResolver resolver = didResolver;
//
//            if (resolver == null) {
//                resolver = new DidKeyResolver();
//            }
//
//            final DidDocument didDocument = resolver.resolve(DidUrl.from(id));
//
//            return didDocument.verificationMethod().stream()
//                    .filter(vm -> keyAdapter.isSupportedType(vm.type()))
//                    .map(did -> new VerificationKeyImpl(
//                            did.id().toUri(),
//                            did.controller().toUri(),
//                            did.type(),
//                            did.publicKey()))
//                    .findFirst().orElseThrow(() -> new VerificationError(Code.UnknownVerificationKey));
//        }
//
//        try {
//            final JsonArray document = JsonLd.expand(id)
//                                            .loader(loader)
//                                            .context(keyAdapter.getContextFor(id)) // an optional expansion context
//                                            .get();
//
//            for (final JsonValue method : document) {
//
//                if (JsonUtils.isNotObject(method)) {
//                    continue;
//                }
//
//                // take the first method matching type
//                if (JsonLdUtils
//                        .getType(method.asJsonObject())
//                        .stream()
//                        .anyMatch(m -> keyAdapter.isSupportedType(m))) {
//
//                    return keyAdapter.deserialize(method.asJsonObject());
//                }
//            }
//
//        } catch (JsonLdError e) {
//            failWithJsonLd(e);
//            throw new DocumentError(ErrorType.Invalid, "document", e);
//        }
//
//        throw new VerificationError(Code.UnknownVerificationKey);
//    }

    private static final void validate(final Credential credential, final StatusVerifier statusVerifier, final SubjectVerifier subjectVerifier)
            throws DocumentError, VerificationError {

        // data integrity - issuance date is a mandatory property
        if (credential.getIssuanceDate() == null
                && credential.getValidFrom() == null
                && credential.getIssued() == null) {
            throw new DocumentError(ErrorType.Missing, VcSchema.ISSUANCE_DATE);
        }

        // validation
        if (credential.isExpired()) {
            throw new VerificationError(Code.Expired);
        }

        if ((credential.getIssuanceDate() != null
                && credential.getIssuanceDate().isAfter(Instant.now()))

                || (credential.getIssued() != null
                        && credential.getIssued().isAfter(Instant.now()))

                || (credential.getValidFrom() != null
                        && credential.getValidFrom().isAfter(Instant.now()))) {

            throw new VerificationError(Code.NotValidYet);
        }

        // status check
        if (statusVerifier != null && credential.getStatus() != null) {
            statusVerifier.verify(credential.getStatus());
        }

        // subject check
        if (subjectVerifier != null) {
            subjectVerifier.verify(credential.getSubject());
        }
    }
//
//    private final void validate(final Proof proof) throws VerificationError, DocumentError {
//
//        // verification method
//        if (proof.getMethod() == null) {
//            throw new DocumentError(ErrorType.Missing, "ProofVerificationMethod");
//        }
//
//        // value
//        if (proof.getValue() == null || proof.getValue().length == 0) {
//            throw new DocumentError(ErrorType.Missing, "ProofValue");
//        }
//
//        if (proof instanceof DataIntegrityProof) {
//            validate((DataIntegrityProof)proof);
//        }
//    }

//    private final void validate(final DataIntegrityProof proof) throws VerificationError, DocumentError {
//        // purpose
//        if (proof.getPurpose() == null) {
//            throw new DocumentError(ErrorType.Missing, "ProofPurpose");
//        }
//
//        // created
//        if (proof.getCreated() == null) {
//            throw new DocumentError(ErrorType.Missing, "ProofCreated");
//        }
//        
//        if (proof.getCreated().isBefore(Instant.now())) {
//            throw new VerificationError(Code.NotValidYet);
//        }
//
////        // domain
////        if (StringUtils.isNotBlank(domain) && !domain.equals(proof.getDomain())) {
////            throw new VerificationError(Code.InvalidProofDomain);
////        }
//    }

//    class VerificationKeyImpl implements VerificationKey {
//
//        final URI id;
//        final String type;
//        final URI controller;
//        final byte[] publicKey;
//
//        public VerificationKeyImpl(URI id, URI controller, String type, byte[] publicKey) {
//            this.id = id;
//            this.type = type;
//            this.controller = controller;
//            this.publicKey = publicKey;
//        }
//
//        @Override
//        public URI id() {
//            return id;
//        }
//
//        @Override
//        public String type() {
//            return type;
//        }
//
//        @Override
//        public URI controller() {
//            return controller;
//        }
//
//        @Override
//        public byte[] publicKey() {
//            return publicKey;
//        }
//    }
}
