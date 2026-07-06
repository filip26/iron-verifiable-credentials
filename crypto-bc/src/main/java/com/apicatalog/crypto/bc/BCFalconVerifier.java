package com.apicatalog.crypto.bc;

import org.bouncycastle.pqc.crypto.falcon.FalconParameters;
import org.bouncycastle.pqc.crypto.falcon.FalconPublicKeyParameters;
import org.bouncycastle.pqc.crypto.falcon.FalconSigner;

class BCFalconVerifier {
    private static final BCFalconVerifier FALCON_512_VERIFIER = new BCFalconVerifier(FalconParameters.falcon_512);

    private final FalconParameters parameters;

    public BCFalconVerifier(FalconParameters parameters) {
        this.parameters = parameters;
    }

    public static BCFalconVerifier get512Instance() {
        return FALCON_512_VERIFIER;
    }

    public boolean verify( byte[] publicKey, final byte[] data, final byte[] signature) {
        var verifier = new FalconSigner();
        
        System.out.println(publicKey.length);
//        FalconPublicKeyParameters pub =
//                new FalconPublicKeyParameters(
//                        FalconParameters.falcon_512,
//                        publicKey
//                );
        
        if (publicKey.length == 898 && publicKey[0] == 0x00) {
           
            publicKey = java.util.Arrays.copyOfRange(publicKey, 1, publicKey.length);
        }

     // Verification is deterministic; the random seed is ignored for init(false, ...).
        // If this method were performing signing (init(true, ...)),
        // use: verifier.init(true, new ParametersWithRandom(privateKey, random));
        
        FalconPublicKeyParameters pubKey =
                new FalconPublicKeyParameters(parameters, publicKey);


//        // NIST KAT -> BC encoding
//        byte[] bcSignature = new byte[signature.length + 1];
//        bcSignature[0] = 0x20; // Falcon-512
//        System.arraycopy(signature, 0, bcSignature, 1, signature.length);

        
//        var pubKeyParams = new FalconPublicKeyParameters(parameters, publicKey);

        verifier.init(false, pubKey);

        return verifier.verifySignature(data, signature);
    }
}
