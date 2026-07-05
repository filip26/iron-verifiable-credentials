package com.apicatalog.crypto.gc.kms;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import com.google.cloud.kms.v1.AsymmetricSignRequest;
import com.google.cloud.kms.v1.CryptoKeyVersion.CryptoKeyVersionAlgorithm;
import com.google.cloud.kms.v1.Digest;
import com.google.cloud.kms.v1.KeyManagementServiceClient;
import com.google.protobuf.ByteString;

public final class KmsAsymmericSigner {

    @FunctionalInterface
    private interface RequestFactory {
        AsymmetricSignRequest createRequest(KeyManagementServiceClient kms, String resource, byte[] data);
    }

    private final RequestFactory requestFactory;
    private final KeyManagementServiceClient kms;
    private final String kmsKeyResource;

    public KmsAsymmericSigner(
            RequestFactory requestFactory,
            KeyManagementServiceClient kms,
            String kmsKeyResource) {
        this.requestFactory = requestFactory;
        this.kms = kms;
        this.kmsKeyResource = kmsKeyResource;
    }

    /**
     * Creates a new {@link KmsAsymmericSigner} instance for the specified KMS
     * algorithm
     */
    public static KmsAsymmericSigner newInstance(
            CryptoKeyVersionAlgorithm algorithm,
            String kmsKeyResource,
            KeyManagementServiceClient kms) {

        return switch (algorithm) {
        case EC_SIGN_P256_SHA256 -> new KmsAsymmericSigner(
                KmsAsymmericSigner::ec256Sign,
                kms,
                kmsKeyResource);

        case EC_SIGN_P384_SHA384 -> new KmsAsymmericSigner(
                KmsAsymmericSigner::ec384Sign,
                kms,
                kmsKeyResource);

        case EC_SIGN_ED25519 -> new KmsAsymmericSigner(
                KmsAsymmericSigner::ed256Sign,
                kms,
                kmsKeyResource);

        // PQ experiments
        case PQ_SIGN_SLH_DSA_SHA2_128S -> new KmsAsymmericSigner(
                KmsAsymmericSigner::dsaSign,
                kms,
                kmsKeyResource);

        case PQ_SIGN_ML_DSA_44 -> new KmsAsymmericSigner(
                KmsAsymmericSigner::dsaSign,
                kms,
                kmsKeyResource);

        case PQ_SIGN_ML_DSA_87 -> new KmsAsymmericSigner(
                KmsAsymmericSigner::dsaSign,
                kms,
                kmsKeyResource);

        case null, default ->
            throw new IllegalArgumentException("Unsupported KMS Key Algorithm [" + algorithm + "]");
        };
    }

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
            throw new IllegalStateException(e);
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
            throw new IllegalStateException(e);
        }
    }

    private static AsymmetricSignRequest dsaSign(KeyManagementServiceClient kms, String resource, byte[] blob) {
        return AsymmetricSignRequest.newBuilder()
                .setName(resource)
                .setData(ByteString.copyFrom(blob))
                .build();
    }
}
