package com.apicatalog.vc;

import com.apicatalog.linkedtree.LinkedFragment;
import com.apicatalog.linkedtree.adapter.NodeAdapterError;
import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multikey.MultiKeyAdapter;

public class TestKeyAdapter extends MultiKeyAdapter {

    protected static Multicodec PUBLIC_KEY_CODEC = Multicodec.of(
            "test-pub",
            Tag.Key,
            12345l);

    protected static Multicodec PRIVATE_KEY_CODEC = Multicodec.of(
            "test-priv",
            Tag.Key,
            12346l);

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

    @Override
    public Class<?> typeInterface() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object materialize(LinkedFragment source) throws NodeAdapterError {
        // TODO Auto-generated method stub
        return null;
    }
}
