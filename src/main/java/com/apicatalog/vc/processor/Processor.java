package com.apicatalog.vc.processor;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.jsonld.uri.UriUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.model.Credential;
import com.apicatalog.vc.model.DataModelVersion;
import com.apicatalog.vc.model.Verifiable;
import com.apicatalog.vc.model.io.CredentialReader;
import com.apicatalog.vc.model.io.PresentationReader;
import com.apicatalog.vc.status.StatusPropertiesValidator;
import com.apicatalog.vc.status.StatusValidator;
import com.apicatalog.vc.subject.SubjectValidator;

import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

abstract class Processor<T extends Processor<?>> {

    protected DocumentLoader loader;
    protected boolean bundledContexts;
    protected URI base;

    protected StatusValidator statusValidator;
    protected SubjectValidator subjectValidator;

    protected DataModelVersion modelVersion;

    protected Processor() {
        // default values
        this.loader = null;
        this.bundledContexts = true;
        this.base = null;
        this.modelVersion = null;

        this.statusValidator = new StatusPropertiesValidator();
        this.subjectValidator = null;
    }

    @SuppressWarnings("unchecked")
    public T loader(DocumentLoader loader) {
        this.loader = loader;
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

    /**
     * Set a credential status verifier. If not set then
     * <code>credentialStatus</code> is ignored if present.
     *
     * @param statusValidator a custom status verifier instance
     * @return the verifier instance
     */
    @SuppressWarnings("unchecked")
    public T statusValidator(StatusValidator statusValidator) {
        this.statusValidator = statusValidator;
        return (T) this;
    }

    /**
     * Set a credential subject verifier. If not set then
     * <code>credentialStatus</code> is not verified.
     *
     * @param subjectValidator a custom subject verifier instance
     * @return the verifier instance
     */
    @SuppressWarnings("unchecked")
    public T subjectValidator(SubjectValidator subjectValidator) {
        this.subjectValidator = subjectValidator;
        return (T) this;
    }

    protected static Verifiable get(final DataModelVersion version, final JsonObject expanded) throws DocumentError {

        // is a credential?
        if (CredentialReader.isCredential(expanded)) {
            // validate the credential object
            return CredentialReader.read(version, expanded);
        }

        // is a presentation?
        if (PresentationReader.isPresentation(expanded)) {
            // validate the presentation object
            return PresentationReader.read(version, expanded);
        }

        // is not expanded JSON-LD object
        if (!JsonLdReader.hasType(expanded)) {
            throw new DocumentError(ErrorType.Missing, LdTerm.TYPE);
        }

        throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
    }

    protected void failWithJsonLd(JsonLdError e) throws DocumentError {
        if (JsonLdErrorCode.LOADING_DOCUMENT_FAILED == e.getCode()) {
            throw new DocumentError(e, ErrorType.Invalid);
        }

        if (JsonLdErrorCode.LOADING_REMOTE_CONTEXT_FAILED == e.getCode()) {
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected void validateData(final Credential credential) throws DocumentError {

        // v1
        if ((credential.getVersion() == null || DataModelVersion.V11.equals(credential.getVersion()))
                && credential.getIssuanceDate() == null) {
            // issuance date is a mandatory property
            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUANCE_DATE);
        }

        // status check
        if (statusValidator != null && JsonUtils.isNotNull(credential.getStatus())) {
            statusValidator.verify(credential.getStatus());
        }
    }

    protected DataModelVersion getVersion(final JsonObject object) throws DocumentError {

        final JsonValue contexts = object.get(Keywords.CONTEXT);

        if (JsonUtils.isNotArray(contexts)) {
            throw new DocumentError(ErrorType.Invalid);
        }

        for (final JsonValue context : contexts.asJsonArray()) {
            if (JsonUtils.isScalar(context)
                    && UriUtils.isURI(((JsonString) context).getString())) {

                final String contextUri = ((JsonString) context).getString();

                if ("https://www.w3.org/2018/credentials/v1".equals(contextUri)) {
                    modelVersion = DataModelVersion.V11;
                    break;
                }
                if ("https://www.w3.org/ns/credentials/v2".equals(contextUri)) {
                    modelVersion = DataModelVersion.V20;
                    break;
                }
            }
        }
        return modelVersion;
    }
}
