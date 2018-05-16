package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;

class LcdImageLineTest {

    @Test
    void colorsMapWork() {
        BitVector.Builder bvb = new BitVector.Builder(32);
        bvb.setInt(0, 0b11111111_11111111_00000000_00000000);
        BitVector bv1 = bvb.build();
        bvb = new BitVector.Builder(32);
        bvb.setInt(0, 0b11111111_00000000_11111111_00000000);
        BitVector bv2 = bvb.build();

        LcdImageLine lcdLine = new LcdImageLine(bv1, bv2, new BitVector(32));
        LcdImageLine result1 = lcdLine.mapColors(0b10110100);
        LcdImageLine result2 = lcdLine.mapColors(0b11111111);
        LcdImageLine result3 = lcdLine.mapColors(0b01011011);

        assertEquals("11111111111111110000000000000000",
                result1.msb().toString());
        assertEquals("00000000111111111111111100000000",
                result1.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                result2.lsb().toString());
        assertEquals(result2.msb(), result2.lsb());
        assertEquals("00000000000000001111111111111111",
                result3.msb().toString());
        assertEquals("11111111111111110000000011111111",
                result3.lsb().toString());
    }

//    @Test
//    void equalsAndHashCodeWork() {
//        BitVector msb = new BitVector(4 * 32, true);
//        BitVector lsb = new BitVector(4 * 32);
//        BitVector opa = BitVector.rand();
//        BitVector msb2 = new BitVector(4 * 32, true);
//        LcdImageLine line1 = new LcdImageLine(msb, lsb, opa);
//        LcdImageLine line2 = new LcdImageLine(msb2, new BitVector(lsb),
//                new BitVector(opa));
//        assertTrue(msb.equals(msb2));
//        assertEquals(line1.hashCode(), line2.hashCode());
//    }

    public static BitVector v0 = new BitVector(32, false);
    public static BitVector v1 = new BitVector(32, true);
    // 11001100000000001010101011110000
    public static BitVector v3 = new BitVector.Builder(32)
            .setByte(0, 0b1111_0000).setByte(1, 0b1010_1010)
            .setByte(3, 0b1100_1100).build();
    // 00000000110101011111111000000000
    public static BitVector v4 = new BitVector.Builder(32)
            .setByte(0, 0b0000_0000).setByte(1, 0b1111_1110)
            .setByte(2, 0b1101_0101).build();
    public static BitVector v5 = new BitVector(64);

    // TESTS CONSTRUCTEUR / GETSIZE / GETMSB / GETLSB / GETOPACITY
    // Cqs Normal
    @Test
    public void constructeurGetTestNormal() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        // GETSIZE
        assertEquals(32, l0.size());
        assertEquals(32, l1.size());
        assertEquals(32, l2.size());
        assertEquals(32, l3.size());
        assertEquals(32, l4.size());
        assertEquals(32, l5.size());
        assertEquals(32, l6.size());
        assertEquals(32, l7.size());

        // GETMSB
        assertEquals(v0, l0.msb());
        assertEquals(v1, l1.msb());
        assertEquals(v1, l2.msb());
        assertEquals(v0, l3.msb());
        assertEquals(v3, l4.msb());
        assertEquals(v4, l5.msb());
        assertEquals(v3, l6.msb());
        assertEquals(v4, l7.msb());

        // GETLSB
        assertEquals(v0, l0.lsb());
        assertEquals(v1, l1.lsb());
        assertEquals(v0, l2.lsb());
        assertEquals(v1, l3.lsb());
        assertEquals(v3, l4.lsb());
        assertEquals(v4, l5.lsb());
        assertEquals(v4, l6.lsb());
        assertEquals(v3, l7.lsb());

