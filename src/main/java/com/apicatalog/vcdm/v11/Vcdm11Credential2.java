package com.apicatalog.vcdm.v11;

import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.orm.Fragment;
import com.apicatalog.linkedtree.orm.Literal;
import com.apicatalog.linkedtree.orm.Term;
import com.apicatalog.linkedtree.orm.Vocab;
import com.apicatalog.linkedtree.xsd.XsdDateTimeAdapter;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmCredential2;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

@Fragment
@Term("VerifiableCredential")
@Vocab("https://www.w3.org/2018/credentials#")
public interface Vcdm11Credential2 extends VcdmCredential2, Credential {

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
        return (issuanceDate() != null && issuanceDate().isAfter(Instant.now()));
    }

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

        if (issuanceDate() == null) {
            // issuance date is a mandatory property
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUANCE_DATE);
        }

        if ((issuanceDate() != null
                && expiration() != null
                && issuanceDate().isAfter(expiration()))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }

    /**
     * A date time when the credential has been issued.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @return a date time from which the credential claims are valid or
     *         <code>null</code>.
     */
    @Literal(XsdDateTimeAdapter.class)
    Instant issuanceDate();

    /**
     * An expiration date of the {@link Verifiable}.
     * 
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    @Literal(XsdDateTimeAdapter.class)
    Instant expiration();

    @Override
    default VcdmVersion version() {
        return VcdmVersion.V11;
    }
}