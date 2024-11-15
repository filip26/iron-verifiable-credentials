package com.apicatalog.vc;

import com.apicatalog.multicodec.Multicodec;
import com.apicatalog.multicodec.Multicodec.Tag;
import com.apicatalog.multicodec.MulticodecDecoder;
import com.apicatalog.multicodec.key.MulticodecKeyMapper;

public class TestMulticodecKeyMapper extends MulticodecKeyMapper {

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
    
    public TestMulticodecKeyMapper() {
        super(CODECS);
    }
    
}
