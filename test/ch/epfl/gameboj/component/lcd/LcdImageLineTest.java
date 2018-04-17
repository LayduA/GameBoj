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

    @Test
    void equalsAndHashCodeWork() {
        BitVector msb = new BitVector(4 * 32, true);
        BitVector lsb = new BitVector(4 * 32);
        BitVector opa = BitVector.rand();
        BitVector msb2 = new BitVector(4*32, true);
        LcdImageLine line1 = new LcdImageLine(msb, lsb, opa);
        LcdImageLine line2 = new LcdImageLine(msb2, new BitVector(lsb), new BitVector(opa));
        assertTrue(msb.equals(msb2));
        assertEquals(line1.hashCode(), line2.hashCode());
    }

}
