package com.apicatalog.vcdm;

import java.net.URI;
import java.util.Collection;

import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.jsonld.JsonLdContext;
import com.apicatalog.linkedtree.pi.ProcessingInstruction;
import com.apicatalog.vc.Verifiable;
import com.apicatalog.vc.writer.VerifiableWriter;
import com.apicatalog.vcdm.v11.Vcdm11Writer;
import com.apicatalog.vcdm.v20.Vcdm20Writer;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;

public class VcdmWriter implements VerifiableWriter {

    protected VerifiableWriter v11;
    protected VerifiableWriter v20;

    public VcdmWriter() {
        this.v11 = new Vcdm11Writer();
        this.v20 = new Vcdm20Writer();
    }

    @Override
    public JsonObject write(Verifiable verifiable, DocumentLoader loader, URI base) throws DocumentError {
        if (verifiable instanceof VcdmVerifiable vcdmVerifiable) {
            return switch (vcdmVerifiable.version()) {
            case V11 -> v11.write(vcdmVerifiable, loader, base);
            case V20 -> v20.write(vcdmVerifiable, loader, base);
            default -> throw new DocumentError(ErrorType.Unknown, "VCDMVersion");
            };
        }

        throw new DocumentError(ErrorType.Unknown, "Document");
    }

    public static JsonArray getContext(final LinkedTree tree) {
        final Collection<ProcessingInstruction> ops = tree.pi(0);
        if (ops != null && !ops.isEmpty()) {
            for (ProcessingInstruction pi : ops) {
                if (pi instanceof JsonLdContext context) {
                    return Json.createArrayBuilder(context.context()).build();
                }
            }
        }
        return JsonValue.EMPTY_JSON_ARRAY;
    }
}
