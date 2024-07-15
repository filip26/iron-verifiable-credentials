package com.apicatalog.vc.status;

import java.net.URI;
import java.util.Collection;

public abstract class Status {

    protected URI id;
    protected Collection<String> type;
    
    protected Status() {
        /* protected */
    }

    public Collection<String> type() {
        return type;
    }
    
    /**
     * Verify the given credential status in an expanded JSON-LD form
     * 
//     * @param suite
     * @param credential
     * @throws DocumentError
     */
//    abstract void verify(
//            //SignatureSuite suite, 
//            //Credential credential
//            JsonValue value
//            ) throws DocumentError;

}
