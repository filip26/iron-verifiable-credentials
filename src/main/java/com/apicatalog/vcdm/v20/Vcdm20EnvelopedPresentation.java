package com.apicatalog.vcdm.v20;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.holder.PresentationHolder;
import com.apicatalog.vc.proof.Proof;

public class Vcdm20EnvelopedPresentation implements Presentation {

    protected URI id;

    protected LinkedFragment ld;

    protected Vcdm20EnvelopedPresentation() {
        // protected
    }

    public static Presentation of(LinkedFragment source) throws NodeAdapterError {
        var presentation = new Vcdm20EnvelopedPresentation();

        // @id
        presentation.id = source.uri();

        presentation.ld = source;

        return presentation;
    }

    @Override
    public URI id() {
        return id;
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }

    @Override
    public Collection<Proof> proofs() {
        return Collections.emptyList();
    }

    @Override
    public PresentationHolder holder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Credential> credentials() {
        return Collections.emptyList();
    }
}
