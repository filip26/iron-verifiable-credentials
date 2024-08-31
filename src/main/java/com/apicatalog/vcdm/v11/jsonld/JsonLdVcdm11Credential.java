package com.apicatalog.vcdm.v11.jsonld;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.linkedtree.Link;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.primitive.LinkableObject;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.jsonld.EmbeddedProof;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Credential;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class JsonLdVcdm11Credential extends JsonLdVcdm11Verifiable implements Vcdm11Credential {

    private static final Logger LOGGER = Logger.getLogger(JsonLdVcdm11Credential.class.getName());

    /** issuanceDate */
    protected Instant issuance;
    /** expirationDate */
    protected Instant expiration;

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<Subject> subject;

    protected Collection<Status> status;

    protected LinkedFragment issuer;

    protected LinkedFragment fragment;

    public static LinkableObject of(
            final Link id,
            final Collection<String> types,
            final Map<String, LinkedContainer> properties,
            final Supplier<LinkedTree> rootSupplier) {

        var credential = new JsonLdVcdm11Credential();
        var fragment = new LinkableObject(id, types, properties, rootSupplier, credential);

        credential.fragment = fragment;

        setup(credential, properties);

        return fragment;
    }

    protected static void setup(JsonLdVcdm11Credential credential, final Map<String, LinkedContainer> properties) {
        credential.expiration = properties.containsKey(VcdmVocab.EXPIRATION_DATE.uri())
                ? properties.get(VcdmVocab.EXPIRATION_DATE.uri())
                        .single(XsdDateTime.class)
                        .datetime()
                : null;

        credential.issuance = properties.containsKey(VcdmVocab.ISSUANCE_DATE.uri())
                ? properties.get(VcdmVocab.ISSUANCE_DATE.uri())
                        .single(XsdDateTime.class)
                        .datetime()
                : null;

        credential.issuer = properties.containsKey(VcdmVocab.ISSUER.uri())
                ? properties.get(VcdmVocab.ISSUER.uri())
                        .singleFragment()
                : null;

        if (properties.containsKey(VcdmVocab.PROOF.uri())) {
            credential.proofs = EmbeddedProof.getProofs(properties.get(VcdmVocab.PROOF.uri()).asTree());
        }
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
    @Override
    public Instant issuanceDate() {
        return issuance;
    }

    public JsonLdVcdm11Credential issuanceDate(Instant issuance) {
        this.issuance = issuance;
        return this;
    }

    /**
     * @see <a href=
     *      "https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    @Override
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
    public Collection<Subject> subject() {
        return subject;
    }

//    public void type(Collection<String> type) {
//        this.type = type;
//    }

    public void subject(Collection<Subject> subject) {
        this.subject = subject;
    }

    public void status(Collection<Status> status) {
        this.status = status;
    }

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

    public static JsonLdVcdm11Credential of(VcdmVersion version, JsonObject document) throws DocumentError {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final JsonLdVcdm11Credential credential = JsonLdVcdm11Credential.of(version, document);

//        final LdNode node = LdNode.of(document);

        // @id
//        credential.id(node.id());

        // @type
//        credential.type(node.type().strings());

        // subject
//        credential.subject(readCollection(version, document.get(VcVocab.SUBJECT.uri()), subjectReader));

        // issuer
//        credential.issuer(readObject(version, document.get(VcVocab.ISSUER.uri()), issuerReader));

        // status
//        credential.status(readCollection(version, document.get(VcVocab.STATUS.uri()), statusReader));

        // issuance date
//        credential.issuanceDate(node.scalar(VcVocab.ISSUANCE_DATE).xsdDateTime());

        // expiration date
//        credential.expiration(node.scalar(VcVocab.EXPIRATION_DATE).xsdDateTime());

        // validFrom - optional
//        credential.validFrom(node.scalar(VcVocab.VALID_FROM).xsdDateTime());

        // validUntil - optional
//        credential.validUntil(node.scalar(VcVocab.VALID_UNTIL).xsdDateTime());

        return credential;
    }

    public static boolean isCredential(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcdmVocab.CREDENTIAL_TYPE.uri(), document);
    }

    @Override
    public Collection<Subject> claims() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> type() {
        return fragment.type();
    }

    @Override
    public URI id() {
        return null;
    }

}
