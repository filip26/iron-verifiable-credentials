package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonArray;

public class HttpMethodResolver implements MethodResolver {

    @Override
    public VerificationMethod resolve(URI id, DocumentLoader loader, Proof proof) throws DocumentError {

        try {
            final JsonArray document = JsonLd.expand(id)
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .context(proof.methodProcessor().context()) // an optional expansion context
                    .get();

            final JsonLdTreeReader reader = JsonLdTreeReader
                    .with(proof.methodProcessor());

            final Linkable tree = reader.readExpanded(document).cast();

            if (tree instanceof VerificationMethod method) {
                return method;
            }

        } catch (DocumentError e) {
            throw e;

        } catch (Exception e) {
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
