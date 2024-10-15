package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.multicodec.MulticodecDecoder;

public class DidKeyMethodResolver implements MethodResolver {

    final DidKeyResolver resolver;

    public DidKeyMethodResolver(final DidKeyResolver resolver) {
        this.resolver = resolver;
    }

    public DidKeyMethodResolver(final MulticodecDecoder codecs) {
        this(new DidKeyResolver(codecs));
    }

    @Override
    public VerificationMethod resolve(URI uri, URI purpose) throws DocumentError {
        try {

            final DidDocument didDocument = resolver.resolve(DidUrl.of(uri));

            return didDocument
                    .verification().stream()
                    .findFirst()
                    .orElseThrow(() -> new DocumentError(ErrorType.Unknown, "ProofVerificationMethod"));
        } catch (IllegalArgumentException e) {
            throw new DocumentError(e, ErrorType.Unknown, "ProofVerificationMethod");
        }
    }

    @Override
    public boolean isAccepted(URI uri) {
        return DidKey.isDidKey(uri);
    }
}
