package com.apicatalog.vcdm.v20;

import java.util.Collection;
import java.util.Collections;

import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.proof.Proof;

@Fragment
@Term("EnvelopedVerifiableCredential")
@Context("https://www.w3.org/ns/credentials/v2")
@Vocab("https://www.w3.org/2018/credentials#")
public interface Vcdm20EnvelopedCredential extends Credential {

    @Override
    default Collection<Proof> proofs() {
        return Collections.emptyList();
    }

    @Override
    default void validate() throws DocumentError {        
        if (id() == null) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.ID);
        }

        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }
        
        if (!id().getScheme().equals("data")) {
            throw new DocumentError(ErrorType.Invalid, "IdScheme");
        }
    }
}
