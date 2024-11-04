package com.apicatalog.vcdm.v11;

import java.time.Instant;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.Linkable;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.DeprecatedVcdmCredential;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

public class DeprecatedVcdm11Credential  {

    /** issuanceDate */
    protected Instant issuance;
    /** expirationDate */
    protected Instant expiration;

    protected DeprecatedVcdm11Credential() {
        // protected
    }

//    public static Credential of(LinkedFragment source) throws NodeAdapterError {
//        var credential = new Vcdm11Credential();
//        VcdmCredential.setup(credential, source);
//        return setup(credential, source);
//    }
//
//    protected static Credential setup(Vcdm11Credential credential, LinkedFragment source) throws NodeAdapterError {
//        // issuance date
//        credential.issuance = source.xsdDateTime(VcdmVocab.ISSUANCE_DATE.uri());
//
//        // expiration date
//        credential.expiration = source.xsdDateTime(VcdmVocab.EXPIRATION_DATE.uri());
//        return credential;
//    }
//
//    /**
//     * Checks if the credential is expired.
//     *
//     * @return <code>true</code> if the credential is expired
//     */
//    @Override
//    public boolean isExpired() {
//        return (expiration() != null && expiration().isBefore(Instant.now()));
//    }
//
//    @Override
//    public boolean isNotValidYet() {
//        return (issuanceDate() != null && issuanceDate().isAfter(Instant.now()));
//    }
//
//    @Override
//    public void validate() throws DocumentError {
//
//        super.validate();
//
//        for (Subject item : subject()) {
//            if (item instanceof Linkable ld
//                    && ld.ld().asFragment().terms().isEmpty()
//                    && item.id() == null) {
//                throw new DocumentError(ErrorType.Invalid, VcdmVocab.SUBJECT);
//            }
//            item.validate();
//        }
//
//        if (issuanceDate() == null) {
//            // issuance date is a mandatory property
//            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUANCE_DATE);
//        }
//
//        if ((issuanceDate() != null
//                && expiration() != null
//                && issuanceDate().isAfter(expiration()))) {
//            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
//        }
//    }
//
//    /**
//     * A date time when the credential has been issued.
//     * 
//     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
//     *      Date - Note</a>
//     * 
//     * @return a date time from which the credential claims are valid or
//     *         <code>null</code>.
//     */
//    public Instant issuanceDate() {
//        return issuance;
//    }
//
//    /**
//     * An expiration date of the {@link Verifiable}.
//     * 
//     * @see <a href=
//     *      "https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
//     * 
//     * @return the expiration date or <code>null</code> if not set
//     */
//    public Instant expiration() {
//        return expiration;
//    }
//
//    @Override
//    public VcdmVersion version() {
//        return VcdmVersion.V11;
//    }
}
