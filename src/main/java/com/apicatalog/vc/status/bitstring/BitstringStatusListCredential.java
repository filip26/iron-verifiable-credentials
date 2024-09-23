package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vcdm.VcdmVocab;

public class BitstringStatusListCredential implements Linkable {

    protected URI id;
    protected Collection<String> type;

    protected Instant validFrom;
    protected Instant validUntil;

    protected LinkedFragment ld;

    protected BitstringStatusListCredential() {
        // protected
    }

    public static BitstringStatusListCredential of(LinkedFragment source) throws NodeAdapterError {
        var credential = new BitstringStatusListCredential();
        return setup(credential, source);
    }

    protected static BitstringStatusListCredential setup(BitstringStatusListCredential credential, LinkedFragment source) throws NodeAdapterError {

        credential.id = source.uri();
        credential.type = source.type().stream().toList();

        credential.validFrom = source.xsdDateTime(VcdmVocab.VALID_FROM.uri());
        credential.validUntil = source.xsdDateTime(VcdmVocab.VALID_UNTIL.uri());

        credential.ld = source;
        return credential;
    }

    public URI id() {
        return id;
    }

    public Collection<String> type() {
        return type;
    }

    @Override
    public LinkedNode ld() {
        return ld;
    }

    public boolean isExpired() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isNotValidYet() {
        // TODO Auto-generated method stub
        return false;
    }

    public BitstringStatusList subject() {
        // TODO Auto-generated method stub
        return null;
    }

    public Instant validFrom() {
        return validFrom;
    }

    public Instant validUntil() {
        return validUntil;
    }

//TODO attach do type adapter     @Override
    public void validate() throws DocumentError {

        if ((validFrom() != null
                && validUntil() != null
                && validFrom().isAfter(validUntil()))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }

}
