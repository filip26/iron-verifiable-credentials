package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.vc.integrity.DataIntegrityKeyPair;
import com.apicatalog.vc.model.Proof;
import com.apicatalog.vc.suite.SignatureSuite;

public class DidUrlMethodResolver implements MethodResolver {

    final DidResolver resolver;

    public DidUrlMethodResolver() {
        resolver = new DidKeyResolver();
    }

    @Override
    public VerificationMethod resolve(URI uri, DocumentLoader loader, Proof proof) throws DocumentError {

        final DidDocument didDocument = resolver.resolve(DidUrl.from(uri));

        return didDocument
                .verificationMethod().stream()
//TODO                .filter(vm -> keyAdapter.isSupportedType(vm.type()))                
                .map(did -> DataIntegrityKeyPair.createVerificationKey(
                        did.id().toUri(),
                        did.controller().toUri(),
                        URI.create(did.type()), // TODO did.type should return URI
                        did.publicKey()))
                .findFirst()
                .orElseThrow(() -> new DocumentError(ErrorType.Unknown, "ProofVerificationMethod"));
    }

    @Override
    public boolean isAccepted(URI uri) {
        return DidUrl.isDidUrl(uri);
    }

}
