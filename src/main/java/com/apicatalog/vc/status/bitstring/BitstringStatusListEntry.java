package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.vc.model.ModelAssertions;
import com.apicatalog.vc.status.Status;

@Fragment
@Vocab(BitstringVocab.VOCAB)
public interface BitstringStatusListEntry extends Status {

    @Term("statusPurpose")
    String purpose();

    @Term("statusListIndex")
    long index();

    @Term("statusListCredential")
    URI credential();

    @Term("statusSize")
    int indexBitLength();

    @Term("statusMessage")
    BitstringStatusListMessages messages();

    @Term("statusReference")
    Collection<URI> references();
    
    @Override
    default void validate() throws DocumentError {
        Status.super.validate();
        
        ModelAssertions.assertNotNull(this::purpose, BitstringVocab.PURPOSE);
        ModelAssertions.assertNotNull(this::credential, BitstringVocab.CREDENTIAL);
        
        if (index() <= 0) {
            throw new DocumentError(ErrorType.Invalid, BitstringVocab.INDEX);
        }
    }
}
