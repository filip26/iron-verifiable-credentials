package com.apicatalog.vc.issuer;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.jsonld.loader.SchemeRouter;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.node.LdNodeBuilder;
import com.apicatalog.ld.node.LdScalar;
import com.apicatalog.ld.signature.CryptoSuite;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.SigningError.Code;
import com.apicatalog.ld.signature.key.KeyPair;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.jsonld.EmbeddedProof;
import com.apicatalog.vc.loader.StaticContextLoader;
import com.apicatalog.vc.reader.ExpandedVerifiable;
import com.apicatalog.vcdi.DataIntegrityProofDraft;
import com.apicatalog.vcdi.DataIntegrityVocab;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public abstract class AbstractIssuer implements Issuer {

    protected final CryptoSuite crypto;
    protected final KeyPair keyPair;
    protected final Multibase proofValueBase;
    
    protected DocumentLoader defaultLoader;
    protected URI base;
    protected boolean bundledContexts;

    protected AbstractIssuer(CryptoSuite crypto, KeyPair keyPair, Multibase proofValueBase) {
        this.crypto = crypto;
        this.keyPair = keyPair;
        this.proofValueBase = proofValueBase;
//        this.reader = null; //new VerifiableReader();

        this.defaultLoader = null;
        this.base = null;
        this.bundledContexts = true;
    }

    protected CryptoSuite cryptosuite() {
        return null;
    }

    @Override
    public ExpandedVerifiable sign(URI location, ProofDraft draft) throws SigningError, DocumentError {
        final DocumentLoader loader = getLoader();
        return sign(fetchDocument(location, loader), draft, loader);
    }

    @Override
    public ExpandedVerifiable sign(JsonObject document, final ProofDraft draft) throws SigningError, DocumentError {
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

    protected ExpandedVerifiable sign(JsonObject document, final ProofDraft draft, final DocumentLoader loader) throws SigningError, DocumentError {

        try {
            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base).get();

            if (expanded.size() == 1) {
                final JsonValue object = expanded.iterator().next();
                if (JsonUtils.isObject(object)) {

//                    final ModelVersion version = VerifiableReader.getVersion(document);
//
//                    return sign(version, getContext(version, document, draft), object.asJsonObject(), draft, loader);
                    return null;
                }
            }
            throw new DocumentError(ErrorType.Invalid); // malformed input, not single object to sign has been found

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected ExpandedVerifiable sign(final VcdmVersion version, final JsonArray context, final JsonObject expanded,
            final ProofDraft draft, final DocumentLoader loader) throws SigningError, DocumentError {

        if (keyPair.privateKey() == null || keyPair.privateKey().length == 0) {
            throw new IllegalArgumentException("The private key is not provided, is null or an empty array.");
        }

        JsonObject object = expanded;

        final Verifiable verifiable = null; //reader.read(version, object);

        // TODO do something with exceptions, unify
        if (verifiable.isCredential() && verifiable.asCredential().isExpired()) {
            throw new SigningError(Code.Expired);
        }

        verifiable.validate();

        // add issuance date if missing
//FIXME        if (verifiable.isCredential()
//                && (verifiable.version() == null
//                        || VcdmVersion.V11.equals(verifiable.version()))
//                && verifiable.asCredential().issuanceDate() == null) {
//
//            final Instant issuanceDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);
//
//            object = Json.createObjectBuilder(object)
//                    .add(VcVocab.ISSUANCE_DATE.uri(), issuanceDate.toString())
//                    .build();
//        }

        // remove proofs
        final JsonObject unsigned = EmbeddedProof.removeProofs(object);

        // signature
        final byte[] signature = sign(context, unsigned, draft);

        final JsonObject proofValue = LdScalar.multibase(proofValueBase, signature);

        // signed proof
        final JsonObject signedProof = DataIntegrityProofDraft.signed(draft.unsigned(), proofValue);

        return new ExpandedVerifiable(EmbeddedProof.addProof(object, signedProof), context, loader);
    }

    protected JsonObject signed1Copy(JsonObject unsigned, JsonObject signature) {
        return new LdNodeBuilder(unsigned).set(DataIntegrityVocab.PROOF_VALUE).value(signature).build();
    }

    /**
     * Returns a signed proof.
     * 
     * @param context
     * @param document
     * @param draft
     * @return
     * @throws SigningError
     * @throws DocumentError
     */
    protected abstract byte[] sign(
            JsonArray context,
            JsonObject document,
            ProofDraft draft) throws SigningError, DocumentError;

    protected JsonArray getContext(VcdmVersion version, JsonObject document, ProofDraft draft) {

        final Collection<String> urls = new HashSet<>();
        final JsonArrayBuilder contexts = Json.createArrayBuilder();

        // extract origin contexts
        if (document != null && document.containsKey(Keywords.CONTEXT)) {
            final JsonValue documentContext = document.get(Keywords.CONTEXT);
            if (JsonUtils.isString(documentContext)) {
                urls.add(((JsonString) documentContext).getString());
                contexts.add(documentContext);

            } else if (JsonUtils.isObject(documentContext)) {
                contexts.add(documentContext);

            } else if (JsonUtils.isArray(documentContext)) {
                for (final JsonValue context : documentContext.asJsonArray()) {
                    if (JsonUtils.isString(context)) {
                        urls.add(((JsonString) context).getString());
                    }
                    contexts.add(context);
                }
            }
        }

        final Collection<String> provided = draft.context(version);

        if (provided != null) {
            // use .stream().filter(Predicate.not(urls::contains))
            for (String url : provided) {
                if (!urls.contains(url)) {
                    urls.add(url);
                    contexts.add(Json.createValue(url));
                }
            }
        }

        return contexts.build();
    }

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
