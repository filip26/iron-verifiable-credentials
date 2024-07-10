package com.apicatalog.vc.status;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonValue;

public abstract class Status {

    
    
    /**
     * Verify the given credential status in an expanded JSON-LD form
     * 
//     * @param suite
     * @param credential
     * @throws DocumentError
     */
    abstract void verify(
            //SignatureSuite suite, 
            //Credential credential
            JsonValue value
            ) throws DocumentError;

}
