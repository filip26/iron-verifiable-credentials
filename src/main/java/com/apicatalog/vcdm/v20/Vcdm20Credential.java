package com.apicatalog.vcdm.v20;

import java.time.Instant;
import java.util.Collection;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.LinkedNode;
import com.apicatalog.linkedtree.adapter.AdapterError;
import com.apicatalog.linkedtree.jsonld.JsonLdKeyword;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.issuer.GenericIssuer;
import com.apicatalog.vc.status.GenericStatus;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.GenericSubject;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVerifiable;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

public class Vcdm20Credential extends VcdmVerifiable implements Credential {

//    private static final Logger LOGGER = Logger.getLogger(Vcdm20Credential.class.getName());

    protected Instant validFrom;
    protected Instant validUntil;

    /** a verifiable credential contains claims about one or more subjects */
    protected Collection<Subject> subject;

    protected Collection<Status> status;

    protected CredentialIssuer issuer;

    protected LinkedFragment ld;

    protected Vcdm20Credential() {
        // protected
    }

    public static Credential of(LinkedFragment source) throws AdapterError {
        return setup(new Vcdm20Credential(), source);
    }

    protected static Vcdm20Credential setup(Vcdm20Credential credential, LinkedFragment source) throws AdapterError {

        // @id
        credential.id = source.uri();

        // subject
        credential.subject = source.collection(
                VcdmVocab.SUBJECT.uri(),
                Subject.class,
                GenericSubject::of);

        // issuer
        credential.issuer = source.fragment(
                VcdmVocab.ISSUER.uri(),
                CredentialIssuer.class,
                GenericIssuer::of);

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

//        if ((issuanceDate() != null
//                && expiration() != null
//                && issuanceDate().isAfter(expiration()))) {
//            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
//        }
    }

    @Override
    public LinkedNode ld() {
        return ld;
    }

    @Override
    public CredentialIssuer issuer() {
        return issuer;
    }

    @Override
    public Collection<Status> status() {
        return status;
    }

    @Override
    public Collection<Subject> subject() {
        return subject;
    }

    @Override
    public Collection<String> type() {
        return ld.type().stream().toList();
    }

    @Override
    public boolean isExpired() {
        return (validUntil != null && validUntil.isBefore(Instant.now()));
    }

    @Override
    public boolean isNotValidYet() {
        return (validFrom != null && validFrom.isAfter(Instant.now()));
    }

    @Override
    public VcdmVersion version() {
        return VcdmVersion.V20;
    }

//  public JsonObject expand() {
//  
//  final LdNodeBuilder builder = new LdNodeBuilder(Json.createObjectBuilder(expanded));
//  
//  if (issuance != null) {
//      builder.set(VcVocab.ISSUANCE_DATE).xsdDateTime(issuance);
//  }
//  
//  if (expiration != null) {
//      builder.set(VcVocab.EXPIRATION_DATE).xsdDateTime(expiration);
//  }
//  
//  if (validFrom != null) {
//      builder.set(VcVocab.VALID_FROM).xsdDateTime(validFrom);
//  }
//  
//  if (validUntil != null) {
//      builder.set(VcVocab.VALID_UNTIL).xsdDateTime(validUntil);
//  }
//  
//  return builder.build();
//}

}
