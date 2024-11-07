package com.apicatalog.vcdm.v20;

import java.util.Collection;
import java.util.Collections;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.holder.PresentationHolder;
import com.apicatalog.vc.proof.Proof;

@Fragment
@Term("EnvelopedCredential")
@Context("https://www.w3.org/ns/credentials/v2")
@Vocab("https://www.w3.org/2018/credentials#")
public interface Vcdm20EnvelopedPresentation extends Presentation {

    @Override
    default Collection<Proof> proofs() {
        return Collections.emptyList();
    }

    @Override
    default PresentationHolder holder() {
        throw new UnsupportedOperationException();
    }

    @Override
    default Collection<Credential> credentials() {
        return Collections.emptyList();
    }

    @Override
    default void validate() throws DocumentError {
        throw new UnsupportedOperationException();
    }
}
