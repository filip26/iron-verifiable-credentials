package com.apicatalog.vcdm.v11;

import java.net.URI;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdWriter;
import com.apicatalog.vc.Credential;
import com.apicatalog.vc.Presentation;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.writer.VerifiableWriter;

import jakarta.json.JsonObject;

public class Vcdm11Writer implements VerifiableWriter {

    protected static final JsonLdWriter WRITER = new JsonLdWriter()
            .scan(Vcdm11Credential.class)
            .scan(Vcdm11Presentation.class)
            .scan(Credential.class)
            .scan(Presentation.class)
            .scan(Verifiable.class)
            ;
    
    @Override
    public JsonObject write(Verifiable verifiable, DocumentLoader loader, URI base) throws DocumentError {

        return WRITER.compacted(verifiable);
        
//        var tree = VerifiableTree.complete(verifiable);

//        JsonArray expanded = new JsonLdTreeWriter().write(tree);
//
//        try {
//            return JsonLd.compact(
//                    JsonDocument.of(expanded),
//                    JsonDocument.of(VcdmWriter.getContext(tree)))
//                    .loader(loader)
//                    .base(base)
//                    .get();
//
//        } catch (JsonLdError e) {
//            DocumentError.failWithJsonLd(e);
//            throw new DocumentError(e, ErrorType.Invalid);
//        }
    }
}
