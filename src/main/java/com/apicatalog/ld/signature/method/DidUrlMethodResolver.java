package com.apicatalog.ld.signature.method;

import java.net.URI;

import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.SignatureSuite;
import com.apicatalog.vc.VcSchemaTag;
import com.apicatalog.vc.integrity.DataIntegrityKeyPair;

public class DidUrlMethodResolver implements MethodResolver {

    final DidResolver resolver;

    public DidUrlMethodResolver() {
        //TODO
        resolver = new DidKeyResolver();
    }
    
    @Override
    public VerificationMethod resolve(URI uri, DocumentLoader loader, SignatureSuite suite) throws DocumentError {
        
        final DidDocument didDocument = resolver.resolve(DidUrl.from(uri));

        return didDocument
                .verificationMethod().stream()
//TODO                .filter(vm -> keyAdapter.isSupportedType(vm.type()))                
                .map(did -> DataIntegrityKeyPair.createVerificationKey(
                        did.id().toUri(),
                        did.controller().toUri(),
                        URI.create(did.type()), //TODO did.type should return URI
                        did.publicKey()))
                .findFirst()
                .orElseThrow(() -> new DocumentError(ErrorType.Unknown, suite.getSchema().property(VcSchemaTag.VerificationMethod.name()).term()));
    }

    @Override
    public boolean isAccepted(URI uri) {
        return DidUrl.isDidUrl(uri);
    }

}
