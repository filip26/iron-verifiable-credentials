package com.apicatalog.vc.issuer;

import java.net.URI;

import com.apicatalog.controller.key.KeyPair;
import com.apicatalog.cryptosuite.CryptoSuite;
import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.Signature;
import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.cryptosuite.SigningError.SignatureErrorCode;
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
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.jsonld.ContextAwareReaderProvider;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.model.ProofAdapter;
import com.apicatalog.vc.model.ProofAdapterProvider;
import com.apicatalog.vc.model.VerifiableMaterial;
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
import jakarta.json.JsonStructure;

public abstract class AbstractIssuer implements Issuer {

    protected final CryptoSuite crypto;
    protected final KeyPair keyPair;
    protected final Multibase proofValueBase;

    protected final VerifiableReaderProvider readerProvider;

    protected DocumentLoader defaultLoader;
    protected URI base;
    protected boolean bundledContexts;

    protected AbstractIssuer(SignatureSuite suite, CryptoSuite crypto, KeyPair keyPair, Multibase proofValueBase) {
        this.crypto = crypto;
        this.keyPair = keyPair;
        this.proofValueBase = proofValueBase;

        ProofAdapter proofAdapter = ProofAdapterProvider.of(suite);
        this.readerProvider = defaultReaders(proofAdapter);

        this.defaultLoader = null;
        this.base = null;
        this.bundledContexts = true;
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

    protected CryptoSuite cryptosuite() {
        return null;
    }

    @Override
    public JsonObject sign(URI location, ProofDraft draft) throws SigningError, DocumentError {
        final DocumentLoader loader = getLoader();
        return sign(fetchDocument(location, loader), draft, loader);
    }

    @Override
    public JsonObject sign(JsonObject document, final ProofDraft draft) throws SigningError, DocumentError {
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

    protected JsonObject sign(JsonObject document, final ProofDraft draft, final DocumentLoader loader) throws SigningError, DocumentError {

        draft.validate();

        final VerifiableReader reader = readerProvider.reader(document);

        if (reader == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        final VerifiableModel model = reader.read(document, loader, base);

        if (model == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        final Verifiable verifiable = reader.materialize(model, loader, base);

        if (verifiable == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        verifiable.validate();

        final VerifiableMaterial unsignedData = model.data();

        final VerifiableMaterial unsignedDraft = draft.unsigned(loader, base);

        final Signature ldSignature = new Signature(crypto, null);

        try {
            ldSignature.sign(unsignedData, unsignedDraft, keyPair.privateKey().rawBytes());

            final VerifiableMaterial signedProof = draft.sign(unsignedDraft, ldSignature.value());

            if (signedProof == null) {
                throw new IllegalStateException();
            }

            final VerifiableMaterial signedDocument = model
                    .withProof(signedProof)
                    .materialize();

            return JsonLdContext.set(signedDocument.context(), signedDocument.compacted());

        } catch (CryptoSuiteError e) {
            throw new SigningError(e, SignatureErrorCode.Unknown);
        }
    }

//    protected JsonObject sign(final VcdmVersion version, final JsonArray context, final JsonObject expanded,
//            final ProofDraft draft, final DocumentLoader loader) throws SigningError, DocumentError {
//
//        if (keyPair.privateKey() == null
//                || keyPair.privateKey().rawBytes() == null
//                || keyPair.privateKey().rawBytes().length == 0) {
//            throw new IllegalArgumentException("The private key is not provided, is null or an empty array.");
//        }
//
//        JsonObject object = expanded;
//
//        final Verifiable verifiable = readerProvider.materialize(object, loader, base);
//
//        // TODO do something with exceptions, unify
//        if (verifiable.isCredential() && verifiable.asCredential().isExpired()) {
//            throw new SigningError(SignatureErrorCode.Expired);
//        }
//
//        verifiable.validate();
//
//        // add issuance date if missing
////FIXME        if (verifiable.isCredential()
////                && (verifiable.version() == null
////                        || VcdmVersion.V11.equals(verifiable.version()))
////                && verifiable.asCredential().issuanceDate() == null) {
////
////            final Instant issuanceDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);
////
////            object = Json.createObjectBuilder(object)
////                    .add(VcVocab.ISSUANCE_DATE.uri(), issuanceDate.toString())
////                    .build();
////        }
//
//        // remove proofs
////        final JsonObject unsigned = EmbeddedProof.removeProofs(object);
//
//        // signature - FIXME
////        final byte[] signature = sign(context,
////                null,
//////                unsigned, 
////                draft);
//
////        final JsonObject proofValue = LdScalar.multibase(proofValueBase, signature);
////
////        // signed proof
////        final JsonObject signedProof = DataIntegrityProofDraft.signed(draft.unsigned(), proofValue);
////
////        return new ExpandedVerifiable(EmbeddedProof.addProof(object, signedProof), context, loader);
//        return expanded; // FIXMe
//    }

//    protected JsonObject signed1Copy(JsonObject unsigned, JsonObject signature) {
//        return new LdNodeBuilder(unsigned).set(VcdiVocab.PROOF_VALUE).value(signature).build();
//    }

//    /**
//     * Returns a signed proof.
//     * 
//     * @param context
//     * @param document
//     * @param draft
//     * @return
//     * @throws SigningError
//     * @throws DocumentError
//     */
//    protected abstract byte[] sign(
//            JsonArray context,
//            LinkedTree document,
//            ProofDraft draft) throws SigningError, DocumentError;

    protected static final JsonObject fetchDocument(URI location, DocumentLoader loader) throws DocumentError, SigningError {
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
}
