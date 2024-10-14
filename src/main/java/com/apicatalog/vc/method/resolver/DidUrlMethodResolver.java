package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.did.resolver.DidResolver;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.multicodec.MulticodecDecoder;

public class DidUrlMethodResolver implements MethodResolver {

    final DidResolver resolver;
    final MulticodecDecoder codecs;

    public DidUrlMethodResolver(final MulticodecDecoder codecs) {
        this.resolver = new DidKeyResolver(codecs);
        this.codecs = codecs;
    }

    @Override
    public VerificationMethod resolve(URI uri) throws DocumentError {
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
        return DidUrl.isDidUrl(uri);
    }
}
