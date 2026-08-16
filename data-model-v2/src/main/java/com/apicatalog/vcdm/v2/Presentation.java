package com.apicatalog.vcdm.v2;

import java.util.Collection;

import com.apicatalog.trust.Document;

public class Presentation {

    public interface CredentialCursor {
        
        boolean next();
        
        Document.Accessor newAccessor();
        
    }
    
    private String id;
    private Collection<?> type;
    private Object holder;
    
    public CredentialCursor newCredentialCursor() {
        return null;
    }
    
}
