package com.apicatalog.security;

import java.util.function.Function;

@FunctionalInterface
public interface HmacFactory {

    Function<byte[], byte[]> newHmac(byte[] hmacKey);

}
