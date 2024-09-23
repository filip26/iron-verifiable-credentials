package com.apicatalog.vc.status.bitstring;

import java.util.Objects;

public record Bitstring(byte[] bits, long length) {

    public static long DEFAULT_SIZE = 131072;

    public Bitstring {
        Objects.requireNonNull(bits);
        Objects.checkIndex(length - 1, bits.length * 8);
    }
    
    /**
     * Check if a given index is set or not.
     * 
     * @param index to check, starts with <code>0</code>
     * @return <code>true</code> if the given index is set
     * 
     * @throws IndexOutOfBoundsException
     */
    public boolean isSet(long index) throws IndexOutOfBoundsException {

        Objects.checkIndex(index, length);

        int byteIndex = (int) (index / 8);
        byte bitIndex = (byte) (index % 8);

        return (bits[byteIndex] & (0x80 >>> bitIndex)) != 0;
    }

    public String toString() {
        return Base2.encode(bits, 0, bits.length);
    }

}
