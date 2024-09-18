package com.apicatalog.vcdm.v11;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeWriter;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.primitive.VerifiableTree;
import com.apicatalog.vc.writer.VerifiableWriter;
import com.apicatalog.vcdm.io.VcdmWriter;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

public class Vcdm11Writer implements VerifiableWriter {

    @Override
    public JsonObject write(Verifiable verifiable, DocumentLoader loader, URI base) throws DocumentError {

        var tree = VerifiableTree.complete(verifiable);

        JsonArray expanded = new JsonLdTreeWriter().write(tree);

        try {
            return JsonLd.compact(
                    JsonDocument.of(expanded),
                    JsonDocument.of(VcdmWriter.getContext(tree)))
                    .loader(loader)
                    .base(base)
                    .get();

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }
}