        // GETOPACITY
        assertEquals(v0, l0.opacity());
        assertEquals(v1, l1.opacity());
        assertEquals(v1, l2.opacity());
        assertEquals(v0, l3.opacity());
        assertEquals(v3, l4.opacity());
        assertEquals(v4, l5.opacity());
        assertEquals(v3, l6.opacity());
        assertEquals(v4, l7.opacity());
    }

    // Cas d'erreur
    @Test
    public void constructeurTestError() {
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0 = new LcdImageLine(v5, v0, v1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0 = new LcdImageLine(v4, v5, v3);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0 = new LcdImageLine(v4, v0, v5);
        });
    }

    // TESTS SHIFT
    @Test
    public void shiftTestNormal() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        l0 = l0.shift(4);
        l1 = l1.shift(-6);
        l2 = l2.shift(10);
        l3 = l3.shift(-9);
        l4 = l4.shift(1);
        l5 = l5.shift(-1);
        l6 = l6.shift(32);
        l7 = l7.shift(-32);

        assertEquals("00000000000000000000000000000000", l0.msb().toString());
        assertEquals("00000000000000000000000000000000", l0.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l0.opacity().toString());

        assertEquals("00000011111111111111111111111111", l1.msb().toString());
        assertEquals("00000011111111111111111111111111", l1.lsb().toString());
        assertEquals("00000011111111111111111111111111",
                l1.opacity().toString());

        assertEquals("11111111111111111111110000000000", l2.msb().toString());
        assertEquals("00000000000000000000000000000000", l2.lsb().toString());
        assertEquals("11111111111111111111110000000000",
                l2.opacity().toString());

        assertEquals("00000000000000000000000000000000", l3.msb().toString());
        assertEquals("00000000011111111111111111111111", l3.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l3.opacity().toString());

        assertEquals("10011000000000010101010111100000", l4.msb().toString());
        assertEquals("10011000000000010101010111100000", l4.lsb().toString());
        assertEquals("10011000000000010101010111100000",
                l4.opacity().toString());

        assertEquals("00000000011010101111111100000000", l5.msb().toString());
        assertEquals("00000000011010101111111100000000", l5.lsb().toString());
        assertEquals("00000000011010101111111100000000",
                l5.opacity().toString());

        assertEquals("00000000000000000000000000000000", l6.msb().toString());
        assertEquals("00000000000000000000000000000000", l6.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l6.opacity().toString());

        assertEquals("00000000000000000000000000000000", l7.msb().toString());
        assertEquals("00000000000000000000000000000000", l7.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l7.opacity().toString());
    }

    // TESTS EXTRACTWRAPPED
    // Cas normal
    @Test
    public void extractWrappedNormalTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        LcdImageLine l0a = l0.extractWrapped(-20, 64);
        LcdImageLine l1a = l1.extractWrapped(-54, 32);
        l0 = l0.extractWrapped(7, 32);
        l1 = l1.extractWrapped(9, 64);
        l2 = l2.extractWrapped(-3, 32);
        l3 = l3.extractWrapped(3, 32);
        l4 = l4.extractWrapped(-5, 32);
        l5 = l5.extractWrapped(5, 32);
        l6 = l6.extractWrapped(-32, 32);
        l7 = l7.extractWrapped(0, 32);

        assertEquals("00000000000000000000000000000000", l0.msb().toString());
        assertEquals("00000000000000000000000000000000", l0.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l0.opacity().toString());

        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000000",
                l0a.msb().toString());
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000000",
                l0a.lsb().toString());
        assertEquals(
                "0000000000000000000000000000000000000000000000000000000000000000",
                l0a.opacity().toString());

        assertEquals(
                "1111111111111111111111111111111111111111111111111111111111111111",
                l1.msb().toString());
        assertEquals(
                "1111111111111111111111111111111111111111111111111111111111111111",
                l1.lsb().toString());
        assertEquals(
                "1111111111111111111111111111111111111111111111111111111111111111",
                l1.opacity().toString());

        assertEquals("11111111111111111111111111111111", l1a.msb().toString());
        assertEquals("11111111111111111111111111111111", l1a.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l1a.opacity().toString());

        assertEquals("11111111111111111111111111111111", l2.msb().toString());
        assertEquals("00000000000000000000000000000000", l2.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l2.opacity().toString());

        assertEquals("00000000000000000000000000000000", l3.msb().toString());
        assertEquals("11111111111111111111111111111111", l3.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l3.opacity().toString());

        assertEquals("10000000000101010101111000011001", l4.msb().toString());
        assertEquals("10000000000101010101111000011001", l4.lsb().toString());
        assertEquals("10000000000101010101111000011001",
                l4.opacity().toString());

        assertEquals("00000000000001101010111111110000", l5.msb().toString());
        assertEquals("00000000000001101010111111110000", l5.lsb().toString());
        assertEquals("00000000000001101010111111110000",
                l5.opacity().toString());

        assertEquals("11001100000000001010101011110000", l6.msb().toString());
        assertEquals("00000000110101011111111000000000", l6.lsb().toString());
        assertEquals("11001100000000001010101011110000",
                l6.opacity().toString());

        assertEquals("00000000110101011111111000000000", l7.msb().toString());
        assertEquals("11001100000000001010101011110000", l7.lsb().toString());
        assertEquals("00000000110101011111111000000000",
                l7.opacity().toString());
    }

    // Cas d'erreur
    @Test
    public void extractWrappedErrorTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0a = l0.extractWrapped(7, 34);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0a = l0.extractWrapped(7, 31);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0a = l0.extractWrapped(7, 33);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0a = l0.extractWrapped(7, -1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0a = l0.extractWrapped(7, 1);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0a = l0.extractWrapped(7, 438);
        });
    }

    // TESTS MAPCOLORS
    // Cas normal
    @Test
    public void mapColorsNormalTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        // 0b11100100

        l0 = l0.mapColors(0b00110110);
        l1 = l1.mapColors(0b01101100);
        l2 = l2.mapColors(0b11000110);
        l3 = l3.mapColors(0b10001110);
        l4 = l4.mapColors(0b10110001);
        l5 = l5.mapColors(0b00011011);
        l6 = l6.mapColors(0b11011000);
        l7 = l7.mapColors(0b11001001);

        // v3 : 11001100000000001010101011110000
        // v4 : 00000000110101011111111000000000

        assertEquals("11111111111111111111111111111111", l0.msb().toString());
        assertEquals("00000000000000000000000000000000", l0.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l0.opacity().toString());

        assertEquals("00000000000000000000000000000000", l1.msb().toString());
        assertEquals("11111111111111111111111111111111", l1.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l1.opacity().toString());

        assertEquals("00000000000000000000000000000000", l2.msb().toString());
        assertEquals("00000000000000000000000000000000", l2.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l2.opacity().toString());

        assertEquals("11111111111111111111111111111111", l3.msb().toString());
        assertEquals("11111111111111111111111111111111", l3.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l3.opacity().toString());

        assertEquals("11001100000000001010101011110000", l4.msb().toString());
        assertEquals("00110011111111110101010100001111", l4.lsb().toString());
        assertEquals("11001100000000001010101011110000",
                l4.opacity().toString());

        assertEquals("11111111001010100000000111111111", l5.msb().toString());
        assertEquals("11111111001010100000000111111111", l5.lsb().toString());
        assertEquals("00000000110101011111111000000000",
                l5.opacity().toString());

        assertEquals("00000000110101011111111000000000", l6.msb().toString());
        assertEquals("11001100000000001010101011110000", l6.lsb().toString());
        assertEquals("11001100000000001010101011110000",
                l6.opacity().toString());

        assertEquals("11001100000000001010101011110000", l7.msb().toString());
        assertEquals("00110011001010101010101100001111", l7.lsb().toString());
        assertEquals("00000000110101011111111000000000",
                l7.opacity().toString());
    }

    // Cas d'erreur
    @Test
    public void mapColorsErrorTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0a = l0.mapColors(0b111111111);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            LcdImageLine l0a = l0.mapColors(0b1111111111);
        });
    }

    // TESTS JOIN
    public void joinTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        // 11001100000000001010101011110000
        // 00000000110101011111111000000000

        l0 = l0.join(l1, 5);
        l1 = l1.join(l2, 9);
        l2 = l2.join(l3, 0);
        l3 = l3.join(l4, 32);
        l4 = l4.join(l5, 2);
        l5 = l5.join(l6, 3);
        l6 = l6.join(l7, 19);
        l7 = l7.join(l7, 9);

        assertEquals("11111111111111111111111111100000", l0.msb().toString());
        assertEquals("11111111111111111111111111100000", l0.lsb().toString());
        assertEquals("11111111111111111111111111100000",
                l0.opacity().toString());

        assertEquals("11111111111111111111111111111111", l1.msb().toString());
        assertEquals("00000000000000000000000111111111", l1.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l1.opacity().toString());

        assertEquals("00000000000000000000000000000000", l2.msb().toString());
        assertEquals("11111111111111111111111111111111", l2.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l2.opacity().toString());

        assertEquals("00000000000000000000000000000000", l3.msb().toString());
        assertEquals("11111111111111111111111111111111", l3.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l3.opacity().toString());

        assertEquals("00000000110101011111111000000000", l4.msb().toString());
        assertEquals("00000000110101011111111000000000", l4.lsb().toString());
        assertEquals("00000000110101011111111000000000",
                l4.opacity().toString());

        assertEquals("11001100000000001010101011110000", l5.msb().toString());
        assertEquals("00000000110101011111111000000000", l5.lsb().toString());
        assertEquals("11001100000000001010101011110000",
                l5.opacity().toString());

        assertEquals("00000000110100001010101011110000", l6.msb().toString());
        assertEquals("11001100000001011111111000000000", l6.lsb().toString());
        assertEquals("00000000110100001010101011110000",
                l6.opacity().toString());

        assertEquals("00000000110101011111111000000000", l7.msb().toString());
        assertEquals("11001100000000001010101011110000", l7.lsb().toString());
        assertEquals("00000000110101011111111000000000",
                l7.opacity().toString());
    }

    // TESTS EQUALS
    @Test
    public void equalsTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        LcdImageLine l0a = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1a = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2a = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3a = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4a = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5a = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6a = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7a = new LcdImageLine(v4, v3, v4);

        // 11001100000000001010101011110000
        // 00000000110101011111111000000000

        assertTrue(l0.equals(l0));
        assertTrue(l1.equals(l1));
        assertTrue(l2.equals(l2));
        assertTrue(l3.equals(l3));
        assertTrue(l4.equals(l4));
        assertTrue(l5.equals(l5));
        assertTrue(l6.equals(l6));
        assertTrue(l7.equals(l7));

        assertFalse(l0.equals(l7));
        assertFalse(l1.equals(l6));
        assertFalse(l2.equals(l5));
        assertFalse(l3.equals(l4));
        assertFalse(l4.equals(l3));
        assertFalse(l5.equals(l2));
        assertFalse(l6.equals(l1));
        assertFalse(l7.equals(l0));

        assertTrue(l0.equals(l0a));
        assertTrue(l1.equals(l1a));
        assertTrue(l2.equals(l2a));
        assertTrue(l3.equals(l3a));
        assertTrue(l4.equals(l4a));
        assertTrue(l5.equals(l5a));
        assertTrue(l6.equals(l6a));
        assertTrue(l7.equals(l7a));
    }

    // TESTS HASHCODE
    @Test
    public void hashCodeTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        LcdImageLine l0a = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1a = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2a = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3a = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4a = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5a = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6a = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7a = new LcdImageLine(v4, v3, v4);

        assertEquals(l0.hashCode(), l0a.hashCode());
        assertEquals(l1.hashCode(), l1a.hashCode());
        assertEquals(l2.hashCode(), l2a.hashCode());
        assertEquals(l3.hashCode(), l3a.hashCode());
        assertEquals(l4.hashCode(), l4a.hashCode());
        assertEquals(l5.hashCode(), l5a.hashCode());
        assertEquals(l6.hashCode(), l6a.hashCode());
        assertEquals(l7.hashCode(), l7a.hashCode());

        assertFalse(l0.hashCode() == l7.hashCode());
        assertFalse(l1.hashCode() == l6.hashCode());
        assertFalse(l2.hashCode() == l5.hashCode());
        assertFalse(l3.hashCode() == l4.hashCode());
        assertFalse(l4.hashCode() == l3.hashCode());
        assertFalse(l5.hashCode() == l2.hashCode());
        assertFalse(l6.hashCode() == l1.hashCode());
        assertFalse(l7.hashCode() == l0.hashCode());
    }

    // TESTS BUILDER
    // Cas normal
    @Test
    public void builderNormal() {
        LcdImageLine.Builder b0 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b1 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b2 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b3 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b4 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b5 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b6 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b7 = new LcdImageLine.Builder(32);

        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        // 11001100 00000000 10101010 11110000
        // 00000000 11010101 11111110 00000000

        LcdImageLine l0a = b0.setBytes(0, 0, 0).setBytes(1, 0, 0)
                .setBytes(2, 0, 0).setBytes(3, 0, 0).build();
        LcdImageLine l1a = b1.setBytes(0, 0b11111111, 0b11111111)
                .setBytes(1, 0b11111111, 0b11111111)
                .setBytes(2, 0b11111111, 0b11111111)
                .setBytes(3, 0b11111111, 0b11111111).build();
        LcdImageLine l2a = b2.setBytes(0, 0b11111111, 0)
                .setBytes(1, 0b11111111, 0).setBytes(2, 0b11111111, 0)
                .setBytes(3, 0b11111111, 0).build();
        LcdImageLine l3a = b3.setBytes(0, 0, 0b11111111)
                .setBytes(1, 0, 0b11111111).setBytes(2, 0, 0b11111111)
                .setBytes(3, 0, 0b11111111).build();
        LcdImageLine l4a = b4.setBytes(0, 0b11110000, 0b11110000)
                .setBytes(1, 0b10101010, 0b10101010)
                .setBytes(2, 0b00000000, 0b00000000)
                .setBytes(3, 0b11001100, 0b11001100).build();
        LcdImageLine l5a = b5.setBytes(0, 0, 0)
                .setBytes(1, 0b11111110, 0b11111110)
                .setBytes(2, 0b11010101, 0b11010101).setBytes(3, 0, 0).build();
        LcdImageLine l6a = b6.setBytes(0, 0b11110000, 0)
                .setBytes(1, 0b10101010, 0b11111110).setBytes(2, 0, 0b11010101)
                .setBytes(3, 0b11001100, 0).build();
        LcdImageLine l7a = b7.setBytes(0, 0, 0b11110000)
                .setBytes(1, 0b11111110, 0b10101010).setBytes(2, 0b11010101, 0)
                .setBytes(3, 0, 0b11001100).build();

        assertEquals(l0.msb(), l0a.msb());
        assertEquals(l0.lsb(), l0a.lsb());
        assertEquals(l1.msb(), l1a.msb());
        assertEquals(l1.lsb(), l1a.lsb());
        assertEquals(l2.msb(), l2a.msb());
        assertEquals(l2.lsb(), l2a.lsb());
        assertEquals(l3.msb(), l3a.msb());
        assertEquals(l3.lsb(), l3a.lsb());
        assertEquals(l4.msb(), l4a.msb());
        assertEquals(l4.lsb(), l4a.lsb());
        assertEquals(l5.msb(), l5a.msb());
        assertEquals(l5.lsb(), l5a.lsb());
        assertEquals(l6.msb(), l6a.msb());
        assertEquals(l6.lsb(), l6a.lsb());
        assertEquals(l7.msb(), l7a.msb());
        assertEquals(l7.lsb(), l7a.lsb());

        assertEquals("00000000000000000000000000000000",
                l0a.opacity().toString());
        assertEquals("11111111111111111111111111111111",
                l1a.opacity().toString());
        assertEquals("11111111111111111111111111111111",
                l2a.opacity().toString());
        assertEquals("11111111111111111111111111111111",
                l3a.opacity().toString());
        assertEquals("11001100000000001010101011110000",
                l4a.opacity().toString());
        assertEquals("00000000110101011111111000000000",
                l5a.opacity().toString());
        assertEquals("11001100110101011111111011110000",
                l6a.opacity().toString());
        assertEquals("11001100110101011111111011110000",
                l7a.opacity().toString());
    }

    // Cas d'erreur
    @Test
    public void builderErrorTest() {
        LcdImageLine.Builder b0 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b1 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b2 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b3 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b4 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b5 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b6 = new LcdImageLine.Builder(32);
        LcdImageLine.Builder b7 = new LcdImageLine.Builder(32);

        LcdImageLine l0a = b0.setBytes(0, 0, 0).setBytes(1, 0, 0)
                .setBytes(2, 0, 0).setBytes(3, 0, 0).build();
        LcdImageLine l1a = b1.setBytes(0, 0b11111111, 0b11111111)
                .setBytes(1, 0b11111111, 0b11111111)
                .setBytes(2, 0b11111111, 0b11111111)
                .setBytes(3, 0b11111111, 0b11111111).build();
        LcdImageLine l2a = b2.setBytes(0, 0b11111111, 0)
                .setBytes(1, 0b11111111, 0).setBytes(2, 0b11111111, 0)
                .setBytes(3, 0b11111111, 0).build();
        LcdImageLine l3a = b3.setBytes(0, 0, 0b11111111)
                .setBytes(1, 0, 0b11111111).setBytes(2, 0, 0b11111111)
                .setBytes(3, 0, 0b11111111).build();
        LcdImageLine l4a = b4.setBytes(0, 0b11110000, 0b11110000)
                .setBytes(1, 0b10101010, 0b10101010)
                .setBytes(2, 0b00000000, 0b00000000)
                .setBytes(3, 0b11001100, 0b11001100).build();
        LcdImageLine l5a = b5.setBytes(0, 0, 0)
                .setBytes(1, 0b11111110, 0b11111110)
                .setBytes(2, 0b11010101, 0b11010101).setBytes(3, 0, 0).build();
        LcdImageLine l6a = b6.setBytes(0, 0b11110000, 0)
                .setBytes(1, 0b10101010, 0b11111110).setBytes(2, 0, 0b11010101)
                .setBytes(3, 0b11001100, 0).build();
        LcdImageLine l7a = b7.setBytes(0, 0, 0b11110000)
                .setBytes(1, 0b11111110, 0b10101010).setBytes(2, 0b11010101, 0)
                .setBytes(3, 0, 0b11001100).build();

        assertThrows(IllegalStateException.class, () -> b0.build());
        assertThrows(IllegalStateException.class, () -> b1.build());
        assertThrows(IllegalStateException.class, () -> b2.build());
        assertThrows(IllegalStateException.class, () -> b3.build());
        assertThrows(IllegalStateException.class, () -> b4.build());
        assertThrows(IllegalStateException.class, () -> b5.build());
        assertThrows(IllegalStateException.class, () -> b6.build());
        assertThrows(IllegalStateException.class, () -> b7.build());

        assertThrows(IllegalStateException.class, () -> b0.setBytes(1, 0, 0));
        assertThrows(IllegalStateException.class, () -> b1.setBytes(1, 0, 0));
        assertThrows(IllegalStateException.class, () -> b2.setBytes(1, 0, 0));
        assertThrows(IllegalStateException.class, () -> b3.setBytes(1, 0, 0));
        assertThrows(IllegalStateException.class, () -> b4.setBytes(1, 0, 0));
        assertThrows(IllegalStateException.class, () -> b5.setBytes(1, 0, 0));
        assertThrows(IllegalStateException.class, () -> b6.setBytes(1, 0, 0));
        assertThrows(IllegalStateException.class, () -> b7.setBytes(1, 0, 0));
    }

    // TESTS BELOW1
    // Cas normal
    @Test
    public void below1NormalTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        // 11001100000000001010101011110000
        // 00000000110101011111111000000000

        LcdImageLine l0a = l0.below(l1, v0);
        LcdImageLine l0b = l0.below(l1, v1);
        LcdImageLine l0c = l0.below(l1, v3);
        LcdImageLine l0d = l0.below(l1, v4);

        LcdImageLine l1a = l1.below(l2, v0);
        LcdImageLine l1b = l1.below(l2, v1);
        LcdImageLine l1c = l1.below(l2, v3);
        LcdImageLine l1d = l1.below(l2, v4);

        LcdImageLine l2a = l2.below(l3, v0);
        LcdImageLine l2b = l2.below(l3, v1);
        LcdImageLine l2c = l2.below(l3, v3);
        LcdImageLine l2d = l2.below(l3, v4);

        LcdImageLine l3a = l3.below(l4, v0);
        LcdImageLine l3b = l3.below(l4, v1);
        LcdImageLine l3c = l3.below(l4, v3);
        LcdImageLine l3d = l3.below(l4, v4);

        LcdImageLine l4a = l4.below(l5, v0);
        LcdImageLine l4b = l4.below(l5, v1);
        LcdImageLine l4c = l4.below(l5, v3);
        LcdImageLine l4d = l4.below(l5, v4);

        LcdImageLine l5a = l5.below(l6, v0);
        LcdImageLine l5b = l5.below(l6, v1);
        LcdImageLine l5c = l5.below(l6, v3);
        LcdImageLine l5d = l5.below(l6, v4);

        LcdImageLine l6a = l6.below(l7, v0);
        LcdImageLine l6b = l6.below(l7, v1);
        LcdImageLine l6c = l6.below(l7, v3);
        LcdImageLine l6d = l6.below(l7, v4);

        assertEquals("00000000000000000000000000000000", l0a.msb().toString());
        assertEquals("00000000000000000000000000000000", l0a.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l0a.opacity().toString());
        assertEquals("11111111111111111111111111111111", l0b.msb().toString());
        assertEquals("11111111111111111111111111111111", l0b.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l0b.opacity().toString());
        assertEquals("11001100000000001010101011110000", l0c.msb().toString());
        assertEquals("11001100000000001010101011110000", l0c.lsb().toString());
        assertEquals("11001100000000001010101011110000",
                l0c.opacity().toString());
        assertEquals("00000000110101011111111000000000", l0d.msb().toString());
        assertEquals("00000000110101011111111000000000", l0d.lsb().toString());
        assertEquals("00000000110101011111111000000000",
                l0d.opacity().toString());

        assertEquals("11111111111111111111111111111111", l1a.msb().toString());
        assertEquals("11111111111111111111111111111111", l1a.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l1a.opacity().toString());
        assertEquals("11111111111111111111111111111111", l1b.msb().toString());
        assertEquals("00000000000000000000000000000000", l1b.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l1b.opacity().toString());
        assertEquals("11111111111111111111111111111111", l1c.msb().toString());
        assertEquals("00110011111111110101010100001111", l1c.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l1c.opacity().toString());
        assertEquals("11111111111111111111111111111111", l1d.msb().toString());
        assertEquals("11111111001010100000000111111111", l1d.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l1d.opacity().toString());

        assertEquals("11111111111111111111111111111111", l2a.msb().toString());
        assertEquals("00000000000000000000000000000000", l2a.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l2a.opacity().toString());
        assertEquals("00000000000000000000000000000000", l2b.msb().toString());
        assertEquals("11111111111111111111111111111111", l2b.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l2b.opacity().toString());
        assertEquals("00110011111111110101010100001111", l2c.msb().toString());
        assertEquals("11001100000000001010101011110000", l2c.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l2c.opacity().toString());
        assertEquals("11111111001010100000000111111111", l2d.msb().toString());
        assertEquals("00000000110101011111111000000000", l2d.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l2d.opacity().toString());

        assertEquals("00000000000000000000000000000000", l3a.msb().toString());
        assertEquals("11111111111111111111111111111111", l3a.lsb().toString());
        assertEquals("00000000000000000000000000000000",
                l3a.opacity().toString());
        assertEquals("11001100000000001010101011110000", l3b.msb().toString());
        assertEquals("11001100000000001010101011110000", l3b.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l3b.opacity().toString());
        assertEquals("11001100000000001010101011110000", l3c.msb().toString());
        assertEquals("11111111111111111111111111111111", l3c.lsb().toString());
        assertEquals("11001100000000001010101011110000",
                l3c.opacity().toString());
        assertEquals("00000000000000001010101000000000", l3d.msb().toString());
        assertEquals("11111111001010101010101111111111", l3d.lsb().toString());
        assertEquals("00000000110101011111111000000000",
                l3d.opacity().toString());

        assertEquals("11001100000000001010101011110000", l4a.msb().toString());
        assertEquals("11001100000000001010101011110000", l4a.lsb().toString());
        assertEquals("11001100000000001010101011110000",
                l4a.opacity().toString());
        assertEquals("00000000110101011111111000000000", l4b.msb().toString());
        assertEquals("00000000110101011111111000000000", l4b.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l4b.opacity().toString());
        assertEquals("00000000000000001010101000000000", l4c.msb().toString());
        assertEquals("00000000000000001010101000000000", l4c.lsb().toString());
        assertEquals("11001100000000001010101011110000",
                l4c.opacity().toString());
        assertEquals("11001100110101011111111011110000", l4d.msb().toString());
        assertEquals("11001100110101011111111011110000", l4d.lsb().toString());
        assertEquals(v3.or(v4).toString(), l4d.opacity().toString());
    }

    // TESTS BELOW2
    // Cas normal
    @Test
    public void below2NormalTest() {
        LcdImageLine l0 = new LcdImageLine(v0, v0, v0);
        LcdImageLine l1 = new LcdImageLine(v1, v1, v1);
        LcdImageLine l2 = new LcdImageLine(v1, v0, v1);
        LcdImageLine l3 = new LcdImageLine(v0, v1, v0);
        LcdImageLine l4 = new LcdImageLine(v3, v3, v3);
        LcdImageLine l5 = new LcdImageLine(v4, v4, v4);
        LcdImageLine l6 = new LcdImageLine(v3, v4, v3);
        LcdImageLine l7 = new LcdImageLine(v4, v3, v4);

        l0 = l0.below(l1);
        l1 = l1.below(l2);
        l2 = l2.below(l3);
        l3 = l3.below(l4);
        l4 = l4.below(l5);
        l5 = l5.below(l6);
        l6 = l6.below(l7);

        // 11001100000000001010101011110000
        // 00000000110101011111111000000000

        assertEquals("11111111111111111111111111111111", l0.msb().toString());
        assertEquals("11111111111111111111111111111111", l0.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l0.opacity().toString());

        assertEquals("11111111111111111111111111111111", l1.msb().toString());
        assertEquals("00000000000000000000000000000000", l1.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l1.opacity().toString());

        assertEquals("11111111111111111111111111111111", l2.msb().toString());
        assertEquals("00000000000000000000000000000000", l2.lsb().toString());
        assertEquals("11111111111111111111111111111111",
                l2.opacity().toString());

        assertEquals("11001100000000001010101011110000", l3.msb().toString());
        assertEquals("11111111111111111111111111111111", l3.lsb().toString());
        assertEquals(v3.toString(), l3.opacity().toString());

        assertEquals("11001100110101011111111011110000", l4.msb().toString());
        assertEquals("11001100110101011111111011110000", l4.lsb().toString());
        assertEquals(v3.or(v4).toString(), l4.opacity().toString());

        assertEquals("11001100110101011111111011110000", l5.msb().toString());
        assertEquals("00000000110101011111111000000000", l5.lsb().toString());
        assertEquals(v3.or(v4).toString(), l5.opacity().toString());

        assertEquals("11001100110101011111111011110000", l6.msb().toString());
        assertEquals("00000000000000001010101000000000", l6.lsb().toString());
        assertEquals(v3.or(v4).toString(), l6.opacity().toString());
    }
}
