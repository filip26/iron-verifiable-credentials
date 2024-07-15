package com.apicatalog.vc.status.reader;

import com.apicatalog.vc.status.Status;

import jakarta.json.JsonObject;

/**
 * An interface that must be implemented by a status reader.
 * 
 * @since 0.15.0
 */
public interface StatusReader {

    /**
     * Materializes a status represented in an expanded JSON-LD form.
     * 
     * @param object an expanded JSON-LD representing the status
     * @return materialized status instance
     */
    Status read(JsonObject object);

}
