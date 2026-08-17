package com.apicatalog.trust.proof;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.apicatalog.trust.payload.CanonicalPayload;
import com.apicatalog.trust.signature.Signature;

/**
 * Represents a generic data integrity proof.
 * 
 * This interface serves as the foundational structure for all proofs, providing
 * the essential properties required to determine the intent, verification
 * method, and cryptographic authenticity of a payload.
 */
public interface Proof extends CanonicalPayload {

    /**
     * Represents the intended purpose of the proof, indicating why it was created
     * and for what purpose it should be verified.
     */
    public enum Purpose {

        /**
         * Used for authentication purposes, proving control over an identifier.
         */
        AUTHENTICATION("authentication"),

        /**
         * Used to assert a claim or statement.
         */
        ASSERTION("assertionMethod"),

        /**
         * Used for establishing a secure communication channel via key agreement.
         */
        KEY_AGREEMENT("keyAgreement"),

        /**
         * Used to invoke a capability.
         */
        CAPABILITY_INVOCATION("capabilityInvocation"),

        /**
         * Used to delegate a capability to another entity.
         */
        CAPABILITY_DELEGATION("capabilityDelegation");

        private final String key;
        private final String uri;

        private static final Map<String, Purpose> LOOKUP;

        static {
            Map<String, Purpose> map = HashMap.newHashMap(Purpose.values().length * 2);
            for (Purpose purpose : values()) {
                map.put(purpose.key, purpose);
                map.put(purpose.uri, purpose);
            }
            LOOKUP = Map.copyOf(map);
        }

        Purpose(String name) {
            this.key = name;
            this.uri = "https://w3id.org/security#" + (name.endsWith("Method") ? name : name + "Method");
        }

        /**
         * Retrieves the short string key identifying this purpose.
         *
         * @return the purpose key
         */
        public String key() {
            return key;
        }

        /**
         * Retrieves the full URI identifying this purpose.
         *
         * @return the purpose URI
         */
        public String uri() {
            return uri;
        }

        /**
         * Resolves a {@link Purpose} from its key or URI string representation.
         *
         * @param nameOrUri the string key or URI to resolve
         * @return the corresponding {@link Purpose}
         * @throws IllegalArgumentException if the input is null, blank, or unknown
         */
        public static Purpose from(String nameOrUri) {
            if (nameOrUri == null || nameOrUri.isBlank()) {
                throw new IllegalArgumentException("Proof purpose cannot be null or blank");
            }

            Purpose rel = LOOKUP.get(nameOrUri);

            if (rel == null) {
                throw new IllegalArgumentException("Unknown proof purpose: " + nameOrUri);
            }

            return rel;
        }
    }

    /**
     * Retrieves the specific type of the proof.
     *
     * @return a string identifying the proof type
     */
    String type();

    /**
     * Retrieves the cryptographic signature associated with this proof. If a
     * signature is present, the proof is considered signed and its authenticity can
     * be verified against the canonical representation.
     *
     * @return the {@link Signature} object, or {@code null} if the proof is
     *         unsigned
     */
    Signature signature();

    /**
     * Retrieves the identifier or URL required to independently verify the proof.
     * 
     * This typically points to the distributed identifier, cryptographic public
     * key, or verification material used to evaluate the signature. This property
     * is mandatory.
     *
     * @return a string representing the URL or identifier of the verification
     *         method
     */
    String verificationMethod();

    /**
     * Retrieves the intent behind the proof's creation.
     * 
     * This indicates the reason why an entity created the proof (e.g.,
     * assertionMethod or authentication). This property is mandatory.
     *
     * @return a URI identifying the proof purpose
     */
    Purpose purpose();

    /**
     * Retrieves the exact date and time the proof was created.
     * 
     * This property is mandatory.
     *
     * @return an {@link Instant} representing the creation timestamp
     */
    Instant created();

    /**
     * Checks whether all mandatory properties of the proof are present, excluding
     * the signature itself.
     *
     * @return {@code true} if all required properties are present, {@code false}
     *         otherwise
     */
    boolean hasRequired();

    /**
     * Checks whether the proof has expired according to its temporal properties and
     * the current system time.
     *
     * @return {@code true} if the proof is expired, {@code false} otherwise
     */
    boolean isExpired();

    /**
     * Checks whether the proof is post-dated (created in the future relative to the
     * current system time).
     *
     * @return {@code true} if the proof's creation time is in the future,
     *         {@code false} otherwise
     */
    default boolean isPostDated() {
        return created() != null && Instant.now().isBefore(created());
    }
}
