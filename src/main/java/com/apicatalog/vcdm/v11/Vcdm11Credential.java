package com.apicatalog.vcdm.v11;

import java.time.Instant;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.orm.Adapter;
import com.apicatalog.linkedtree.orm.Context;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.linkedtree.xsd.XsdDateTimeAdapter;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.CredentialSchema;
import com.apicatalog.vc.Evidence;
import com.apicatalog.vc.RefreshService;
import com.apicatalog.vc.Subject;
import com.apicatalog.vc.TermsOfUse;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

/**
 * Represents W3C VCDM 1.1 Verifiable Credential.
 * 
 * @see <a href=
 *      "https://www.w3.org/TR/vc-data-model-1.1/#credentials">Verifiable
 *      Credentials Data Model v1.1</a>
 */
@Fragment
@Term("VerifiableCredential")
@Vocab("https://www.w3.org/2018/credentials#")
@Context("https://www.w3.org/2018/credentials/v1")
public interface Vcdm11Credential extends VcdmVerifiable, Credential {

    /**
     * A date time when the credential has been issued.
     * 
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model-1.1/#issuance-date">Issuance
     *      Date</a>
     * 
     * @return a date time from which the credential claims are valid or
     *         <code>null</code>.
     */
    @Term("issuanceDate")
    @Adapter(XsdDateTimeAdapter.class)
    Instant issuance();

    /**
     * An expiration date of the {@link Vcdm11Credential}.
     * 
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model-1.1/#expiration">Expiration
     *      Date</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    @Term("expirationDate")
    @Adapter(XsdDateTimeAdapter.class)
    Instant expiration();

    /**
     * Checks if the credential is expired.
     *
     * @return <code>true</code> if the credential is expired
     */
    @Override
    default boolean isExpired() {
        return (expiration() != null && expiration().isBefore(Instant.now()));
    }

    @Override
    default boolean isNotValidYet() {
        return (issuance() != null && issuance().isAfter(Instant.now()));
    }

    @Provided
    @Override
    Collection<Proof> proofs();

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

        if (issuance() == null) {
            // issuance date is a mandatory property
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUANCE_DATE);
        }

        if (expiration() != null && issuance().isAfter(expiration())) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }

        if (status() != null && !status().isEmpty()) {
            for (Status status : status()) {
                if (status.id() == null) {
                    throw new DocumentError(ErrorType.Missing, "StatusId");
                }                
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
                if (refreshService.id() == null) {
                    throw new DocumentError(ErrorType.Missing, "RefreshServiceId");
                }
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

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V11;
    }
}