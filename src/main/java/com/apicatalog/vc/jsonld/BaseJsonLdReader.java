package com.apicatalog.vc.jsonld;

import java.net.URI;
import java.util.ArrayList;
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
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdType;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.VerificationError.Code;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.processor.AbstractProcessor;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.jsonld.JsonLdVcdmAdapter;

import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;

public class BaseJsonLdReader extends AbstractProcessor<BaseJsonLdReader> {

    private static final Logger LOGGER = Logger.getLogger(BaseJsonLdReader.class.getName());

    
    protected boolean failOnUnsupportedProof = true;

    protected JsonLdVerifiableAdapter verifiableAdapter;

    protected BaseJsonLdReader(final SignatureSuite... suites) {
        super(suites);
        //FIXME
        this.verifiableAdapter = new JsonLdVcdmAdapter(suites, base);
    }

    public static BaseJsonLdReader with(final SignatureSuite... suites) {
        return new BaseJsonLdReader(suites);
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param document
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable read(final JsonObject document) throws VerificationError, DocumentError {
        Objects.requireNonNull(document);
        return read(document, getLoader());
    }

    /**
     * Verifies VC/VP document. Throws VerificationError if the document is not
     * valid or cannot be verified.
     *
     * @param location
     * @return {@link Verifiable} object representing the verified credentials or a
     *         presentation
     * 
     * @throws VerificationError
     * @throws DocumentError
     */
    public Verifiable read(final URI location) throws VerificationError, DocumentError {
        Objects.requireNonNull(location);
        return read(location, getLoader());
    }

    protected Verifiable read(final URI location, DocumentLoader loader) throws VerificationError, DocumentError {
        try {
            // load the document
            final DocumentLoaderOptions options = new DocumentLoaderOptions();
            final Document loadedDocument = loader.loadDocument(location, options);

            final JsonStructure json = loadedDocument.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));

            if (JsonUtils.isNotObject(json)) {
                throw new DocumentError(ErrorType.Invalid);
            }

            return read(json.asJsonObject(), loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected Verifiable read(final JsonObject document, DocumentLoader loader) throws VerificationError, DocumentError {

        // extract context
        final Collection<String> context = JsonLdContext.strings(document);

        final JsonLdVerifiableReader reader = verifiableAdapter.reader(context);

        if (reader == null) {
            LOGGER.log(Level.INFO, "An unknown document model {0}", context);
            throw new DocumentError(ErrorType.Unknown, "DocumentModel");
        }
        
        return reader.read(context, document, loader);

        
//        try {

//            // expand the document
//            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
//                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
//                    .loader(loader)
//                    .base(base).get();
//
//            return readExpanded(
//                    context,
//                    expanded,
//                    loader);
//
//        } catch (JsonLdError e) {
//            DocumentError.failWithJsonLd(e);
//            throw new DocumentError(e, ErrorType.Invalid);
//        }
//    }
//
//    private Verifiable readExpanded(JsonArray context, JsonArray expanded, DocumentLoader loader)
//            throws VerificationError, DocumentError {
//
//        if (expanded == null || expanded.isEmpty() || expanded.size() > 1) {
//            throw new DocumentError(ErrorType.Invalid);
//        }
//
//        final JsonObject expandedObject = expanded.iterator().next().asJsonObject();
//
//        // get proofs - throws an exception if there is no proof, never null nor an
//        // empty collection
//        final Collection<JsonObject> expandedProofs = EmbeddedProof.getProof(expandedObject);
//
//        // a data before issuance - no proof attached
//        final JsonObject expandedUnsigned = EmbeddedProof.removeProofs(expandedObject);

//        final JsonLdVerifiableReader reader = verifiableAdapter.reader(context);

        // return reader.read(document);

        // get a verifiable representation
//        final Verifiable verifiable = reader
//                .read(document);
//                .readExpanded(expandedUnsigned)
//                .single(Verifiable.class);

//        if (verifiable.isCredential()) {
//
//            // data integrity and metadata validation
////TODO            validate(verifiable.asCredential());
//
////FIXME            verifiable.proofs(read(context, expanded, loader));
//
//            return verifiable;
//
//        } else if (verifiable.isPresentation()) {
//
//            // verify presentation proofs
////            verifiable.proofs(readProofs(context, expandedProofs, loader));
//
//            final Collection<Credential> credentials = new ArrayList<>();
//
////            for (final JsonObject presentedCredentials : VerifiableReader.getCredentials(expanded)) {
////
////                if (!VerifiableReader.isCredential(presentedCredentials)) {
////                    throw new DocumentError(ErrorType.Invalid, VcVocab.VERIFIABLE_CREDENTIALS, Term.TYPE);
////                }
//////var params = new HashMap<>();
//////FIXME                credentials.add(verifyExpanded(version, context, presentedCredentials, params, loader).asCredential());
////            }
//
//            ((JsonLdPresentation) verifiable.asPresentation()).credentials(credentials);
//
//            return verifiable;
//        }
//        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    protected Collection<Proof> readProofs(JsonStructure co1ntext, Collection<JsonObject> expandedProofs, DocumentLoader loader) throws VerificationError, DocumentError {

        final Collection<Proof> proofs = new ArrayList<>(expandedProofs.size());

        // read attached proofs
        for (final JsonObject expandedProof : expandedProofs) {

            final Collection<String> proofTypes = LdType.strings(expandedProof);

            if (proofTypes == null || proofTypes.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF, Term.TYPE);
            }

            // tady potrebuji ziskat proofReader
            final SignatureSuite signatureSuite = findSuite(proofTypes, expandedProof);
//signatureSuite.proofAdapter().
            Proof proof = null;

            if (signatureSuite != null) {
//                proof = signatureSuite.getProof(expandedProof, loader);
            }

            if (proof == null) {
                if (failOnUnsupportedProof) {
                    throw new VerificationError(Code.UnsupportedCryptoSuite);
                }
//FIXME                proof = new UnknownProof(expandedProof);
            }

            proofs.add(proof);
        }
        return proofs;
    }

}
