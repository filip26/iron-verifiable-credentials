package com.apicatalog.vc.model;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

@FunctionalInterface
public interface ModelAdapterProvider {

    DocumentModelAdapter reader(JsonObject document) throws DocumentError;
}
