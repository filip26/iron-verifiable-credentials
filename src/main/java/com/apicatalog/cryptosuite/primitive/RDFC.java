package com.apicatalog.cryptosuite.primitive;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.CryptoSuiteError.CryptoSuiteErrorCode;
import com.apicatalog.cryptosuite.algorithm.CanonicalizationMethod;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfNQuad;
import com.apicatalog.rdf.canon.RdfCanonicalizer;
import com.apicatalog.vc.model.VerifiableMaterial;

import jakarta.json.JsonObject;

public class RDFC implements CanonicalizationMethod {

    @Override
    public byte[] canonicalize(VerifiableMaterial document) throws CryptoSuiteError {

        try {
            final JsonObject expanded = document.expanded();

            final JsonLdOptions options = new JsonLdOptions();
            options.setUndefinedTermsPolicy(ProcessingPolicy.Fail);

            final RdfDataset dataset = JsonLd
                    .toRdf(JsonDocument.of(expanded))
                    .options(options)
                    .get();

            final Collection<RdfNQuad> canonical = RdfCanonicalizer.canonicalize(dataset.toList());

            final StringWriter writer = new StringWriter();
            
            canonical.stream()
                    .map(RdfNQuad::toString)
                    .forEach(nquad -> {
                        writer.write(nquad);
                        writer.write('\n');
                    });

            return writer.toString().getBytes(StandardCharsets.UTF_8);

        } catch (JsonLdError e) {
            throw new CryptoSuiteError(CryptoSuiteErrorCode.Canonicalization, e);
        }
    }
}
