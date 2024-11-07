package com.apicatalog.vcdm.v20;

import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.lang.LangStringSelector;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Literal;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.linkedtree.xsd.XsdDateTimeAdapter;
import com.apicatalog.vc.CredentialSchema;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmCredential;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

@Fragment
@Term("VerifiableCredential")
@Vocab("https://www.w3.org/2018/credentials#")
@Context("https://www.w3.org/ns/credentials/v2")
public interface Vcdm20Credential extends VcdmCredential {

    /**
     * A date time from the credential is valid.
     * 
     * @return a date time
     */
    @Literal(XsdDateTimeAdapter.class)
    Instant validFrom();

    /**
     * The date and time the credential ceases to be valid, which could be a date
     * and time in the past. Note that this value represents the latest point in
     * time at which the information associated with the credentialSubject property
     * is valid.
     * 
     * @return the date and time the credential ceases to be valid
     */
    @Literal(XsdDateTimeAdapter.class)
    Instant validUntil();

    @Term
    LangStringSelector description();

    @Term
    LangStringSelector name();

    // TODO
    // relatedResource
    // confidenceMethod

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V20;
    }

    @Override
    default boolean isExpired() {
        return (validUntil() != null && validUntil().isBefore(Instant.now()));
    }

    @Override
    default boolean isNotValidYet() {
        return (validFrom() != null && validFrom().isAfter(Instant.now()));
    }

    @Override
    default void validate() throws DocumentError {

        if (type() == null || type().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
        }

        if (issuer() != null) {
            issuer().validate();
        }

        if (subject() == null || subject().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.SUBJECT);
        }

        for (Subject subject : subject()) {
            subject.validate();
        }

        if ((validFrom() != null
                && validUntil() != null
                && validFrom().isAfter(validUntil()))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
        
        if (status() != null && !status().isEmpty()) {
            for (Status status : status()) {
                status.validate();
            }
        }
        
        if (schema() != null && !schema().isEmpty()) {
            for (CredentialSchema schema: schema()) {
                schema.validate();
            }
        }
    }
}
