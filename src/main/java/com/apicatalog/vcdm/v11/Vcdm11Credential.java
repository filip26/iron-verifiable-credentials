package com.apicatalog.vcdm.v11;

import java.time.Instant;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.issuer.GenericIssuer;
import com.apicatalog.vc.status.GenericStatus;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.GenericSubject;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

public class Vcdm11Credential extends VcdmVerifiable implements Credential {

//    private static final Logger LOGGER = Logger.getLogger(Vcdm11Credential.class.getName());

    /** issuanceDate */
    protected Instant issuance;
    /** expirationDate */
    protected Instant expiration;

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<Subject> subject;

    protected Collection<Status> status;

    protected CredentialIssuer issuer;

    protected LinkedFragment ld;

    protected Vcdm11Credential() {
        // protected
    }

    public static Credential of(LinkedFragment source) throws AdapterError {
        return setup(new Vcdm11Credential(), source);
    }

    protected static Credential setup(Vcdm11Credential credential, LinkedFragment source) throws AdapterError {

        // @id
        credential.id = source.uri();

        // subject
        credential.subject = source.collection(
                VcdmVocab.SUBJECT.uri(),
                Subject.class,
                GenericSubject::of
                );

        // issuer
        credential.issuer = source.fragment(
                VcdmVocab.ISSUER.uri(),
                CredentialIssuer.class,
                GenericIssuer::of
                );

        // status
        credential.status = source.collection(
                VcdmVocab.STATUS.uri(),
                Status.class,
                GenericStatus::new);

        // issuance date
        credential.issuance = source.xsdDateTime(VcdmVocab.ISSUANCE_DATE.uri());

        // expiration date
        credential.expiration = source.xsdDateTime(VcdmVocab.EXPIRATION_DATE.uri());

        credential.ld = source;
        return credential;
    }

    /**
     * Checks if the credential is expired.
     *
     * @return <code>true</code> if the credential is expired
     */
    @Override
    public boolean isExpired() {
        return (expiration() != null && expiration().isBefore(Instant.now()));
    }

    @Override
    public boolean isNotValidYet() {
        return (issuanceDate() != null && issuanceDate().isAfter(Instant.now()));
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
//FIXME        for (Subject item : subject()) {
//            item.validate();
//        }

        // issuer
        if (issuer() == null) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.ISSUER);
        }
//FIXME        issuer().validate();

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

    @Override
    public LinkedNode ld() {
        return ld;
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }

    /**
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuer">Issuerr</a>
     * @return {@link CredentialIssuer} representing the issuer in an expanded form
     */
    @Override
    public CredentialIssuer issuer() {
        return issuer;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
     * 
     * @return
     */
    @Override
    public Collection<Status> status() {
        return status;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#credential-subject">Credential
     *      Subject</a>
     * 
     * @return
     */
    @Override
    public Collection<Subject> subject() {
        return subject;
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
    public Instant issuanceDate() {
        return issuance;
    }

    /**
     * An expiration date of the {@link Verifiable}.
     * 
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    public Instant expiration() {
        return expiration;
    }

    @Override
    public VcdmVersion version() {
        return VcdmVersion.V11;
    }
}
