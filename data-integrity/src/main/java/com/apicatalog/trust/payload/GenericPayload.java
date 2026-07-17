package com.apicatalog.trust.payload;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A generic container for data that has been prepared for cryptographic
 * operations.
 *
 * <p>
 * This implementation maintains the canonical byte representation, and the
 * canonicalization algorithm identifier. It supports optional, thread-safe
 * caching of cryptographic digests.
 * </p>
 */
//TODO deprecate? no need for DigestiblePayload be an interface now, documents inherit 'Data'
public class GenericPayload implements DigestiblePayload {

    private final byte[] canonicalPayload;

    /**
     * Lazily initialized cache for cryptographic digests.
     */
    private volatile Map<String, byte[]> digests;

    /**
     * Constructs a new {@code GenericPayload}.
     *
     * @param canonicalPayload the canonical byte array of the document
     * @param c14n             the canonicalization algorithm identifier
     * @throws NullPointerException if any argument is null
     */
    public GenericPayload(byte[] canonicalPayload) {
        Objects.requireNonNull(canonicalPayload, "canonicalPayload must not be null");
        this.canonicalPayload = canonicalPayload;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns the canonical bytes.
     * </p>
     */
    @Override
    public byte[] canonicalPayload() {
        return canonicalPayload;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Stores the provided digest in a thread-safe cache.
     * </p>
     *
     * @param algorithm the cryptographic hash algorithm
     * @param value     the computed digest value to cache
     */
    @Override
    public void digest(String algorithm, byte[] value) {
        if (value == null) {
            return;
        }

        if (digests == null) {
            synchronized (this) {
                if (digests == null) {
                    digests = new ConcurrentHashMap<>(2);
                }
            }
        }
        digests.put(algorithm, value.clone());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Retrieves a defensive copy of the cached digest if available.
     * </p>
     *
     * @param algorithm the cryptographic hash algorithm
     * @return the cached digest, or {@code null} if not found
     */
    @Override
    public byte[] digest(String algorithm) {
        if (digests == null) {
            return null;
        }

        byte[] value = digests.get(algorithm);
        return value != null ? value.clone() : null;
    }

    @Override
    public Collection<String> digestAlgorithms() {
        if (digests == null) {
            return Set.of();
        }
        return Set.copyOf(digests.keySet());
    }

    @Override
    public String c14n() {
        // TODO Auto-generated method stub
        return null;
    }
}
