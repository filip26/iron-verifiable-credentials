package com.apicatalog.vc.reader;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeWriter;
import com.apicatalog.linkedtree.pi.ProcessingInstruction;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.primitive.VerifiableTree;
import com.apicatalog.vc.processor.DocumentProcessor;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmResolver;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public class Reader extends DocumentProcessor<Reader> {

    private static final Logger LOGGER = Logger.getLogger(Reader.class.getName());

    protected final ReaderResolver readerResolver;

    protected Reader(final SignatureSuite... suites) {
        super(suites);

        this.readerResolver = vcdmResolver(suites);
    }

    protected static ReaderResolver vcdmResolver(final SignatureSuite... suites) {
        var resolver = new VcdmResolver();
        resolver.v11(new Vcdm11Reader(suites));
        resolver.v20(new Vcdm20Reader(resolver, suites));
        return resolver;
    }

    /**
     * Set of accepted verification suites.
     * 
     * @param suites
     * @return
     */
    public static Reader with(final SignatureSuite... suites) {
        return new Reader(suites);
    }

    /**
     * Read VC/VP document.
     *
     * @param document
     * @return {@link Verifiable} object representing credentials or a presentation
     * 
     * @throws DocumentError
     * 
     */
    public Verifiable read(final JsonObject document) throws DocumentError {
        Objects.requireNonNull(document);
        return read(document, getLoader());
    }

    /**
     * Read VC/VP document.
     * 
     * @param location
     * @return {@link Verifiable} object representing credentials or a presentation
     * 
     * @throws DocumentError
     */
    public Verifiable read(final URI location) throws DocumentError {
        try {
            final DocumentLoader loader = getLoader();

            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument
                    .getJsonContent()
                    .orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return read(json.asJsonObject(), loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    //TODO needs refactor, it even does not belong here
    public JsonObject compact(Verifiable verifiable) throws DocumentError {
        
        var tree = VerifiableTree.compose(verifiable);

        JsonArray expanded = new JsonLdTreeWriter().write(tree);

        try {
            
            JsonObject compacted = JsonLd.compact(
                    JsonDocument.of(expanded),
                    JsonDocument.of(getContext(tree)))
                    .loader(getLoader())
                    .base(base)
                    .get();
            
            if (verifiable.isCredential() || !verifiable.isPresentation() || verifiable.asPresentation().credentials().isEmpty()) {
                return compacted;
            }
            
            var credentials = Json.createArrayBuilder();
            
            for (Credential cred : verifiable.asPresentation().credentials()) {
                credentials.add(compactCredential(cred));
            }
            
            return Json.createObjectBuilder(compacted)
                    .add(VcdmVocab.VERIFIABLE_CREDENTIALS.name(), credentials)
                    .build();
            
        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected JsonObject compactCredential(Credential credential) throws DocumentError {
        var tree = VerifiableTree.compose(credential);

        JsonArray expanded = new JsonLdTreeWriter().write(tree);

        try {

            return JsonLd.compact(
                    JsonDocument.of(expanded),
                    JsonDocument.of(getContext(tree)))
                    .loader(getLoader())
                    .base(base)
                    .get();

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected JsonArray getContext(LinkedTree tree) {
        Collection<ProcessingInstruction> ops = tree.pi(0);
        if (ops != null && !ops.isEmpty()) {
            for (ProcessingInstruction pi : ops) {
                if (pi instanceof JsonLdContext context) {
                    JsonArrayBuilder builder = Json.createArrayBuilder();
                    context.context().forEach(builder::add);
                    return builder.build();
                }
            }
        }
        return JsonValue.EMPTY_JSON_ARRAY;
    }

    protected Verifiable read(final JsonObject document, DocumentLoader loader) throws DocumentError {

        // extract context
        final Collection<String> context;

        try {
            context = JsonLdContext.strings(document);
        } catch (IllegalArgumentException e) {
            throw new DocumentError(e, ErrorType.Invalid, "Context");
        }

        final VerifiableReader reader = readerResolver.resolveReader(context);

        if (reader == null) {
            LOGGER.log(Level.INFO, "An unknown document model {0}", context);
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        return reader.read(context, document, loader, base);
    }

}
