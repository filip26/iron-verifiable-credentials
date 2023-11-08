package com.apicatalog.ld.signature.key;

import com.apicatalog.multicodec.Multicodec;

public final class MulticodecKey {

    private final Multicodec codec;
    private final byte[] raw;
    
    protected MulticodecKey(Multicodec codec, byte[] raw) {
        this.codec = codec;
        this.raw =raw;
    }
    
    public static final MulticodecKey getInstance(Multicodec codec, byte[] raw) {
        return new MulticodecKey(codec, raw);
    }
    
    public final byte[] getEbcided() {
        return codec.encode(raw);
    }
    
    /**
     * A codec describing the key
     * 
     * @return a codec
     */
    public Multicodec codec() {
        return codec;
    }
    
    /**
     * A raw byte array representing the key.
     * 
     * @return a raw byte array
     */
    public byte[] bytes() {
        return raw;
    }
}
