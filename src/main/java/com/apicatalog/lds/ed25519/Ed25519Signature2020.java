package com.apicatalog.lds.ed25519;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.http.media.MediaType;
import com.apicatalog.lds.SignatureSuite;
import com.apicatalog.rdf.Rdf;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.io.RdfWriter;
import com.apicatalog.rdf.io.error.RdfWriterException;
import com.apicatalog.rdf.io.error.UnsupportedContentException;

import io.setl.rdf.normalization.RdfNormalize;
import jakarta.json.JsonObject;

public class Ed25519Signature2020 implements SignatureSuite {

    @Override
    public byte[] canonicalize(JsonObject document) {
        // canonicalization
        try {
            RdfDataset dataset = JsonLd.toRdf(JsonDocument.of(document)).get();
            
            RdfDataset canonical = RdfNormalize.normalize(dataset);
            
            StringWriter writer = new StringWriter();
            
            RdfWriter rdfWriter = Rdf.createWriter(MediaType.N_QUADS, writer);
            
            rdfWriter.write(canonical);

System.out.println(">>> " + writer.toString().substring(0, writer.toString().length() -1) + "'");
            return writer.toString()
                    .substring(0, writer.toString().length() -1)
                    .getBytes(StandardCharsets.UTF_8);

            
        } catch (JsonLdError e) {
            //FIXME ...
            e.printStackTrace();
            
        } catch (UnsupportedContentException e) {
            e.printStackTrace();
            
        } catch (IOException e) {
            e.printStackTrace();
            
        } catch (RdfWriterException e) {
            e.printStackTrace();
            
        }
        throw new IllegalStateException();
    }

    @Override
    public byte[] digest(byte[] data) {
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @Override
    public boolean verify(byte[] publicKey, byte[] signature, byte[] data) {
        
        try {            
            Signature suite = Signature.getInstance("Ed25519");

            suite.initVerify(getPublicKey(publicKey));
            suite.update(data);

            boolean r = suite.verify(signature);
            System.out.println("V: " + r);
            return r;
 
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        return false;
    }
    
    @Override
    public byte[] sign(byte[] privateKey, byte[] data) {

        try {            
            Signature suite = Signature.getInstance("Ed25519");

            suite.initSign(getPrivateKey(privateKey));
            suite.update(data);

            return suite.sign();
 
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }        
        return null;
    }    

    static PublicKey getPublicKey(byte[] publicKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {

//      val lastIndex = publicKey.lastIndex
//      val lastByte = publicKey[lastIndex]
//      val lastByteAsInt = lastByte.toInt()
//      val isXOdd = lastByteAsInt.and(255).shr(7) == 1
//
//      publicKey[lastIndex] = (lastByteAsInt and 127).toByte()
//
//      var y = publicKey.reversedArray().asBigInteger;

//      var keyFactory = KeyFactory.getInstance("Ed25519");
//      var nameSpec = NamedParameterSpec.ED25519;
//      var point = new EdECPoint(isXOdd, publicKey);
//      var keySpec = new EdECPublicKeySpec(nameSpec, point);
//      var key = keyFactory.generatePublic(keySpec);

        
        byte[] pk = Arrays.copyOfRange(publicKey, 2, publicKey.length -2);

        //TODO validate the key starts with 0xed01
        //System.out.println(Integer.toHexString((publicKey[0] << 8)  + publicKey[1] ) );
        
        // key is already converted from hex string to a byte array.
        KeyFactory kf = KeyFactory.getInstance("Ed25519");
        
        // determine if x was odd.
        boolean xisodd = false;
        int lastbyteInt = pk[pk.length - 1];
        if ((lastbyteInt & 255) >> 7 == 1) {
            xisodd = true;
        }
        // make sure most significant bit will be 0 - after reversing.
        pk[pk.length - 1] &= 127;
              
       pk = reverse(pk);
        BigInteger y = new BigInteger(1, pk);

        NamedParameterSpec paramSpec = new NamedParameterSpec("Ed25519");
        EdECPoint ep = new EdECPoint(xisodd, y);
        EdECPublicKeySpec pubSpec = new EdECPublicKeySpec(paramSpec, ep);
        PublicKey pub = kf.generatePublic(pubSpec);
        return pub;
    }

    static PrivateKey getPrivateKey(byte[] privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {
        
        byte[] pk = Arrays.copyOfRange(privateKey, 2, privateKey.length -2);

        //TODO validate the key starts with 0xed01
        //System.out.println(Integer.toHexString((publicKey[0] << 8)  + publicKey[1] ) );
        
        // key is already converted from hex string to a byte array.
        KeyFactory kf = KeyFactory.getInstance("Ed25519");
        
        // determine if x was odd.
        boolean xisodd = false;
        int lastbyteInt = pk[pk.length - 1];
        if ((lastbyteInt & 255) >> 7 == 1) {
            xisodd = true;
        }
        // make sure most significant bit will be 0 - after reversing.
        pk[pk.length - 1] &= 127;
              
        pk = reverse(pk);
        BigInteger y = new BigInteger(1, pk);

        NamedParameterSpec paramSpec = new NamedParameterSpec("Ed25519");
        EdECPrivateKeySpec spec = new EdECPrivateKeySpec(paramSpec, privateKey);
        return kf.generatePrivate(spec);
    }

    
    static byte[] reverse(byte[] data) {
        final byte[] reversed = new byte[data.length];
        for (int i=0; i<data.length; i++) {
            reversed[data.length - i - 1] = data[i];
        }
        
        
        return reversed;
    }


}
