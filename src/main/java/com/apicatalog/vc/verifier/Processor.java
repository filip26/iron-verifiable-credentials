package com.apicatalog.vc.verifier;

import java.net.URI;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.status.StatusPropertiesValidator;
import com.apicatalog.vc.status.StatusValidator;
import com.apicatalog.vc.subject.SubjectValidator;

abstract class Processor<T extends Processor<?>> {

    protected DocumentLoader defaultLoader;
    protected boolean bundledContexts;
    protected URI base;

    protected StatusValidator statusValidator;
    protected SubjectValidator subjectValidator;

    protected ModelVersion modelVersion;

    protected Processor() {
        // default values
        this.defaultLoader = null;
        this.bundledContexts = true;
        this.base = null;
        this.modelVersion = null;

        this.statusValidator = new StatusPropertiesValidator();
        this.subjectValidator = null;
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

    protected void validateData(final Credential credential) throws DocumentError {

        // v1
        if ((credential.getVersion() == null || ModelVersion.V11.equals(credential.getVersion()))
                && credential.getIssuanceDate() == null) {
            // issuance date is a mandatory property
            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUANCE_DATE);
        }

        // status check
        if (statusValidator != null && JsonUtils.isNotNull(credential.getStatus())) {
            statusValidator.verify(credential.getStatus());
        }
    }

}
