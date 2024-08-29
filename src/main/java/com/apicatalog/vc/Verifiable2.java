package com.apicatalog.vc;

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
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.reader.ObjectReader;
import com.apicatalog.vcdm.VcdmVersion;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

/**
 * Represents a common ancestor for verifiable data.
 * 
 * @since 0.9.0
 */
public abstract class Verifiable2 {

    private static final Logger LOGGER = Logger.getLogger(Verifiable2.class.getName());

    protected Collection<Proof> proofs;
    protected VcdmVersion version;

    protected Verifiable2(VcdmVersion version) {
        this.version = version;
    }

    public Collection<Proof> proofs() {
        return proofs;
    }

    public void proofs(Collection<Proof> proofs) {
        this.proofs = proofs;
    }

    public boolean isCredential() {
        return false;
    }

    public boolean isPresentation() {
        return false;
    }

    public Credential asCredential() {
        throw new ClassCastException();
    }

    public Presentation asPresentation() {
        throw new ClassCastException();
    }

    public abstract void validate() throws DocumentError;

    /**
     * Creates a new verifiable instance from the given expanded JSON-LD input.
     * 
     * @param version  model version
     * @param expanded an expanded JSON-LD representing a verifiable
     * @return materialized verifiable instance
     * 
     * @throws DocumentError
     */
//    public static Verifiable2 of(final ModelVersion version, final JsonObject expanded) throws DocumentError {
//
//        // is a credential?
//        if (Credential.isCredential(expanded)) {
//            // read the credential object
//            return Credential.of(version, expanded);
//        }
//
//        // is a presentation?
//        if (Presentation.isPresentation(expanded)) {
//            // read the presentation object
//            return Presentation.of(version, expanded);
//        }
//
//        // is not expanded JSON-LD object
//        if (JsonUtils.isNull(expanded.get(Keywords.TYPE))) {
//            throw new DocumentError(ErrorType.Missing, Term.TYPE);
//        }
//
//        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
//    }

//    public Verifiable2 read(final JsonObject expanded) throws DocumentError {
//        return of(getVersion(expanded), expanded);
//    }
//    
    public static VcdmVersion getVersion(final JsonObject object) throws DocumentError {

        final JsonValue contexts = object.get(Keywords.CONTEXT);

        for (final JsonValue context : JsonUtils.toCollection(contexts)) {
            if (JsonUtils.isString(context)
                    && UriUtils.isURI(((JsonString) context).getString())) {

                final String contextUri = ((JsonString) context).getString();

                if ("https://www.w3.org/2018/credentials/v1".equals(contextUri)) {
                    return VcdmVersion.V11;
                }
                if ("https://www.w3.org/ns/credentials/v2".equals(contextUri)) {

                    if (JsonUtils.isNotArray(contexts)) {
                        LOGGER.log(Level.FINE,
                                "VC model requires @context declaration be an array, it is inconsistent with another requirement on compaction. Therefore this requirement is not enforced by Iron VC");
                    }

                    return VcdmVersion.V20;
                }
            } else {
                throw new DocumentError(ErrorType.Invalid, Keywords.CONTEXT);
            }
        }
        return VcdmVersion.V20;
    }

    protected static <T> Collection<T> readCollection(VcdmVersion version, JsonValue value, ObjectReader<JsonObject, T> reader) throws DocumentError {
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

    protected static <T> T readObject(VcdmVersion version, JsonValue value, ObjectReader<JsonObject, T> reader) throws DocumentError {
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
//
//    @Override
//    protected Predicate<String> termsFilter() {
//        return super.termsFilter().and(term -> !VcVocab.PROOF.equals(term));
//    }

//
//    /**
//     * Verifiable credentials data model version.
//     * 
//     * @return the data model version, never <code>null</code>
//     */
//    @Override
//    public ModelVersion version() {
//        return null;    //TODO
//    }
}
