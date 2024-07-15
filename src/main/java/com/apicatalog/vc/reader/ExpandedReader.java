package com.apicatalog.vc.reader;

import com.apicatalog.ld.DocumentError;

import jakarta.json.JsonObject;

/**
 * 
 * @since 0.15.0
 */

public interface ExpandedReader<T> {

    T read(JsonObject expanded) throws DocumentError;
}
