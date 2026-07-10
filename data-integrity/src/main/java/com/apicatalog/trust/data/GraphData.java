package com.apicatalog.trust.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A container for data that has been prepared for cryptographic
 * operations.
 *
 * <p>
 * This implementation maintains the original source document, the canonical
 * byte representation, and the canonicalization algorithm identifier. It
 * supports optional, thread-safe caching of cryptographic digests.
 * </p>
 */
//TODO make record
public class GraphData implements Data {

    private final Collection<String[]> data;
    private final String c14n;
    
//    private DigestiblePayload payload;
//    private Map<Collection<String>, DigestiblePayload> payloads;

    /**
     * Constructs a new {@code DigestibleDocument}.
     *
     * @param data         the original source document map
     * @param c14n             the canonicalization algorithm identifier
     * @throws NullPointerException if any argument is null
     */
    public GraphData(Collection<String[]> data, String c14n) {
        Objects.requireNonNull(data, "document must not be null");
        this.data = List.copyOf(data);
        this.c14n = c14n;
    }

    /**
     * Returns an unmodifiable view of the original source document.
     *
     * @return the source map
     */
    public Collection<String[]> data() {
        return data;
    }

//    @Override
//    public DigestiblePayload digestiblePayload(Collection<String> withProofs) {
//        if (withProofs.isEmpty()) {
//            return payload;
//        }
//        if (payloads != null) {
//            return payloads.get(withProofs);
//        }
//        return null;
//    }
    
    @Override
    public String c14n() {
        return c14n;
    }

//    @Override
//    public void digestiblePayload(Collection<String> withProofs, DigestiblePayload payload) {
//        if (withProofs.isEmpty()) {
//            this.payload = payload; 
//            return;
//        }        
//        if (payloads == null) {
//            payloads = new HashMap<>();
//        }
//        payloads.put(withProofs, payload);
//    }
}