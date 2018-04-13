package ch.epfl.gameboj.bits;

public class BitVertorTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        BitVector bv1 =new BitVector(96, true);
        
        BitVector bv3 = BitVector.rand();
        //BitVector bv2 = bv3.extractZeroExtended(-64, 256);
        BitVector bv4 = bv3.extractWrapped(100,32);
        System.out.println(new BitVector(64,true));
        
    }

}
