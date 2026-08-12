package com.apicatalog.vcdm.v2;

import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    static final String CONTEXT_URI = "https://www.w3.org/ns/credentials/v2";

    private VCDM2() {
        // protected, it's just container
    }

    public static boolean isDefined(SequencedCollection<?> context) {
        return !context.isEmpty()
                && CONTEXT_URI.equals(context.getFirst());
    }

//    public static Object read(String id, Map<String, Collection<String[]>> nodes) {
//        
//        
//        var types = new ArrayList<String>();
//        
//        var statements = nodes.get(id);
//        
//        for (var statement : statements) {
//            switch (statement[0]) {
//            case "#type":
//                types.add(statement[1]);
//                break;
//            }
//        }
//        
//        
//    }
    
    interface Credential {

        // mandatory

        Collection<String> type();

        // optional
        URI id();

        // returns lang-map
        Map<String, String> name();

        // returns lang-map
        Map<String, String> description();

        Instant validUntil();

        Instant validFrom();

        Object issuer();

        Object subject();

        default Collection<Object> status() {
            return List.of();
        }

        /**
         * Checks if the credential is expired.
         *
         * @return <code>true</code> if the credential is expired
         */
        default boolean isExpired() {
            return validUntil() != null && Instant.now().isAfter(validUntil());
        }

        /**
         * Checks if the credential is active, i.e. does not define validFrom property
         * or the property datetime is before now.
         * 
         * @since 0.90.0
         * 
         * @return <code>true</code> if the credential is active
         */
        default boolean isNotValidYet() {
            return validFrom() != null && validFrom().isAfter(Instant.now());
        }

    }

    public class GraphReader {

    }
}
