package com.apicatalog.vc.method;

import java.util.List;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.controller.method.VerificationMethodProvider;
import com.apicatalog.controller.resolver.ControllerResolver;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.proof.Proof;

public class ControllableKeyProvider implements VerificationKeyProvider {

    protected final VerificationMethodProvider methodProvider;

    public ControllableKeyProvider(VerificationMethodProvider methodProvider) {
        this.methodProvider = methodProvider;
    }

    public static final ControllableKeyProvider of(ControllerResolver... resolver) {
        return new ControllableKeyProvider(
                new VerificationMethodProvider(
                        List.of(resolver)));
    }

    @Override
    public VerificationKey keyFor(Proof proof) throws DocumentError {

        if (proof == null || proof.method() == null || proof.method().id() == null) {
            return null;
        }

        VerificationMethod method = methodProvider.retrieve(proof.method().id(), proof.purpose());

        if (method instanceof VerificationKey key) {
            return key;
        }
        
        throw new DocumentError(ErrorType.Unknown, "ProofVerificationMethod");
    }

}
