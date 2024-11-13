package com.apicatalog.vc.method.resolver;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.apicatalog.controller.ControllerDocument;
import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.resolver.ControllerResolver;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.proof.Proof;

public class ControllableKeyProvider implements VerificationKeyProvider {

    protected final Collection<ControllerResolver> resolvers;
    
    public ControllableKeyProvider(Collection<ControllerResolver> resolver) {
        Objects.nonNull(resolver);
        this.resolvers = resolver;
    }
    
    public static final ControllableKeyProvider of(ControllerResolver... resolver) {
        return new ControllableKeyProvider(List.of(resolver));
    }
    
    @Override
    public VerificationKey verificationKey(Proof proof) throws DocumentError {

        if (proof == null || proof.method() == null || proof.id() == null) {
            return null;
        }
        
        ControllerResolver resolver = resolvers.stream()
                .filter(r -> r.isAccepted(proof.method().id()))
                .findFirst()
                .orElseThrow();
        
        ControllerDocument controller = resolver.resolve(proof.method().id());
        
        if (controller == null) {
            //FIXME
        }
        
        if (controller.verification() == null || controller.verification().isEmpty()) {
            //FIXME
        }
        
        //FIXME controller + purpose
        
        //FIXME
        return controller
                .verification().stream()
                .filter(VerificationKey.class::isInstance)
                .map(VerificationKey.class::cast)
                .findFirst()
                .orElseThrow(() -> new DocumentError(ErrorType.Unknown, "ProofVerificationMethod"));
    }

}
