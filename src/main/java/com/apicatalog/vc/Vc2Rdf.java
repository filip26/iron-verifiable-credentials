package com.apicatalog.vc;

import com.apicatalog.jsonld.lang.BlankNode;
import com.apicatalog.lds.ProofOptions;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfResource;

public class Vc2Rdf {

    public static RdfDataset toRdf(ProofOptions options) {
        
        RdfDataset dataset = Rdf.createDataset();
        
        RdfResource b0 = Rdf.createBlankNode("b0");
        
        dataset.add(Rdf.createTriple(
                    b0,
                    Rdf.createIRI("https://w3id.org/security#verificationMethod"),
                    Rdf.createIRI("https://example.com/issuer/123#key-0")
                    )
                );
        
        return dataset;
        
    }

}
