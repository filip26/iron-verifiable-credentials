package com.apicatalog.vc.status;

import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.reader.ExpandedObjectReader;

import jakarta.json.JsonObject;

public class StatusReader extends ExpandedObjectReader<Status> {

    @Override
    protected Status newInstance(ModelVersion version, JsonObject expanded) {
        return new Status(version, expanded);
    }
}
