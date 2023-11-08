package com.apicatalog.jsonld.schema.adapter;

import com.apicatalog.ld.signature.key.MulticodecKey;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicoder;

public class MultibaseAdapter implements LdValueAdapter<String, MulticodecKey> {

    protected final Algorithm algorithm;
    protected final Multicoder multicoder;

    public MultibaseAdapter(Algorithm algorithm, Multicoder multicoder) {
        this.algorithm = algorithm;
        this.multicoder = multicoder;
    }

    @Override
    public MulticodecKey read(String value) {

        final byte[] debased = Multibase.decode(value);

        final Multicodec code = multicoder.getCodec(debased)
                .orElseThrow(() -> new IllegalArgumentException("Unsupported multicodec "));
        
        final byte[] raw  = code.decode(debased);
        
        return MulticodecKey.getInstance(code, raw);
    }

    @Override
    public String write(MulticodecKey value) {
        return Multibase.encode(algorithm, value.getEbcided());
    }
}
