package com.apicatalog.vc.model.io;

import com.apicatalog.jsonld.InvalidJsonLdValue;
import com.apicatalog.jsonld.JsonLdReader;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.schema.LdTerm;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.model.Credential;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

class CredentialV11Reader {

    static void read(final Credential credential, final JsonObject expandedDocument) throws DocumentError {

        if (expandedDocument == null) {
            throw new IllegalArgumentException("The 'expandedDocument' parameter must not be null.");
        }

        // @type
        credential.setType(JsonLdReader.getType(expandedDocument));
        
        if (credential.getType() == null 
                || credential.getType().isEmpty()
                || !credential.getType().contains(VcVocab.CREDENTIAL_TYPE.uri())
                ) {

            if (!JsonLdReader.hasType(expandedDocument)) {
                throw new DocumentError(ErrorType.Missing, LdTerm.TYPE);
            }

            throw new DocumentError(ErrorType.Unknown, LdTerm.TYPE);
        }

        // subject - mandatory
        if (!JsonLdReader.hasPredicate(expandedDocument, VcVocab.SUBJECT.uri())) {
            throw new DocumentError(ErrorType.Missing, VcVocab.SUBJECT);
        }
        
        credential.setSubject(expandedDocument.get(VcVocab.SUBJECT.uri()));

        try {
            // @id - optional
            credential.setId(JsonLdReader.getId(expandedDocument).orElse(null));

            if (!JsonLdReader.hasPredicate(expandedDocument, VcVocab.ISSUER.uri())) {
                throw new DocumentError(ErrorType.Missing, VcVocab.ISSUER);
            }
            
            credential.setIssuer(expandedDocument.get(VcVocab.ISSUER.uri()));

            // issuer @id - mandatory
            JsonLdReader
                    .getId(expandedDocument, VcVocab.ISSUER.uri())
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid, VcVocab.ISSUER));
            
            // issuance date - mandatory for verification
            credential.setIssuanceDate(JsonLdReader.getXsdDateTime(expandedDocument, VcVocab.ISSUANCE_DATE.uri()).orElse(null));

            // expiration date - optional
            credential.setExpiration(JsonLdReader.getXsdDateTime(expandedDocument, VcVocab.EXPIRATION_DATE.uri()).orElse(null));
            
            credential.setStatus(expandedDocument.get(VcVocab.STATUS.uri()));

        } catch (InvalidJsonLdValue e) {
            if (Keywords.ID.equals(e.getProperty())) {
                throw new DocumentError(ErrorType.Invalid, LdTerm.ID);
            }
            throw new DocumentError(ErrorType.Invalid, LdTerm.create(e.getProperty().substring(VcVocab.CREDENTIALS_VOCAB.length()), VcVocab.CREDENTIALS_VOCAB));
        }
    }
}
