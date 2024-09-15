package com.apicatalog.vc.holder;

import java.util.Collection;
import java.util.Objects;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdType;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.SigningError.Code;
import com.apicatalog.vc.processor.VerificationProcessor;
import com.apicatalog.vc.reader.ExpandedVerifiable;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.EmbeddedProof;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

public class Holder extends VerificationProcessor<Holder> {

    protected Holder(final SignatureSuite... suites) {
        super(suites);
    }

    public static Holder with(final SignatureSuite... suites) {
        return new Holder(suites);
    }

    public ExpandedVerifiable derive(JsonObject document, Collection<String> selectors) throws SigningError, DocumentError {
        Objects.requireNonNull(document);
        return derive(document, selectors, getLoader());
    }

    protected ExpandedVerifiable derive(final JsonObject document, Collection<String> selectors, DocumentLoader loader) throws SigningError, DocumentError {
        try {
            // extract context
            final JsonStructure context = document.containsKey(Keywords.CONTEXT)
                    ? JsonUtils.toJsonArray(document.get(Keywords.CONTEXT))
                    : null;

            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base).get();

            return deriveExpanded(document, context, expanded, selectors, loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    private ExpandedVerifiable deriveExpanded(final JsonObject document, JsonStructure context, JsonArray expanded, Collection<String> selectors, DocumentLoader loader)
            throws SigningError, DocumentError {

        if (expanded == null || expanded.isEmpty() || expanded.size() > 1) {
            throw new DocumentError(ErrorType.Invalid);
        }

        final JsonValue verifiable = expanded.iterator().next();

        if (JsonUtils.isNotObject(verifiable)) {
            throw new DocumentError(ErrorType.Invalid);
        }

        return deriveExpanded(document, context, verifiable.asJsonObject(), selectors, loader);
    }

    private ExpandedVerifiable deriveExpanded(final JsonObject document, JsonStructure context, final JsonObject expanded, Collection<String> selectors,
            DocumentLoader loader) throws SigningError, DocumentError {

//        if (VerifiableReader.isCredential(expanded)) {
//            return deriveProof(context, document, expanded, selectors, loader);
//
//        } else if (VerifiableReader.isPresentation(expanded)) {
//            // ?
//        }
        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    protected ExpandedVerifiable deriveProof(JsonStructure context, final JsonObject document, JsonObject expanded, Collection<String> selectors, DocumentLoader loader)
            throws DocumentError, SigningError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonObject> expandedProofs = EmbeddedProof.assertProof(expanded);

        if (expandedProofs.size() > 1) {
            throw new DocumentError(ErrorType.Invalid);
        }

        // a data before issuance - no proof attached
//        final JsonObject unsigned = EmbeddedProof.removeProofs(expanded);

        final JsonObject expandedProof = expandedProofs.iterator().next();

        final Collection<String> proofTypes = LdType.strings(expandedProof);

        if (proofTypes == null || proofTypes.isEmpty()) {
            throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF, Term.TYPE);
        }
        //FIXME
//        final SignatureSuite signatureSuite = findSuite(proofTypes, expandedProof);
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
