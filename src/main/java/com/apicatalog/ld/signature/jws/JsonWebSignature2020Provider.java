package com.apicatalog.ld.signature.jws;

import com.apicatalog.ld.signature.KeyGenError;
import com.apicatalog.ld.signature.SigningError;
import com.apicatalog.ld.signature.VerificationError;
import com.apicatalog.ld.signature.ed25519.Ed25519Signature2020Provider;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Collections;
import java.util.UUID;

import static com.apicatalog.ld.signature.jws.JsonWebSignature2020.getCurve;
import static com.apicatalog.ld.signature.jws.JsonWebSignature2020.getUnsupportedAlgErr;

/**
 * Provider which implements crypto operations sign, verify and key generation.
 *
 * Based on {@link Ed25519Signature2020Provider}
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JsonWebSignature2020Provider implements JwsSignatureAlgorithm {

    private final String alg;

    public JsonWebSignature2020Provider(String alg) {
        this.alg = alg;
    }

    /**
     * Verify provided signature over provided data with public key [within SW]
     *
     * @param publicKey Json Web Key (public)
     * @param jws Json Web Signature with unencoded (detached) payload (using JWS Compact Serialization)
     * @param data payload
     * @return true if signature is valid, false otherwise
     * @throws VerificationError thrown in case verification fails
     */
    @Override
    public boolean verify(JWK publicKey, String jws, byte[] data) throws VerificationError {
//        System.out.println("verify() - publicKey = \n" + publicKey);
//        System.out.println("verify() - data = \n" + bytesToHex(data));
//        System.out.println("verify() - jws = \n" + jws);

        //EcdsaVerifyJce verifier = new EcdsaVerifyJce(getECPublicKeyFromBytes(publicKey), Enums.HashType.SHA256, EllipticCurves.EcdsaEncoding.IEEE_P1363);
        //EcdsaVerifyJce from Google Tink lib. works only with secp256r1 (NIST), not with secp256k1, so lets use nimbus instead

        //Verification with Nimbus library
        Curve curve = getCurve(alg);
        try {
            JWSVerifier verifier;
            if (curve == Curve.Ed25519) {
                verifier = new Ed25519Verifier((OctetKeyPair) publicKey);
            } else if (curve == Curve.SECP256K1 || curve == Curve.P_256 || curve == Curve.P_384 || curve == Curve.P_521) {
                verifier = new ECDSAVerifier((ECKey) publicKey);
            } else if (curve == null) { //PS256
                verifier = new RSASSAVerifier((RSAKey) publicKey);
            } else {
                throw new IllegalArgumentException(getUnsupportedAlgErr(alg));
            }

            Payload detachedPayload = new Payload(data);
            JWSObject jwsObject = JWSObject.parse(jws, detachedPayload); //deserialize the JWS with unencoded payload (RFC 7797)
            return jwsObject.verify(verifier);

        } catch (ParseException e) {
            throw new VerificationError(VerificationError.Code.InvalidSignature, e);
        } catch (IllegalArgumentException | JOSEException e) {
            throw new VerificationError(e);
        }
    }

    /**
     * Sign provided data with private key [within SW]
     *
     * @param privateKey Json Web Key (private)
     * @param data payload to be signed
     * @return JWS (Json Web Signature) with unencoded (detached) payload
     * @throws SigningError thrown in case signing fails
     */
    @Override
    public String sign(JWK privateKey, byte[] data) throws SigningError {
//        System.out.println("sign() - privateKey = \n" + privateKey);
//        System.out.println("sign() - data = \n" + bytesToHex(data));

        //EcdsaSignJce from Google Tink lib. works only with secp256r1 (NIST), not with secp256k1, so lets use nimbus instead

        //Signing with Nimbus library
        Curve curve = getCurve(alg);
        try {
            JWSSigner signer;
            JWSHeader protectedHeader;
            if (curve == Curve.Ed25519) {
                signer = new Ed25519Signer((OctetKeyPair) privateKey);
                protectedHeader = new JWSHeader.Builder(JWSAlgorithm.EdDSA)
                        .base64URLEncodePayload(false)
                        .criticalParams(Collections.singleton("b64"))
                        .build();
            } else if (curve == Curve.SECP256K1 || curve == Curve.P_256 || curve == Curve.P_384 || curve == Curve.P_521) {
                signer = new ECDSASigner((ECKey) privateKey);
                protectedHeader = new JWSHeader.Builder(getEcdsaJwsAlg(curve))
                        .base64URLEncodePayload(false)
                        .criticalParams(Collections.singleton("b64"))
                        .build();
            } else if (curve == null) { //PS256
                signer = new RSASSASigner((RSAKey) privateKey);
                protectedHeader = new JWSHeader.Builder(JWSAlgorithm.PS256)
                        .base64URLEncodePayload(false)
                        .criticalParams(Collections.singleton("b64"))
                        .build();
            } else {
                throw new IllegalArgumentException(getUnsupportedAlgErr(alg));
            }

            Payload detachedPayload = new Payload(data);
            JWSObject jwsObject = new JWSObject(protectedHeader, detachedPayload);
            jwsObject.sign(signer);
            return jwsObject.serialize(true); //serialize the JWS with unencoded payload (RFC 7797)

        } catch (IllegalArgumentException | JOSEException e) {
            throw new SigningError(e);
        }
    }

    @Override
    public boolean verify(PublicKey publicKey, String keyId, String jws, byte[] data) throws VerificationError {
        if(keyId == null)
            keyId = UUID.randomUUID().toString();

        Curve curve = getCurve(alg);
        JWK jwk;

        if (curve == Curve.Ed25519) {
            throw new VerificationError(VerificationError.Code.Internal, new IllegalArgumentException("Cannot create JWK out of provided key."));
        } else if (curve == Curve.SECP256K1 || curve == Curve.P_256 || curve == Curve.P_384 || curve == Curve.P_521) {
            jwk = new ECKey.Builder(curve, (ECPublicKey) publicKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(keyId)
                    .build();
        } else if (curve == null) { //PS256
            jwk = new RSAKey.Builder((RSAPublicKey)publicKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } else {
            throw new IllegalArgumentException(getUnsupportedAlgErr(alg));
        }

        return verify(jwk, jws, data);
    }

    @Override
    public String sign(PrivateKey privateKey, PublicKey publicKey, String keyId, byte[] data) throws SigningError {
        if(keyId == null)
            keyId = UUID.randomUUID().toString();

        Curve curve = getCurve(alg);
        JWK jwk;

        if (curve == Curve.Ed25519) {
            throw new SigningError(SigningError.Code.Internal, new IllegalArgumentException("Cannot create JWK out of provided key."));
        } else if (curve == Curve.SECP256K1 || curve == Curve.P_256 || curve == Curve.P_384 || curve == Curve.P_521) {
            jwk = new ECKey.Builder(curve, (ECPublicKey) publicKey)
                    .privateKey((ECPrivateKey) privateKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(keyId)
                    .build();
        } else if (curve == null) { //PS256
            jwk = new RSAKey.Builder((RSAPublicKey)publicKey)
                    .privateKey((RSAPrivateKey) privateKey)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(UUID.randomUUID().toString())
                    .build();
        } else {
            throw new IllegalArgumentException(getUnsupportedAlgErr(alg));
        }

        return sign(jwk, data);
    }

    /**
     * Generate JWK (Json Web Key) key pair [within SW]
     *
     * @return JWK (with public and private key attributes)
     * @throws KeyGenError thrown in case generation fails
     */
    @Override
    public JWK keygen(/*int length*/) throws KeyGenError {
        //https://connect2id.com/products/nimbus-jose-jwt/examples/jwk-generation
        Curve curve = getCurve(alg);
        try {
            if (curve == Curve.Ed25519) {
                // Generate Ed25519 Octet key pair in JWK format, attach some metadata
                return new OctetKeyPairGenerator(Curve.Ed25519)
                        .keyUse(KeyUse.SIGNATURE)
                        .keyID(UUID.randomUUID().toString())
                        .generate();
            } else if (curve == Curve.SECP256K1 || curve == Curve.P_256 || curve == Curve.P_384) {
                // Generate EC key pair in JWK format
                return new ECKeyGenerator(curve)
                        .keyUse(KeyUse.SIGNATURE)
                        .keyID(UUID.randomUUID().toString())
                        .generate();
            } else if (curve == null) {
                // Generate the RSA key pair (for alg. PS256)
                return new RSAKeyGenerator(2048)
                        .keyUse(KeyUse.SIGNATURE) // indicate the intended use of the key
                        .keyID(UUID.randomUUID().toString()) // give the key a unique ID
                        .generate();
            } else {
                throw new IllegalArgumentException(getUnsupportedAlgErr(alg));
            }
        } catch (IllegalArgumentException | JOSEException e) {
            throw new KeyGenError(e);
        }
    }

    /**
     * Get ECDSA {@link JWSAlgorithm} by {@link Curve}
     * @return ECDSA {@link JWSAlgorithm} (Nimbus signature algorithm object)
     */
    private JWSAlgorithm getEcdsaJwsAlg(Curve curve) {
        if (curve == Curve.SECP256K1) {
            return JWSAlgorithm.ES256K;
        } else if (curve == Curve.P_256) {
            return JWSAlgorithm.ES256;
        } else if (curve == Curve.P_384) {
            return JWSAlgorithm.ES384;
        } else if (curve == Curve.P_521) {
            return JWSAlgorithm.ES512;
        } else {
            return JWSAlgorithm.ES256K; //default
        }
    }



}
