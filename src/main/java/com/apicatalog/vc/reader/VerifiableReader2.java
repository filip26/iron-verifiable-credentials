package com.apicatalog.vc.reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.issuer.CredentialIssuer;
import com.apicatalog.vc.status.Status;
import com.apicatalog.vc.subject.Subject;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

/**
 * Materializes an expanded JSON-LD representing a verifiable credential or
 * presentation.
 * 
 * @since 0.15.0
 */
public class VerifiableReader2 {

    private static final Logger LOGGER = Logger.getLogger(VerifiableReader2.class.getName());

    protected ObjectReader<JsonObject, CredentialIssuer> issuerReader;
    protected ObjectReader<JsonObject, Subject> subjectReader;
    protected ObjectReader<JsonObject, Status> statusReader;
    protected ObjectReader<JsonObject, Status> tosReader;

    public VerifiableReader2() {
//        this.issuerReader = new ExpandedIssuerDetailsReader();
//        this.subjectReader = new SubjectReader();
//        this.statusReader = new StatusReader();
    }
    
    protected VerifiableReader2(ObjectReader<JsonObject, CredentialIssuer> issuerReader, ObjectReader<JsonObject, Subject> subjectReader, ObjectReader<JsonObject, Status> statusReader) {
        this.issuerReader = issuerReader;
        this.subjectReader = subjectReader;
        this.statusReader = statusReader;
    }
    

    public static Collection<JsonObject> getCredentials(final JsonObject document) throws DocumentError {

        JsonValue credentials = document.get(VcdmVocab.VERIFIABLE_CREDENTIALS.uri());

        if (JsonUtils.isNotArray(credentials)
                || credentials.asJsonArray().size() == 0) {
            return Collections.emptyList();
        }

        final Collection<JsonObject> result = new ArrayList<>(credentials.asJsonArray().size());

        for (final JsonValue cred : credentials.asJsonArray()) {
            if (JsonUtils.isNotObject(cred)
                    || JsonUtils.isNotArray(cred.asJsonObject().get(Keywords.GRAPH))
                    || cred.asJsonObject().getJsonArray(Keywords.GRAPH).size() != 1
                    || JsonUtils.isNotObject(cred.asJsonObject().getJsonArray(Keywords.GRAPH).get(0))) {
                throw new DocumentError(ErrorType.Invalid, VcdmVocab.CREDENTIALS_VOCAB);
            }

            result.add(cred.asJsonObject().getJsonArray(Keywords.GRAPH).getJsonObject(0));
        }

        return result;
    }


}
