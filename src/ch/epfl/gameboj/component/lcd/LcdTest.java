package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.bits.BitVector;

public class LcdTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        BitVector b = new BitVector(32,true);
        LcdImageLine lcd = new LcdImageLine(b, b, b);
    }

}
