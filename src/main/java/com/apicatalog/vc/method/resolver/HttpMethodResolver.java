package com.apicatalog.vc.method.resolver;

import java.net.URI;
import java.util.Collection;
import java.util.logging.Logger;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMappingBuilder;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonArray;
import jakarta.json.JsonStructure;

@Deprecated
public class HttpMethodResolver implements DeprecatedVerificationMethodResolver {

    private static final Logger LOGGER = Logger.getLogger(HttpMethodResolver.class.getName());

    Collection<String> contexts;
    
    final DocumentLoader loader;

    final JsonLdTreeReader reader;

    public HttpMethodResolver(
            Collection<String> contexts,
            DocumentLoader loader,
            JsonLdTreeReader reader) {
        this.contexts = contexts;
        this.loader = loader;
        this.reader = reader;
    }

    public static HttpMethodResolver getInstance(DocumentLoader loader, Class<?>... classes) {

//        try {
            TreeReaderMappingBuilder mapping = TreeReaderMapping.createBuilder()
                    .scan(VerificationMethod.class);

            if (classes != null && classes.length > 0) {
                for (Class<?> clazz : classes) {
                    mapping.scan(clazz);
                }
            }

            JsonLdTreeReader reader = JsonLdTreeReader.of(mapping.build());
            
            Collection<String> contexts = mapping.contexts(VerificationMethod.class);

            return new HttpMethodResolver(contexts, loader, reader);

//        } catch (NodeAdapterError e) {
//            LOGGER.log(Level.SEVERE, e, () -> "An unexpected error, falling back to a generic reader.");
//        }

//        return new HttpMethodResolver(Collections.emptyList(), loader, JsonLdTreeReader.generic());
    }

    @Override
    public VerificationKey resolve(URI id, Proof proof) throws DocumentError {

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

            return reader.read(VerificationKey.class, document);

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
