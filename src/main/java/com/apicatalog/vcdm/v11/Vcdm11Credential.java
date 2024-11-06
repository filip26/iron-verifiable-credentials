package com.apicatalog.vcdm.v11;

import java.time.Instant;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Literal;
import com.apicatalog.linkedtree.orm.Provided;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.linkedtree.xsd.XsdDateTimeAdapter;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmCredential;
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
public interface Vcdm11Credential extends VcdmCredential {

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
    @Literal(XsdDateTimeAdapter.class)
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
    @Literal(XsdDateTimeAdapter.class)
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

        for (Subject item : subject()) {
            if (item instanceof Linkable ld
                    && ld.ld().asFragment().terms().isEmpty()
                    && item.id() == null) {
                throw new DocumentError(ErrorType.Invalid, VcdmVocab.SUBJECT);
            }
            item.validate();
        }

        if (issuance() == null) {
            // issuance date is a mandatory property
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUANCE_DATE);
        }

        if ((issuance() != null
                && expiration() != null
                && issuance().isAfter(expiration()))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V11;
    }
}