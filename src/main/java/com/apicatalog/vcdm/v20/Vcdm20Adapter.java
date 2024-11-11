package com.apicatalog.vcdm.v20;

import java.util.Collection;

import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vcdm.CredentialAdapter;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.io.VcdmAdapter;

public class Vcdm20Adapter extends VcdmAdapter {

    public Vcdm20Adapter(JsonLdTreeReader reader, CredentialAdapter credentialAdapter, ProofAdapter proofMaterializer) {
        super(reader, credentialAdapter, proofMaterializer);
    }

    @Override
    protected boolean isCredential(Collection<String> types) {
        return super.isCredential(types) || types.contains(VcdmVocab.ENVELOPED_CREDENTIAL_TYPE.uri());
    }

    @Override
    protected boolean isPresentation(Collection<String> types) {
        return super.isPresentation(types) || types.contains(VcdmVocab.ENVELOPED_PRESENTATION_TYPE.uri());
    }
}