package ch.epfl.gameboj.component;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.component.lcd.LcdImageLine;

public class lcdTest {

    BitVector msb = new BitVector(32, true);
    BitVector lsb = new BitVector(32, false);
    BitVector opacity = new BitVector(32, true);
    LcdImageLine a = new LcdImageLine(msb, lsb, opacity);

    BitVector msbb = new BitVector(32, false);
    BitVector lsbb = new BitVector(32, false);
    BitVector opacityy = new BitVector.Builder(32).setByte(0, 0b1111_0000)
            .setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
    // 11001100000000001010101011110000
    LcdImageLine b = new LcdImageLine(msbb, lsbb, opacityy);

    BitVector msbbb = new BitVector.Builder(32).setByte(0, 0b1111_0000)
            .setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
    // 11001100000000001010101011110000
    BitVector lsbbb = new BitVector.Builder(32).setByte(0, 0b0000_0110)
            .setByte(1, 0b1010_1110).setByte(3, 0b1100_0000).build();
    // 11000000000000001010111000000110
    BitVector opacityyy = new BitVector(32, true);
    LcdImageLine c = new LcdImageLine(msbbb, lsbbb, opacityyy);

    BitVector msbbbb = new BitVector(32, false);
    BitVector lsbbbb = new BitVector(32, true);
    BitVector opacityyyy = new BitVector(32, false);
    LcdImageLine d = new LcdImageLine(msbbbb, lsbbbb, opacityyyy);

    @Test
    void shiftTest() {

        LcdImageLine c = a.shift(3);
        LcdImageLine d = a.shift(-5);
        assertEquals("11111111111111111111111111111000", c.msb().toString());
        assertEquals("00000000000000000000000000000000", c.lsb().toString());
        assertEquals("11111111111111111111111111111000",
                c.opacity().toString());
        assertEquals("00000111111111111111111111111111", d.msb().toString());
        assertEquals("00000000000000000000000000000000", d.lsb().toString());
        assertEquals("00000111111111111111111111111111",
                d.opacity().toString());

    }

    @Test
    void colorTest() {

        LcdImageLine v1 = c.mapColors(0b11100100);
        assertEquals(v1, c);
        LcdImageLine v2 = c.mapColors(0b11100001);
        System.out.println(v2.msb().toString());
        System.out.println(v2.lsb().toString());
        //            11001100000000001010101011110000
        assertEquals("11001100000000001010101011110000", v2.msb().toString());
        //            11000000000000001010111000000110
        assertEquals("11110011111111111111101100001001", v2.lsb().toString());

    }

    
    @Test
    void colorTest2() {

        LcdImageLine v1 = c.mapColors(0b11100100);
        assertEquals(v1, c);
        LcdImageLine v2 = c.mapColors(0b01001110);
        //            11001100000000001010101011110000
        assertEquals("00110011111111110101010100001111", v2.msb().toString());
        //            11000000000000001010111000000110
        assertEquals("11000000000000001010111000000110", v2.lsb().toString());

    }


    @Test
    void below1() {

        LcdImageLine v1 = a.below(b);
        assertEquals("11111111111111111111111111111111",
                v1.opacity().toString());
        assertEquals("00110011111111110101010100001111", v1.msb().toString());
        assertEquals("00000000000000000000000000000000", v1.lsb().toString());
        // 11001100000000001010101011110000

    }

    @Test
    void below2() {
        BitVector z = new BitVector.Builder(32).setByte(0, 0b0000_0110)
                .setByte(1, 0b1010_1110).setByte(3, 0b1100_0000).build();
        // 11000000000000001010111000000110
        LcdImageLine v1 = a.below(b, z);
        assertEquals("11111111111111111111111111111111",
                v1.opacity().toString());
        assertEquals("00111111111111110101000111111001", v1.msb().toString());
        assertEquals("00000000000000000000000000000000", v1.lsb().toString());
        // 11000000000000001010111000000110

    }

    @Test

    void join() {

        LcdImageLine v1 = a.join(d, 4);

        assertEquals("11110000000000000000000000000000", v1.msb().toString());
        assertEquals("00001111111111111111111111111111", v1.lsb().toString());
        assertEquals("11110000000000000000000000000000",
                v1.opacity().toString());

    }
    
    @Test
    public void builder() {

        // 00000000110101011111111000000000 // lsb
        LcdImageLine v1 = new LcdImageLine.Builder(32)
                .setBytes(0, 0b0000_0000, 0b0000_0000)
                .setBytes(1, 0b0000_0000, 0b1111_1110)
                .setBytes(2, 0b0000_0000, 0b1101_0101).build();

        assertEquals("00000000110101011111111000000000", v1.lsb().toString());
        assertEquals("00000000000000000000000000000000", v1.msb().toString());
        assertEquals("00000000110101011111111000000000",
                v1.opacity().toString());

        // 00000000000000000000000011000000 // lsb
        // 00000000110101011111111000000000 // msb
        LcdImageLine v2 = new LcdImageLine.Builder(32)
                .setBytes(0, 0b0000_0000,0b1100_0000).setBytes(1, 0b1111_1110,0b0000_0000)
                .setBytes(2, 0b1101_0101,0b0000_0000).build();

        assertEquals("00000000000000000000000011000000", v2.lsb().toString());
        assertEquals("00000000110101011111111000000000", v2.msb().toString());
        assertEquals("00000000110101011111111011000000",
                v2.opacity().toString());

    }
    
}