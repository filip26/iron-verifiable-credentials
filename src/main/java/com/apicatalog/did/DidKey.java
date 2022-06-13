package com.apicatalog.did;

import java.net.URI;

import com.apicatalog.multibase.Multibase;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Codec;
import com.apicatalog.multicodec.Multicodec.Type;

/**
 *
 * https://pr-preview.s3.amazonaws.com/w3c-ccg/did-method-key/pull/51.html
 *
 * did-key-format := did:key:MULTIBASE(base58-btc, MULTICODEC(public-key-type, raw-public-key-bytes))
 *
 */
public class DidKey {

    public static final String SCHEME = "did";
    public static final String KEY = "key";

    private final String version;
    
    private final Codec codec;
    
    private final byte[] rawKey;
    
    private final String encoded;

    protected DidKey(String version, Codec codec, byte[] rawValue, String encoded) {
        this.version = version;
        this.codec = codec;
        this.rawKey = rawValue;
        this.encoded = encoded;
    }

    /**
     * Creates a new DID key instance from the given {@link URI}.
     *
     * @param uri The source URI to be transformed into DID key
     * @return The new DID key
     * 
     * @throws NullPointerException
     *         If {@code uri} is {@code null}
     *         
     * @throws IllegalArgumentException
     *         If the given {@code uri} is not valid DID key
     */    
    public static final DidKey create(final URI uri) {

        if (!isDidKey(uri)) {
            throw new IllegalArgumentException("The given uri is not valid DID key, does not start with did:key prefix but [" + uri + "]");
        }

        // default DID key version
        String version = "1";

        String encoded = uri.getSchemeSpecificPart().substring(KEY.length() + 1);
        int versionIndex = encoded.indexOf(":");
        
        if (versionIndex != -1) {
            version = encoded.substring(0, versionIndex);
            encoded = encoded.substring(versionIndex + 1);            
        }
        
        if (!Multibase.isAlgorithmSupported(encoded)) {
            throw new IllegalArgumentException();
        }

        final byte[] decoded = Multibase.decode(encoded);

        final Codec codec = Multicodec.codec(Type.Key, decoded).orElseThrow(IllegalArgumentException::new);

        final byte[] rawKey = Multicodec.decode(codec, decoded);

        return new DidKey(version, codec, rawKey, encoded);
    }
    
    public static boolean isDidKey(final URI uri) {
        return SCHEME.equals(uri.getScheme())
                && uri.getSchemeSpecificPart() != null
                && uri.getSchemeSpecificPart().startsWith(KEY)
                ;
    }


    public String getVersion() {
        return version;
    }
    
    public Codec getCodec() {
        return codec;
    }

    public byte[] getRawKey() {
        return rawKey;
    }
    
    public String getPublicKeyEncoded() {
        return encoded;
    }

    @Override
    public String toString() {
        return SCHEME + ":" + KEY + (!"1".equals(version) ? ":" + version : "") + ":" + encoded; 
    }
    
    public URI toURI() {
        return URI.create(toString()); 
    }
}
