package com.apicatalog.vcdm.v20;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.holder.PresentationHolder;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vcdm.DeprecatedVcdmPresentation;
import com.apicatalog.vcdm.VcdmVersion;

public class Vcdm20Presentation extends DeprecatedVcdmPresentation implements Presentation {

//    public static Presentation of(LinkedFragment source) throws NodeAdapterError {
//        return setup(new Vcdm20Presentation(), source);
//    }
//
//    @Override
//    public VcdmVersion version() {
//        return VcdmVersion.V20;
//    }

    @Override
    public URI id() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> type() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Proof> proofs() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LinkedNode ld() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PresentationHolder holder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Credential> credentials() {
        // TODO Auto-generated method stub
        return null;
    }
}
