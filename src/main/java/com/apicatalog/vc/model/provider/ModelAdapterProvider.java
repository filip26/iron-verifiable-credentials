package com.apicatalog.vc.model.provider;

import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.adapter.DocumentModelAdapter;

import jakarta.json.JsonObject;

@FunctionalInterface
public interface ModelAdapterProvider {

    DocumentModelAdapter reader(JsonObject document) throws DocumentError;
}
