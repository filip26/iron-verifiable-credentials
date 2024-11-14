package com.apicatalog.vcdm.v20;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeWriter;
import com.apicatalog.linkedtree.jsonld.io.JsonLdWriter;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.primitive.VerifiableTree;
import com.apicatalog.vc.writer.VerifiableWriter;
import com.apicatalog.vcdm.VcdmVocab;
import com.apicatalog.vcdm.io.VcdmWriter;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;

public class Vcdm20Writer implements VerifiableWriter {

    protected static final JsonLdWriter WRITER = new JsonLdWriter()
            .scan(Vcdm20Credential.class)
            .scan(Vcdm20Presentation.class)
            .scan(Credential.class)
            .scan(Presentation.class)
            .scan(Verifiable.class);

    @Override
    public JsonObject write(Verifiable verifiable, DocumentLoader loader, URI base) throws DocumentError {
        return WRITER.compacted(verifiable);
    }
    
    public JsonObject write2(Verifiable verifiable, DocumentLoader loader, URI base) throws DocumentError {

        var tree = VerifiableTree.compose(verifiable);

        JsonArray expanded = new JsonLdTreeWriter().write(tree);

        try {

            JsonObject compacted = JsonLd.compact(
                    JsonDocument.of(expanded),
                    JsonDocument.of(VcdmWriter.getContext(tree)))
                    .loader(loader)
                    .base(base)
                    .get();

            if (verifiable.isCredential() || !verifiable.isPresentation() || verifiable.asPresentation().credentials().isEmpty()) {
                return compacted;
            }

            var credentials = Json.createArrayBuilder();

            for (Credential cred : verifiable.asPresentation().credentials()) {
                credentials.add(compactCredential(cred, loader, base));
            }

            return Json.createObjectBuilder(compacted)
                    .add(VcdmVocab.VERIFIABLE_CREDENTIALS.name(), credentials)
                    .build();

        } catch (JsonLdError e) {
            DocumentError.failWithJsonLd(e);
            throw new DocumentError(e, ErrorType.Invalid);
        }
    }

    protected JsonObject compactCredential(Credential credential, DocumentLoader loader, URI base) throws DocumentError {
        var tree = VerifiableTree.compose(credential);

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
