package com.apicatalog.vc.holder;

import java.util.Collection;
import java.util.Objects;

import com.apicatalog.cryptosuite.SigningError;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.VocabTerm;
import com.apicatalog.linkedtree.jsonld.JsonLdType;
import com.apicatalog.vc.VerifiableDocument;
import com.apicatalog.vc.model.VerifiableMaterial;
import com.apicatalog.vc.model.VerifiableModel;
import com.apicatalog.vc.model.VerifiableReader;
import com.apicatalog.vc.processor.SuitesProcessor;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

@Deprecated
public class Holder extends SuitesProcessor<Holder> {

    protected Holder(final SignatureSuite... suites) {
        super(suites);
    }

    public static Holder with(final SignatureSuite... suites) {
        return new Holder(suites);
    }

    public JsonObject derive(JsonObject document, Collection<String> selectors) throws SigningError, DocumentError {
        Objects.requireNonNull(document);
        return derive(document, selectors, getLoader());
    }

    protected JsonObject derive(final JsonObject document, Collection<String> selectors, DocumentLoader loader) throws SigningError, DocumentError {

//        final Verifiable verifiable = read(document, loader);
//
//        if (verifiable != null) {
//            return derive(verifiable, selectors, loader);
//        }
//        throw new DocumentError(ErrorType.Unknown, "Model");
//    }
//
//    protected JsonObject derive(final Verifiable verifiable, Collection<String> selectors) throws SigningError, DocumentError {
//        return derive(verifiable, selectors, getLoader());
//    }
//
//    protected JsonObject derive(final Verifiable verifiable, Collection<String> selectors, DocumentLoader loader) throws SigningError, DocumentError {
//        

        final VerifiableReader reader = readerProvider.reader(document);

        if (reader == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        final VerifiableModel model = reader.read(document, loader, base);

        if (model == null) {
            throw new DocumentError(ErrorType.Unknown, "Model");
        }

        
//        if (verifiable.isCredential()) {
//            return deriveProof(context, document, expanded, selectors, loader);
//
////        }
//    }
//
//    protected JsonObject deriveProof(
//            JsonStructure context,
//            final JsonObject document,
//            JsonObject expanded,
//            Collection<String> selectors,
//            DocumentLoader loader) throws DocumentError, SigningError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
//        final Collection<JsonObject> expandedProofs = EmbeddedProof.assertProof(expanded);
//
//        if (expandedProofs.size() > 1) {
//            throw new DocumentError(ErrorType.Invalid);
//        }

        // a data before issuance - no proof attached
//        final JsonObject unsigned = EmbeddedProof.removeProofs(expanded);
//
//        final JsonObject expandedProof = expandedProofs.iterator().next();
//
//        final Collection<String> proofTypes = JsonLdType.strings(expandedProof);
//
//        if (proofTypes == null || proofTypes.isEmpty()) {
//            throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF, VocabTerm.TYPE);
//        }
        // FIXME
//        final SignatureSuite signatureSuite =  findSuite(proofTypes, expandedProof);
//
//        if (signatureSuite == null) {
//            throw new SigningError(Code.UnsupportedCryptoSuite);
//        }

//        final Proof proof = signatureSuite.getProof(expandedProof, loader);
//
//        if (proof == null) {
//            throw new IllegalStateException("The suite [" + signatureSuite + "] returns null as a proof.");
//        }
//
//        final ProofValue proofValue = proof.signature();
//
//        if (proofValue == null) {
//            throw new DocumentError(ErrorType.Missing, "ProofValue");
//        }
//
//        if (!(proofValue instanceof BaseProofValue)) {
//            throw new DocumentError(ErrorType.Invalid, "ProofValue");
//        }
//
//        final JsonObject derivedProof = proof.derive(context, unsigned, selectors);
//
//        final Collection<String> combinedPointers = Stream.of(
//                        ((BaseProofValue) proofValue).pointers(),
//                        (selectors != null ? selectors : Collections.<String>emptyList()),
//                        Arrays.asList("/" + Keywords.CONTEXT))
//                        .flatMap(Collection::stream)
//                        .collect(Collectors.toList());
//
//        final JsonObject reveal = DocumentSelector.of(combinedPointers).getNodes(document);
//
//        try {
//            return new ExpandedVerifiable(EmbeddedProof.addProof(
//                    JsonLd.expand(JsonDocument.of(reveal))
//                            .undefinedTermsPolicy(ProcessingPolicy.Fail)
//                            .loader(loader)
//                            .get().getJsonObject(0),
//                    derivedProof), context, loader);
//        } catch (JsonLdError e) {
//            throw new DocumentError(e, ErrorType.Invalid);
//        }
        return null;
    }

}
