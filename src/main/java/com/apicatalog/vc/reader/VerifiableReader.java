package com.apicatalog.vc.reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.uri.UriUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdNode;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.ModelVersion;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.VcVocab;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.issuer.reader.ExpandedIssuerDetailsReader;
import com.apicatalog.vc.issuer.reader.IssuerDetailsReader;
import com.apicatalog.vc.status.reader.ExpandedStatusReader;
import com.apicatalog.vc.status.reader.StatusReader;
import com.apicatalog.vc.subject.reader.ExpandedSubjectReader;
import com.apicatalog.vc.subject.reader.SubjectReader;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * Materializes an expanded JSON-LD representing a verifiable credential or
 * presentation.
 * 
 * @since 0.15.0
 */
public class VerifiableReader {

    private static final Logger LOGGER = Logger.getLogger(VerifiableReader.class.getName());

    protected IssuerDetailsReader issuerReader;
    protected SubjectReader subjectReader;
    protected StatusReader statusReader;

    public VerifiableReader() {
        this.issuerReader = new ExpandedIssuerDetailsReader();
        this.subjectReader = new ExpandedSubjectReader();
        this.statusReader = new ExpandedStatusReader();
    }

    public static ModelVersion getVersion(final JsonObject object) throws DocumentError {

        final JsonValue contexts = object.get(Keywords.CONTEXT);

        for (final JsonValue context : JsonUtils.toCollection(contexts)) {
            if (JsonUtils.isString(context)
                    && UriUtils.isURI(((JsonString) context).getString())) {

                final String contextUri = ((JsonString) context).getString();

                if ("https://www.w3.org/2018/credentials/v1".equals(contextUri)) {
                    return ModelVersion.V11;
                }
                if ("https://www.w3.org/ns/credentials/v2".equals(contextUri)) {

                    if (JsonUtils.isNotArray(contexts)) {
                        LOGGER.log(Level.FINE,
                                "VC model requires @context declaration be an array, it is inconsistent with another requirement on compaction. Therefore this requirement is not enforced by Iron VC");
                    }

                    return ModelVersion.V20;
                }
            } else {
                throw new DocumentError(ErrorType.Invalid, Keywords.CONTEXT);
            }
        }
        return ModelVersion.V20;
    }

    /**
     * Creates a new verifiable instance from the given expanded JSON-LD input.
     * 
     * @param version  model version
     * @param expanded an expanded JSON-LD representing a verifiable
     * @return materialized verifiable instance
     * 
     * @throws DocumentError
     */
    public Verifiable read(final ModelVersion version, final JsonObject expanded) throws DocumentError {

        // is a credential?
        if (isCredential(expanded)) {
            // validate the credential object
            return readCredential(version, expanded);
        }

        // is a presentation?
        if (isPresentation(expanded)) {
            // validate the presentation object
            return readPresentation(version, expanded);
        }

        // is not expanded JSON-LD object
        if (JsonUtils.isNull(expanded.get(Keywords.TYPE))) {
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    public Verifiable read(final JsonObject expanded) throws DocumentError {
        return read(getVersion(expanded), expanded);
    }

    public Credential readCredential(final ModelVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final ExpandedCredential credential = new ExpandedCredential(version, document);

        final LdNode node = LdNode.of(document);

        // @id
        credential.id(node.id());

        // @type
        credential.type(node.type().strings());

        // subject
        credential.subject(readCollection(version, document.get(VcVocab.SUBJECT.uri()), subjectReader));

        // issuer
        credential.issuer(readObject(version, document.get(VcVocab.ISSUER.uri()), issuerReader));

        // status
        credential.status(readCollection(version, document.get(VcVocab.STATUS.uri()), statusReader));

        // issuance date
        credential.issuanceDate(node.scalar(VcVocab.ISSUANCE_DATE).xsdDateTime());

        // expiration date
        credential.expiration(node.scalar(VcVocab.EXPIRATION_DATE).xsdDateTime());

        // validFrom - optional
        credential.validFrom(node.scalar(VcVocab.VALID_FROM).xsdDateTime());

        // validUntil - optional
        credential.validUntil(node.scalar(VcVocab.VALID_UNTIL).xsdDateTime());

        return credential;
    }

    public static boolean isCredential(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcVocab.CREDENTIAL_TYPE.uri(), document);
    }

    public static boolean isPresentation(final JsonValue document) {
        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }
        return LdNode.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document);
    }

    public static Presentation readPresentation(final ModelVersion version, final JsonObject document) throws DocumentError {

        if (document == null) {
            throw new IllegalArgumentException("The 'document' parameter must not be null.");
        }

        final ExpandedPresentation presentation = new ExpandedPresentation(version, document);

        // @type
        if (!LdNode.isTypeOf(VcVocab.PRESENTATION_TYPE.uri(), document)) {

            if (LdNode.hasType(document)) {
                throw new DocumentError(ErrorType.Unknown, Term.TYPE);
            }
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        final LdNode node = LdNode.of(document);

        // @id - optional
        presentation.id(node.id());

        // holder - optional
        presentation.holder(node.node(VcVocab.HOLDER).id());

        return presentation;
    }

    public static Collection<JsonObject> getCredentials(final JsonObject document) throws DocumentError {

        JsonValue credentials = document.get(VcVocab.VERIFIABLE_CREDENTIALS.uri());

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
                throw new DocumentError(ErrorType.Invalid, VcVocab.CREDENTIALS_VOCAB);
            }

            result.add(cred.asJsonObject().getJsonArray(Keywords.GRAPH).getJsonObject(0));
        }

        return result;
    }

    protected static <T> Collection<T> readCollection(ModelVersion version, JsonValue value, ExpandedObjectReader<T> reader) throws DocumentError {
        if (JsonUtils.isNotArray(value)) {
            return Collections.emptyList();
        }

        final JsonArray values = value.asJsonArray();
        final Collection<T> instance = new ArrayList<>(values.size());

        for (final JsonValue item : values) {
            if (JsonUtils.isNotObject(item)) {
                // TODO print warning or error? -> processing policy
                continue;
            }
            instance.add(reader.read(version, item.asJsonObject()));
        }
        return instance;
    }

    protected static <T> T readObject(ModelVersion version, JsonValue value, ExpandedObjectReader<T> reader) throws DocumentError {
        if (JsonUtils.isNotArray(value) || value.asJsonArray().size() != 1) { // TODO throw an error if size > 1
            return null;
        }

        final JsonValue item = value.asJsonArray().get(0);
        if (JsonUtils.isNotObject(item)) {
            // TODO print warning or error? -> processing policy
            return null;
        }
        return reader.read(version, item.asJsonObject());
    }

}
