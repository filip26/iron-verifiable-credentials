package com.apicatalog.vcdm.v20;

import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.lang.LangStringSelector;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.model.CredentialSchema;
import com.apicatalog.vc.model.Evidence;
import com.apicatalog.vc.model.RefreshService;
import com.apicatalog.vc.model.TermsOfUse;
import com.apicatalog.vc.status.Status;
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
        
        credential.name = source.langMap(VcdmVocab.NAME.uri());
        credential.description = source.langMap(VcdmVocab.DESCRIPTION.uri());
        
        return credential;
    }

    @Override
    public void validate() throws DocumentError {

        // @type - mandatory
        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }

        // subject - mandatory
        if (subject() == null || subject().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.SUBJECT);
        }
        for (Subject item : subject()) {
            if (item.ld().asFragment().terms().isEmpty()) {
                throw new DocumentError(ErrorType.Invalid, VcdmVocab.SUBJECT);
            }
            item.validate();
        }

        // issuer
        if (issuer() == null) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUER);
        }
        if (issuer().id() == null) {
            throw new DocumentError(ErrorType.Missing, "IssuerId");
        }

        // status
        if (status() != null) {
            for (final Status item : status()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "StatusType");
                }
                item.validate();
            }
        }

        if (termsOfUse() != null) {
            for (final TermsOfUse item : termsOfUse()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "TermsOfUseType");
                }
            }
        }

        if (evidence() != null) {
            for (final Evidence item : evidence()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "EvidenceType");
                }
            }
        }

        if (refreshService() != null) {
            for (final RefreshService item : refreshService()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "RefreshServiceType");
                }
            }
        }

        if (schema() != null) {
            for (final CredentialSchema item : schema()) {
                if (item.type() == null || item.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "CredentialSchemaType");
                }
            }
        }

//        if ((issuanceDate() != null
//                && expiration() != null
//                && issuanceDate().isAfter(expiration()))) {
//            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
//        }
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
