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
import com.apicatalog.vc.model.VerifiableReader;
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

    protected final VerifiableReader reader;

    protected Reader(final SignatureSuite[] suites) {
        super(suites);

        ProofAdapter proofAdapter = ProofAdapterProvider.of(suites);

        this.reader = defaultReaders(proofAdapter);
    }

    protected static VerifiableReader defaultReaders(final ProofAdapter proofAdapter) {

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

            return read(json.asJsonObject(), loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable read(final JsonObject document, DocumentLoader loader) throws DocumentError {

        final Verifiable verifiable = reader.read(document, loader, base);

        if (verifiable == null) {
            throw new DocumentError(ErrorType.Unknown, "DocumentModel");
        }

        return verifiable;
    }
}
