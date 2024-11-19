package com.apicatalog.vc.status.bitstring;

import com.apicatalog.ld.VocabTerm;

public final class BitstringVocab {

    public static final String VOCAB = "https://www.w3.org/ns/credentials/status#";
    
    public static final VocabTerm PURPOSE = VocabTerm.create("statusPurpose", VOCAB);
    public static final VocabTerm INDEX = VocabTerm.create("statusListIndex", VOCAB);
    public static final VocabTerm CREDENTIAL = VocabTerm.create("statusListCredential", VOCAB);
    
}
