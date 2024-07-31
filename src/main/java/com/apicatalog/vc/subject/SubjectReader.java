package com.apicatalog.vc.subject;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.vc.model.VerifiableObject;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.reader.DataObjectReader;

import jakarta.json.JsonObject;

public class SubjectReader extends DataObjectReader {

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

//    @Override
//    protected Subject newInstance(ModelVersion version, JsonObject expanded) {
//        return new Subject(version, expanded);
//    }

}
