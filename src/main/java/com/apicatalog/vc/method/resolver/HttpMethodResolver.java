package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

public class HttpMethodResolver implements MethodResolver {

    @Override
    public VerificationMethod resolve(URI id, DocumentLoader loader, Proof proof) throws DocumentError {

        try {
            final JsonArray document = JsonLd.expand(id)
                    .loader(loader)
                    .context(proof.methodProcessor().context()) // an optional expansion context
                    .get();

            for (final JsonValue method : document) {
                if (JsonUtils.isObject(method)) {
                    return proof.methodProcessor().read(method.asJsonObject());
                }
            }

        } catch (JsonLdError e) {
            e.printStackTrace();
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
