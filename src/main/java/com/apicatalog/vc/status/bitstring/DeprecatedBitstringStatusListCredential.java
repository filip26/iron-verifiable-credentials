package com.apicatalog.vc.status.bitstring;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vcdm.VcdmVocab;

public class DeprecatedBitstringStatusListCredential {

    protected URI id;
    protected Collection<String> type;

    protected Instant validFrom;
    protected Instant validUntil;

    protected BitstringStatusList subject;
    
    protected DeprecatedBitstringStatusListCredential() {
        // protected
    }

    public static DeprecatedBitstringStatusListCredential of(LinkedFragment source) throws NodeAdapterError {
        var credential = new DeprecatedBitstringStatusListCredential();
        return setup(credential, source);
    }

    protected static DeprecatedBitstringStatusListCredential setup(DeprecatedBitstringStatusListCredential credential, LinkedFragment source) throws NodeAdapterError {

        credential.id = source.uri();
        credential.type = source.type().stream().toList();

        credential.validFrom = source.xsdDateTime(VcdmVocab.VALID_FROM.uri());
        credential.validUntil = source.xsdDateTime(VcdmVocab.VALID_UNTIL.uri());

        credential.subject = source.fragment(
                VcdmVocab.SUBJECT.uri(), 
                BitstringStatusList.class, 
                BitstringStatusList::of);

        return credential;
    }

    public URI id() {
        return id;
    }

    public Collection<String> type() {
        return type;
    }

    public boolean isExpired() {
        return (validUntil != null && validUntil.isBefore(Instant.now()));
    }

    public boolean isNotValidYet() {
        return (validFrom != null && validFrom.isAfter(Instant.now()));
    }

    public BitstringStatusList subject() {
        return subject;
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
