package com.apicatalog.vc.status.bitstring;

public record Bitstring(byte[] bits, long length) {

    public static long DEFAULT_SIZE = 131072;

    /**
     * Check if a given index is set or not.
     * 
     * @param index to check
     * @return <code>true</code> if the given index is set
     * 
     * @throws IllegalArgumentException
     * @throws IndexOutOfBoundsException
     */
    public boolean isSet(long index) throws IllegalArgumentException, IndexOutOfBoundsException {

        if (index < 0) {
            throw new IllegalArgumentException("The index must greater or equal to zero but is " + index);
        }
        if (index > length) {
            throw new IndexOutOfBoundsException(index);
        }

        int byteIndex = (int) (index / 8);
        byte bitIndex = (byte) (index % 8);

        return (bits[byteIndex] & (0x80 >>> bitIndex)) != 0;
    }

    public String toString() {
        return Base2.encode(bits, 0, bits.length);
    }

}
