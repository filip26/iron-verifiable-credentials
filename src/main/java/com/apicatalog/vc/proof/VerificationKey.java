package com.apicatalog.vc.proof;

import jakarta.json.JsonObject;

public class VerificationKey {

    private String id;
    private String type;
    private String controller;
    private String publicKeyMultibase;
   
    public static final VerificationKey from(JsonObject json) {

        final VerificationKey key = new VerificationKey();
        //FIXME better
        key.id = json.getString("id");
        key.type = json.getString("type");
        key.controller = json.getString("controller");
        key.publicKeyMultibase = json.getString("publicKeyMultibase");        
        return key;
    }
    
    public String getId() {
        return id;
    }
    
    public String getType() {
        return type;
    }

    public String getController() {
        return controller;
    }
    
    public String getPublicKeyMultibase() {
        return publicKeyMultibase;
    }
   
}
