package com.apicatalog.vc.processor;

import java.net.URI;
import java.util.function.Function;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.jsonld.ContextAwareReaderProvider;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.ProofAdapterProvider;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vc.model.VerifiableReaderProvider;
import com.apicatalog.vc.model.generic.GenericReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdi.VcdiVocab;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.JsonObject;

public class DocumentReader extends DocumentProcessor<DocumentReader> {

    protected final ProofAdapter proofAdapter;
    protected VerifiableReaderProvider readerProvider;

    protected DocumentReader(final SignatureSuite... suites) {
        super(suites);
        this.proofAdapter = ProofAdapterProvider.of(suites);
        this.readerProvider = defaultReaders(proofAdapter);
    }

    /**
     * Set recognized signature suites.
     * 
     * @param suites
     * @return
     */
    public static DocumentReader with(final SignatureSuite... suites) {
        return new DocumentReader(suites);
    }

    /**
     * Set custom model provider.
     * 
     * @param provider
     * @return
     */
    public DocumentReader model(final Function<ProofAdapter, VerifiableReaderProvider> provider) {
        this.readerProvider = provider.apply(proofAdapter);
        return this;
    }
    
    /**
     * Read a verifiable document.
     * 
     * @param document
     * @return
     * @throws DocumentError
     */
    public VerifiableDocument read(final JsonObject document) throws DocumentError {
        return read(document, getLoader());
    }

    /**
     * Read a verifiable document.
     * 
     * @param location
     * @return
     * @throws DocumentError
     */
    public VerifiableDocument read(final URI  location) throws DocumentError {
        return read(fetch(location), getLoader());
    }

    protected VerifiableDocument read(final JsonObject document, DocumentLoader loader) throws DocumentError {
        final VerifiableReader reader = readerProvider.reader(document);

        if (reader != null) {
            final VerifiableModel model = reader.read(document, loader, base);
System.out.println(": " + model.data().compacted());

            if (model != null) {
                return reader.materialize(model, loader, base);
            }
        }
        throw new DocumentError(ErrorType.Unknown, "Model");
    }
    
    protected static VerifiableReaderProvider defaultReaders(final ProofAdapter proofAdapter) {

        Vcdm11Reader vcdm11 = Vcdm11Reader.with(proofAdapter);

        return new ContextAwareReaderProvider()
                .with(VcdmVocab.CONTEXT_MODEL_V1, vcdm11)
                .with(VcdmVocab.CONTEXT_MODEL_V2, Vcdm20Reader.with(proofAdapter)
                        // add VCDM 1.1 credential support
                        .v11(vcdm11))
                // plain DI proof
                .with(VcdiVocab.CONTEXT_MODEL_V2, GenericReader.with(proofAdapter))
                ;
    }
}
