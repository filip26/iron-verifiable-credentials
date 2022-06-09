package com.apicatalog.vc.api;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;

public abstract class CommonApi<T extends CommonApi<T>> {

    protected DocumentLoader loader;
    protected boolean bundledContexts;
    protected URI base;

    protected CommonApi() {
        // default values
        this.loader = null;
        this.bundledContexts = true;
        this.base = null;
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
    public <T extends CommonApi> T useBundledContexts(boolean enable) {
        this.bundledContexts = enable;
        return (T)this;
    }
    
    /**
     * If set, this overrides the input document's IRI.
     * 
     * @param base
     * @return
     */
    public CommonApi base(URI base) {
       this.base = base;
       return this;
    }
}
