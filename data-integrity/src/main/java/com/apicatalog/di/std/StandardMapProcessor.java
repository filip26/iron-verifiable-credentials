package com.apicatalog.di.std;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.apicatalog.trust.model.LexicalModel;
import com.apicatalog.trust.payload.DigestiblePayload;
import com.apicatalog.trust.payload.GenericPayload;
import com.apicatalog.trust.processor.MapProcessor;

public class StandardMapProcessor implements MapProcessor {

    private final LexicalModel model;
    private final Collection<String> context;
    private final Map<String, Object> document;

    private Collection<String> includedProofs;
    private Collection<?> proofs;

    public StandardMapProcessor(
            LexicalModel model,
            Collection<String> context,
            Map<String, Object> document) {
        this.model = model;
        this.context = context;
        this.document = new LinkedHashMap<String, Object>(document);

        this.includedProofs = null;
    }

    @Override
    public <T extends DigestiblePayload> T digestible(Function<byte[], T> payloadFactory) {
        var canonical = model.canonize(document);
        return payloadFactory.apply(canonical);
    }

    @Override
    public void withProofs(Collection<String> ids) {
        this.includedProofs = ids;

    }

    @Override
    public Collection<?> proofs() {

        var proofProperty = document.remove("proof");

        if (proofProperty == null) {
            return null;
        }

        if (!(proofProperty instanceof Collection<?> col)) {
            proofs = List.of(proofProperty);

        } else if (col.isEmpty()) {
            return List.of();

        } else {
            proofs = col;
        }

        return proofs;
    }
}
