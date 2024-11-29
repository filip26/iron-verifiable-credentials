package com.apicatalog.vc.reader;

import java.net.URI;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.vc.adapter.ProofAdapter;
import com.apicatalog.vc.adapter.ProofAdapterProvider;
import com.apicatalog.vc.di.VcdiVocab;
import com.apicatalog.vc.jsonld.ContextAwareModelProvider;
import com.apicatalog.vc.model.DocumentError;
import com.apicatalog.vc.model.DocumentModel;
import com.apicatalog.vc.model.VerifiableDocument;
import com.apicatalog.vc.model.DocumentError.ErrorType;
import com.apicatalog.vc.model.adapter.DocumentModelAdapter;
import com.apicatalog.vc.model.generic.GenericReader;
import com.apicatalog.vc.model.provider.ModelAdapterProvider;
import com.apicatalog.vc.processor.DocumentProcessor;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class DocumentReader extends DocumentProcessor<DocumentReader> {

    protected final ModelAdapterProvider readerProvider;

    protected DocumentReader(final SignatureSuite[] suites) {
        super(suites);

        ProofAdapter proofAdapter = ProofAdapterProvider.of(suites);

        this.readerProvider = defaultReaders(proofAdapter);
    }

    protected static ModelAdapterProvider defaultReaders(final ProofAdapter proofAdapter) {

        Vcdm11Reader vcdm11 = Vcdm11Reader.with(proofAdapter);

        return new ContextAwareModelProvider()
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
    public static DocumentReader with(final SignatureSuite... suites) {
        return new DocumentReader(suites);
    }

    /**
     * Read VC/VP document.
     *
     * @param document
     * @return {@link VerifiableDocument} object representing credentials or a presentation
     * 
     * @throws DocumentError
     * 
     */
    public VerifiableDocument read(final JsonObject document) throws DocumentError {
        Objects.requireNonNull(document);
        return materialize(document, getLoader(), base);
    }

    /**
     * Read VC/VP document.
     * 
     * @param location
     * @return {@link VerifiableDocument} object representing credentials or a presentation
     * 
     * @throws DocumentError
     */
    public VerifiableDocument read(final URI location) throws DocumentError {

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

    public VerifiableDocument materialize(JsonObject document, DocumentLoader loader, URI base) throws DocumentError {

        final DocumentModelAdapter reader = readerProvider.reader(document);

        if (reader != null) {

            final DocumentModel model = reader.read(document, loader, base);

            if (model != null) {
                return reader.materialize(model, loader, base);
            }
        }

        throw new DocumentError(ErrorType.Unknown, "Model");
    }
}
