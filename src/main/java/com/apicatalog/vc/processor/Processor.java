package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.Arrays;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.DataError.ErrorType;
import com.apicatalog.ld.signature.SignatureAdapter;
import com.apicatalog.ld.signature.SignatureAdapters;
import com.apicatalog.ld.signature.ed25519.Ed25519SignatureAdapter;

import jakarta.json.JsonObject;

abstract class Processor<T extends Processor<?>> {

    protected static final SignatureAdapter DEFAULT_SIGNATURE_ADAPTERS =
                                new SignatureAdapters(
                                        Arrays.asList(new SignatureAdapter[] {
                                                new Ed25519SignatureAdapter()
                                        }));

    protected DocumentLoader loader;
    protected boolean bundledContexts;
    protected URI base;

    protected SignatureAdapter signatureAdapter;

    protected Processor() {
        // default values
        this.loader = null;
        this.bundledContexts = true;
        this.base = null;
        this.signatureAdapter = null;
    }

    @SuppressWarnings("unchecked")
    public T loader(DocumentLoader loader) {
        this.loader = loader;
        return (T) this;
    }

    /**
     * Use well-known contexts that are bundled with the library instead of fetching it online.
     * <code>true</code> by default. Disabling might cause slower processing.
     *
     * @param enable
     * @return
     */
    @SuppressWarnings("unchecked")
    public T useBundledContexts(boolean enable) {
        this.bundledContexts = enable;
        return (T)this;
    }

    /**
     * If set, this overrides the input document's IRI.
     *
     * @param base
     * @return
     */
    @SuppressWarnings("unchecked")
    public T base(URI base) {
       this.base = base;
       return (T)this;
    }

    @SuppressWarnings("unchecked")
    public T signatureAdapter(SignatureAdapter adapter) {
        this.signatureAdapter = adapter;
        return (T)this;
    }
    
    protected static Verifiable get(JsonObject expanded, boolean issue /*FIXME hack remove */) throws DataError {
        // is a credential?
        if (Credential.isCredential(expanded)) {

            final JsonObject object = expanded.asJsonObject();

            // validate the credential object
            final Credential credential = Credential.from(object, issue);

            return credential;
        }

        // is a presentation?
        if (Presentation.isPresentation(expanded)) {

            final JsonObject object =expanded.asJsonObject();

            // validate the presentation object
            final Presentation presentation = Presentation.from(object, issue);

            return presentation;
        }

        // is not expanded JSON-LD object
        if (!JsonLdUtils.hasType(expanded)) {
            throw new DataError(ErrorType.Missing, Keywords.TYPE);
        }

        throw new DataError(ErrorType.Unknown, Keywords.TYPE);
    }
}
