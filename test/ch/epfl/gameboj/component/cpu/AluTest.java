package ch.epfl.gameboj.component.cpu;

import static ch.epfl.gameboj.component.cpu.Alu.*;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AluTest {

    @Test
    void maskZNHCWorksForKnownValues() {
        assertEquals(0b1010_0000, maskZNHC(true, false, true, false));
        assertEquals(0b1111_0000, maskZNHC(true, true, true, true));
    }

    @Test
    void addFailsForInvalidValues() {
        assertThrows(IllegalArgumentException.class,
                () -> Alu.add(0b1_0000_0000, 0b1, false));
        assertThrows(IllegalArgumentException.class,
                () -> Alu.add(0b1000_0000, 0b1_0000_0000, false));
    }

    // TODO
    @Test
    void addWorksForKnownValues() {
        assertEquals(Alu.add(0b0000_1100, 0b0000_1110, false),
                0b0001_1010_0010_0000);
        assertEquals(Alu.add(0b0000_1100, 0b0000_1110, true),
                0b0001_1011_0010_0000);
        assertEquals(Alu.add(0b0011_1000, 0b0100_1110, false),
                0b1000_0110_0010_0000);
        assertEquals(Alu.add(0b1111_0101, 0b0011_1101, false),
                0b0011_0010_0011_0000);

    }

    @Test
    void add16LWorksForKnownValues() {
        assertEquals(Alu.add16L(0b0011_0101_0001_1100, 0b0100_1000_1110_1011),
                0b0111_1110_0000_0111_0011_0000);
        assertEquals(Alu.add16L(0b0100_1101_1001_0001, 0b0001_1000_0101_1010),
                0b0110_0101_1110_1011_0000_0000);
    }

    @Test
    void add16HWorksForKnownValues() {
        assertEquals(Alu.add16H(0b0011_0101_0001_1100, 0b0100_1000_1110_1011),
                0b0111_1110_0000_0111_0000_0000);
        assertEquals(Alu.add16H(0b0100_1101_1001_0001, 0b0001_1000_0101_1010),
                0b0110_0101_1110_1011_0010_0000);
    }

    @Test
    void subWorksForKnownValues() {
        assertEquals(Alu.sub(0b1101_0110, 0b0101_0101, false),
                0b1000_0001_0100_0000);
        assertEquals(Alu.sub(0b1101_0110, 0b0101_0101, true),
                0b1000_0000_0100_0000);
        assertEquals(Alu.sub(0b0000_1111, 0b1111_0000, false),
                0b0001_1111_0101_0000);
    }

    @Test
    void andWorksForKnownValues() {
        assertEquals(Alu.and(0b1001_1010, 0b1101_1001), 0b1001_1000_0010_0000);
    }

    @Test
    void orWorksForKnownValues() {
        assertEquals(Alu.or(0b1001_1010, 0b1101_1001), 0b1101_1011_0000_0000);
    }

    @Test
    void xorWorksForKnownValues() {
        assertEquals(Alu.xor(0b1001_1010, 0b1101_1001), 0b0100_0011_0000_0000);
    }

    @Test
    void shiftLeftWorksForKnownValues() {
        assertEquals(Alu.shiftLeft(0b1110_0001), 0b1100_0010_0001_0000);
        assertEquals(Alu.shiftLeft(0b0110_0001), 0b1100_0010_0000_0000);
    }

    @Test
    void shiftRightAWorksForKnownValues() {
        assertEquals(Alu.shiftRightA(0b1110_0001), 0b1111_0000_0001_0000);
        assertEquals(Alu.shiftRightA(0b1110_0000), 0b1111_0000_0000_0000);
    }

    @Test
    void shiftRightLWorksForKnownValues() {
        assertEquals(Alu.shiftRightL(0b1110_0001), 0b0111_0000_0001_0000);
        assertEquals(Alu.shiftRightL(0b1110_0000), 0b0111_0000_0000_0000);
    }

    @Test
    void rotateWorksForKnownValues() {
        assertEquals(Alu.rotate(RotDir.LEFT, 0b0101_1110),
                0b1011_1100_0000_0000);
        assertEquals(Alu.rotate(RotDir.LEFT, 0b1101_1110),
                0b1011_1101_0001_0000);
        assertEquals(Alu.rotate(RotDir.RIGHT, 0b0101_1110),
                0b0010_1111_0000_0000);

    }

    @Test
    void rotate2WorksForKnownValues() {
        assertEquals(Alu.rotate(RotDir.LEFT, 0b1100_1011, true),
                0b1001_0111_0001_0000);
        assertEquals(Alu.rotate(RotDir.RIGHT, 0b1100_1011, false),
                0b0110_0101_0001_0000);
    }

    @Test
    void swapWorksForKnownValues() {
        assertEquals(Alu.swap(0b0011_1011), 0b1011_0011_0000_0000);
        assertEquals(Alu.swap(0b0000_0000), 0b0000_0000_1000_0000);
    }

    @Test
    void testBitFailsForInvalidIndex() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> Alu.testBit(0b1101_0101, 8));
        assertThrows(IndexOutOfBoundsException.class,
                () -> Alu.testBit(0b1101_0101, -1));
        assertThrows(IndexOutOfBoundsException.class,
                () -> Alu.testBit(0b1101_0101, 31));
    }

    @Test
    void testBitWorksForKnownValues() {
        assertEquals(Alu.testBit(0b1101_0010, 3), 0b0000_0000_0010_0000);
        assertEquals(Alu.testBit(0b1101_0011, 0), 0b0000_0000_1010_0000);
        assertEquals(Alu.testBit(0b1101_0010, 7), 0b0000_0000_1010_0000);
    }
}
