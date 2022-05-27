package com.apicatalog.lds;

import jakarta.json.JsonObject;

public class KeyPair {

    private String id;
    private String type;
    private String controller;
    private String publicKeyMultibase;
    private String privateKeyMultibase;
   
    public static final KeyPair from(JsonObject json) {

        final KeyPair key = new KeyPair();
        //FIXME better
        key.id = json.getString("id");
        key.type = json.getString("type");
        key.controller = json.getString("controller");
        key.publicKeyMultibase = json.getString("publicKeyMultibase");        
        key.privateKeyMultibase = json.getString("publicKeyMultibase");
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

    public String getPrivateKeyMultibase() {
        return privateKeyMultibase;
    }
   
}
