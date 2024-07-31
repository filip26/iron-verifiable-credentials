package com.apicatalog.vc.status;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.VerifiableObject;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.reader.DataObjectReader;

import jakarta.json.JsonObject;

public class StatusReader extends DataObjectReader {

//    @Override
//    protected Object newInstance(ModelVersion version, Object expanded) {
//        return new Status(version, expanded);
//    }

//    @Override
//    public Object read(ModelVersion version, Object expanded) throws DocumentError {
//        // TODO Auto-generated method stub
//        return null;
//    }

    @Override
    protected VerifiableObject newInstance(ModelVersion version, JsonObject expanded) {
        // TODO Auto-generated method stub
        return null;
    }
}
