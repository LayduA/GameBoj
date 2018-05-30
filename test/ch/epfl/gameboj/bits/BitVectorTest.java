package ch.epfl.gameboj.bits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

public class BitVectorTest {
    @Test
    public void printExample() {
//        Supposed to write : 
//        11111111111111111111111111111111
//        00000000000000011111111111111111
//        1111111111100000000000000011111111111111111000000000000000111111
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = v1.extractZeroExtended(-32, 64).not();
        BitVector v3 = v2.extractWrapped(11, 64);
        BitVector v4 = v2.extractZeroExtended(11, 64);
        for (BitVector v: List.of(v1, v2, v3,v4))
          System.out.println(v);
    }


// TESTS CONSTRUCTEUR 1
    // Cas normal
    @Test
    public void contstructeur1TestNormal() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32, false);
        
        assertEquals("11111111111111111111111111111111", v1.toString());
        assertEquals("00000000000000000000000000000000", v2.toString());
    }
    
    // Cas d'erreur
//    @Test
//    public void constructeur1TestError() {
//        assertThrows(IllegalArgumentException.class,() -> {BitVector v1 = new BitVector(-1, true);});
//        assertThrows(IllegalArgumentException.class,() -> {BitVector v2 = new BitVector(-546, false);});
//        assertThrows(IllegalArgumentException.class,() -> {BitVector v3 = new BitVector(4, true);});
//        assertThrows(IllegalArgumentException.class,() -> {BitVector v4 = new BitVector(31, false);});
//    }
//    
    
// TESTS CONSTRUCTEUR2
    // Cas normal
    @Test
    public void constructeur2TestNormal() {
        BitVector v2 = new BitVector(32);
        assertEquals("00000000000000000000000000000000", v2.toString());
    }
    
    // Cas d'erreur
//    @Test
//    public void constructeur2TestError() {
//        assertThrows(IllegalArgumentException.class,() -> {BitVector v1 = new BitVector(-1);});
//        assertThrows(IllegalArgumentException.class,() -> {BitVector v2 = new BitVector(-546);});
//        assertThrows(IllegalArgumentException.class,() -> {BitVector v3 = new BitVector(4);});
//        assertThrows(IllegalArgumentException.class,() -> {BitVector v4 = new BitVector(31);});
//    }
    
    
// TESTS TESTBIT()
    // Cas Normaux
    @Test
    public void testBitNormal() {
        // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        for(int i = 0 ; i < 4 ; ++i) {
            assertEquals(false, v1.testBit(i));
        }
        for(int i = 4 ; i < 8 ; ++i) {
            assertEquals(true, v1.testBit(i));
        }
        for(int i = 16 ; i < 26 ; ++i) {
            assertEquals(false, v1.testBit(i));
        }
        for(int i = 28 ; i < 30 ; ++i) {
            assertEquals(false, v1.testBit(i));
        }
        for(int i = 30 ; i < 32 ; ++i) {
            assertEquals(true, v1.testBit(i));
        }
    }
    
    // Cas limites
    @Test
    public void testBitLimit() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        BitVector v3 = new BitVector(64, true);
        BitVector v4 = new BitVector(64, false);
        BitVector v5 = new BitVector(96, true);
        BitVector v6 = new BitVector(96);
        
        assertEquals(true, v1.testBit(0));
        assertEquals(true, v1.testBit(31));
        assertEquals(true, v1.testBit(24));
        
        assertEquals(false, v2.testBit(0));
        assertEquals(false, v2.testBit(31));
        assertEquals(false, v2.testBit(5));
        
        assertEquals(true, v3.testBit(0));
        assertEquals(true, v3.testBit(63));
        assertEquals(true, v3.testBit(54));
        
        assertEquals(false, v4.testBit(0));
        assertEquals(false, v4.testBit(63));
        assertEquals(false, v4.testBit(17));
        
        assertEquals(true, v5.testBit(0));
        assertEquals(true, v5.testBit(95));
        assertEquals(true, v5.testBit(84));
        
        assertEquals(false, v6.testBit(0));
        assertEquals(false, v6.testBit(95));
        assertEquals(false, v6.testBit(2));  
    }
    
    // Cas d'erreur
    @Test
    public void testBitError() {
        BitVector v1 = new BitVector(32, true);
        
        assertThrows(IndexOutOfBoundsException.class,() -> v1.testBit(-1));
        assertThrows(IndexOutOfBoundsException.class,() -> v1.testBit(32));
        assertThrows(IndexOutOfBoundsException.class,() -> v1.testBit(45));
        assertThrows(IndexOutOfBoundsException.class,() -> v1.testBit(-3));
    }
    

// TESTS NOT
    // Cas Normaux
    @Test
    public void testNotNormal() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        BitVector v2 = v1.not();
        assertEquals("00110011111111110101010100001111", v2.toString());
    }
    
    // Cas Limites
    @Test
    public void testNotLimit() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        BitVector v3 = v1.not();
        BitVector v4 = v2.not();
        
        assertEquals("00000000000000000000000000000000", v3.toString());
        assertEquals("11111111111111111111111111111111", v4.toString());
    }
    
    
// TESTS OR
    // Cas Normaux
    @Test
    public void testOrNormal() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        assertEquals("11001100110101011111111011110000", v1.or(v2).toString());
    }
    
    // Cas Limites
    @Test
    public void testOrLimit() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        
        assertEquals("11111111111111111111111111111111", v1.or(v2).toString());
        assertEquals("11111111111111111111111111111111", v1.or(v1).toString());
        assertEquals("00000000000000000000000000000000", v2.or(v2).toString());
    }
    
    // Cas d'erreurs
    @Test
    public void testOrError() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(64);
        
        assertThrows(IllegalArgumentException.class,() -> v1.or(v2));
    }
    
    
