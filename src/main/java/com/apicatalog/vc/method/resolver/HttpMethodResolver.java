package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.ld.signature.GenericVerificationMethod;
import com.apicatalog.ld.signature.VerificationMethod;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.linkedtree.writer.NodeDebugWriter;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonArray;

public class HttpMethodResolver implements MethodResolver {

    protected DocumentLoader loader;

    protected JsonLdTreeReader reader;
    
    public HttpMethodResolver(
            DocumentLoader loader
            ) {
        this.loader = loader;
        this.reader = JsonLdTreeReader
                .create()
//              .with(proof.methodProcessor());
                .build();
    }
    
    @Override
    public VerificationMethod resolve(URI id) throws DocumentError {

        try {
            final JsonArray document = JsonLd.expand(id)
                    .undefinedTermsPolicy(ProcessingPolicy.Fail)
                    .loader(loader)
//                    .context(proof.methodProcessor().context()) // an optional expansion context
                    .get();

            final LinkedTree tree = reader.read(document);
NodeDebugWriter.writeToStdOut(tree);
            if (tree != null
                    && tree.nodes().size() == 1
                    && tree.node().isFragment()) {
                if (tree.fragment().type().isAdaptableTo(VerificationMethod.class)) {
                    return tree.fragment().type().materialize(VerificationMethod.class);
                }
                return GenericVerificationMethod.of(tree.fragment());                
            }

        } catch (Exception e) {
            throw new DocumentError(e, ErrorType.Invalid, "ProofVerificationMethod");
        }

        throw new DocumentError(ErrorType.Unknown, "ProofVerificationMethod");
    }

    @Override
    public boolean isAccepted(URI id) {
        return "http".equalsIgnoreCase(id.getScheme())
                || "https".equalsIgnoreCase(id.getScheme());
    }

}
