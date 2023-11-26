package com.apicatalog.vc;

import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.multikey.MultiKeyAdapter;
import com.apicatalog.uvarint.UVarInt;

public class TestKeyAdapter extends MultiKeyAdapter {

    protected static Multicodec PUBLIC_KEY_CODEC = new Multicodec(
            "test-pub",
            Tag.Key,
            12345l,
            UVarInt.encode(12345l));

    protected static Multicodec PRIVATE_KEY_CODEC = new Multicodec(
            "test-priv",
            Tag.Key,
            12346l,
            UVarInt.encode(12346l));

    protected static MulticodecDecoder DECODER = MulticodecDecoder.getInstance(
            PUBLIC_KEY_CODEC,
            PRIVATE_KEY_CODEC);

    public TestKeyAdapter() {
        super(DECODER);
    }

    @Override
    protected Multicodec getPublicKeyCodec(String algo, int keyLength) {
        return PUBLIC_KEY_CODEC;
    }

    @Override
    protected Multicodec getPrivateKeyCodec(String algo, int keyLength) {
        return PRIVATE_KEY_CODEC;
    }
}
