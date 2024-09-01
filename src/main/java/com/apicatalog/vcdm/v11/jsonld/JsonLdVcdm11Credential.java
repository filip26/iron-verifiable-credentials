package com.apicatalog.vcdm.v11.jsonld;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.Link;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.primitive.LinkableObject;
import com.apicatalog.linkedtree.writer.NodeDebugWriter;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.jsonld.EmbeddedProof;
import com.apicatalog.vc.lt.ObjectFragmentMapper;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Credential;

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
            final Supplier<LinkedTree> rootSupplier) throws DocumentError {

        var credential = new JsonLdVcdm11Credential();
        var fragment = new LinkableObject(id, types, properties, rootSupplier, credential);

        credential.fragment = fragment;

        var selector = new ObjectFragmentMapper(properties);

        setup(id, types, credential, selector);

        return fragment;
    }

    protected static void setup(final Link id, final Collection<String> types, JsonLdVcdm11Credential credential, final ObjectFragmentMapper selector) throws DocumentError {
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

        if (selector.properties().containsKey(VcdmVocab.PROOF.uri())) {
//            
//            var proofs = selector.properties().get(VcdmVocab.PROOF.uri());
//            
//            NodeDebugWriter.printToStdout();
            
            credential.proofs = EmbeddedProof.getProofs(
                    selector.properties().get(VcdmVocab.PROOF.uri()).asContainer());
        }
    }

    @Override
    public LinkedNode ld() {
        return fragment;
    }

    @Override
    public Collection<String> type() {
        return fragment.type();
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

//    public JsonLdVcdm11Credential issuanceDate(Instant issuance) {
//        this.issuance = issuance;
//        return this;
//    }

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
    @Override
    public Collection<Subject> subject() {
        // TODO Auto-generated method stub
        return null;
    }

//    public void subject(Collection<Subject> subject) {
//        this.subject = subject;
//    }
//
//    public void status(Collection<Status> status) {
//        this.status = status;
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

}
