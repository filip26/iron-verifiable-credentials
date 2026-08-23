package com.apicatalog.vcdm.v2;

import java.util.Collection;

import com.apicatalog.trust.Document;

public class Presentation {

    public static final String TYPE_URI = "https://www.w3.org/2018/credentials#VerifiablePresentation"; 
    public static final String TYPE_NAME = "VerifiablePresentation";

    public static final String PREDICATE_CREDENTIAL = "https://www.w3.org/2018/credentials#verifiableCredential";
    public static final String PREDICATE_HOLDER = "https://www.w3.org/2018/credentials#holder";
    public static final String PREDICATE_TERMS_OF_USE = "https://www.w3.org/2018/credentials#termsOfUse";
    
    public interface CredentialCursor {
        
        boolean next();
        
        Document.Accessor newAccessor();
        
    }
    
    private String id;
    private Collection<String> type;
    private Object holder;
    private Object termsOfUse;
    
    public CredentialCursor newCredentialCursor() {
        return null;
    }
    
}
