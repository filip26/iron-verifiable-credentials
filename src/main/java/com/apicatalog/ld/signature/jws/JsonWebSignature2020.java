package com.apicatalog.ld.signature.jws;

import com.apicatalog.ld.signature.primitive.MessageDigest;
import com.apicatalog.ld.signature.primitive.Urdna2015;
import com.apicatalog.ld.signature.proof.ProofOptions;
import com.apicatalog.ld.signature.proof.VerificationMethod;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.JWK;

import java.net.URI;
import java.util.Objects;


/**
 * For JsonWebSignature2020 use following JSON-LD document: <a href="https://w3id.org/security/suites/jws-2020/v1">https://w3id.org/security/suites/jws-2020/v1</a>
 * (this should be loaded by your implementation of {@link com.apicatalog.jsonld.loader.DocumentLoader}).
 * <p />
 * <p />
 * Note:
 * <ul>
 *      <li> This signature suite is defined <a href="https://w3c-ccg.github.io/lds-jws2020/">here</a></li>
 *      <li> JsonWebSignature2020 suite uses: </li>
 *      <ul>
 *          <li><a href="https://w3c-ccg.github.io/data-integrity-spec/">LD-PROOF Signature and Verification algorithms</a></li>
 *          <li><a href="https://w3id.org/security#URDNA2015">URDNA2015 canonicalization algorithm</a> including <a href="https://json-ld.github.io/rdf-dataset-canonicalization/spec/">RDF Dataset Normalization</a></li>
 *          <li><a href="https://www.rfc-editor.org/rfc/rfc4634">SHA-256 Message Digest Algorithm</a></li>
 *          <li><a href="https://www.rfc-editor.org/rfc/rfc7797">JWS with Unencoded Payload Option</a></li>
 *      </ul>
 *      <li> There are 2 JSON-LD documents </li>
 *      <ul>
 *          <li> <a href="https://w3id.org/security/suites/jws-2020/v1">https://w3id.org/security/suites/jws-2020/v1</a> - has both public key "JsonWebKey2020" and "privateKeyJwk", doesn't have "proof" <i>-> USE THIS ONE</i> </li>
 *          <li> <a href="https://ns.did.ai/suites/jws-2020/v1/">https://ns.did.ai/suites/jws-2020/v1/</a> - has only public key "JsonWebKey2020" (with "controller" and "revoked"), and has "proof"  </li>
 *      </ul>
 *      <li>As a basis for creation was used {@link com.apicatalog.ld.signature.ed25519.Ed25519Signature2020}</li>
 *      <li>This JsonWebSignature2020 suite implementation uses <a href="https://connect2id.com/products/nimbus-jose-jwt">Nimbus library</a> for JWK and JWS handling.</li>
 * </ul>
 *
 * @author petr apeltauer, KAPRION Technologies GmbH
 */
public class JsonWebSignature2020 extends JwsSignatureSuite {

//    static { //https://connect2id.com/products/nimbus-jose-jwt/jca-algorithm-support
//        Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
//        Security.addProvider(BouncyCastleProviderSingleton.getInstance());
//    }

    protected static final String BASE = "https://w3id.org/security#";
    protected static final String TYPE = "JsonWebSignature2020";

    /**
     * @param alg signature algorithm. It must be one of the following: EdDSA, ES256K, ES256, ES384, PS256. <p />
     *            It will be used in JWS for the protected header, e.g. {"alg":"ES256K","b64": false,"crit": ["b64"]}.
     *            Where all attributes except "alg" are fixed and defined by JsonWebSignature2020.
     *            "b64" set to false means that the payload in the JWS (JWT) is detached.
     */
    public JsonWebSignature2020(String alg) {
        super(
                BASE + TYPE,
                new Urdna2015(),
                new MessageDigest("SHA-256"),
                new JsonWebSignature2020Provider(alg),
                new JsonWebProof2020Adapter(),
                alg
        );
        //Check allowed signature alg. as specified here: https://w3c-ccg.github.io/lds-jws2020/#jose-conformance
        if(!alg.equals("EdDSA")
                && !alg.equals("ES256K")
                && !alg.equals("ES256")
                && !alg.equals("ES384")
                && !alg.equals("PS256")) {
            throw new IllegalArgumentException(getUnsupportedAlgErr(alg));
        }
    }

