package com.apicatalog.vcdm.v11;

import java.util.Collection;

import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vcdm.VcdmPresentation;
import com.apicatalog.vcdm.VcdmVersion;

@Fragment
@Term("VerifiablePresentation")
@Vocab("https://www.w3.org/2018/credentials#")
public interface Vcdm11Presentation extends VcdmPresentation {

    @Provided
    @Override
    Collection<Proof> proofs();

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V11;
    }
}
