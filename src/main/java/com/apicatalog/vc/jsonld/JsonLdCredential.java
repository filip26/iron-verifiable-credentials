package com.apicatalog.vc.jsonld;

import java.net.URI;
import java.time.Instant;
import java.util.Collection;
import java.util.logging.Logger;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.issuer.IssuerDetails;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Represents a verifiable credential (VC).
 *
 * @see <a href= "https://www.w3.org/TR/vc-data-model/#credentials">v1.1</a>
 * @see <a href= "https://w3c.github.io/vc-data-model/#credentials">v2.0</a>
 * 
 * @since 0.9.0
 */
public class JsonLdCredential extends JsonLdVerifiable implements Credential  {

    private static final Logger LOGGER = Logger.getLogger(JsonLdCredential.class.getName());
    
    /** issuanceDate - v1.1 */
    protected Instant issuance;
    /** expirationDate - v1.1 */
    protected Instant expiration;

    /** model v2.0 - issanceDate replacement */
    protected Instant validUntil;
    /** model v2.0 - expirationDate replacement */
    protected Instant validFrom;

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<Subject> subject;
    
    protected Collection<Status> status;
    
    protected IssuerDetails issuer;    
    
    //TODO termsOfUse
    
    protected JsonLdCredential(VcdmVersion version, JsonObject expanded) {
        super(version, expanded);
    }

    /**
     * A date time when the credential has been issued. VC data model v1.1.
     * Deprecated in favor of {@link JsonLdCredential#validFrom()} by VC data model
     * v2.0.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @since 0.8.1
     * 
     * @return a date time from which the credential claims are valid or
     *         <code>null</code>.
     */
    public Instant issuanceDate() {
        return issuance;
    }

    public JsonLdCredential issuanceDate(Instant issuance) {
        this.issuance = issuance;
        return this;
    }

    /**
     * VC data model v1.1 only. Deprecated in favor of
     * {@link JsonLdCredential#validUntil()} by VC data model v2.0.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#expiration">Expiration</a>.
     * 
     * @return the expiration date or <code>null</code> if not set
     */
    public Instant expiration() {
        return expiration;
    }

    public void expiration(Instant expiration) {
        this.expiration = expiration;
    }

    /**
     * A date time from the credential is valid. VC data model v2.0.
     * 
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuance-date">Issuance
     *      Date - Note</a>
     * 
     * @since 0.8.1
     * 
     * @return a date time
     */
    public Instant validFrom() {
        return validFrom;
    }

    public void validFrom(Instant validFrom) {
        this.validFrom = validFrom;
    }

    /**
     * The date and time the credential ceases to be valid, which could be a date
     * and time in the past. Note that this value represents the latest point in
     * time at which the information associated with the credentialSubject property
     * is valid. VC data model version 2.0.
     * 
     * @return the date and time the credential ceases to be valid
     */
    public Instant validUntil() {
        return validUntil;
    }

    public void validUntil(Instant validUntil) {
        this.validUntil = validUntil;
    }

    /**
     *
     * @see <a href="https://www.w3.org/TR/vc-data-model/#issuer">Issuerr</a>
     * @return {@link IssuerDetails} representing the issuer in an expanded form
     */
    public LinkedFragment issuer() {
//        return issuer;
        return null;
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

    public void issuer(IssuerDetails issuer) {
        this.issuer = issuer;
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

    public static JsonLdCredential of(VcdmVersion version, JsonObject document) throws DocumentError {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final JsonLdCredential credential = JsonLdCredential.of(version, document);

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
    public URI id() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> type() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Proof> proofs() {
        // TODO Auto-generated method stub
        return null;
    }

//    @Override
//    public VcdmVersion version() {
//        // TODO Auto-generated method stub
//        return null;
//    }

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
