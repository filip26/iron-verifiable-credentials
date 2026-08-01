package com.apicatalog.crypto.gc.kms;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Objects;

import com.google.cloud.kms.v1.AsymmetricSignRequest;
import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm;
import com.google.cloud.kms.v1.Digest;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.protobuf.ByteString;

/**
 * Provides digital signature operations using Google Cloud KMS private keys
 * without exposing private key material locally.
 */
public final class KmsAsymmetricSigner {

    @FunctionalInterface
    private interface RequestFactory {
        AsymmetricSignRequest createRequest(KeyManagementServiceClient kms, String resource, byte[] data);
    }

    private final RequestFactory requestFactory;
    private final KeyManagementServiceClient kms;
    private final String kmsKeyResource;

    /**
     * Constructs a new {@code KmsAsymmetricSigner}.
     *
     * @param requestFactory factory function to build signing requests
     * @param kms            Google Cloud KMS client instance
     * @param kmsKeyResource full resource identifier of the KMS crypto key version
     */
    public KmsAsymmetricSigner(
            RequestFactory requestFactory,
            KeyManagementServiceClient kms,
            String kmsKeyResource) {
        this.requestFactory = requestFactory;
        this.kms = kms;
        this.kmsKeyResource = kmsKeyResource;
    }

    /**
     * Creates a new {@link KmsAsymmetricSigner} instance for the specified KMS
     * algorithm.
     *
     * @param algorithm      the KMS cryptographic key algorithm
     * @param kmsKeyResource full resource identifier of the KMS key version
     * @param kms            Google Cloud KMS client instance
     * @return configured {@link KmsAsymmetricSigner} instance
     * @throws IllegalArgumentException if the algorithm is unsupported or null
     */
    public static KmsAsymmetricSigner newInstance(
            CryptoKeyVersionAlgorithm algorithm,
            String kmsKeyResource,
            KeyManagementServiceClient kms) {

        Objects.requireNonNull(algorithm, "CryptoKeyVersionAlgorithm must not be null\"");
        Objects.requireNonNull(kmsKeyResource, "kmsKeyResource must not be null");
        Objects.requireNonNull(kms, "kms client must not be null");

        return switch (algorithm) {
        case EC_SIGN_P256_SHA256 -> new KmsAsymmetricSigner(
                KmsAsymmetricSigner::ec256Sign,
                kms,
                kmsKeyResource);

        case EC_SIGN_P384_SHA384 -> new KmsAsymmetricSigner(
                KmsAsymmetricSigner::ec384Sign,
                kms,
                kmsKeyResource);

        case EC_SIGN_ED25519 -> new KmsAsymmetricSigner(
                KmsAsymmetricSigner::ed256Sign,
                kms,
                kmsKeyResource);

        // PQ
        case PQ_SIGN_SLH_DSA_SHA2_128S,
                PQ_SIGN_ML_DSA_44,
                PQ_SIGN_ML_DSA_87 ->
            new KmsAsymmetricSigner(
                    KmsAsymmetricSigner::dsaSign,
                    kms,
                    kmsKeyResource);

        default -> throw new IllegalArgumentException("Unsupported KMS Key Algorithm [" + algorithm + "]");
        };
    }

    /**
     * Signs input data using the configured Google Cloud KMS key.
     *
     * @param data byte array to sign, must not be null
     * @return generated signature byte array
     * @throws SignatureException if signature generation fails
     */
    public byte[] sign(byte[] data) throws SignatureException {
        return kms.asymmetricSign(requestFactory.createRequest(kms, kmsKeyResource, data))
                .getSignature()
                .toByteArray();
    }

    private static AsymmetricSignRequest ed256Sign(KeyManagementServiceClient kms, String resource, byte[] blob) {
        return AsymmetricSignRequest.newBuilder()
                .setName(resource)
                .setData(ByteString.copyFrom(blob))
                .build();
    }

    private static AsymmetricSignRequest ec256Sign(KeyManagementServiceClient kms, String resource, byte[] blob) {
        try {
            final var hash = MessageDigest.getInstance("SHA-256").digest(blob);
            return AsymmetricSignRequest.newBuilder()
                    .setName(resource)
                    .setDigest(Digest.newBuilder()
                            .setSha256(ByteString.copyFrom(hash))
                            .build())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 digest algorithm not available", e);
        }
    }

    private static AsymmetricSignRequest ec384Sign(KeyManagementServiceClient kms, String resource, byte[] blob) {
        try {
            final var hash = MessageDigest.getInstance("SHA-384").digest(blob);
            return AsymmetricSignRequest.newBuilder()
                    .setName(resource)
                    .setDigest(Digest.newBuilder()
                            .setSha384(ByteString.copyFrom(hash))
                            .build())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-384 digest algorithm not available", e);
        }
    }

    private static AsymmetricSignRequest dsaSign(KeyManagementServiceClient kms, String resource, byte[] blob) {
        return AsymmetricSignRequest.newBuilder()
                .setName(resource)
                .setData(ByteString.copyFrom(blob))
                .build();
    }
}
