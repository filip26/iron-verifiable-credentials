package com.apicatalog.vcdm.v20;

import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.lang.LangStringSelector;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmCredential;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

public class Vcdm20Credential extends VcdmCredential implements Credential {

    protected Instant validFrom;
    protected Instant validUntil;

    protected LangStringSelector name;
    protected LangStringSelector description;

    // TODO
    // relatedResource
    // confidenceMethod

    protected Vcdm20Credential() {
        // protected
    }

    public static Credential of(LinkedFragment source) throws NodeAdapterError {
        var credential = new Vcdm20Credential();
        VcdmCredential.setup(credential, source);
        return setup(credential, source);
    }

    protected static Vcdm20Credential setup(Vcdm20Credential credential, LinkedFragment source) throws NodeAdapterError {
        credential.validFrom = source.xsdDateTime(VcdmVocab.VALID_FROM.uri());
        credential.validUntil = source.xsdDateTime(VcdmVocab.VALID_UNTIL.uri());
        
        credential.name = source.languageMap(VcdmVocab.NAME.uri());
        credential.description = source.languageMap(VcdmVocab.DESCRIPTION.uri());
        
        return credential;
    }

    @Override
    public void validate() throws DocumentError {

        super.validate();
        
        for (Subject item : subject()) {
            if (item.ld().asFragment().terms().isEmpty()) {
                throw new DocumentError(ErrorType.Invalid, VcdmVocab.SUBJECT);
            }
            item.validate();
        }
        
        if ((validFrom() != null
                && validUntil() != null
                && validFrom().isAfter(validUntil()))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }

    @Override
    public boolean isExpired() {
        return (validUntil != null && validUntil.isBefore(Instant.now()));
    }

    @Override
    public boolean isNotValidYet() {
        return (validFrom != null && validFrom.isAfter(Instant.now()));
    }

    @Override
    public VcdmVersion version() {
        return VcdmVersion.V20;
    }

    /**
     * A date time from the credential is valid.
     * 
     * @return a date time
     */
    public Instant validFrom() {
        return validFrom;
    }

    /**
     * The date and time the credential ceases to be valid, which could be a date
     * and time in the past. Note that this value represents the latest point in
     * time at which the information associated with the credentialSubject property
     * is valid.
     * 
     * @return the date and time the credential ceases to be valid
     */
    public Instant validUntil() {
        return validUntil;
    }

    public LangStringSelector description() {
        return description;
    }

    public LangStringSelector name() {
        return name;
    }
}
