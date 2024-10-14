package com.apicatalog.vc.reader;

import java.net.URI;
import java.util.Collection;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.processor.DocumentProcessor;
import com.apicatalog.vc.status.bitstring.BitstringStatusListEntry;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.io.VcdmResolver;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class Reader extends DocumentProcessor<Reader> {

    private static final Logger LOGGER = Logger.getLogger(Reader.class.getName());

    protected final ReaderResolver readerResolver;

    protected Reader(final SignatureSuite... suites) {
        super(suites);

        this.readerResolver = vcdmResolver(suites);
    }

    protected static ReaderResolver vcdmResolver(final SignatureSuite... suites) {
        var resolver = new VcdmResolver();
//        resolver.v11(Vcdm11Reader.with(
//                r -> {
//                },
//                suites));
//        resolver.v20(Vcdm20Reader.with(
//                r -> r.with(
//                        "https://www.w3.org/ns/credentials/status#BitstringStatusListEntry",
//                        BitstringStatusListEntry.class,
//                        BitstringStatusListEntry::of),
//                resolver,
//                suites));
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
