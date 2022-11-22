package com.apicatalog.ld.signature.json;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.apicatalog.ld.signature.proof.ProofProperty;

public class EmbeddedProofProperty {

    protected static final Map<ProofProperty, String> EXPANDED_KEYS;

    static {
    	Map<ProofProperty, String> keys = new LinkedHashMap<>();
        keys.put(ProofProperty.Created, "http://purl.org/dc/terms/created");
        keys.put(ProofProperty.Purpose, "https://w3id.org/security#proofPurpose");
        keys.put(ProofProperty.VerificationMethod, "https://w3id.org/security#verificationMethod");
        keys.put(ProofProperty.Domain, "https://w3id.org/security#domain");
        keys.put(ProofProperty.Value, "https://w3id.org/security#proofValue");
        EXPANDED_KEYS = Collections.unmodifiableMap(keys);
    }

	public String expand(ProofProperty property) {
		return EXPANDED_KEYS.get(property);
	}
	
}
