package com.apicatalog.vc.method.resolver;

import java.util.Objects;

import com.apicatalog.controller.ControllerDocument;
import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.resolver.ControllerDocumentResolver;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.proof.Proof;

public class ControllableKeyProvider implements VerificationKeyProvider {

    protected final ControllerDocumentResolver resolver;
    
    public ControllableKeyProvider(ControllerDocumentResolver resolver) {
        Objects.nonNull(resolver);
        this.resolver = resolver;
    }
    
    @Override
    public VerificationKey verificationKey(Proof proof) throws DocumentError {

        if (proof == null || proof.method() == null || proof.id() == null) {
            return null;
        }
        
        ControllerDocument controller = resolver.resolve(proof.method().id());
        
        if (controller == null) {
            //FIXME
        }
        
        if (controller.verification() == null || controller.verification().isEmpty()) {
            //FIXME
        }
        
        //FIXME controller + purpose
        
        //FIXME
        return null;
    }

}
