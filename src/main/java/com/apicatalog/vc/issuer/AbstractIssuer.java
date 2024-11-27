package com.apicatalog.vc.issuer;

import java.net.URI;
import java.util.function.Function;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.Signature;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.jsonld.ContextAwareReaderProvider;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.ProofAdapterProvider;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vc.model.VerifiableReaderProvider;
import com.apicatalog.vc.model.generic.GenericMaterial;
import com.apicatalog.vc.model.generic.GenericReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdi.VcdiVocab;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Reader;
import com.apicatalog.vcdm.v20.Vcdm20Reader;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public abstract class AbstractIssuer implements Issuer {

    protected final SignatureSuite suite;
    protected final CryptoSuite cryptosuite;
    protected final KeyPair keyPair;
    protected final Multibase proofValueBase;

    protected final Function<VerificationMethod, ? extends ProofDraft> proofDraftProvider;
    protected final VerifiableReaderProvider readerProvider;

    protected DocumentLoader defaultLoader;
    protected URI base;
    protected boolean bundledContexts;

    protected AbstractIssuer(
            SignatureSuite suite,
            CryptoSuite crypto,
            KeyPair keyPair,
            Multibase proofValueBase,
            Function<VerificationMethod, ? extends ProofDraft> proofDraftProvider) {
        this.suite = suite;
        this.cryptosuite = crypto;
        this.keyPair = keyPair;
        this.proofValueBase = proofValueBase;

        this.proofDraftProvider = proofDraftProvider;

        ProofAdapter proofAdapter = ProofAdapterProvider.of(suite);
        this.readerProvider = defaultReaders(proofAdapter);

        this.defaultLoader = null;
        this.base = null;
        this.bundledContexts = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends ProofDraft> T createDraft(VerificationMethod method) {
        return (T) proofDraftProvider.apply(method);
    }

    @Override
    public JsonObject sign(URI location, ProofDraft draft) throws DocumentError, CryptoSuiteError {
        final DocumentLoader loader = getLoader();
        return sign(fetchDocument(location, loader), draft, loader);
    }

    @Override
    public JsonObject sign(JsonObject document, final ProofDraft draft) throws DocumentError, CryptoSuiteError {
        return sign(document, draft, getLoader());
    }

    @Override
    public Issuer base(URI base) {
        this.base = base;
        return this;
    }

    @Override
    public Issuer loader(DocumentLoader loader) {
        this.defaultLoader = loader;
        return this;
    }

    @Override
    public Issuer useBundledContexts(boolean enable) {
        this.bundledContexts = enable;
        return this;
    }

    @Override
    public CryptoSuite cryptosuite() {
        return cryptosuite;
    }

    protected JsonObject sign(JsonObject document, final ProofDraft draft, final DocumentLoader loader) throws DocumentError, CryptoSuiteError {

        draft.validate();

        final VerifiableReader reader = readerProvider.reader(document);

        if (reader == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        final VerifiableModel model = reader.read(document, loader, base);

        if (model == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        final VerifiableDocument verifiable = reader.materialize(model, loader, base);

        if (verifiable == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        verifiable.validate();

        final VerifiableMaterial unsignedDraft = draft.unsigned(model.data().context(), loader, base);

        final VerifiableMaterial unsignedData = new GenericMaterial(unsignedDraft.context(), model.data().compacted(), model.data().expanded());

        return sign(model, unsignedData, unsignedDraft, draft);
    }

    protected JsonObject sign(VerifiableModel model, VerifiableMaterial unsignedData, VerifiableMaterial unsignedDraft, ProofDraft draft) throws DocumentError, CryptoSuiteError {

        final Signature ldSignature = new Signature(cryptosuite, null);

        ldSignature.sign(unsignedData, unsignedDraft, keyPair.privateKey().rawBytes());

        final VerifiableMaterial signedProof = draft.sign(unsignedDraft, ldSignature.byteArrayValue());

        if (signedProof == null) {
            throw new IllegalStateException();
        }

        final VerifiableMaterial signedDocument = model
                .withProof(signedProof)
                .materialize();

        return JsonLdContext.set(signedDocument.context(), signedDocument.compacted());
    }

    protected static final JsonObject fetchDocument(URI location, DocumentLoader loader) throws DocumentError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return json.asJsonObject();

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected DocumentLoader getLoader() {
        DocumentLoader loader = defaultLoader;

        if (loader == null) {
            // default loader
            loader = SchemeRouter.defaultInstance();
        }

        if (bundledContexts) {
            loader = new StaticContextLoader(loader);
        }
        return loader;
    }

    protected static VerifiableReaderProvider defaultReaders(final ProofAdapter proofAdapter) {

        Vcdm11Reader vcdm11 = Vcdm11Reader.with(proofAdapter);

        return new ContextAwareReaderProvider()
                .with(VcdmVocab.CONTEXT_MODEL_V1, vcdm11)
                .with(VcdmVocab.CONTEXT_MODEL_V2, Vcdm20Reader.with(proofAdapter)
                        // add VCDM 1.1 credential support
                        .v11(vcdm11))
                .with(VcdiVocab.CONTEXT_MODEL_V2, GenericReader.with(proofAdapter));
    }
}
