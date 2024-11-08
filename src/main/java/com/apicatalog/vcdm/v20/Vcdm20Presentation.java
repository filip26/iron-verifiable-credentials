package com.apicatalog.vcdm.v20;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVersion;

@Fragment
@Term("VerifiablePresentation")
@Vocab("https://www.w3.org/2018/credentials#")
@Context("https://www.w3.org/ns/credentials/v2")
public interface Vcdm20Presentation extends VcdmVerifiable, Presentation {

    @Provided
    @Override
    Collection<Proof> proofs();

    @Provided
    @Override
    Collection<Credential> credentials();

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V20;
    }

    @Override
    default void validate() throws DocumentError {
        if (credentials() != null) {
            for (Credential credential : credentials()) {
                credential.validate();
            }
        }
    }
}
