package com.apicatalog.vc;

import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.codec.KeyCodec;
import com.apicatalog.multikey.MultiKeyAdapter;

public class TestKeyAdapter extends MultiKeyAdapter {

    protected static MulticodecDecoder DECODER = MulticodecDecoder.getInstance(KeyCodec.ED25519_PRIVATE_KEY, KeyCodec.ED25519_PUBLIC_KEY);

    public TestKeyAdapter() {
        super(DECODER);
    }

    @Override
    protected byte[] encodeKey(String algorightm, byte[] key, boolean secret) {
        if (secret) {
            return KeyCodec.ED25519_PRIVATE_KEY.encode(key);
        }
        return KeyCodec.ED25519_PUBLIC_KEY.encode(key);
    }
}
