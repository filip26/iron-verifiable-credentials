package com.apicatalog.vcdm.v20;

import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.lang.LocalizedString;
import com.apicatalog.linkedtree.orm.Adapter;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.linkedtree.xsd.XsdDateTimeAdapter;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.CredentialSchema;
import com.apicatalog.vc.Evidence;
import com.apicatalog.vc.RefreshService;
import com.apicatalog.vc.Subject;
import com.apicatalog.vc.TermsOfUse;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

@Fragment
@Term("VerifiableCredential")
@Vocab("https://www.w3.org/2018/credentials#")
@Context("https://www.w3.org/ns/credentials/v2")
public interface Vcdm20Credential extends VcdmVerifiable, Credential {

    /**
     * A date time from the credential is valid.
     * 
     * @return a date time
     */
    @Adapter(XsdDateTimeAdapter.class)
    Instant validFrom();

    /**
     * The date and time the credential ceases to be valid, which could be a date
     * and time in the past. Note that this value represents the latest point in
     * time at which the information associated with the credentialSubject property
     * is valid.
     * 
     * @return the date and time the credential ceases to be valid
     */
    @Adapter(XsdDateTimeAdapter.class)
    Instant validUntil();

    @Term
    LocalizedString description();

    @Term
    LocalizedString name();

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

        if (issuer() == null) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUER);
        }

        if (issuer().id() == null) {
            throw new DocumentError(ErrorType.Missing, "IssuerId");
        }

        issuer().validate();

        if (subject() == null || subject().isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.SUBJECT);
        }

        for (Subject subject : subject()) {
            if (subject.id() == null && !subject.hasClaims()) {
                throw new DocumentError(ErrorType.Missing, "CredentialSubjectClaims");
            }
            subject.validate();
        }

        if ((validFrom() != null
                && validUntil() != null
                && validFrom().isAfter(validUntil()))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }

        if (status() != null && !status().isEmpty()) {
            for (Status status : status()) {
                if (status.type() == null || status.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "StatusType");
                }
                status.validate();
            }
        }

        if (schema() != null && !schema().isEmpty()) {
            for (CredentialSchema schema : schema()) {
                if (schema.id() == null) {
                    throw new DocumentError(ErrorType.Missing, "SchemaId");
                }
                if (schema.type() == null || schema.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "SchemaType");
                }
                schema.validate();
            }
        }

        if (refreshService() != null && !refreshService().isEmpty()) {
            for (RefreshService refreshService : refreshService()) {
                if (refreshService.type() == null || refreshService.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "RefreshServiceType");
                }
                refreshService.validate();
            }
        }

        if (evidence() != null && !evidence().isEmpty()) {
            for (Evidence evidence : evidence()) {
                if (evidence.type() == null || evidence.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "EvidenceType");
                }
                evidence.validate();
            }
        }

        if (termsOfUse() != null && !termsOfUse().isEmpty()) {
            for (TermsOfUse tou : termsOfUse()) {
                if (tou.type() == null || tou.type().isEmpty()) {
                    throw new DocumentError(ErrorType.Missing, "TermsOfUseType");
                }
                tou.validate();
            }
        }    
    }
}
