package com.apicatalog.vcdm.v11.jsonld;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.Term;
import com.apicatalog.ld.node.LdType;
import com.apicatalog.ld.node.adapter.XsdDateTimeAdapter;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.primitive.LinkableObject;
import com.apicatalog.linkedtree.xsd.XsdConstants;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.jsonld.EmbeddedProof;
import com.apicatalog.vc.jsonld.JsonLdVerifiableReader;
import com.apicatalog.vc.proof.Proof;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model/">Verifiable Credentials Data
 * Model v1.1</a>
 */
public class JsonLdVcdm11Reader implements JsonLdVerifiableReader {

    protected final SignatureSuite[] suites;

    protected URI base;

    public JsonLdVcdm11Reader(final SignatureSuite... suites) {
        this.suites = suites;

        // default values
        this.base = null;

//        this.methodResolvers = defaultResolvers();
    }

    @Override
    public Verifiable read(Collection<String> context, JsonObject document, DocumentLoader loader) throws DocumentError {

        try {

            // load the document
            final JsonArray expanded = JsonLd.expand(JsonDocument.of(document))
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
                    .base(base).get();

            return readExpanded(
                    VcdmVersion.V11,
                    context,
                    expanded,
                    loader);

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    Verifiable readExpanded(
            final VcdmVersion version,
            final Collection<String> context,
            final JsonArray expanded,
            final DocumentLoader loader) throws DocumentError {

        if (expanded == null || expanded.isEmpty() || expanded.size() > 1) {
            throw new DocumentError(ErrorType.Invalid);
        }

        final JsonValue verifiable = expanded.iterator().next();

        if (JsonUtils.isNotObject(verifiable)) {
            throw new DocumentError(ErrorType.Invalid);
        }

        return readExpanded(version, context, verifiable.asJsonObject(), loader);
    }

    Verifiable readExpanded(
            final VcdmVersion version,
            final Collection<String> context,
            final JsonObject expanded,
            final DocumentLoader loader) throws DocumentError {

        // FIXME move, static?
        // get a reader
        JsonLdTreeReader reader = JsonLdTreeReader.create()
                .with(VcdmVocab.CREDENTIAL_TYPE.uri(), JsonLdVcdm11Credential::of)
                .with(XsdDateTime.TYPE, XsdDateTime::of)
                .build();

        // get a verifiable
        final Verifiable verifiable = reader.readExpanded(expanded)
                .single(Verifiable.class);

        // FIXMe reader.read(version, expanded);

        if (verifiable.isCredential()) {
//
//            // data integrity and metadata validation
////TODO            validate(verifiable.asCredential());
//
////FIXME            verifiable.proofs(read(context, expanded, loader));
//
            return verifiable;
//
        } else if (verifiable.isPresentation()) {
//
//            // verify presentation proofs
//            verifiable.proofs(readProofs(context, expanded, loader));
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
//            ((JsonLdPresentation)verifiable.asPresentation()).credentials(credentials);
//
            return verifiable;
        }
        throw new DocumentError(ErrorType.Unknown, Term.TYPE);
    }

    protected Collection<Proof> readProofs(JsonStructure context, JsonObject expanded, DocumentLoader loader) throws DocumentError {

        // get proofs - throws an exception if there is no proof, never null nor an
        // empty collection
        final Collection<JsonObject> expandedProofs = EmbeddedProof.assertProof(expanded);

        // a data before issuance - no proof attached
        final JsonObject unsigned = EmbeddedProof.removeProofs(expanded);

        final Collection<Proof> proofs = new ArrayList<>(expandedProofs.size());

        // read attached proofs
        for (final JsonObject expandedProof : expandedProofs) {

            final Collection<String> proofTypes = LdType.strings(expandedProof);

            if (proofTypes == null || proofTypes.isEmpty()) {
                throw new DocumentError(ErrorType.Missing, VcdmVocab.PROOF, Term.TYPE);
            }

            final SignatureSuite signatureSuite = findSuite(proofTypes, expandedProof);

            Proof proof = null;

            if (signatureSuite != null) {
//                proof = signatureSuite.getProof(expandedProof, loader);
            }

            if (proof == null) {
//                if (failOnUnsupportedProof) {
//                    throw new VerificationError(Code.UnsupportedCryptoSuite);
//                }
//FIXME                proof = new UnknownProof(expandedProof);
            }

            proofs.add(proof);
        }
        return proofs;
    }

    protected SignatureSuite findSuite(Collection<String> proofTypes, JsonObject expandedProof) {
        for (final SignatureSuite suite : suites) {
            for (final String proofType : proofTypes) {
//                if (suite.isSupported(proofType, expandedProof)) {
//                    return suite;
//                }
            }
        }
        return null;
    }
//
//
//    public static JsonLdVerifiableReader with(TestSignatureSuite suite) {
//        // TODO Auto-generated method stub
//        return null;
//    }

}