package com.apicatalog.vc.model.io;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.model.Credential;
import com.apicatalog.vc.model.ModelVersion;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class CredentialReader {

    protected CredentialReader() {
        // protected
    }

    public static boolean isCredential(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcVocab.CREDENTIAL_TYPE.uri(), document);
    }

    public static Credential read(final ModelVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Credential credential = new Credential(version);

        final LdNode node = LdNode.of(document);
        
        // @type
        credential.setType(node.type().strings());
        if (!credential.getType().contains(VcVocab.CREDENTIAL_TYPE.uri())) {
            if (credential.getType().isEmpty()) {
                throw new DocumentError(ErrorType.Missing, Term.TYPE);
            }
            throw new DocumentError(ErrorType.Unknown, Term.TYPE);
        }

        // subject - mandatory
        if (!node.node(VcVocab.SUBJECT).exists()) {
            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
        }

        credential.setSubject(document.get(VcVocab.SUBJECT.uri()));

        // @id - optional
        credential.setId(node.id());

        final LdNode issuer = node.node(VcVocab.ISSUER);
        
        if (!issuer.exists()) {
            throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
        }
        
        // issuer @id - mandatory
        if (issuer.id() == null) {
            throw new DocumentError(ErrorType.Invalid, VcVocab.ISSUER);
        }
        
        credential.setIssuer(document.get(VcVocab.ISSUER.uri()));

        credential.setStatus(document.get(VcVocab.STATUS.uri()));

        // issuance date - mandatory for verification
        credential.setIssuanceDate(node.scalar(VcVocab.ISSUANCE_DATE).xsdDateTime());

        // expiration date - optional
        credential.setExpiration(node.scalar(VcVocab.EXPIRATION_DATE).xsdDateTime());

        // validFrom - optional
        credential.setValidFrom(node.scalar(VcVocab.VALID_FROM).xsdDateTime());

        // validUntil - optional
        credential.setValidUntil(node.scalar(VcVocab.VALID_UNTIL).xsdDateTime());

        credential.setStatus(document.get(VcVocab.STATUS.uri()));

        assertValidyPeriod(credential);
        
        return credential;
    }

    private static void assertValidyPeriod(Credential credential) throws DocumentError {
        // model v1
        if ((credential.getIssuanceDate() != null
                && credential.getExpiration() != null
                && credential.getIssuanceDate().isAfter(credential.getExpiration()))
                // model v2
                || (credential.getValidFrom() != null
                        && credential.getValidUntil() != null
                        && credential.getValidFrom().isAfter(credential.getValidUntil()))) {
            throw new DocumentError(ErrorType.Invalid, "ValidityPeriod");
        }
    }
}