// TESTS AND
 // Cas Normaux
    @Test
    public void testAndNormal() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        assertEquals("00000000000000001010101000000000", v1.and(v2).toString());
    }
    
    // Cas Limites
    @Test
    public void testAndLimit() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        
        assertEquals("00000000000000000000000000000000", v1.and(v2).toString());
        assertEquals("11111111111111111111111111111111", v1.and(v1).toString());
        assertEquals("00000000000000000000000000000000", v2.and(v2).toString());
    }
    
    // Cas d'erreurs
    @Test
    public void testAndError() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(64);
        
        assertThrows(IllegalArgumentException.class,() -> v1.and(v2));
    }
    
    
// TESTS EXTRACTZEROEXTENDED
    @Test
    public void extractZeroExtendedTest() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = v1.extractZeroExtended(-4, 64);
        assertEquals("0000000000000000000000000000111111111111111111111111111111110000", v5.toString());
        BitVector v6 = v2.extractZeroExtended(-4, 64);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", v6.toString());
        BitVector v7 = v3.extractZeroExtended(-1, 32);
        assertEquals("10011000000000010101010111100000", v7.toString());
        BitVector v8 = v4.extractZeroExtended(-14, 64);
        assertEquals("0000000000000000000000000011010101111111100000000000000000000000", v8.toString());
        BitVector v9 = v3.extractZeroExtended(-32, 32);
        assertEquals("00000000000000000000000000000000", v9.toString());
    }
    
    
// TESTS EXTRACTWRAPPED
    @Test
    public void extractWrappedTest() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = v1.extractWrapped(-4, 32);
        assertEquals("11111111111111111111111111111111", v5.toString());
        BitVector v6 = v2.extractWrapped(-4, 64);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", v6.toString());
        BitVector v7 = v3.extractWrapped(-1, 32);
        assertEquals("10011000000000010101010111100001", v7.toString());
        BitVector v8 = v4.extractWrapped(-14, 64);
        assertEquals("0111111110000000000000000011010101111111100000000000000000110101", v8.toString());
                  // "0111111110000000000000000011010101111111100000000000000000110101"
        BitVector v9 = v3.extractWrapped(-32, 32);
        assertEquals("11001100000000001010101011110000", v9.toString());
    }
    
    
// TESTS SHIFT
    @Test
    public void shiftTest() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = v1.shift(4);
        assertEquals("11111111111111111111111111110000", v5.toString());
        BitVector v6 = v1.shift(-6);
        assertEquals("00000011111111111111111111111111", v6.toString());
        
        BitVector v7 = v2.shift(12);
        assertEquals("00000000000000000000000000000000", v7.toString());
        BitVector v8 = v2.shift(-12);
        assertEquals("00000000000000000000000000000000", v8.toString());
        
        BitVector v9 = v3.shift(11);
        assertEquals("00000101010101111000000000000000", v9.toString());
        BitVector v10 = v3.shift(-5);
        assertEquals("00000110011000000000010101010111", v10.toString());
        
        BitVector v11 = v4.shift(2);
        assertEquals("00000011010101111111100000000000", v11.toString());
        BitVector v12 = v4.shift(-3);
        assertEquals("00000000000110101011111111000000", v12.toString());
    }
    
    
// TESTS EQUALS
    @Test
    public void equalsTest() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
     
     // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = new BitVector(64, true);
        BitVector v6 = new BitVector(64, true);
        
        BitVector v7 = new BitVector(5*32);
        BitVector v8 = new BitVector(5*32);
        
        assertEquals(true, v1.equals(v3));
        assertEquals(true, v2.equals(v4));
        assertEquals(true, v5.equals(v6));
        assertEquals(true, v7.equals(v8));
        
        assertEquals(false, v1.equals(v8));
        assertEquals(false, v2.equals(v1));
        assertEquals(false, v5.equals(v7));
        assertEquals(false, v3.equals(v8));
    }
    
 
// TESTS HASHCODE
    @Test
    public void hashCodeTest() {
        // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
     
     // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = new BitVector(64, true);
        BitVector v6 = new BitVector(64, true);
        
        BitVector v7 = new BitVector(5*32);
        BitVector v8 = new BitVector(5*32);
        
        assertEquals(v1.hashCode(), v3.hashCode());
        assertEquals(v2.hashCode(), v4.hashCode());
        assertEquals(v5.hashCode(), v6.hashCode());
        assertEquals(v7.hashCode(), v8.hashCode());
        
        assertNotEquals(v1.hashCode(), v8.hashCode());
        assertNotEquals(v2.hashCode(), v1.hashCode());
        assertNotEquals(v7.hashCode(), v5.hashCode());
        assertNotEquals(v3.hashCode(), v8.hashCode());
    }
    
// TESTS TOSTRING
    // Cas normaux
    @Test
    public void toStringNormal() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
   
        BitVector v6 = new BitVector(32, true);
        
        BitVector v8 = new BitVector(32);
        
        assertEquals("11001100000000001010101011110000", v1.toString());
        assertEquals("00000000110101011111111000000000", v2.toString());
        assertEquals("11111111111111111111111111111111", v6.toString());
        assertEquals("00000000000000000000000000000000", v8.toString());
    }
}