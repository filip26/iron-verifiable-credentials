package com.apicatalog.vc;

import java.util.Map;

import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.di.DataIntegrityProof;
import com.apicatalog.vc.model.DocumentError;

@Fragment
@Context("https://w3id.org/security/data-integrity/v2")
@Vocab("https://w3id.org/security#")
@Term("DataIntegrityProof")
public interface TestDataIntegrityProof extends DataIntegrityProof {

    @Override
    default void validate(Map<String, Object> params) throws DocumentError {
//        DataIntegrityProof.assertNotNull(this::created, VcdiVocab.CREATED);
        DataIntegrityProof.super.validate(params);
    }
}
