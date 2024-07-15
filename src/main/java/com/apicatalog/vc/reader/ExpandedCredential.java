package com.apicatalog.vc.reader;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.VcVocab;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class ExpandedCredential extends Credential {

    protected JsonObject expanded;
    
    protected ExpandedCredential(ModelVersion version, JsonObject expanded) {
        super(version);
        this.expanded = expanded;
    }
    
    public void id(URI id) {
        this.id = id;
    }
    
    public void type(Collection<String> type) {
        this.type = type;
    }
    
    public JsonObject expand() {
        
        final LdNodeBuilder builder = new LdNodeBuilder(Json.createObjectBuilder(expanded));
        
        if (issuance != null) {
            builder.set(VcVocab.ISSUANCE_DATE).xsdDateTime(issuance);
        }
        
        if (expiration != null) {
            builder.set(VcVocab.EXPIRATION_DATE).xsdDateTime(expiration);
        }
        
        if (validFrom != null) {
            builder.set(VcVocab.VALID_FROM).xsdDateTime(validFrom);
        }
        
        if (validUntil != null) {
            builder.set(VcVocab.VALID_UNTIL).xsdDateTime(validUntil);
        }
        
        return builder.build();
    }
}
