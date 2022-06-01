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

    private final Codec codec;
    private final byte[] rawKey;
    
    protected DidKey(Codec codec, byte[] rawValue) {
        this.codec = codec;
        this.rawKey = rawValue;
    }
    
    /**
     * Creates a new {@link DidKey} instance from the given {@link URI}.
     * 
     * @param uri
     * @return
     */
    public static final DidKey create(URI uri) {
       
        if (!SCHEME.equals(uri.getScheme())
                || uri.getSchemeSpecificPart() == null 
                || !uri.getSchemeSpecificPart().startsWith(KEY)
                ) {
            throw new IllegalArgumentException();
        }
        
        //FIXME version scheme:key:version:encoded
        final String encoded = uri.getSchemeSpecificPart().substring(KEY.length() + 1);
        
        if (!Multibase.isAlgorithmSupported(encoded)) {
            throw new IllegalArgumentException();
        }
        
        final byte[] decoded = Multibase.decode(encoded);
        
        final Codec codec = Multicodec.codec(Type.Key, decoded).orElseThrow(IllegalArgumentException::new);

        final byte[] rawKey = Multicodec.decode(codec, decoded);
                
        return new DidKey(codec, rawKey);
    }
    
    public Codec getCodec() {
        return codec;
    }

    public byte[] getRawKey() {
        return rawKey;
    }
}
