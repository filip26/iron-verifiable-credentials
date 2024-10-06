package com.apicatalog.vc.method.resolver;

import java.net.URI;

import com.apicatalog.controller.method.GenericVerificationMethod;
import com.apicatalog.controller.method.VerificationMethod;
import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdOptions.ProcessingPolicy;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.LinkedTree;
import com.apicatalog.linkedtree.jsonld.io.JsonLdTreeReader;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseLiteral;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multikey.MultiKey;

import jakarta.json.JsonArray;

public class HttpMethodResolver implements MethodResolver {

    protected DocumentLoader loader;

    protected JsonLdTreeReader reader;
    

//    protected static Multicodec PUBLIC_KEY_CODEC = Multicodec.of(
//            "test-pub",
//            Tag.Key,
//            12345l);
//
//    protected static Multicodec PRIVATE_KEY_CODEC = Multicodec.of(
//            "test-priv",
//            Tag.Key,
//            12346l);
//
//    protected static MulticodecDecoder DECODER = MulticodecDecoder.getInstance(
//            PUBLIC_KEY_CODEC,
//            PRIVATE_KEY_CODEC);
//    
    public HttpMethodResolver(
            DocumentLoader loader
            ) {
        this.loader = loader;
        this.reader = JsonLdTreeReader
                .create()
//                .with(
//                        MultiKey.typeName(), 
//                        MultiKey.class,
//                        source -> MultiKey.of(source, DECODER)
//                        )
                .with(MultibaseLiteral.typeAdapter(Multibase.BASE_58_BTC))
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
//NodeDebugWriter.writeToStdOut(tree);
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
