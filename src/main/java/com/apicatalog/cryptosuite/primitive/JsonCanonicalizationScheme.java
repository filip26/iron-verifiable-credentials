package com.apicatalog.cryptosuite.primitive;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.apicatalog.cryptosuite.CryptoSuiteError;
import com.apicatalog.cryptosuite.algorithm.Canonicalizer;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.linkedtree.json.JsonCanonicalizer;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.vc.model.VerifiableMaterial;

import jakarta.json.Json;
import jakarta.json.JsonObject;

public class JsonCanonicalizationScheme implements Canonicalizer {

    @Override
    public byte[] canonicalize(VerifiableMaterial material) throws CryptoSuiteError {

        JsonObject compacted = material.compacted();

        if (!compacted.containsKey(Keywords.CONTEXT)) {
            compacted = Json.createObjectBuilder(compacted).add(Keywords.CONTEXT, Json.createArrayBuilder(material.context())).build();
        }
        

        
        var x = JsonCanonicalizer.canonicalize(compacted).getBytes(StandardCharsets.UTF_8);
//        System.out.println("> " + new String(x));        
//        try {
//            System.out.println("< " + Multibase.BASE_64.encode(MessageDigest.getInstance("md5").digest(x)));
//        } catch (NoSuchAlgorithmException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        
        return x;
        
    }

    /*
     * 
> {"@context":["https://w3id.org/security/data-integrity/v2"],"created":"2022-05-28T17:02:05Z","cryptosuite":"eddsa-jcs-2022","proofPurpose":"assertionMethod","type":"DataIntegrityProof","verificationMethod":"did:key:z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y#z6Mkska8oQD7QQQWxqa7L5ai4mH98HfAdSwomPFYKuqNyE2y"}
< mc5JMVUHZjoBv0erlBce3pg
> {"@context":["https://www.w3.org/ns/credentials/v2"],"credentialSubject":"did:example:ebfeb1f712ebc6f1c276e12ec21","id":"https://apicatalog/com/vc/test-credentials#0023","issuer":"https://github.com/filip26/iron-verifiable-credentials/issuer/23","type":"VerifiableCredential","validFrom":"2023-01-01T00:00:00Z"}
< m0vjjYXa2UE8x4zO8P7m44A     
     */
    
}
