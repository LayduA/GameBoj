package ch.epfl.gameboj.bits;

import static ch.epfl.gameboj.Preconditions.*;

import java.util.Objects;

/** 
 * Different methods of bits manipulation.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */

public final class Bits {

    private static int[] INVERSES = new int[] { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60,
            0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0, 0x08,
            0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58,
            0xD8, 0x38, 0xB8, 0x78, 0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24,
            0xA4, 0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74,
            0xF4, 0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C,
            0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC, 0x02, 0x82, 0x42,
            0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32,
            0xB2, 0x72, 0xF2, 0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A,
            0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA, 0x06,
            0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16, 0x96, 0x56,
            0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E, 0xCE, 0x2E,
            0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E,
            0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1, 0x11,
            0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49,
            0xC9, 0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39,
            0xB9, 0x79, 0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65,
            0xE5, 0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D,
            0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED, 0x1D, 0x9D, 0x5D,
            0xDD, 0x3D, 0xBD, 0x7D, 0xFD, 0x03, 0x83, 0x43, 0xC3, 0x23,
            0xA3, 0x63, 0xE3, 0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73,
            0xF3, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B,
            0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07, 0x87, 0x47,
            0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57, 0xD7, 0x37,
            0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F,
            0xEF, 0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF, };
    private Bits() {
    }

    /**
     * Returns an int with a unique 1 at the index given.
     * @param index , the index of the int where the 1 will be placed.
     * @return an int with a unique 1 at the index given.
     * @throws IndexOutOfBoundsException - if index is not valid (namely negative or >= 32).
     */
    public static int mask(int index) {
        checkIndex(index);
        return (1 << index);
    }

    /**
     * Checks the bit of the int given at the index given.
     * @param bits , the int from which a bit will be tested.
     * @param index , the index of the bit we want to test.
     * @return true if the bit tested is 1, false if the bit tested is 0.
     * @throws IndexOutOfBoundsException - if index is not valid (namely negative or >= 32).
     */
    public static boolean test(int bits, int index) {
        checkIndex(index);
        return ((bits & mask(index)) != 0);
    }

    /**
     * Checks the bit of the int given at the index of the bit given.
     * @param bits , the int from which a bit will be tested.
     * @param bit , the bit from which the index to test will be obtained.
     * @return true if the bit tested is 1, false if the bit tested is 0. 
     * @throws IndexOutOfBoundsException - if index is not valid (namely negative or >= 32).
     */
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }

    /**
     * Returns an int with the same bits of the int given, except from the bit of the index given, which takes the value given.
     * @param bits , the int to modify.
     * @param index , the index of the int to modify.
     * @param newValue , the value to modify the int at the index given.
     * @return the int given with the bit of the index changed to 1 if newValue is true, 0 if newValue is false.
     * @throws IndexOutOfBoundsException - if index is not valid (namely negative or >= 32).
     */
    public static int set(int bits, int index, boolean newValue) {
        checkIndex(index);
        if (newValue) {
            return bits | mask(index);
        } else {
            return bits & ~mask(index);
        }
    }

    /**
     * Keeps only the least important bits of an integer.
     * @param size , the number of bits to keep (all those before will be set to 0).
     * @param bits , the integer to clip.
     * @return the truncated integer.
     * @throws IllegalArgumentException if size is bigger or equal to 32 or smaller or equal to 0.
     */
    public static int clip(int size, int bits) {
        checkArgument(size <= Integer.SIZE && size >= 0);
        if(size == Integer.SIZE ) return bits;
        bits = set(bits, 31, false);
        return (bits%(1<<size));
    }

    /**
     * Returns an int with the size least significant bits of the int given rotated a given number of times (distance).
     * @param size , the number of least significant bits of the int given we rotate.
     * @param bits , the int we modify. 
     * @param distance , the number of times we rotate the bits.
     * @return an int with the size least significant bits rotated a given number of times and the other bits unchanged from the int given.
     * @throws IllegalArgumentException if size is bigger or equal to 32 or strictly smaller than 0.
     */
    public static int rotate(int size, int bits, int distance) {

        checkArgument(size <= Integer.SIZE && size > 0);
        distance = Math.floorMod(distance, size);
        int strongBits = extract(bits, size, Integer.SIZE - size);
        bits = (clip(size, (bits << distance)) | (bits >>> size - distance)); //the method advised by the course.

        return bits | (strongBits << size);
    }

    /**
     * Keeps only the bits of the int given, starting at start and of size size.
     * @param bits , the int from which we extract bits.
     * @param start , the index of the bit of the int given from which we extract bits.
     * @param size , the size of the new int we extract.
     * @return an int of size size we extracted from the int given from the index start.
     * @throws IndexOutOfBoundsException if the first index is smaller than 0 or if there
     * is an index bigger than the size of an integer.
     */
    public static int extract(int bits, int start, int size) {
        Objects.checkFromIndexSize(start, size, Integer.SIZE);
        bits = clip(start + size, bits);
        bits = bits >>> (start);
        return (size == 0 ? 0 : bits);
    }

    /**
     * Extends an int from an 8-bit value, with the most significant bit extended to the index bits 8 to 31.
     * @param b , the int we modify.
     * @return an int with the bits of index 8 to 31 being 1 if the bit of index 7 is 1, or 0 if the bit of index 7 is 0.
     * @throws IllegalArgumentException if b is not an 8-bit value.
     */
    public static int signExtend8(int b) {
        checkBits8(b);
        byte v = (byte) b;
        return (int) v;
    }

    /**
     * Reverses an int of 8-bit value.
     * @param b , the int to reverse.
     * @return an int with the given int reversed bit-to-bit (the bits reversed are 
     * the bits 0 and 7, 1 and 6, 2 and 5, 3 and 4. eg 1100_1010 returns 0101_0011).
     * @throws IllegalArgumentException if b is not an 8-bit value.
     */
    public static int reverse8(int b) {
        checkBits8(b);
        
        return INVERSES[b];
    }

    /**
     * Returns an int with every single bit replaced by his opposite.
     * @param b , the int to inverse bit-to-bit.
     * @return an int, with the 1's replaced by 0's, and the 0's replaced by 1's. eg 0110_0100 returns 1001_1011.
     * @throws IllegalArgumentException if b is not an 8-bits value.
     */
    public static int complement8(int b) {
        checkBits8(b);
        return b ^ 0b1111_1111;
    }

    /**
     * Returns a 16-bit int with the two 8-bits parameters.
     * @param highB , an 8-bit int which represent the 8 most significant bits of the new int.
     * @param lowB , an 8-bit int which represent the 8 least significant bits of the new int.
     * @return a 16-bit int with the 8 most significant bits being the ones of highB and the
     * 8 least significant bits being the ones of lowB.
     * @throws IllegalArgumentException if b is not an 8-bit value.
     */
    public static int make16(int highB, int lowB) {
        checkBits8(highB);
        checkBits8(lowB);
        return (highB << 8) | lowB;
    }

}
