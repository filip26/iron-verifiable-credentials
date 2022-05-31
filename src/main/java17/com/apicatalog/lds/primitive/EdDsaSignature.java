package com.apicatalog.lds.primitive;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

import com.apicatalog.lds.algorithm.SignatureAlgorithm;

public class EdDsaSignature implements SignatureAlgorithm {

    private final String type;
    
    public EdDsaSignature(String type) {
        this.type = type;
    }


    @Override
    public boolean verify(byte[] publicKey, byte[] signature, byte[] data) {
        
        try {            
            java.security.Signature suite = java.security.Signature.getInstance(type);

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
            java.security.Signature suite = java.security.Signature.getInstance(type);

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

    PublicKey getPublicKey(byte[] publicKey)
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

        
        byte[] pk = Arrays.copyOfRange(publicKey, 2, publicKey.length);

        //TODO validate the key starts with 0xed01
        //System.out.println(Integer.toHexString((publicKey[0] << 8)  + publicKey[1] ) );
        
        // key is already converted from hex string to a byte array.
        KeyFactory kf = KeyFactory.getInstance(type);
        
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

    PrivateKey getPrivateKey(byte[] privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidParameterSpecException {

        KeyFactory kf = KeyFactory.getInstance("Ed25519");
        
        NamedParameterSpec paramSpec = new NamedParameterSpec(type);
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
