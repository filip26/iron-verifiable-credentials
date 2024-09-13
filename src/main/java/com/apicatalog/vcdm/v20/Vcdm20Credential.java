package com.apicatalog.vcdm.v20;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.LinkedContainer;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.link.Link;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.issuer.GenericIssuer;
import com.apicatalog.vc.status.GenericStatus;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.GenericSubject;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Verifiable;

public class Vcdm20Credential extends Vcdm11Verifiable implements Credential {

//    private static final Logger LOGGER = Logger.getLogger(Vcdm20Credential.class.getName());

    protected Instant validFrom;
    protected Instant validUntil;

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<Subject> subject;

    protected Collection<Status> status;

    protected CredentialIssuer issuer;

    protected LinkedFragment ld;

    public static Vcdm20Credential of(
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
        return credential;
    }

    protected static Vcdm20Credential setup(Vcdm20Credential credential, LinkedFragment source) throws AdapterError {
        
        // @id
        credential.id = source.uri();

        // subject
        credential.subject = source.collection(
                VcdmVocab.SUBJECT.uri(),
                Subject.class,
                f -> new GenericSubject(f.asFragment().uri(), f)
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

        credential.validFrom = source.xsdDateTime(VcdmVocab.VALID_FROM.uri());
        credential.validUntil = source.xsdDateTime(VcdmVocab.VALID_UNTIL.uri());

        credential.ld = source;
        return credential;
    }

    @Override
    public LinkedNode ld() {
        return ld;
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
    public Collection<Status> status() {
        return status;
    }

    public Collection<Subject> subject() {
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

//    public static boolean isCredential(final JsonValue document) {
//        if (document == null) {
//            throw new IllegalArgumentException("The 'document' parameter must not be null.");
//        }
//        return LdNode.isTypeOf(VcdmVocab.CREDENTIAL_TYPE.uri(), document);
//    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
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