    public static boolean isTypeOf(final String type) {
        return Objects.equals(BASE + TYPE, type);
    }

    public static ProofOptions createOptions(VerificationMethod method, URI purpose) {
        return ProofOptions.create(BASE + TYPE, method, purpose);
    }

    /**
     * Convert Proof Options to unsigned JWS proof
     * Based on {@link ProofOptions#toUnsignedProof()}
     *
     * @return {@link JwsProof} with JWS as proof value
     */
    public static JwsProof toUnsignedJwsProof(ProofOptions proofOptions) {
        final JwsProof proof = new JwsProof();
        proof.type = proofOptions.type();
        proof.created = proofOptions.created();
        proof.domain = proofOptions.domain();
        proof.purpose = proofOptions.purpose();
        proof.verificationMethod = proofOptions.verificationMethod();
        return proof;
    }

    /**
     * Get curve for given signature algorithm
     *
     * @param alg algorithm
     * @return related curve or null if there is no related curve (PS256 alg.)
     * @throws IllegalArgumentException thrown if provided algorithm is not supported by JsonWebSignature2020
     */
    public static Curve getCurve(String alg) throws IllegalArgumentException {
        //Based on https://w3c-ccg.github.io/lds-jws2020/#jose-conformance
        Curve curve;
        switch (alg) {
            case "EdDSA":
                curve = Curve.Ed25519;
                break;
            case "ES256K":
                curve = Curve.SECP256K1;
                break;
            case "ES256":
                curve = Curve.P_256;
                break;
            case "ES384":
                curve = Curve.P_384;
                break;
            case "PS256":
                curve = null;
                break;
            default:
                throw new IllegalArgumentException(getUnsupportedAlgErr(alg));
        }
        return curve;
    }

    /**
     * Get signature algorithm for given curve
     *
     * @param curve curve or null if RSA type is used -> in this case it will return "PS256" alg.
     * @return related algorithm
     * @throws IllegalArgumentException thrown if provided curve is not supported by JsonWebSignature2020
     */
    public static String getAlgorithm(String curve) throws IllegalArgumentException {
        //Based on https://w3c-ccg.github.io/lds-jws2020/#jose-conformance
        if(curve == null)
            return "PS256";

        Curve crv = Curve.parse(curve);
        String alg;
        if (Curve.Ed25519.equals(crv)) {
            alg = "EdDSA";
        } else if (Curve.SECP256K1.equals(crv)) {
            alg = "ES256K";
        } else if (Curve.P_256.equals(crv)) {
            alg = "ES256";
        } else if (Curve.P_384.equals(crv)) {
            alg = "ES384";
        } else {
            throw new IllegalArgumentException(getUnsupportedCurveErr(curve));
        }
        return alg;
    }

    /**
     * Get signature algorithm from given JWK
     *
     * @param jwk Json Web Key
     * @return related algorithm
     */
    public static String getAlgorithm(JWK jwk) {
        Object crv = jwk.toJSONObject().get("crv");
        String curveName = ((crv != null) ? crv.toString() : null);
        System.out.println("SIGNING TEST - curve = " + curveName);
        String algorithm = JsonWebSignature2020.getAlgorithm(curveName);
        System.out.println("SIGNING TEST - algorithm = " + algorithm);
        return algorithm;
    }

    public static String getUnsupportedAlgErr(String algorithm) {
        return "Signature algorithm \"" + algorithm + "\" is not supported by JsonWebSignature2020.";
    }

    public static String getUnsupportedCurveErr(String curve) {
        return "Key curve \"" + curve + "\" is not supported by JsonWebSignature2020.";
    }

}
