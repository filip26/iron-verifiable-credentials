package com.apicatalog.ld.signature.method;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.vc.VcTag;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

public class HttpMethodResolver implements MethodResolver {

    @Override
    public VerificationMethod resolve(URI id, DocumentLoader loader, SignatureSuite suite) throws DocumentError {

        final LdProperty<VerificationMethod> property = suite.getSchema().tagged(VcTag.VerificationMethod.name());

        try {
            final JsonArray document = JsonLd.expand(id)
                    .loader(loader)
                    .context(suite.getProofType().context()) // an optional expansion context
                    .get();

            for (final JsonValue method : document) {
                if (JsonUtils.isObject(method)) {
                    return property.read(method);
                }
            }

        } catch (JsonLdError e) {
            throw new DocumentError(e, ErrorType.Invalid, property.term());
        }

        throw new DocumentError(ErrorType.Unknown, property.term());
    }

    @Override
    public boolean isAccepted(URI id) {
        return "http".equalsIgnoreCase(id.getScheme())
                || "https".equalsIgnoreCase(id.getScheme());
    }

}
