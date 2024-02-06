package com.apicatalog.vc.model.io;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.DocumentError.ErrorType;
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
        return JsonLdReader.isTypeOf(VcVocab.CREDENTIAL_TYPE.uri(), document);
    }

    public static Credential read(final ModelVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final Credential credential = new Credential(version);

        // @type
        credential.setType(JsonLdReader.getType(document));

        if (credential.getType() == null
                || credential.getType().isEmpty()
                || !credential.getType().contains(VcVocab.CREDENTIAL_TYPE.uri())) {

            if (JsonLdReader.hasType(document)) {
                throw new DocumentError(ErrorType.Unknown, Term.TYPE);
            }

            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        // subject - mandatory
        if (!JsonLdReader.hasPredicate(document, VcVocab.SUBJECT.uri())) {
            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
        }

        credential.setSubject(document.get(VcVocab.SUBJECT.uri()));

        try {
            // @id - optional
            credential.setId(JsonLdReader.getId(document).orElse(null));

            if (!JsonLdReader.hasPredicate(document, VcVocab.ISSUER.uri())) {
                throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
            }

            credential.setIssuer(document.get(VcVocab.ISSUER.uri()));

            // issuer @id - mandatory
            JsonLdReader
                    .getId(document, VcVocab.ISSUER.uri())
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, VcVocab.ISSUER));

            credential.setStatus(document.get(VcVocab.STATUS.uri()));

            // issuance date - mandatory for verification
            credential.setIssuanceDate(JsonLdReader.getXsdDateTime(document, VcVocab.ISSUANCE_DATE.uri()).orElse(null));

            // expiration date - optional
            credential.setExpiration(JsonLdReader.getXsdDateTime(document, VcVocab.EXPIRATION_DATE.uri()).orElse(null));

            // validFrom - optional
            credential.setValidFrom(JsonLdReader.getXsdDateTime(document, VcVocab.VALID_FROM.uri()).orElse(null));

            // validUntil - optional
            credential.setValidUntil(JsonLdReader.getXsdDateTime(document, VcVocab.VALID_UNTIL.uri()).orElse(null));

            credential.setStatus(document.get(VcVocab.STATUS.uri()));

            assertValidyPeriod(credential);
            
            return credential;

        } catch (InvalidJsonLdValue e) {
            if (Keywords.ID.equals(e.getProperty())) {
                throw new DocumentError(ErrorType.Invalid, Term.ID);
            }
            throw new DocumentError(ErrorType.Invalid, Term.create(e.getProperty().substring(VcVocab.CREDENTIALS_VOCAB.length()), VcVocab.CREDENTIALS_VOCAB));
        }
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
