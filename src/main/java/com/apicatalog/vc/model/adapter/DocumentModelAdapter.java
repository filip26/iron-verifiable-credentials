package com.apicatalog.vc.model.adapter;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;

import jakarta.json.JsonObject;

public interface DocumentModelAdapter extends DocumentAdapter {

    DocumentModel read(
            JsonObject document,
            DocumentLoader loader,
            URI base) throws DocumentError;
}
