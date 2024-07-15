package com.apicatalog.vc.status.reader;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.reader.ExpandedReader;
import com.apicatalog.vc.status.Status;

import jakarta.json.JsonObject;

/**
 * An interface that must be implemented by a status reader.
 * 
 * @since 0.15.0
 */
public interface StatusReader extends ExpandedReader<Status> {

    /**
     * Materializes a status represented in an expanded JSON-LD form.
     * 
     * @param object an expanded JSON-LD representing the status
     * @return materialized status instance
     * @throws DocumentError 
     */
    @Override
    Status read(JsonObject object) throws DocumentError;

}
