package com.apicatalog.vc.jsonld;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdType;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public interface JsonLdVerifiableReader {

    /**
     * Reads VC/VP document.
     * 
     * @param context
     * @param document
     * @param loader
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws DocumentError if the document cannot be read or parsed
     * 
     */
    Verifiable read(final Collection<String> context, final JsonObject document, DocumentLoader loader) throws DocumentError;
}
