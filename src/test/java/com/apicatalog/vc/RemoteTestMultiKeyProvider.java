package com.apicatalog.vc;

import com.apicatalog.controller.key.VerificationKey;
import com.apicatalog.controller.loader.ControllerContextLoader;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.DocumentError.ErrorType;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.builder.TreeBuilderError;
import com.apicatalog.linkedtree.fragment.FragmentPropertyError;
import com.apicatalog.linkedtree.jsonld.io.JsonLdReader;
import com.apicatalog.linkedtree.orm.mapper.TreeReaderMapping;
import com.apicatalog.multikey.Multikey;
import com.apicatalog.vc.method.resolver.VerificationKeyProvider;
import com.apicatalog.vc.proof.Proof;

import jakarta.json.JsonStructure;

public class RemoteTestMultiKeyProvider implements VerificationKeyProvider {

    static TreeReaderMapping MAPPING = TreeReaderMapping
            .createBuilder()
            .scan(TestMultikey.class)
            .build();

    static JsonLdReader READER = JsonLdReader.of(MAPPING, ControllerContextLoader.resources());

    protected final DocumentLoader loader;
    
    public RemoteTestMultiKeyProvider(DocumentLoader loader) {
        this.loader = loader;
    }
    
    @Override
    public VerificationKey keyFor(Proof proof) throws DocumentError {
        
        if (proof == null || proof.method() == null || proof.method().id() == null) {
            return null;
        }
        
        try {
            Document doc = loader.loadDocument(proof.method().id(), new DocumentLoaderOptions());
            
            JsonStructure json = doc.getJsonContent().orElseThrow(() -> new DocumentError(ErrorType.Invalid));
            
            return READER.read(Multikey.class, json.asJsonObject());
                     
        } catch (FragmentPropertyError e) {
            throw DocumentError.of(e);
            
        } catch (JsonLdError | NodeAdapterError | TreeBuilderError e) {
            throw new DocumentError(e, ErrorType.Invalid);
        }        
    }
    
}
