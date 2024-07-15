package com.apicatalog.vc.subject.reader;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.reader.ExpandedReader;
import com.apicatalog.vc.subject.Subject;

import jakarta.json.JsonObject;

/**
 * An interface that must be implemented by a subject reader.
 * 
 * @since 0.15.0
 */

public interface SubjectReader extends ExpandedReader<Subject> {

    /**
     * Materializes a subject represented in an expanded JSON-LD form.
     * 
     * @param object an expanded JSON-LD representing the subject
     * @return materialized status instance
     * @throws DocumentError 
     */
    @Override
    Subject read(JsonObject object) throws DocumentError;
}
