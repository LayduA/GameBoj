package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {

    private final int[] elements;
    private final static int intSize = Integer.SIZE;

    private BitVector(int[] elements) {
        this.elements = elements;

    }

    /**
     * Creates a bit vector of a given size, of which all bits are the same given value.
     * @param size : an integer divisible by 32, the size of the bit vector.
     * @param initialValue : the value of the bits. True represents 1, false represents 0.
     * @throws : IllegalArgumentException if the size is negative or not divisible by 32.
     */
    public BitVector(int size, boolean initialValue) {
        
        Preconditions.checkArgument(size %intSize == 0 && size >=0);
        final int[] elements = new int[size / intSize];
        Arrays.fill(elements, constructInt(initialValue));
        this.elements = elements;
    }

    /**
     * Creates a bit vector of a given size, of which all bits are 0.
     * @param size : the size of the vector.
     * @throws : IllegalArgumentException if the size is negative or not divisible by 32.
     */
    public BitVector(int size) {
        this(size, false);
    }
    
    public BitVector(BitVector b) {
        this(b.elements);
    }

    /**
     * Gets the bit vector's size.
     * @return an integer, the bit vector's size.
     */
    public int size() {
        return elements.length * Integer.SIZE;
    }

    /**
     * Tests the value of a given bit in the bit vector.
     * @param index : the bit to test.
     * @return a boolean, true if the bit tested is 1, false otherwise.
     * @throws IllegalArgumentException if the index is too large (eg >= the bit vector's size)
     */
    public boolean testBit(int index) {
        Preconditions.checkArgument(index<size());
        return Bits.test(elements[index / intSize], index % intSize);
    }

    /**
     * Computes and creates the binary complement (bit-to-bit) of the Bit vector, and returns it.
     * @return the complement of the bit vector
     */
    public BitVector not() {
        final int[] newElements = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            newElements[i] = ~elements[i];
        }
        return new BitVector(newElements);
    }

    /**
     * Computes and creates the binary conjunction (bit-to-bit) of the Bit vector with another given one.
     * @param other : the other bit vector.
     * @return the conjunction of the bit vectors.
     * @throws IllegalArgumentException if the two bit vectors are not the same size.
     */
    public BitVector and(BitVector other) {
        Preconditions.checkArgument(size() == other.size());
        final int[] newElements = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            newElements[i] = this.getElement(i) & other.getElement(i);
        }
        return new BitVector(newElements);
    }

    /**
     * Computes and creates the binary disjunction (bit-to-bit) of the Bit vector with another given one.
     * @param other : the other bit vector.
     * @return the disjunction of the bit vectors.
     * @throws IllegalArgumentException if the two bit vectors are not the same size.
     */
    public BitVector or(BitVector other) {
        Preconditions.checkArgument(size()==other.size());
        final int[] newElements = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            newElements[i] = this.getElement(i) | other.getElement(i);
        }
        return new BitVector(newElements);
    }
    
    private int getElement(int index) {
        return elements[elements.length-1-index];
    }

    private int constructInt(boolean b) {
        if (b)
            return -1;
        else
            return 0;
    }

    private enum Extension {
        ZERO, WRAP
    };


    /**
     * Extracts a bit vector of a given size from a given index, from the extension
     * by 0 of the original bit vector.
     * @param index : the index to start at (can be negative).
     * @param size : the size of the bit vector to extract.
     * @return the extracted bit vector.
     * @throws IllegalArgumentException if the size is not divisible by 32.
     */
    public BitVector extractZeroExtended(int index, int size) {
        return extract(index,size,Extension.ZERO);
    }

    /**
     * Extracts a bit vector of a given size from a given index, from the wrapped extension
     * of the original bit vector.
     * @param index : the index to start at (can be negative).
     * @param size : the size of the bit vector to extract.
     * @return the extracted bit vector.
     * @throws IllegalArgumentException if the size is not divisible by 32.
     */
    public BitVector extractWrapped(int index, int size) {
        return extract(index,size, Extension.WRAP);
    }
    private BitVector extract(int index, int size, Extension e) {
        
        Preconditions.checkArgument(size%32 == 0);
        final int elementsNumber = size /intSize;
        final int[] newElements = new int[elementsNumber];
        for(int i = 0;i<newElements.length;i++) {
            newElements[newElements.length-1-i] = getElementFromInfiniteExtension(index +intSize*i,e);
        }
        
        return new BitVector(newElements);
       
    }

  
    private int getElementFromInfiniteExtension(int index, Extension e) {
        final int iOver32 = Math.floorDiv(index, intSize);
        final int iMod32 = Math.floorMod(index, intSize);
        if (Math.floorMod(index, intSize) == 0) {
            if ((iOver32 < 0|| iOver32 >= elements.length)&&e == Extension.ZERO) {
                return 0;
            } else {
                return getElement(Math.floorMod(iOver32, elements.length));
            }
        }else {
            final int strongBits = Bits.clip(iMod32,getElementFromInfiniteExtension(index-iMod32+32,e));
            final int weakBits = Bits.extract(getElementFromInfiniteExtension(index-iMod32,e),iMod32,32-iMod32);
            return (strongBits << intSize-iMod32) | weakBits;
            
        }
    }
    /**
     * Shifts the bit vector a given distance (positive distance shifts to 
     * the left, negative distance to the right)
     * @param distance : the distance to shift.
     * @return a bit vector, with the value shifted.
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(-1 * distance, size());
    }
    
    /**
     * Converts the bit vector into a string composed of 1 and 0.
     */
    public String toString() {
        String s = "";
        for(int i = 0;i<elements.length;i++) {
            int a = getElement(i);
            s = Integer.toBinaryString(a)+s;
            while(s.length()%32!=0) {
                s = "0"+s;
            }
        }
        return s;
    }
    //TODO : a supprimer pour le rendu final
    public static BitVector rand() {
        int[] val = {-1, -8,2839, 7};
        return new BitVector(val);
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof BitVector) return equals((BitVector)other);
        return false;
    }
    
    private boolean equals(BitVector other) {
        if(size()!= other.size()) return false;
        for(int i = 0; i<elements.length;i++) {
            if(elements[i] != (other.elements)[i]) return false; 
        }
        return true;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }
}
