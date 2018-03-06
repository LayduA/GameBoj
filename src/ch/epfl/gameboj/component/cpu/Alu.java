package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

import static ch.epfl.gameboj.bits.Bits.*;

import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.*;

public final class Alu {

    private Alu() {
    }

    public enum Flag implements Bit {

        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3, C, H, N, Z

    };

    public enum RotDir {

        LEFT, RIGHT
    }

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

    public static int unpackValue(int valueFlags) {

        return extract(valueFlags,8,16);
    }

    public static int unpackFlags(int valueFlags) {

        return extract(valueFlags, 0, 8);
    }

    public static int add(int l, int r, boolean c0) {

        checkBits8(l);
        checkBits8(r);
        int carry0 = (c0 ? 1 : 0);
        int value = clip(8, r + l + carry0);
        int flags = maskZNHC(isZero(value), false,
                (clip(4, r) + clip(4, l) + carry0) > 0xF,
                (l + r + carry0) > 0xFF);

        return (value << 8) | flags;
    }

    public static int add(int l, int r) {

        return add(l, r, false);
    }

    public static int add16L(int l, int r) {

        checkBits16(l);
        checkBits16(r);

        int value = clip(16, l + r);
        int flags = maskZNHC(false, false, (clip(4, l) + clip(4, r)) > 0xF,
                clip(8, l) + clip(8, r) > 0xFF);

        return (value << 8) | flags;
    }

    public static int add16H(int l, int r) {

        checkBits16(l);
        checkBits16(r);
        int value = clip(16, l + r);
        int flags = maskZNHC(false, false,
                extract(l, 8, 4) + extract(r, 8, 4) > 0xF,
                extract(l, 8, 8) + extract(r, 8, 8) > 0xFF);
        return (value << 8) | flags;
    }

    public static int sub(int l, int r, boolean b0) {

        checkBits8(l);
        checkBits8(r);

        int borrow0 = (b0 ? 1 : 0);
        int value = clip(8, l - r - borrow0);

        int flags = maskZNHC(isZero(value), true,
                clip(4, l) < clip(4, r) + borrow0, l < r + borrow0);

        return (value << 8) | flags;
    }

    public static int sub(int l, int r) {

        return sub(l, r, false);
    }

    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        checkBits8(v);

        boolean fixL = h || (!n && clip(4, v) > 9);
        boolean fixH = c || (!n && v > 0x99);

        int fixStep1 = (fixH ? 0x60 : 0);
        int fixStep2 = fixStep1 + (fixL ? 0x6 : 0);

        int value = (n ? v - fixStep2 : v + fixStep2);

        return packValueZNHC(value, isZero(value), n, false, fixH);
    }

    public static int and(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        int value = l & r;

        return packValueZNHC(value, isZero(value), false, true, false);
    }

    public static int or(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        int value = l | r;

        return packValueZNHC(value, isZero(value), false, false, false);
    }

    public static int xor(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        int value = l ^ r;

        return packValueZNHC(value, isZero(value), false, false, false);
    }

    public static int shiftLeft(int v) {
        checkBits8(v);

        int value = clip(8, v << 1);

        return packValueZNHC(value, isZero(value), false, false, test(v, 7));
    }

    public static int shiftRightA(int v) {
        checkBits8(v);

        int value = (test(v, 7) ? v >> 1 | mask(7) : v >> 1);

        return packValueZNHC(value, isZero(value), false, false, test(v, 0));
    }

    public static int shiftRightL(int v) {
        checkBits8(v);

        int value = v >> 1;

        return packValueZNHC(value, isZero(value), false, false, test(v, 0));
    }

    public static int rotate(RotDir d, int v) {
        checkBits8(v);

        int value = (d == RotDir.RIGHT ? Bits.rotate(8, v, -1)
                : Bits.rotate(8, v, 1));
        int bitSwitched = (d == RotDir.RIGHT ? 0 : 7);

        return packValueZNHC(value, isZero(value), false, false,
                test(v, bitSwitched));
    }

    public static int rotate(RotDir d, int v, boolean c) {
        checkBits8(v);

        int value = (c ? v | mask(8) : v);
        value = (d == RotDir.RIGHT ? Bits.rotate(9, value, -1)
                : Bits.rotate(9, value, 1));
        boolean getC = test(value, 8);
        value = clip(8, value);

        return packValueZNHC(value, isZero(value), false, false, getC);
    }

    public static int swap(int v) {
        checkBits8(v);

        int value = (clip(4, v) << 4) | extract(v, 4, 4);

        return packValueZNHC(value, isZero(value), false, false, false);
    }

    public static int testBit(int v, int bitIndex) {
        checkBits8(v);
        Objects.checkIndex(bitIndex, 8);
        return packValueZNHC(0, test(v, bitIndex), false, true, false);
    }

    private static int packValueZNHC(int v, boolean z, boolean n, boolean h,
            boolean c) {
        return (v << 8) | maskZNHC(z, n, h, c);
    }

    private static boolean isZero(int v) {
        return (v == 0);
    }
    
}
