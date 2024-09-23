package com.apicatalog.vc.status.bitstring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BistringTest {

    @Test
    void testIsSetInRange() {
        var bstr = new Bitstring(new byte[] { 0, 0, 0, (byte) 0x80 }, 30);

        assertEquals(bstr.length(), 30);

        for (int i = 0; i < bstr.length(); i++) {
            assertEquals(i == (8 * 3), bstr.isSet(i), "index: " + i);
        }
    }

    @Test
    void testIsSetOneByte() {
        var bstr = new Bitstring(new byte[] { (byte) 0x01 }, 8);
        assertEquals(bstr.length(), 8);

        for (int i = 0; i < bstr.length(); i++) {
            assertEquals(i == 7, bstr.isSet(i), "index: " + i);
        }
    }

    @Test
    void testIsSetTwoBytes() {
        var bstr = new Bitstring(new byte[] { 0, (byte) 0x80 }, 9);
        assertEquals(bstr.length(), 9);

        for (int i = 0; i < bstr.length(); i++) {
            assertEquals(i == 8, bstr.isSet(i), "index: " + i);
        }
    }

    @Test
    void testIsSetOutOfRangeByte() {
        var bstr = new Bitstring(new byte[] { (byte) 0x02 }, 4);
        assertEquals(bstr.length(), 4);

        for (int i = 0; i < bstr.length(); i++) {
            assertEquals(i == 5, bstr.isSet(i), "index: " + i);
        }
    }

    @Test
    void testOfZeros() {
        var bstr = Bitstring.ofZeros(100);
        assertEquals(13, bstr.bits().length);
        for (int i = 0; i < bstr.bits().length; i++) {
            assertEquals(0, bstr.bits()[i]);
        }
    }

    @Test
    void testSetUnset() {
        var bstr = Bitstring.ofZeros(10).set(9).set(1).set(3).unset(1);
        for (int i = 0; i < bstr.length(); i++) {
            assertEquals(i == 3 || i == 9, bstr.isSet(i), "index: " + i);
        }
    }

    @Test
    void testOutOfRangeUp() {
        assertThrows(IndexOutOfBoundsException.class, () -> new Bitstring(new byte[] { (byte) 0x02 }, 9));
    }

    @Test
    void testOutOfRangeDown() {
        assertThrows(IndexOutOfBoundsException.class, () -> new Bitstring(new byte[] { (byte) 0x02 }, 0));
    }

    @Test
    void testEmpty() {
        assertThrows(IndexOutOfBoundsException.class, () -> new Bitstring(new byte[0], 1));
    }

}
