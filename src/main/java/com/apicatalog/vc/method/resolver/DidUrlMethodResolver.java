package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.document.DidVerificationMethod;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.multibase.MultibaseDecoder;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multikey.MultiKey;
import com.apicatalog.multikey.MultiKeyAdapter;
import com.apicatalog.vc.proof.Proof;

public class DidUrlMethodResolver implements MethodResolver {

    final DidResolver resolver;

    public DidUrlMethodResolver(MultibaseDecoder bases, MulticodecDecoder codecs) {
        resolver = new DidKeyResolver(bases, codecs);
    }

    @Override
    public VerificationMethod resolve(URI uri, DocumentLoader loader, Proof proof) throws DocumentError {
        try {

            final DidDocument didDocument = resolver.resolve(DidUrl.from(uri));

            return didDocument
                    .verificationMethod().stream()
                    .map(DidUrlMethodResolver::from)
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

    public static final MultiKey from(DidVerificationMethod did) {
        final MultiKey multikey = new MultiKey();
        multikey.setId(did.id().toUri());
        multikey.setController(did.controller().toUri());
        multikey.setAlgorithm(MultiKeyAdapter.getAlgorithmName(did.codec()));        
        multikey.setPublicKey(did.publicKey());
        return multikey;
    }
}
