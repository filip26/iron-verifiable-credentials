package com.apicatalog.jsonld.schema.adapter;

import com.apicatalog.ld.DocumentError;
import com.apicatalog.ld.signature.key.MulticodecKey;

public class MultibaseAdapter implements LdValueAdapter<String, MulticodecKey> {

    @Override
    public MulticodecKey read(String value) throws DocumentError {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String write(MulticodecKey value) throws DocumentError {
        // TODO Auto-generated method stub
        return null;
    }

//    protected final Algorithm algorithm;
//    protected final Multicoder multicoder;
//
//    public MultibaseAdapter(Algorithm algorithm, Multicoder multicoder) {
//        this.algorithm = algorithm;
//        this.multicoder = multicoder;
//    }
//
//    @Override
//    public MulticodecKey read(String value) {
//
//        final byte[] debased = Multibase.decode(value);
//
//        final Multicodec codec = multicoder.getCodec(debased)
//                .orElseThrow(() -> new IllegalArgumentException("Unsupported multicodec "));
//        
//        final byte[] raw  = codec.decode(debased);
//        
//        return MulticodecKey.getInstance(codec, raw);
//    }
//
//    @Override
//    public String write(MulticodecKey value) {
//        return Multibase.encode(algorithm, value.getEbcided());
//    }
}
