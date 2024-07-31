package com.apicatalog.vc.jsonld;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.uri.UriUtils;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.oxygen.ld.LinkedData;
import com.apicatalog.ld.Term;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.model.ModelVersion;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.reader.ObjectReader;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;

public abstract class JsonLdVerifiable implements Verifiable {

    private static final Logger LOGGER = Logger.getLogger(JsonLdVerifiable.class.getName());
    
    protected Collection<Proof> proofs;
    protected ModelVersion version;

    protected JsonObject expanded;

    protected JsonLdVerifiable(ModelVersion version, JsonObject expanded) {
        this.version = version;
        this.expanded = expanded;
    }
    

    @Override
    public Collection<Proof> proofs() {
        return proofs;
    }

    @Override
    public ModelVersion version() {
        return version;
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
    public Collection<String> terms() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Optional<Collection<LinkedData>> term(String name) {
        // TODO Auto-generated method stub
        return Optional.empty();
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
    public static Verifiable of(final ModelVersion version, final JsonObject expanded) throws DocumentError {

        // is a credential?
        if (JsonLdCredential.isCredential(expanded)) {
            // read the credential object
            return JsonLdCredential.of(version, expanded);
        }

        // is a presentation?
        if (JsonLdPresentation.isPresentation(expanded)) {
            // read the presentation object
            return JsonLdPresentation.of(version, expanded);
        }

        // is not expanded JSON-LD object
        if (JsonUtils.isNull(expanded.get(Keywords.TYPE))) {
            throw new DocumentError(ErrorType.Missing, Term.TYPE);
        }

        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
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


    protected static <T> Collection<T> readCollection(ModelVersion version, JsonValue value, ObjectReader<JsonObject, T> reader) throws DocumentError {
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

    protected static <T> T readObject(ModelVersion version, JsonValue value, ObjectReader<JsonObject, T> reader) throws DocumentError {
        if (JsonUtils.isNotArray(value) || value.asJsonArray().size() != 1) { // TODO throw an error if size > 1?
            return null;
        }

        final JsonValue item = value.asJsonArray().get(0);
        if (JsonUtils.isNotObject(item)) {
            // TODO print warning or error? -> processing policy
            return null;
        }
        return reader.read(version, item.asJsonObject());
    }


    public void proofs(Collection<Proof> proofs2) {
        // TODO Auto-generated method stub
        
    }
}
