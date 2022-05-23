package com.apicatalog.vc;

import java.util.Set;

public class ProofValue {

    private String value;
    private Set<String> type;
    
    public ProofValue(String value, Set<String> type) {
        this.value = value;
        this.type = type;
    }
    
    public String getValue() {
        return value;
    }
    
    public Set<String> getType() {
        return type;
    }
}
