package com.apicatalog.vc.model;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

public interface DocumentModelAdapter extends DocumentAdapter {

    DocumentModel read(
            JsonObject document,
            DocumentLoader loader,
            URI base) throws DocumentError;
}
