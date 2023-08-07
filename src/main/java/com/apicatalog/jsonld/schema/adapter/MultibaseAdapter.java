package com.apicatalog.jsonld.schema.adapter;

import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.Multibase.Algorithm;
import com.apicatalog.multicodec.Multicodec;

public class MultibaseAdapter implements LdValueAdapter<String, byte[]> {

    protected final Algorithm algorithm;
    protected final Multicodec codec;

    public MultibaseAdapter(Algorithm algorithm) {
        this(algorithm, null);
    }

    public MultibaseAdapter(Algorithm algorithm, Multicodec codec) {
        this.algorithm = algorithm;
        this.codec = codec;
    }

    @Override
    public byte[] read(String value) {

        final byte[] debased = Multibase.decode(value); // ;)

        if (codec == null) {
            return debased;
        }

        return codec.decode(debased);
    }

    @Override
    public String write(byte[] value) {

        if (codec == null) {
            return Multibase.encode(algorithm, value);
        }

        return Multibase.encode(algorithm, codec.encode(value));
    }
}
