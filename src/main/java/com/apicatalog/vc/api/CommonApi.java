package com.apicatalog.vc.api;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.signature.SignatureAdapter;

public abstract class CommonApi<T extends CommonApi<?>> {

    protected DocumentLoader loader;
    protected boolean bundledContexts;
    protected URI base;

    protected SignatureAdapter signatureAdapter;
    
    protected CommonApi() {
        // default values
        this.loader = null;
        this.bundledContexts = true;
        this.base = null;
        this.signatureAdapter = null;
    }

    public T loader(DocumentLoader loader) {
        this.loader = loader;
        return (T)this;
    }

    /**
     * Use well-known contexts that are bundled with the library instead of fetching it online.
     * <code>true</code> by default. Disabling might cause slower processing.
     *
     * @param enable
     * @return
     */
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
    public T base(URI base) {
       this.base = base;
       return (T)this;
    }

    public T signatureAdapter(SignatureAdapter adapter) {
        this.signatureAdapter = adapter;
        return (T)this;
    }
}
