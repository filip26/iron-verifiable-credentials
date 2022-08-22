package com.apicatalog.ld.signature.jws;

import com.apicatalog.did.Did;
import com.apicatalog.did.DidResolver;
import com.apicatalog.did.DidUrl;
import com.apicatalog.did.document.DidDocument;
import com.apicatalog.did.document.DidVerificationMethod;
import com.apicatalog.did.key.DidKey;
import com.nimbusds.jose.jwk.*;
import com.nimbusds.jose.util.Base64URL;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.*;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.copyOfRange;

/**
 * For resolving did:key methods
 *
 * NOTE: currently limited by {@link DidKey#from(Did)} method which works only for Ed25519 / X25519 keys
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JwsDidKeyResolver implements DidResolver {
    private static final String JsonWebKey2020_TYPE = "https://w3id.org/security#JsonWebKey2020";

    public JwsDidKeyResolver() {
    }

    public DidDocument resolve(Did did) {
        if (!DidKey.isDidKey(did)) {
            throw new IllegalArgumentException();
        } else {
            DidKey didKey = DidKey.from(did);

//            DidDocumentBuilder builder = DidDocumentBuilder.create();
//            DidVerificationMethod signatureMethod = createSignatureMethod(didKey);
//            builder.add(signatureMethod);
//            builder.add(createEncryptionMethod(didKey));
//            builder.id(did);
//            return builder.build();

            DidVerificationMethod signatureMethod = createSignatureMethod(didKey);
            Set<DidVerificationMethod> verificationMethod = new HashSet();
            verificationMethod.add(signatureMethod);
            return new DidDocument(did, (Set)null, verificationMethod);
        }
    }

    public static DidVerificationMethod createSignatureMethod(DidKey didKey) {
        return new DidVerificationMethod(
                DidUrl.from(didKey, (String)null, (String)null, didKey.getMethodSpecificId()),
                DidUrl.from(didKey, (String)null, (String)null, didKey.getMethodSpecificId()),
                JsonWebKey2020_TYPE,
                didKey.getRawKey()
        );
    }

//    public static DidVerificationMethod createEncryptionMethod(DidKey didKey) {
//        String encodingType = "MultiKey";
//        return new DidVerificationMethod(DidUrl.from(didKey, (String)null, (String)null, didKey.getMethodSpecificId()), DidUrl.from(didKey, (String)null, (String)null, didKey.getMethodSpecificId()), encodingType, didKey.getRawKey());
//    }

    public static JWK getJwk(DidVerificationMethod didVerificationMethod) throws NoSuchAlgorithmException, InvalidKeySpecException {
        //https://github.com/mitreid-connect/json-web-key-generator/tree/master/src/main/java/org/mitre/jose/jwk
        String did = didVerificationMethod.id().getMethodSpecificId();
        byte[] publicKey = didVerificationMethod.publicKey();

        if(did.startsWith("z6Mk")){
            return new OctetKeyPair.Builder(Curve.Ed25519, Base64URL.encode(publicKey)).build();
        } else if (did.startsWith("z6LS")) {
            return new OctetKeyPair.Builder(Curve.X25519, Base64URL.encode(publicKey)).build();
        } else if (did.startsWith("zQ3s")) {
            return getJwk(publicKey, Curve.SECP256K1);                                                      //not tested as we cannot get DidVerificationMethod from SECP256K1 key (missing com.apicatalog.multicodec.Codec entry)
        } else if (did.startsWith("zDn")) {
            return getJwk(publicKey, Curve.P_256);                                                          //not tested as we cannot get DidVerificationMethod from P-256 key  (missing com.apicatalog.multicodec.Codec entry)
        } else if (did.startsWith("z82")) {
            return getJwk(publicKey, Curve.P_384);                                                          //not tested as we cannot get DidVerificationMethod from P-384 key (missing com.apicatalog.multicodec.Codec entry)
        } else if (did.startsWith("z2J9")) {
            return getJwk(publicKey, Curve.P_521);                                                          //not tested as we cannot get DidVerificationMethod from P-521 key (missing com.apicatalog.multicodec.Codec entry)
        } else if (did.startsWith("z4MX") || did.startsWith("zgg")) {
            PublicKey pk = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey)); //not tested as we cannot get DidVerificationMethod from RSA key (missing com.apicatalog.multicodec.Codec entry)
            return new RSAKey.Builder((RSAPublicKey) pk).build();
        } else {
            throw new IllegalArgumentException("The did key is not supported by JsonWebSignature2020 suite.");
        }
    }

    private static JWK getJwk(byte[] publicKey, Curve nimbusCurve){
        try {
            ECParameterSpec params = nimbusCurve.toECParameterSpec();
            int keySize = params.getCurve().getField().getFieldSize() / 8;
            KeyFactory fact = KeyFactory.getInstance("EC");
            byte[] x = copyOfRange(publicKey, 1, keySize + 1);
            byte[] y = copyOfRange(publicKey, keySize + 1, publicKey.length);
            ECPoint w = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
            ECPublicKeySpec ecPubSpec = new ECPublicKeySpec(w, params);
            ECPublicKey pub = (ECPublicKey) fact.generatePublic(ecPubSpec);
            return new ECKey.Builder(nimbusCurve, pub).build();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

}

