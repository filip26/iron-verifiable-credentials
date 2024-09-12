package com.apicatalog.vcdm.v11;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.fragment.LinkableObject;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.linkedtree.link.Link;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.lt.ObjectFragmentMapper;
import com.apicatalog.vcdm.VcdmVocab;

public class Vcdm11Credential extends Vcdm11Verifiable implements Credential {

    private static final Logger LOGGER = Logger.getLogger(Vcdm11Credential.class.getName());

    /** issuanceDate */
    protected Instant issuance;
    /** expirationDate */
    protected Instant expiration;

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<LinkedFragment> subject;

    protected Collection<LinkedFragment> status;

    protected LinkedFragment issuer;

    protected LinkedFragment fragment;
    
    protected Vcdm11Credential() {
        // protected
    }

    public static Vcdm11Credential of(LinkedFragment source) throws AdapterError {

        var credential = new Vcdm11Credential();
//        var fragment = new LinkableObject(id, types, properties, credential);

//        credential.fragment = fragment;

//        var selector = new ObjectFragmentMapper(properties);

//        setup(id, types, credential, selector);

//        return fragment;
        return credential;
    }

//    protected static LangStringSelector getLangMap(Map<String, LinkedContainer> properties, String term) {
//        final LinkedContainer container = properties.get(term);
//        if (container != null) {
//            return LanguageMap.of(container);
//        }
//        return null;
//    }

    protected static void setup(final Link id, final Collection<String> types, Vcdm11Credential credential, final ObjectFragmentMapper selector) throws DocumentError {
        // @id
        credential.id = selector.id(id);

        // subject
        credential.subject = selector.fragments(VcdmVocab.SUBJECT);

        // issuer
        // TODO IssuerDetails
        credential.issuer = selector.fragment(VcdmVocab.ISSUER);

        // status
        credential.status = selector.fragments(VcdmVocab.STATUS);

        // issuance date
        credential.issuance = selector.xsdDateTime(VcdmVocab.ISSUANCE_DATE);

        // expiration date
        credential.expiration = selector.xsdDateTime(VcdmVocab.EXPIRATION_DATE);

//        if (selector.properties().containsKey(VcdmVocab.PROOF.uri())) {
//            credential.proofs = EmbeddedProof.getProofs(
//                    selector.properties().get(VcdmVocab.PROOF.uri()).asContainer());
//        }
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
//            for (final Status item : status()) {
//                item.validate();
//            }
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
        return fragment;
    }

    @Override
    public Collection<String> type() {
        return fragment.type().stream().toList();
    }

    /**
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuer">Issuerr</a>
     * @return {@link IssuerDetails} representing the issuer in an expanded form
     */
    @Override
    public LinkedFragment issuer() {
        return issuer;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#status">Status</a>
     * 
     * @return
     */
    @Override
    public Collection<LinkedFragment> status() {
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
    public Collection<LinkedFragment> subject() {
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
}
