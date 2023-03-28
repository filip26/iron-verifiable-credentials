package com.apicatalog.vc.method;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.schema.LdProperty;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcTag;
import com.apicatalog.vc.suite.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

public class HttpMethodResolver implements MethodResolver {

    @Override
    public VerificationMethod resolve(URI id, DocumentLoader loader, SignatureSuite suite) throws DocumentError {

//        final LdProperty<VerificationMethod> property = suite.getSchema().tagged(VcTag.VerificationMethod.name());

        try {
            final JsonArray document = JsonLd.expand(id)
                    .loader(loader)
                    .context(suite.context()) // an optional expansion context
                    .get();

            for (final JsonValue method : document) {
                if (JsonUtils.isObject(method)) {
                    return suite.readMethod(method.asJsonObject());
                }
            }

        } catch (JsonLdError e) {
            throw new DocumentError(e, ErrorType.Invalid, "ProofVerificationMethod");
        }

        throw new DocumentError(ErrorType.Unknown, "ProofVerificationMethod");
    }

    @Override
    public boolean isAccepted(URI id) {
        return "http".equalsIgnoreCase(id.getScheme())
                || "https".equalsIgnoreCase(id.getScheme());
    }

}
