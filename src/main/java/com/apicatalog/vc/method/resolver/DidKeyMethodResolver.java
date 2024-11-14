package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.key.DidKey;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.vc.proof.Proof;


@Deprecated
public class DidKeyMethodResolver implements VerificationKeyProvider {

    final DidKeyResolver resolver;

    public DidKeyMethodResolver(final DidKeyResolver resolver) {
        this.resolver = resolver;
    }

    public DidKeyMethodResolver(final MulticodecDecoder codecs) {
        this(new DidKeyResolver(codecs));
    }

    @Override
    public VerificationKey keyFor(Proof proof) throws DocumentError {

        final VerificationMethod method = proof.method();

        if (method == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethod");
        }

        URI uri = method.id();

        if (uri == null) {
            throw new DocumentError(ErrorType.Missing, "VerificationMethodId");
        }

        try {
            final DidDocument didDocument = resolver.resolve(DidUrl.of(uri));

            // FIXME must match purpose
            return didDocument
                    .verification().stream()
                    .filter(VerificationKey.class::isInstance)
                    .map(VerificationKey.class::cast)
                    .findFirst()
                    .orElseThrow(() -> new DocumentError(ErrorType.Unknown, "ProofVerificationMethod"));
        } catch (IllegalArgumentException e) {
            throw new DocumentError(e, ErrorType.Unknown, "ProofVerificationMethod");
        }
    }

//    @Override
//    public boolean isAccepted(URI uri) {
//        return DidKey.isDidKey(uri);
//    }
}
