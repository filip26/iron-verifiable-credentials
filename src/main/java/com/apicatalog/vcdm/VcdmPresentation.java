package com.apicatalog.vcdm;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;

public abstract class VcdmPresentation extends VcdmVerifiable implements Presentation {

    protected URI holder;

    protected Collection<Credential> credentials;

    protected LinkedFragment ld;

    protected static VcdmPresentation setup(VcdmPresentation presentation, LinkedFragment source) throws AdapterError {
        
        // @id
        presentation.id = source.uri();

        // holder
        presentation.holder = source.uri(VcdmVocab.HOLDER.uri());

        presentation.ld = source;
        return presentation;
    }

    @Override
    public LinkedNode ld() {
        return ld;
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }

    @Override
    public void validate() throws DocumentError {
        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }
    }

    @Override
    public URI holder() {
        return holder;
    }

    @Override
    public Collection<Credential> credentials() {
        return credentials;
    }

    public void credentials(Collection<Credential> credentials) {
        this.credentials = credentials;
    }
}
