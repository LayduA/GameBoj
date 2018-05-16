package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

import static ch.epfl.gameboj.bits.Bits.*;

import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.*;

/**
 * The Arithmetic and Logic Unit of the Gameboy.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
public final class Alu {

    private Alu() {
    }

    /**
     * The flags, represented as an 8-bit number.
     * @author Adrien Laydu, Michael Tasev
     *
     */
    public enum Flag implements Bit {

        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z

    };

    /**
     * The two possible direction of rotation of a bit.
     * @author Adrien Laydu, Michael Tasev
     *
     */
    public enum RotDir {
        LEFT, RIGHT
    }

    /**
     * Creates a mask of flags, under the form ZNHC0000, with Z,N,H,C = 1 if the corresponding parameter is true, 0 otherwise
     * @param z : value of flag Z (true represents 1, false 0)
     * @param n : value of flag N (true represents 1, false 0)
     * @param h : value of flag H (true represents 1, false 0)
     * @param c : value of flag C (true represents 1, false 0)
     * @return an integer, of the form ZNHC0000
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {

        int mask = 0;
        if (z)
            mask = mask | Flag.Z.mask();
        if (n)
            mask = mask | Flag.N.mask();
        if (h)
            mask = mask | Flag.H.mask();
        if (c)
            mask = mask | Flag.C.mask();
        return mask;
    }
    /**
     * Returns the 16-bit value stored in a value-flags pair.
     * @param valueFlags : the value-flags pair
     * @return the value stored.
     * @throws IllegalArgumentException, if the number is not a value-flags pack.
     */
    public static int unpackValue(int valueFlags) {
        checkArgument(valueFlags>= 0);
        return extract(valueFlags,8,16);
    }

    /**
     * Returns the flags contained in a value-flags pair.
     * @param valueFlags : the value-flags pair
     * @return the flags, under the form ZNHC0000.
     * @throws IllegalArgumentException, if the number is not a value-flags pack.
     */
    public static int unpackFlags(int valueFlags) {
        checkArgument(valueFlags >= 0);
        return extract(valueFlags, 0, 8);
    }

    /**
     * Adds 2 8-bits numbers, with an initial carry c0, and returns the result packed with corresponding flags
     * @param l : the first number to add.
     * @param r : the second number to add.
     * @param c0 : the initial carry (true represents 1, false represents 0).
     * @return a value-flags pack, containing the sum of the two numbers and the correct flags.
     * @throws IllegalArgumentException, if one of the two numbers is not a valid 8-bit number.
     */
    public static int add(int l, int r, boolean c0) {

        checkBits8(l);
        checkBits8(r);
        final int carry0 = (c0 ? 1 : 0);
        final int value = clip(8, r + l + carry0);
        final int flags = maskZNHC(isZero(value), false,
                (clip(4, r) + clip(4, l) + carry0) > 0xF,
                (l + r + carry0) > 0xFF);

        return (value << 8) | flags;
    }

    /**
     * Adds 2 8-bits numbers, and returns the result packed with corresponding flags
     * @param l : the first number to add.
     * @param r : the second number to add.
     * @return : a value-flags pack, containing the sum of the two numbers and the correct flags.
     * @throws IllegalArgumentException, if one of the two numbers is not a valid 8-bit number.
     */
    public static int add(int l, int r) {

        return add(l, r, false);
    }

    /**
     * Adds two 16-bits number, and returns the result packed with corresponding flags.
     * Note that the H and C flags are produced by the addition of the 8 least significant bits
     * of the two number.
     * @param l : the first number to add.
     * @param r : the second number to add.
     * @return : a value-flags pack, containing the sum of the two numbers and the correct flags.
     * @throws IllegalArgumentException if one of the number is not a 16-bit number.
     */
    public static int add16L(int l, int r) {

        checkBits16(l);
        checkBits16(r);

        final int value = clip(16, l + r);
        final int flags = maskZNHC(false, false, (clip(4, l) + clip(4, r)) > 0xF,
                clip(8, l) + clip(8, r) > 0xFF);

        return (value << 8) | flags;
    }

    /**
     * Adds two 16-bits number, and returns the result packed with corresponding flags.
     * Note that the H and C flags are produced by the addition of the 8 most significant bits
     * of the two number.
     * @param l : the first number to add.
     * @param r : the second number to add.
     * @return : a value-flags pack, containing the sum of the two numbers and the correct flags.
     * @throws IllegalArgumentException if one of the number is not a 16-bit number.
     */
    public static int add16H(int l, int r) {

        checkBits16(l);
        checkBits16(r);
        final int value = clip(16, l + r);
        final int carry = (clip(8, l) + clip(8, r) > 0xFF ? 1 : 0);
        final int flags = maskZNHC(false, false,
                (extract(l, 8, 4) + extract(r, 8, 4) > 0xF-carry),
                (extract(l, 8, 8) + extract(r, 8, 8) > 0xFF-carry));
        return (value << 8) | flags;
    }

    /**
     * Subtracts 2 8-bits numbers, with an initial borrow b0, and returns the result packed with corresponding flags
     * @param l : the first number to add.
     * @param r : the second number to add.
     * @param b0 : the initial borrow(true represent 1, false represents 0)
     * @return : a value-flags pack, containing the difference of the two numbers and the correct flags.
     * @throws IllegalArgumentException, if one of the two numbers is not a valid 8-bit number.
     */
    public static int sub(int l, int r, boolean b0) {

        checkBits8(l);
        checkBits8(r);

        final int borrow0 = (b0 ? 1 : 0);
        final int value = clip(8, l - r - borrow0);

        final int flags = maskZNHC(isZero(value), true,
                clip(4, l) < clip(4, r) + borrow0, l < r + borrow0);

        return (value << 8) | flags;
    }

    /**
     * Subtracts 2 8-bits numbers, and returns the result packed with corresponding flags
     * @param l : the first number to add.
     * @param r : the second number to add.
     * @return : a value-flags pack, containing the difference of the two numbers and the correct flags.
     * @throws IllegalArgumentException, if one of the two numbers is not a valid 8-bit number.
     */
    public static int sub(int l, int r) {

        return sub(l, r, false);
    }

    /**
     * Converts an 8-bit number into binary coded decimal.
     * @param v : the value to convert.
     * @param n : the N flag.
     * @param h : the H flag.
     * @param c : the C flag.
     * @return : the given value, encoded in binary coded decimal.
     * @throws IllegalArgumentException if the given value is not an 8-bit number.
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        checkBits8(v);

        final boolean fixL = h || (!n && clip(4, v) > 9);
        final boolean fixH = c || (!n && v > 0x99);

        final int fix = (fixH ? 0x60 : 0) + (fixL ? 0x6 : 0);
        final int value = (n ? v - fix : v + fix);

        return packValueZNHC(clip(8,value), isZero(clip(8,value)), n, false, fixH);
    }

    /**
     * Computes the bit-to-bit conjunction of two 8-bit numbers, and returns the result in a value-flags pack.
     * @param l : the first number
     * @param r : the second number
     * @return : the bit-to-bit conjunction of the two numbers, packed with the corresponding flags.
     * @throws IllegalArgumentException if one of the numbers is not an 8-bit number.
     */
    public static int and(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        final int value = l & r;

        return packValueZNHC(value, isZero(value), false, true, false);
    }

    /**
     * Computes the bit-to-bit disjunction of two 8-bits numbers, and returns the result in a value-flags pack.
     * @param l : the first number
     * @param r : the second number
     * @return : the bit-to-bit disjunction of the two numbers, packed with the corresponding flags.
     * @throws IllegalArgumentException if one of the numbers is not an 8-bit number.
     */
    public static int or(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        final int value = l | r;

        return packValueZNHC(value, isZero(value), false, false, false);
    }

    /**
     * Computes the bit-to-bit exclusive disjunction of two 8-bit numbers, and returns the result in a value-flags pack.
     * @param l : the first number
     * @param r : the second number
     * @return : the bit-to-bit exclusive disjunction of the two numbers, packed with the corresponding flags.
     * @throws IllegalArgumentException if one of the numbers is not an 8-bit number.
     */
    public static int xor(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        final int value = l ^ r;

        return packValueZNHC(value, isZero(value), false, false, false);
    }

    /**
     * Shifts an 8-bit number one bit left, then returns it packed with corresponding flags.
     * @param v : the number to shift.
     * @return : the number, shifted one bit to the left, packed with corresponding flags.
     * @throws IllegalArgumentException if the number is not an 8-bit number.
     */
    public static int shiftLeft(int v) {
        checkBits8(v);

        final int value = clip(8, v << 1);

        return packValueZNHC(value, isZero(value), false, false, test(v, 7));
    }

    /**
     * Shifts an 8-bit number one bit right (using arithmetic shifting), then returns it 
     * packed with corresponding flags.
     * @param v : the number to shift.
     * @return : the number, shifted one bit to the right (arithmetic shifting), packed with corresponding flags..
     * @throws IllegalArgumentException if the number is not an 8-bit number.
     */
    public static int shiftRightA(int v) {
        checkBits8(v);

        final int value = (test(v, 7) ? v >> 1 | mask(7) : v >> 1);

        return packValueZNHC(value, isZero(value), false, false, test(v, 0));
    }

    /**
     * Shifts an 8-bit number one bit right (using logical shifting), then returns it 
     * packed with corresponding flags.
     * @param v : the number to shift.
     * @return : the number, shifted one bit to the right (logical shifting), packed with corresponding flags..
     * @throws IllegalArgumentException if the number is not an 8-bit number.
     */
    public static int shiftRightL(int v) {
        checkBits8(v);

        final int value = v >> 1;

        return packValueZNHC(value, isZero(value), false, false, test(v, 0));
    }

    /**
     * Rotates an 8-bit number one bit in the given directions, then returns the results packed
     * with corresponding flags.
     * @param d : the direction to rotate in.
     * @param v : the value to rotate
     * @return : the rotated number, packed with corresponding flags.
     * @throws IllegalArgumentException if the number is not an 8-bit number.
     */
    public static int rotate(RotDir d, int v) {
        checkBits8(v);

        final int value = (d == RotDir.RIGHT ? Bits.rotate(8, v, -1)
                : Bits.rotate(8, v, 1));
        final int bitSwitched = (d == RotDir.RIGHT ? 0 : 7);

        return packValueZNHC(value, isZero(value), false, false,
                test(v, bitSwitched));
    }

    /**
     * Rotates an 8-bit number through the carry c one bit in the given directions,
     *  then returns the results packed with corresponding flags.
     * @param d : the direction to rotate in.
     * @param v : the value to rotate
     * @param c : the carry to rotate through (true represents 1, false represents 0)
     * @return : the rotated number, packed with corresponding flags.
     * @throws IllegalArgumentException if the number is not an 8-bit number.
     */
    public static int rotate(RotDir d, int v, boolean c) {
        checkBits8(v);

        int value = (c ? v | mask(8) : v);
        value = (d == RotDir.RIGHT ? Bits.rotate(9, value, -1)
                : Bits.rotate(9, value, 1));
        final boolean getC = test(value, 8);
        value = clip(8, value);

        return packValueZNHC(value, isZero(value), false, false, getC);
    }

    /**
     * Swaps the 4 least significant bits of an 8-bit number with its 4 most significants ones, 
     * and returns the result packed with corresponding flags.
     * @param v : the number to change.
     * @return the number, with its bits swapped, packed with the corresponding flags.
     * @throws IllegalArgumentException if the number is not an 8-bit number.
     */
    public static int swap(int v) {
        checkBits8(v);

        final int value = (clip(4, v) << 4) | extract(v, 4, 4);

        return packValueZNHC(value, isZero(value), false, false, false);
    }

    /**
     * Tests the bit of given index in an 8-bit number, and returns a ZNHC mask with Z being the
     * opposite of the value of the tested bit
     * @param v : the number to test in.
     * @param bitIndex : the index of the bit to test.
     * @return an int of the form X0000000, with X being the opposite value of the tested bit. 
     * @throws IndexOutOfBoundsException if the index is not between 0 and 7 (included)
     * @throws IllegalArgumentException if the number is not an 8-bit number.
     */
    public static int testBit(int v, int bitIndex) {
        checkBits8(v);
        Objects.checkIndex(bitIndex, 8);
        return packValueZNHC(0, !test(v, bitIndex), false, true, false);
    }

    private static int packValueZNHC(int v, boolean z, boolean n, boolean h,
            boolean c) {
        return (v << 8) | maskZNHC(z, n, h, c);
    }

    private static boolean isZero(int v) {
        return (v == 0);
    }
    
}
