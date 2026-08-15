package com.apicatalog.vcdm.v2;

import java.util.SequencedCollection;

/**
 * Represents a verifiable credential (VC).
 *
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model-2.0/#verifiable-credentials">v2.0</a>
 * 
 * @since 1.0.0
 */
public class VCDM2 {

    public static final String CONTEXT_URI = "https://www.w3.org/ns/credentials/v2";

    public static final String CREDENTIAL_TYPE_URI = "https://www.w3.org/2018/credentials#VerifiableCredential";

    private VCDM2() {
        // protected, it's just container
    }

    public static boolean isDefined(SequencedCollection<?> context) {
        return !context.isEmpty()
                && CONTEXT_URI.equals(context.getFirst());
    }
}
