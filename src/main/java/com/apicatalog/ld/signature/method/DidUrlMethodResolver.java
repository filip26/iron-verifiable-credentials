package com.apicatalog.ld.signature.method;

import java.net.URI;

import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.key.DidKeyResolver;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;

public class DidUrlMethodResolver implements MethodResolver {

    final DidResolver resolver;

    public DidUrlMethodResolver() {
        //TODO
        resolver = new DidKeyResolver();
    }
    
    @Override
    public VerificationMethod resolve(URI uri) {
        
        final DidDocument didDocument = resolver.resolve(DidUrl.from(uri));

        //FIXME
        return null;
//        return didDocument
//                .verificationMethod().stream()
//                .filter(vm -> keyAdapter.isSupportedType(vm.type()))
//                .map(did -> new VerificationKeyImpl(
//                        did.id().toUri(),
//                        did.controller().toUri(),
//                        did.type(),
//                        did.publicKey()))
//                .findFirst()
//                .orElse(null);  //FIXME
                //.orElseThrow(() -> new VerificationError(Code.UnknownVerificationKey));
    }

    @Override
    public boolean isAccepted(URI uri) {
        return DidUrl.isDidUrl(uri);
    }

}
