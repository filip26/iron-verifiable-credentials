package com.apicatalog.ld.signature.method;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.schema.LdProperty;
import com.apicatalog.ld.schema.LdTag;
import com.apicatalog.ld.signature.SignatureSuite;

import jakarta.json.JsonArray;
import jakarta.json.JsonValue;

public class HttpMethodResolver implements MethodResolver {

    @Override
    public VerificationMethod resolve(URI id, DocumentLoader loader, SignatureSuite suite) throws DocumentError {

        try {
            final JsonArray document = JsonLd.expand(id)
                    .loader(loader)
                    .context(suite.getProofType().context()) // an optional expansion context
                    .get();

            for (final JsonValue method : document) {

                if (JsonUtils.isNotObject(method)) {
                    continue;
                }
                
                final Collection<String> types = JsonLdReader.getType(method.asJsonObject());
                
                if (types == null || types.isEmpty()) {
                    continue;
                }
                System.out.println(">> >> " + types);                
                final LdProperty<VerificationMethod> property = suite.getSchema().property(LdTag.VerificationMethod);

                return property.read(method);

//                // take the first method matching type
//                final MethodAdapter adapter = types.stream()
//                        .map(suite::getMethodAdapter)
//                        .filter(Objects::nonNull)
//                        .findFirst()
//                        .orElseThrow(() -> new DocumentError(ErrorType.Unknown, "VerificationMethod"));
//
//                return adapter.deserialize(method.asJsonObject());
                
            }

        } catch (JsonLdError e) {
            throw new DocumentError(ErrorType.Invalid, "VerificationMethod", e);
        }

        throw new DocumentError(ErrorType.Unknown, "VerificationMethod");        
    }

    @Override
    public boolean isAccepted(URI id) {
        System.out.println("!!! >>> " + id);
        return "http".equalsIgnoreCase(id.getScheme())
                || "https".equalsIgnoreCase(id.getScheme());
    }

}
