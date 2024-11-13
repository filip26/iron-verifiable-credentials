package com.apicatalog.vc;

import com.apicatalog.linkedtree.LinkedLiteral;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.linkedtree.literal.adapter.DataTypeNormalizer;
import com.apicatalog.multibase.Multibase;
import com.apicatalog.multibase.MultibaseAdapter;
import com.apicatalog.multibase.MultibaseLiteral;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.key.MulticodecKey;
import com.apicatalog.multicodec.key.MulticodecKeyLiteral;
import com.apicatalog.uvarint.UVarInt;

public class TestMulticodecKeyAdapter extends MultibaseAdapter implements DataTypeNormalizer<MulticodecKey> {

    static Multicodec PUBLIC_KEY_CODEC = Multicodec.of(
            "test-pub",
            Tag.Key,
            12345l);

    static Multicodec PRIVATE_KEY_CODEC = Multicodec.of(
            "test-priv",
            Tag.Key,
            12346l);

    static MulticodecDecoder CODECS = MulticodecDecoder.getInstance(
            PUBLIC_KEY_CODEC,
            PRIVATE_KEY_CODEC);

    @Override
    public LinkedLiteral materialize(String source) throws NodeAdapterError {
        
        Multibase base = decoder.getBase(source).orElseThrow(IllegalArgumentException::new);

        return getKey(source, base, decoder.decode(source));
    }

    @Override
    public Class<? extends LinkedLiteral> typeInterface() {
        return MulticodecKeyLiteral.class;
    }

    protected static final MulticodecKeyLiteral getKey(String source, Multibase base, final byte[] encodedKey) throws NodeAdapterError {

        if (encodedKey == null || encodedKey.length == 0) {
            return null;
        }

        return CODECS.getCodec(encodedKey)
                .map(codec -> new MulticodecKeyLiteral(source, MultibaseLiteral.typeName(), codec, base, codec.decode(encodedKey)))
                .orElseThrow(() -> new NodeAdapterError("Unsupported multicodec code=" + UVarInt.decode(encodedKey) + "."));
    }

    @Override
    public String normalize(MulticodecKey value) {
        
        if (value instanceof MulticodecKeyLiteral literal) {
            return literal.lexicalValue();
        }
        
        if (value == null || value.rawBytes() == null || value.rawBytes().length == 0) {
            return null;
        }
        
        return Multibase.BASE_58_BTC.encode(value.codec().encode(value.rawBytes()));
    }
}
