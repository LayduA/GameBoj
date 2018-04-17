package ch.epfl.gameboj.bits;

import java.util.Arrays;

public class BitVertorTest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        BitVector bv1 =new BitVector(96, true);
        
        BitVector bv3 = BitVector.rand();
        //BitVector bv2 = bv3.extractZeroExtended(-64, 256);
        BitVector bv4 = bv3.extractWrapped(100,32);
        int[] a = new int[4];
        int[] b = new int[4];
        for(int i = 0; i<4;i++) {
            a[i] = i;
            b[i] = i;
        }
        System.out.println(Arrays.hashCode(b));
        
    }

}
