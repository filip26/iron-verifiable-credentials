package com.apicatalog.jsonld;

import com.apicatalog.jsonld.loader.DocumentLoader;

import jakarta.json.JsonObject;

//TODO rename to JsonLdObjectAdapter
public interface JsonLdDocumentAdapter<T> {

    /**
     * Get supported JSON-LD object  <code>@type</code>.
     * 
     * @return the supported <code>@type</code>
     */
    String type();

    T deserialize(JsonObject object, DocumentLoader loader);

    JsonObject serialize(T document);
}
