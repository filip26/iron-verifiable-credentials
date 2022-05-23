package com.apicatalog.vc.proof;

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

    /**
     * Checks is the proof is of the given type.
     * 
     * @param type
     * @return <code>true</code> if the given type matches the proof type
     */
    public boolean isTypeOf(String type) {
        return this.type != null && this.type.contains(type);
    }

}
