package com.apicatalog.vc.reader;

import java.net.URI;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.jsonld.ContextAwareReaderProvider;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.ProofAdapterProvider;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vc.model.VerifiableReaderProvider;
import com.apicatalog.vc.model.generic.GenericReader;
import com.apicatalog.vc.processor.DocumentProcessor;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdi.VcdiVocab;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class Reader extends DocumentProcessor<Reader> {

    protected final VerifiableReaderProvider readerProvider;

    protected Reader(final SignatureSuite[] suites) {
        super(suites);

        ProofAdapter proofAdapter = ProofAdapterProvider.of(suites);

        this.readerProvider = defaultReaders(proofAdapter);
    }

    protected static VerifiableReaderProvider defaultReaders(final ProofAdapter proofAdapter) {

        Vcdm11Reader vcdm11 = Vcdm11Reader.with(proofAdapter);

        return new ContextAwareReaderProvider()
                .with(VcdmVocab.CONTEXT_MODEL_V1, vcdm11)
                .with(VcdmVocab.CONTEXT_MODEL_V2, Vcdm20Reader.with(proofAdapter)
                        // add VCDM 1.1 credential support
                        .v11(vcdm11))
                .with(VcdiVocab.CONTEXT_MODEL_V2, GenericReader.with(proofAdapter));

//        
//        var vcdm = new VcdmResolver();
//        vcdm.v11(Vcdm11Reader.with(proofAdapter));
//        vcdm.v20(Vcdm20Reader.with(proofAdapter)
//                // add VCDM 1.1 credential support
//                .v11(vcdm.v11().adapter()));

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
        return materialize(document, getLoader(), base);
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

        Objects.requireNonNull(location);

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

            return materialize(json.asJsonObject(), loader, base);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    public Verifiable materialize(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {

        final VerifiableReader reader = readerProvider.reader(document);

        if (reader != null) {

            final VerifiableModel model = reader.read(document, loader, base);

            if (model != null) {
                return reader.materialize(model, loader, base);
            }
        }

        throw new DocumentError(ErrorType.Unknown, "Model");
    }
}
