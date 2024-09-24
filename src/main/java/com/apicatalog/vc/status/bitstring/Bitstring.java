package com.apicatalog.vc.status.bitstring;

import java.util.Arrays;
import java.util.Objects;

public record Bitstring(byte[] bits, long length) {

    public static long DEFAULT_SIZE = 131072;

    public Bitstring {
        Objects.requireNonNull(bits);
        Objects.checkIndex(length - 1, bits.length * 8);
    }

    public static Bitstring ofZeros(int length) {
        int bytes = (int) (length / 8) + (length % 8 != 0 ? 1 : 0);
        byte[] bits = new byte[bytes];
        Arrays.fill(bits, (byte) 0);
        return new Bitstring(bits, length);
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

    public Bitstring set(long index) throws IndexOutOfBoundsException {
        return bit(index, true);
    }

    public Bitstring unset(long index) throws IndexOutOfBoundsException {
        return bit(index, false);
    }

    public Bitstring bit(long index, boolean set) throws IndexOutOfBoundsException {

        Objects.checkIndex(index, length);

        int byteIndex = (int) (index / 8);
        byte bitIndex = (byte) (index % 8);

        if (set) {
            bits[byteIndex] |= (0x80 >>> bitIndex);
        } else {
            bits[byteIndex] &= (0xff7f >>> bitIndex);
        }

        return this;
    }

    public int bits(long index, int length) throws IndexOutOfBoundsException {

        Objects.checkIndex(length - 1, 7); // max 8 bits
        Objects.checkIndex(index + length - 1, this.length);

        int code = 0;

        for (long i = 0; i < length; i++) {
            code |= isSet(index + length - i - 1)
                    ? 1 << (i)
                    : 0;

        }
        return code;

    }
}
