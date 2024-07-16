package com.apicatalog.vc.subject;

import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.reader.ExpandedObjectReader;

import jakarta.json.JsonObject;

public class SubjectReader extends ExpandedObjectReader<Subject> {

    @Override
    protected Subject newInstance(ModelVersion version, JsonObject expanded) {
        return new Subject(version, expanded);
    }

}
