package com.apicatalog.vc;

import java.net.URI;

import com.apicatalog.jsonld.JsonLdUtils;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.lds.DataError;
import com.apicatalog.lds.DataError.ErrorType;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class Presentation implements Verifiable {

    public static final String BASE = "https://www.w3.org/2018/credentials#";

    public static final String TYPE = "VerifiablePresentation";
    
    public static final String HOLDER = "holder";
    
    public static final String VERIFIABLE_CREDENTIALS = "verifiableCredential";

    protected URI id;
    
    protected URI holder;

    protected Presentation() {}
    
    public static boolean isPresentation(JsonValue expanded) {
        if (expanded == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }

        return JsonUtils.isObject(expanded) && JsonLdUtils.isTypeOf(BASE + TYPE, expanded.asJsonObject());
    }
    
    public static Presentation from(JsonObject expanded) throws DataError {

        if (expanded == null) {
            throw new IllegalArgumentException("The 'expanded' parameter must not be null.");
        }
        
        final Presentation presentation = new Presentation();
        
        // @type
        if (!JsonLdUtils.isTypeOf(BASE + TYPE, expanded)) {

            if (!JsonLdUtils.hasType(expanded)) {
                throw new DataError(ErrorType.Missing, Keywords.TYPE);
            }

            throw new DataError(ErrorType.Unknown, Keywords.TYPE);
        }

        // @id - optional
        if (JsonLdUtils.hasPredicate(expanded, Keywords.ID)) {            
            presentation.id = JsonLdUtils.getId(expanded)
                    .orElseThrow(() -> new DataError(ErrorType.Invalid, Keywords.ID));
        }

        // holder - optional
        if (JsonLdUtils.hasPredicate(expanded, BASE + HOLDER)) {
            presentation.holder = JsonLdUtils.assertId(expanded, BASE, HOLDER);
        }

        return presentation;
    }

    @Override
    public boolean isPresentation() {
        return true;
    }

    @Override
    public Presentation asPresentation() {
        return this;
    }
    
    @Override
    public URI getId() {
        return id;
    }

    /**
     * see {@link https://www.w3.org/TR/vc-data-model/#dfn-holders}
     * @return
     */
    public URI getHolder() {
        return holder;
    }
}
