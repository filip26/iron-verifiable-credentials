package com.apicatalog.vcdm;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.vc.Presentation;

@Fragment
@Term("VerifiablePresentation")
public interface VcdmPresentation extends VcdmVerifiable, Presentation {

//
//    protected static VcdmPresentation2 setup(VcdmPresentation2 presentation, LinkedFragment source) throws NodeAdapterError {
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

    @Override
    default void validate() throws DocumentError {
        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }
        // credentials
        if (credentials() == null || credentials().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.VERIFIABLE_CREDENTIALS);
        }
    }

//    public void credentials(Collection<Credential> credentials) {
//        this.credentials = credentials;
//    }
}
