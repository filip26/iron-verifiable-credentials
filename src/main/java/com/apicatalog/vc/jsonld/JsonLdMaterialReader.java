package com.apicatalog.vc.jsonld;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.api.ExpansionApi;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.model.io.VerifiableMaterialReader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class JsonLdMaterialReader implements VerifiableMaterialReader {

    protected boolean keepContext;
    
    @Override
    public VerifiableMaterial read(
            final JsonObject document,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        JsonLdContext context = JsonLdContext.of(document);
        
        try {
            // expand the document
            final ExpansionApi expansion = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base);

            final JsonArray expanded = expansion.get();

            if (expanded != null && expanded.size() == 1) {

                final JsonValue item = expanded.iterator().next();

                if (JsonUtils.isObject(item)) {
                    
                    JsonObject compacted = document;
                    
                    if (compacted.containsKey(JsonLdKeyword.CONTEXT)) {
                        compacted = Json.createObjectBuilder(compacted).remove(JsonLdKeyword.CONTEXT).build();
                    }
                    
                    return new GenericMaterial(
                            context,
                            compacted,
                            item.asJsonObject());
                }
            }

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
        throw new DocumentError(ErrorType.Invalid, "Document");
    }
}
