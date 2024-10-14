package com.apicatalog.vc.method.resolver;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jwk.JsonWebKey;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeMappingBuilder;
import com.apicatalog.multikey.Multikey;

import jakarta.json.JsonArray;
import jakarta.json.JsonStructure;

public class HttpMethodResolver implements MethodResolver {

    private static final Logger LOGGER = Logger.getLogger(HttpMethodResolver.class.getName());

    final DocumentLoader loader;

    final JsonLdTreeReader reader;

    public HttpMethodResolver(
            DocumentLoader loader,
            JsonLdTreeReader reader) {
        this.loader = loader;
        this.reader = reader;
    }

    public static HttpMethodResolver getInstance(DocumentLoader loader, Class<?>... classes) {

        try {
            TreeMappingBuilder mapping = TreeMapping.createBuilder()
                    .scan(VerificationMethod.class);

            if (classes == null || classes.length == 0) {
                mapping.scan(Multikey.class)
                        .scan(JsonWebKey.class);
            } else {
                for (Class<?> clazz : classes) {
                    mapping.scan(clazz);
                }
            }

            JsonLdTreeReader reader = JsonLdTreeReader.of(mapping.build());

            return new HttpMethodResolver(loader, reader);

        } catch (NodeAdapterError e) {
            LOGGER.log(Level.SEVERE, e, () -> "An unexpected error, falling back to a generic reader.");
        }

        return new HttpMethodResolver(loader, JsonLdTreeReader.generic());
    }

    @Override
    public VerificationMethod resolve(URI id) throws DocumentError {

        try {

            Document doc = loader.loadDocument(id, new DocumentLoaderOptions());

            JsonStructure input = doc.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid, "Document"));

            JsonStructure context;

            // TODO inject @context
//          .context(proof.methodProcessor().context()) // an optional expansion context

            final JsonArray document = JsonLd.expand((JsonDocument) doc)
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .get();

            return reader.read(VerificationMethod.class, document);

        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, "ProofVerificationMethod");
        }

//        throw new DocumentError(ErrorType.Unknown, "ProofVerificationMethod");
    }

    @Override
    public boolean isAccepted(URI id) {
        return "http".equalsIgnoreCase(id.getScheme())
                || "https".equalsIgnoreCase(id.getScheme());
    }
}
