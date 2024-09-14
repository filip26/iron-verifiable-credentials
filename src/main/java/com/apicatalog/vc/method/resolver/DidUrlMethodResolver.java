package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.document.DidVerificationMethod;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multikey.MultiKey;
import com.apicatalog.multikey.MultiKeyAdapter;

public class DidUrlMethodResolver implements MethodResolver {

    final DidResolver resolver;
    final MulticodecDecoder codecs;

    public DidUrlMethodResolver(final MultibaseDecoder bases, final MulticodecDecoder codecs) {
        this.resolver = new DidKeyResolver(bases);
        this.codecs = codecs;
    }

    @Override
    public VerificationMethod resolve(URI uri) throws DocumentError {
        try {

            final DidDocument didDocument = resolver.resolve(DidUrl.from(uri));

            return didDocument
                    .verificationMethod().stream()
                    .map(did -> from(did, codecs))
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

    public static final MultiKey from(DidVerificationMethod did, final MulticodecDecoder codecs) {
        final MultiKey multikey = new MultiKey();
        multikey.id(did.id().toUri());
        multikey.controller(did.controller().toUri());
        
        //TODO improve
        final Multicodec codec = codecs.getCodec(did.publicKey()).orElseThrow(IllegalArgumentException::new);
        
        if (!codec.isEncoded(did.publicKey())) {
            throw new IllegalArgumentException("Unsupported key encoding [" + did + "].");
        }

        multikey.algorithm(MultiKeyAdapter.getAlgorithmName(codec));
        multikey.publicKey(codec.decode(did.publicKey()));
        return multikey;
    }
}
