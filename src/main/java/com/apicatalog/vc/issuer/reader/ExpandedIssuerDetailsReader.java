package com.apicatalog.vc.issuer.reader;

import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.reader.ExpandedObjectReader;

import jakarta.json.JsonObject;

public class ExpandedIssuerDetailsReader extends ExpandedObjectReader<IssuerDetails> {

    @Override
    protected IssuerDetails newInstance(ModelVersion version, JsonObject expanded) {
        return new IssuerDetails(version, expanded);
    }

    
}
