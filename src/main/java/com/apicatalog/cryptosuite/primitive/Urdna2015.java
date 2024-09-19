package com.apicatalog.cryptosuite.primitive;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.CryptoSuiteError.CryptoSuiteErrorCode;
import com.apicatalog.cryptosuite.algorithm.Canonicalizer;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeWriter;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;
import com.apicatalog.rdf.io.error.RdfWriterException;
import com.apicatalog.rdf.io.error.UnsupportedContentException;

import io.setl.rdf.normalization.RdfNormalize;
import jakarta.json.JsonArray;

public class Urdna2015 implements Canonicalizer {

    @Override
    public byte[] canonicalize(LinkedTree document) throws CryptoSuiteError {
        try {
            var treeWriter = new JsonLdTreeWriter();

            // JSON-LD version - FIXME temporary, remove
            final JsonArray expanded = treeWriter.write(document);

            final JsonLdOptions options = new JsonLdOptions();
            options.setUndefinedTermsPolicy(ProcessingPolicy.Fail);

            final RdfDataset dataset = JsonLd
                    .toRdf(JsonDocument.of(expanded))
                    .options(options)
                    .get();

            final RdfDataset canonical = RdfNormalize.normalize(dataset);

            final StringWriter writer = new StringWriter();

            final RdfWriter rdfWriter = Rdf.createWriter(MediaType.N_QUADS, writer);

            rdfWriter.write(canonical);

            return writer.toString().getBytes(StandardCharsets.UTF_8);

        } catch (JsonLdError | UnsupportedContentException | IOException | RdfWriterException e) {
            throw new CryptoSuiteError(CryptoSuiteErrorCode.Canonicalization, e);
        }
    }
}
