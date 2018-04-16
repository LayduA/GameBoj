package ch.epfl.gameboj.bits;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;


/**
 * @author hugo
 */
public class BitVectorTestPerso {
    @Test
    void toStringAndBuilderWork() {
        BitVector.Builder builder = new BitVector.Builder(32)
                .setByte(0, 0b1100_1100)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0b0101_0101)
                .setByte(3, 0b1001_0110);
        BitVector a = builder.build();
        assertTrue("10010110010101011010101011001100".equals(a.toString()));
        assertThrows(IllegalStateException.class, () -> {
            builder.setByte(0, 0);
        });
    }
    
    @Test
    void constructorsWork() {
        BitVector a = new BitVector(32);
        BitVector b = new BitVector(64, true);
        BitVector c = new BitVector(128, false);
        assertTrue("00000000000000000000000000000000".equals(a.toString()));
        assertTrue("1111111111111111111111111111111111111111111111111111111111111111".equals(b.toString()));
        assertTrue(("0000000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000000").equals(c.toString()));
    }
    
    @Test
    void testBitWorks() {
        BitVector.Builder builder = new BitVector.Builder(32)
                .setByte(0, 0b1100_1100)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0b0101_0101)
                .setByte(3, 0b1001_0110);
        BitVector a = builder.build();
        BitVector b = new BitVector(64, true);
        assertTrue(a.testBit(31));
        assertTrue(!a.testBit(0));
        assertTrue(b.testBit(52));

    }
    
    @Test
    void complementWorks() {
        BitVector.Builder builder = new BitVector.Builder(32)
                .setByte(0, 0b1100_1100)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0b0101_0101)
                .setByte(3, 0b1001_0110);
        BitVector a = builder.build();
        BitVector b = new BitVector(64, true);
        BitVector c = a.not();
        BitVector d = b.not();
        assertTrue("01101001101010100101010100110011".equals(c.toString()));
        assertTrue("0000000000000000000000000000000000000000000000000000000000000000".equals(d.toString()));
    }
    
    @Test
    void andOrWork() {
        BitVector.Builder builder = new BitVector.Builder(32)
                .setByte(0, 0b1100_1100)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0b0101_0101)
                .setByte(3, 0b1001_0110);
        BitVector a = builder.build();
        builder = new BitVector.Builder(32)
                .setByte(0, 0b1110_1110)
                .setByte(1, 0b0001_0001)
                .setByte(2, 0b1101_1101)
                .setByte(3, 0b0101_0101);
        BitVector b = builder.build();
        BitVector c = a.or(b);
        BitVector d = b.and(a);
        assertTrue("11010111110111011011101111101110".equals(c.toString()));
        assertTrue("00010100010101010000000011001100".equals(d.toString()));
    }
    
    @Test
    void extractsWork() {
        BitVector.Builder builder = new BitVector.Builder(64)
                .setByte(0, 0b11001100)
                .setByte(1, 0b10101010)
                .setByte(2, 0b01010101)
                .setByte(3, 0b10010110)
                .setByte(4, 0b11001100)
                .setByte(5, 0b10101010)
                .setByte(6, 0b01010101)
                .setByte(7, 0b10010110);
        BitVector a = builder.build();
        builder = new BitVector.Builder(64)
                .setByte(0, 0b11101110)
                .setByte(1, 0b00010001)
                .setByte(2, 0b11011101)
                .setByte(3, 0b01010101)
                .setByte(4, 0b11101110)
                .setByte(5, 0b00010001)
                .setByte(6, 0b11011101)
                .setByte(7, 0b01010101);
        // a : 1001011001010101101010101100110010010110010101011010101011001100
        // b : 0101010111011101000100011110111001010101110111010001000111101110
        BitVector b = builder.build();
        BitVector c1 = a.extractZeroExtended(-7, 32);
        BitVector c2 = a.extractZeroExtended(15, 64);
        BitVector c3 = a.extractZeroExtended(15, 32);
        BitVector c4 = a.extractZeroExtended(-32, 32);
        assertTrue("00101010110101010110011000000000".equals(c1.toString()));
        assertTrue("0000000000000001001011001010101101010101100110010010110010101011".equals(c2.toString()));
        assertTrue("01010101100110010010110010101011".equals(c3.toString()));
        assertTrue("00000000000000000000000000000000".equals(c4.toString()));
        BitVector d1 = b.extractWrapped(0, 32);
        BitVector d2 = b.extractWrapped(0, 64);
        BitVector d3 = b.extractWrapped(1, 64);
        BitVector d4 = b.extractWrapped(130, 64);
        assertTrue("01010101110111010001000111101110".equals(d1.toString()));
        assertTrue("0101010111011101000100011110111001010101110111010001000111101110".equals(d2.toString()));
        assertTrue("0010101011101110100010001111011100101010111011101000100011110111".equals(d3.toString()));
        assertTrue("1001010101110111010001000111101110010101011101110100010001111011".equals(d4.toString()));
    }
    
    @Test
    void builderWorksOnRewrite() {
        BitVector.Builder builder = new BitVector.Builder(64)
                .setByte(0, 0b11001100)
                .setByte(1, 0b10101010)
                .setByte(2, 0b01010101)
                .setByte(3, 0b10010110)
                .setByte(4, 0b11001100)
                .setByte(5, 0b10101010)
                .setByte(6, 0b01010101)
                .setByte(7, 0b10010110)
                .setByte(0, 0b11101110)
                .setByte(1, 0b00010001)
                .setByte(2, 0b11011101)
                .setByte(3, 0b01010101)
                .setByte(4, 0b11101110)
                .setByte(5, 0b00010001)
                .setByte(6, 0b11011101)
                .setByte(7, 0b01010101);
        BitVector a = builder.build();
        assertTrue("0101010111011101000100011110111001010101110111010001000111101110".equals(a.toString()));
    }

    // HashCode, Equals
}