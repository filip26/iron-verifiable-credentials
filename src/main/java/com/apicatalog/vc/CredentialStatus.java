package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.lds.DataIntegrityError;
import com.apicatalog.lds.DataIntegrityError.ErrorType;

import jakarta.json.JsonValue;

/**
 * see {@link https://www.w3.org/TR/vc-data-model/#status}
 *
 */
public class CredentialStatus {

    private URI id;
    private String type;
    
    protected CredentialStatus() {
        
    }
    
    public static CredentialStatus from(final JsonValue object) throws DataIntegrityError {

        final CredentialStatus status = new CredentialStatus();
                
        if (!JsonLdUtils.hasType(object)) {
            throw new DataIntegrityError(ErrorType.Missing, "status", Keywords.TYPE);
        }
        
        status.id = JsonLdUtils.getId(object).orElseThrow(() -> new DataIntegrityError(ErrorType.Missing, "status", Keywords.ID));
        
        
        return status;
    }

    public URI getId() {
        return id;
    }

    public String getType() {
        return type;
    }    
}
