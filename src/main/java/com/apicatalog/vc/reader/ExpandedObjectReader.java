package com.apicatalog.vc.reader;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.ModelVersion;

import jakarta.json.JsonObject;

/**
 * 
 * @since 0.15.0
 */

public interface ExpandedObjectReader<T> {

    T read(ModelVersion version, JsonObject expanded) throws DocumentError;
}
