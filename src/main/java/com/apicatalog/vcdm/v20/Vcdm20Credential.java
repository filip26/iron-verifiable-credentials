package com.apicatalog.vcdm.v20;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.fragment.LinkableObject;
import com.apicatalog.linkedtree.link.Link;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.lt.ObjectFragmentMapper;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Verifiable;

import jakarta.json.JsonValue;

public class Vcdm20Credential extends Vcdm11Verifiable implements Credential {

    private static final Logger LOGGER = Logger.getLogger(Vcdm20Credential.class.getName());

    /** issuanceDate */
    protected Instant issuance;
    /** expirationDate */
    protected Instant expiration;

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<LinkedFragment> subject;

    protected Collection<Status> status;

    protected LinkedFragment issuer;

    protected LinkedFragment fragment;

    public static LinkableObject of(
            final Link id,
            final Collection<String> types,
            final Map<String, LinkedContainer> properties,
            final Supplier<LinkedTree> rootSupplier) throws DocumentError {

        var credential = new Vcdm20Credential();
//        var fragment = new LinkableObject(id, types, properties, rootSupplier, credential);
//
//        credential.fragment = fragment;
//
//        var selector = new ObjectFragmentMapper(properties);
//
//        setup(id, types, credential, selector);
//
//        return fragment;
        return null;
    }

    protected static void setup(final Link id, final Collection<String> types, Vcdm20Credential credential, final ObjectFragmentMapper selector) throws DocumentError {
        // @id
        credential.id = selector.id(id);

        // subject
//      credential.subject(readCollection(version, document.get(VcVocab.SUBJECT.uri()), subjectReader));

        // issuer
        //TODO IssuerDetails
        credential.issuer = selector.single(
                VcdmVocab.ISSUER,
                LinkedFragment.class);

        // status
//      credential.status(readCollection(version, document.get(VcVocab.STATUS.uri()), statusReader));

        // issuance date
        credential.issuance = selector.single(
                VcdmVocab.ISSUANCE_DATE,
                XsdDateTime.class,
                XsdDateTime::datetime);

        // expiration date
        credential.expiration = selector.single(
                VcdmVocab.EXPIRATION_DATE,
                XsdDateTime.class,
                XsdDateTime::datetime);

//        if (selector.properties().containsKey(VcdmVocab.PROOF.uri())) {
//            credential.proofs = EmbeddedProof.getProofs(
//                    selector.properties().get(VcdmVocab.PROOF.uri()).asTree());
//        }
    }

    @Override
    public LinkedNode ld() {
        return fragment;
    }

    /**
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @return a date time from which the credential claims are valid or
     *         <code>null</code>.
     */
    public Instant issuanceDate() {
        return issuance;
    }

    public Vcdm20Credential issuanceDate(Instant issuance) {
        this.issuance = issuance;
        return this;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    public Instant expiration() {
        return expiration;
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
    public Collection<LinkedFragment> subject() {
        return subject;
    }

//    public void type(Collection<String> type) {
//        this.type = type;
//    }


//    public JsonObject expand() {
//        
//        final LdNodeBuilder builder = new LdNodeBuilder(Json.createObjectBuilder(expanded));
//        
//        if (issuance != null) {
//            builder.set(VcVocab.ISSUANCE_DATE).xsdDateTime(issuance);
//        }
//        
//        if (expiration != null) {
//            builder.set(VcVocab.EXPIRATION_DATE).xsdDateTime(expiration);
//        }
//        
//        if (validFrom != null) {
//            builder.set(VcVocab.VALID_FROM).xsdDateTime(validFrom);
//        }
//        
//        if (validUntil != null) {
//            builder.set(VcVocab.VALID_UNTIL).xsdDateTime(validUntil);
//        }
//        
//        return builder.build();
//    }
//
//    @Override
//    protected Predicate<String> termsFilter() {
//        return super.termsFilter().and(term -> !TERMS.contains(term));
//    }
//
//    public static JsonLdVcdm11Credential of(VcdmVersion version, JsonObject document) throws DocumentError {
//        if (document == null) {
//            throw new IllegalArgumentException("The 'document' parameter must not be null.");
//        }
//
//        final JsonLdVcdm11Credential credential = JsonLdVcdm11Credential.of(version, document);
//
////        final LdNode node = LdNode.of(document);
//
//        // @id
////        credential.id(node.id());
//
//        // @type
////        credential.type(node.type().strings());
//
//        // subject
////        credential.subject(readCollection(version, document.get(VcVocab.SUBJECT.uri()), subjectReader));
//
//        // issuer
////        credential.issuer(readObject(version, document.get(VcVocab.ISSUER.uri()), issuerReader));
//
//        // status
////        credential.status(readCollection(version, document.get(VcVocab.STATUS.uri()), statusReader));
//
//        // issuance date
////        credential.issuanceDate(node.scalar(VcVocab.ISSUANCE_DATE).xsdDateTime());
//
//        // expiration date
////        credential.expiration(node.scalar(VcVocab.EXPIRATION_DATE).xsdDateTime());
//
//        // validFrom - optional
////        credential.validFrom(node.scalar(VcVocab.VALID_FROM).xsdDateTime());
//
//        // validUntil - optional
////        credential.validUntil(node.scalar(VcVocab.VALID_UNTIL).xsdDateTime());
//
//        return credential;
//    }

    public static boolean isCredential(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcdmVocab.CREDENTIAL_TYPE.uri(), document);
    }

    @Override
    public Collection<String> type() {
        return fragment.type().stream().toList();
    }

    @Override
    public boolean isExpired() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isNotValidYet() {
        // TODO Auto-generated method stub
        return false;
    }


}
