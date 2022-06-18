package com.apicatalog.ld.signature.primitive;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.ld.signature.DataError;
import com.apicatalog.ld.signature.algorithm.CanonicalizationAlgorithm;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;
import com.apicatalog.rdf.io.error.RdfWriterException;
import com.apicatalog.rdf.io.error.UnsupportedContentException;

import io.setl.rdf.normalization.RdfNormalize;
import jakarta.json.JsonStructure;

public class Urdna2015 implements CanonicalizationAlgorithm {

    @Override
    public byte[] canonicalize(JsonStructure document) throws DataError {
        try {
            RdfDataset dataset = JsonLd.toRdf(JsonDocument.of(document)).get();

            RdfDataset canonical = RdfNormalize.normalize(dataset);

            StringWriter writer = new StringWriter();

            RdfWriter rdfWriter = Rdf.createWriter(MediaType.N_QUADS, writer);

            rdfWriter.write(canonical);

            return writer.toString()
                    .substring(0, writer.toString().length() -1)
                    .getBytes(StandardCharsets.UTF_8);

        } catch (JsonLdError | UnsupportedContentException | IOException | RdfWriterException e) {
            throw new DataError(e);
        }
    }
}
