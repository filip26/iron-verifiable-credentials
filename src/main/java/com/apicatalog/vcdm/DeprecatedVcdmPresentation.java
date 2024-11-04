package com.apicatalog.vcdm;

import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.holder.PresentationHolder;
import com.apicatalog.vc.holder.PresentationHolderReference;

public abstract class DeprecatedVcdmPresentation  {

    protected PresentationHolder holder;

    protected Collection<Credential> credentials;

    protected LinkedFragment ld;

//    protected static VcdmPresentation setup(VcdmPresentation presentation, LinkedFragment source) throws NodeAdapterError {
//
//        // @id
//        presentation.id = source.uri();
//
//        // holder
//        presentation.holder = source.fragment(
//                VcdmVocab.HOLDER.uri(), 
//                PresentationHolder.class,
//                PresentationHolderReference::of             
//                );
//
//        presentation.ld = source;
//        return presentation;
//    }
//
//    @Override
//    public LinkedNode ld() {
//        return ld;
//    }
//
//    @Override
//    public Collection<String> type() {
//        return ld.type().stream().toList();
//    }
//
//    @Override
//    public void validate() throws DocumentError {
//        // @type - mandatory
//        if (type() == null || type().isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
//        }
//        // credentials
//        if (credentials == null || credentials.isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, VcdmVocab.VERIFIABLE_CREDENTIALS);
//        }
//    }
//
//    @Override
//    public PresentationHolder holder() {
//        return holder;
//    }
//
//    @Override
//    public Collection<Credential> credentials() {
//        return credentials;
//    }

    public void credentials(Collection<Credential> credentials) {
        this.credentials = credentials;
    }
}
