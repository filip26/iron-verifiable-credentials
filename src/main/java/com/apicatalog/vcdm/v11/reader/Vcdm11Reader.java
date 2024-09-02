package com.apicatalog.vcdm.v11.reader;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.xsd.XsdDateTime;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.jsonld.JsonLdVerifiableReader;
import com.apicatalog.vc.suite.SignatureSuite;
import com.apicatalog.vcdm.VcdmVersion;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.v11.Vcdm11Credential;
import com.apicatalog.vcdm.v11.Vcdm11Presentation;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

/**
 * A JSON-LD based reader conforming to the
 * <a href="https://www.w3.org/TR/vc-data-model/">Verifiable Credentials Data
 * Model v1.1</a>
 */
public class Vcdm11Reader implements JsonLdVerifiableReader {

    protected final Collection<SignatureSuite> suites;

    protected URI base;

//    protected boolean failOnUnsupportedProof = true;
//    if (failOnUnsupportedProof) {
//        throw new VerificationError(Code.UnsupportedCryptoSuite);
//    }

    public Vcdm11Reader(final SignatureSuite... suites) {
        this.suites = Arrays.asList(suites);

        // default values
        this.base = null;

//        this.methodResolvers = defaultResolvers();
    }

    @Override
    public Verifiable read(JsonObject document, DocumentLoader loader) throws DocumentError {

        try {
            Collection<String> context = JsonLdContext.strings(document);
            
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

        // FIXME move, static?
        final JsonLdTreeReader.Builder readerBuilder = JsonLdTreeReader.create()
                .with(VcdmVocab.CREDENTIAL_TYPE.uri(), Vcdm11Credential::of)
                .with(VcdmVocab.PRESENTATION_TYPE.uri(), Vcdm11Presentation::of)
                .with(XsdDateTime.TYPE, XsdDateTime::of);

        suites.stream()
                .forEach(s -> {
                    readerBuilder.with(s.proofAdapter().proofType(), s.proofAdapter());
                });

        // get a reader
        final JsonLdTreeReader reader = readerBuilder.build();

        try {
            // get a verifiable
            final LinkedTree verifiable = reader.readExpanded(context, expanded);
            if (verifiable == null
                    || verifiable.size() != 1
                    || !verifiable.single().isFragment()
                    || !(verifiable.single().asFragment().cast() instanceof Verifiable)) {
                throw new DocumentError(ErrorType.Invalid, "document");
            }

            return verifiable.single(Verifiable.class);

        } catch (DocumentError e) {
            throw e;
        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, "document");
        }
    }
}
