package com.apicatalog.vc;

import java.util.Map;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vcdi.DataIntegrityProof;
import com.apicatalog.vcdi.VcdiVocab;

@Fragment
@Context("https://w3id.org/security/data-integrity/v2")
@Vocab("https://w3id.org/security#")
@Term("DataIntegrityProof")
public interface TestDataIntegrityProof extends DataIntegrityProof {

    @Override
    default void validate(Map<String, Object> params) throws DocumentError {
        DataIntegrityProof.assertNotNull(this::created, VcdiVocab.CREATED);
        DataIntegrityProof.super.validate(params);
    }
}
