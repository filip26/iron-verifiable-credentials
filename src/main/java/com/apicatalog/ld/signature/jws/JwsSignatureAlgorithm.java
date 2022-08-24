package com.apicatalog.ld.signature.jws;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.algorithm.SignatureAlgorithm;

import com.nimbusds.jose.jwk.JWK;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Based on {@link SignatureAlgorithm}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public interface JwsSignatureAlgorithm {

    /**
     * Verify the provided data
     *
     * @param publicKey Json Web Key (public)
     * @param jws Json Web Signature with detached payload (using JWS Compact Serialization)
     * @param data payload
     * @throws VerificationError any error during verification
     * @return true if signature is valid, false otherwise
     */
    boolean verify(JWK publicKey, String jws, byte[] data) throws VerificationError;

    /**
     * Sign the provided data
     *
     * @param privateKey Json Web Key (private)
     * @param data payload to be signed
     * @return signature over provided data
     * @throws SigningError any error during signing
     */
    String sign(JWK privateKey, byte[] data) throws SigningError;

    /**
     * Verify the provided data
     *
     * @param publicKey public key
     * @param keyId key ID may be null. If null, random UUID will be generated.
     * @param jws Json Web Signature with detached payload (using JWS Compact Serialization)
     * @param data payload
     * @throws VerificationError any error during verification
     * @return true if signature is valid, false otherwise
     */
    boolean verify(PublicKey publicKey, String keyId, String jws, byte[] data) throws VerificationError;

    /**
     * Sign the provided data
     *
     * @param privateKey private key
     * @param publicKey public key (also needed in case of Json Web Key creation)
     * @param keyId key ID may be null. If null, random UUID will be generated.
     * @param data payload to be signed
     * @return signature over provided data
     * @throws SigningError any error during signing
     */
    String sign(PrivateKey privateKey, PublicKey publicKey, String keyId, byte[] data) throws SigningError;

    //-------------------------------------------------------------------------------------------

    /**
     * Generate JWK key pair
     *
     * @param length use only with RSA keys (alg. PS256), for other keys the provided length is ignored
     * @return key pair
     * @throws KeyGenError any error during key pair generation
     */
    JWK keygen(int length) throws KeyGenError;
}
