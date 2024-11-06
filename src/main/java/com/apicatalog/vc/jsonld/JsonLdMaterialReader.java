package com.apicatalog.vc.jsonld;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableMaterialReader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue.ValueType;

public class JsonLdMaterialReader implements VerifiableMaterialReader {

    @Override
    public VerifiableMaterial read(
            final Collection<String> context,
            final JsonObject document,
            final DocumentLoader loader,
            final URI base) throws DocumentError {

        try {
            // expand the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .context(Json.createArrayBuilder(context).build())
                    .loader(loader)
                    .base(base).get();

            if (expanded == null
                    || expanded.size() != 1
                    || ValueType.OBJECT != expanded.iterator().next().getValueType()) {
                throw new DocumentError(ErrorType.Invalid, "Credential");
            }

            return new VerifiableMaterial(
                    context,
                    document,
                    expanded.iterator().next().asJsonObject());

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }
}
