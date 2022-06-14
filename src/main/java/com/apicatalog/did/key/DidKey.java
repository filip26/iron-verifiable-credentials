package com.apicatalog.did.key;

import java.net.URI;

import com.apicatalog.did.Did;
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
public class DidKey extends Did {

    protected static final String METHOD_KEY = "key";
    
    private final Codec codec;
    
    private final byte[] rawKey;

    protected DidKey(Did did, Codec codec, byte[] rawValue) {
        super(did.getMethod(), did.getVersion(), did.getMethodSpecificId());
        this.codec = codec;
        this.rawKey = rawValue;
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
    public static final DidKey from(final URI uri) {

        final Did did = Did.from(uri);

        if (!METHOD_KEY.equalsIgnoreCase(did.getMethod())) {
            throw new IllegalArgumentException("The given URI [" + uri + "] is not valid DID key, does not start with 'did:key'.");
        }

        return from(did);
    }

    public static final DidKey from(final Did did) {
        
        if (!METHOD_KEY.equalsIgnoreCase(did.getMethod())) {
            throw new IllegalArgumentException("The given DID method [" + did.getMethod() + "] is not 'key'. DID [" + did.toString() + "].");
        }
        
        if (!Multibase.isAlgorithmSupported(did.getMethodSpecificId())) {
            throw new IllegalArgumentException();
        }

        final byte[] decoded = Multibase.decode(did.getMethodSpecificId());

        final Codec codec = Multicodec.codec(Type.Key, decoded).orElseThrow(IllegalArgumentException::new);

        final byte[] rawKey = Multicodec.decode(codec, decoded);

        return new DidKey(did, codec, rawKey);
    }

    public static boolean isDidKey(final Did did) {
        return !did.isDidUrl() && METHOD_KEY.equalsIgnoreCase(did.getMethod());
    }
    
    public static boolean isDidKey(final URI uri) {
        return Did.SCHEME.equals(uri.getScheme())
                && uri.getSchemeSpecificPart() != null
                && uri.getSchemeSpecificPart().toLowerCase().startsWith(METHOD_KEY + ":")
                //FIXME path .. #fragment must be blank
                ;
    }

    public static boolean isDidKey(final String uri) {
        //FIXME path .. #fragment must be blank
        return uri != null && uri.toLowerCase().startsWith(SCHEME + ":" + METHOD_KEY + ":");
    }
    
    public Codec getCodec() {
        return codec;
    }

    public byte[] getRawKey() {
        return rawKey;
    }    
}
