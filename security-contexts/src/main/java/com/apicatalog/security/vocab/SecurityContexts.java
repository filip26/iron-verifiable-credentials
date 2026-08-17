package com.apicatalog.security.vocab;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Objects;

/**
 * Provides static access to JSON-LD security contexts related to W3C Verifiable
 * Credentials (VCs).
 * <p>
 * This non-instantiable utility class maintains an immutable registry mapping
 * context URIs to local classpath resources and their corresponding SHA-256
 * integrity digests for off-line context retrieval and validation.
 */
public class SecurityContexts {

    private SecurityContexts() {
    }

    private static final Map<String, ContextResource> INDEX = Map.of(

            "https://www.w3.org/ns/credentials/v2", new ContextResource(
                    "credentials-v2.json",
                    "59955ced6697d61e03f2b2556febe5308ab16842846f5b586d7f1f7adec92734"),

            "https://w3id.org/security/data-integrity/v2", new ContextResource(
                    "vcdi-v2.json",
                    "67f21e6e33a6c14e5ccfd2fc7865f7474fb71a04af7e94136cb399dfac8ae8f4"),

            "https://www.w3.org/ns/did/v1", new ContextResource(
                    "did-v1.json",
                    "4f3eae5568c9c5f036a082088f9e192019ee06faa78973c87ff91d5421b88dad"),

            "https://w3id.org/security/suites/ed25519-2020/v1", new ContextResource(
                    "ed25519-2020-v1.json",
                    "b9e1ab971fd8bf2c7553e0c4a9438e0b9450afde1ea1ca5b2492368b9f549588"),

            "https://w3id.org/security/multikey/v1", new ContextResource(
                    "multikey-v1.json",
                    "ba2c182de2d92f7e47184bcca8fcf0beaee6d3986c527bf664c195bbc7c58597"),

            "https://w3id.org/security/jwk/v1", new ContextResource(
                    "jwk-v1.json",
                    "0f14b62f6071aafe00df265770ea0c7508e118247d79b7d861a406d2aa00bece"),

            "https://www.w3.org/ns/cid/v1", new ContextResource(
                    "cid-v1.json",
                    "ea216ecc1cb02cd39b693dba2250141e270ba0bf95890be107dd9a9e8e43de85"),

            "https://www.w3.org/2018/credentials/v1", new ContextResource(
                    "credentials-v1.json",
                    "ab4ddd9a531758807a79a5b450510d61ae8d147eab966cc9a200c07095b0cdcc"),

            "https://w3id.org/security/data-integrity/v1", new ContextResource(
                    "vcdi-v1.json",
                    "b5d829bd09aa7c42abc6efa0c8ed7635313b5487f37ccfce3ecd149ca9418554"));

    /**
     * Retrieves the context resource associated with the given URI.
     *
     * @param uri the context URI string
     * @return the corresponding {@link ContextResource}, or {@code null} if not
     *         found
     */
    public static ContextResource getContext(String uri) {
        return INDEX.get(uri);
    }

    /**
     * Opens an input stream for the context resource associated with the given URI.
     *
     * @param uri the context URI string
     * @return an {@link InputStream} for the resource, or {@code null} if the URI
     *         is not indexed or the resource is missing
     */
    public static InputStream getContextAsStream(String uri) {
        var resource = INDEX.get(uri);
        return resource != null ? resource.asInputStream() : null;
    }

    /**
     * Returns the context as byte array.
     *
     * @param uri the context URI string
     * @return a byte array containing resource contents, or {@code null} if the URI
     *         is not indexed
     * @throws UncheckedIOException if an I/O error occurs while reading the
     *                              resource
     */
    public static byte[] getContextAsBytes(String uri) {
        var resource = INDEX.get(uri);
        return resource != null ? resource.asBytes() : null;
    }

    /**
     * Represents a static context resource along with its expected SHA-256 digest.
     *
     * Example to obtain digest:
     * 
     * <pre>{@code
     *  curl -sL -H "Accept: application/ld+json" CONTEXT | openssl dgst -sha256
     * }</pre>
     * 
     * @param resource     the relative path name of the resource file
     * @param sha256Digest the expected SHA-256 hash bytes
     */
    public static record ContextResource(
            String resource,
            byte[] sha256Digest) {

        public ContextResource {
            Objects.requireNonNull(resource, "resource name must not be null");
            Objects.requireNonNull(sha256Digest, "SHA-256 digest must not be null");
        }

        /**
         * Constructs a context resource using a hexadecimal string representation of
         * the SHA-256 digest.
         *
         * @param resource the relative path name of the resource file
         * @param hash     the hex-encoded SHA-256 digest string
         */
        public ContextResource(String resource, String hash) {
            this(resource, HexFormat.of().parseHex(hash));
        }

        /**
         * Validates that the resource content matches its SHA-256 digest.
         *
         * @return {@code true} if the computed digest matches the expected digest;
         *         {@code false} if content does not match or resource is missing
         * @throws IllegalStateException if SHA-256 algorithm is unavailable
         * @throws UncheckedIOException  if an I/O error occurs during reading
         */
        public boolean isValid() {
            try {

                return isValid(asInputStream().readAllBytes());

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Validates that the resource content matches its SHA-256 digest.
         *
         * @param resource
         * @return {@code true} if the computed digest matches the expected digest;
         *         {@code false} if content does not match or resource is missing
         * @throws IllegalStateException if SHA-256 algorithm is unavailable
         */
        public boolean isValid(byte[] resource) {
            try {
                var digestor = MessageDigest.getInstance("SHA-256");

                var digest = digestor.digest(resource);

                return Arrays.equals(sha256Digest, digest);

            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e);
            }
        }

        /**
         * Returns the context as a byte array.
         *
         * @return byte array containing resource content
         * @throws IllegalStateException if resource stream cannot be opened
         * @throws UncheckedIOException  if an I/O error occurs
         */
        public byte[] asBytes() {
            try {
                return SecurityContexts.class.getResourceAsStream("contexts/" + resource).readAllBytes();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        /**
         * Opens an {@link InputStream} to read the resource file.
         *
         * @return an {@link InputStream} for the resource, or {@code null} if not found
         */
        public InputStream asInputStream() {
            return SecurityContexts.class.getResourceAsStream("contexts/" + resource);
        }
    }
}
