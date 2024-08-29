package com.apicatalog.vcdm.v11;

import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

public interface Vcdm11Credential extends Credential {

    /**
     * A date time when the credential has been issued. VC data model v1.1.
     * Deprecated in favor of {@link Vcdm11Credential#validFrom()} by VC data model
     * v2.0.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @return a date time from which the credential claims are valid or
     *         <code>null</code>.
     */
    Instant issuanceDate();

    /**
     * An expiration date of the {@link Verifiable}.
     * 
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
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
        return (issuanceDate() != null && issuanceDate().isAfter(Instant.now()));
    }
    
    /**
     * Verifiable credentials data model version. Will be moved into a separate
     * interface specialized to VCDM.
     * 
     * @return the data model version, never <code>null</code>
     */
    default VcdmVersion version() {
        return VcdmVersion.V11;
    }

//    @Override
    default void validate() throws DocumentError {

        // @type - mandatory
//        if (type() == null || type().isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, JsonLdKeyword.TYPE);
//        }

        // subject - mandatory
//        if (subject() == null || subject().isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
//        }
//        for (Subject item : subject()) {
//            item.validate();
//        }

        // issuer
        if (issuer() == null) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUER);
        }
        issuer().validate();

        // status
        if (status() != null) {
            for (final Status item : status()) {
                item.validate();
            }
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
}
