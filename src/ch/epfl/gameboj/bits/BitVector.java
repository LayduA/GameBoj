package ch.epfl.gameboj.bits;

import java.util.Arrays;

import ch.epfl.gameboj.Preconditions;

/**
 * A vector of bits which size is a multiple of 32.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
public final class BitVector {

    private final int[] elements;
    private final static int INT_SIZE = Integer.SIZE;

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
        
        Preconditions.checkArgument(size %INT_SIZE == 0 && size >=0);
        final int[] elements = new int[size / INT_SIZE];
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
    
    /**
     * Creates a copy of the bit vector given.
     * @param b : the bit vector to copy.
     */
    public BitVector(BitVector b) {
        this(b.elements);
    }

    /**
     * Gets the bit vector's size.
     * @return an integer, the bit vector's size.
     */
    public int size() {
        return elements.length * INT_SIZE;
    }

    /**
     * Tests the value of a given bit in the bit vector.
     * @param index : the bit to test.
     * @return a boolean, true if the bit tested is 1, false otherwise.
     * @throws IllegalArgumentException if the index is too large (eg >= the bit vector's size)
     */
    public boolean testBit(int index) {
        if (index >= size()) throw new IndexOutOfBoundsException();
        return Bits.test(elements[index / INT_SIZE], index % INT_SIZE);
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
        return elements[index];
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
        final int elementsNumber = size /INT_SIZE;
        final int[] newElements = new int[elementsNumber];
        for(int i = 0;i<newElements.length;i++) {
            newElements[i] = getElementFromInfiniteExtension(index +INT_SIZE*i,e);
        }
        
        return new BitVector(newElements);
       
    }

  
    private int getElementFromInfiniteExtension(int index, Extension e) {
        final int iOver32 = Math.floorDiv(index, INT_SIZE);
        final int iMod32 = Math.floorMod(index, INT_SIZE);
        if (Math.floorMod(index, INT_SIZE) == 0) {
            if ((iOver32 < 0|| iOver32 >= elements.length)&&e == Extension.ZERO) {
                return 0;
            } else {
                return getElement(Math.floorMod(iOver32, elements.length));
            }
        }else {
            final int strongBits = Bits.clip(iMod32,getElementFromInfiniteExtension(index-iMod32+32,e));
            final int weakBits = Bits.extract(getElementFromInfiniteExtension(index-iMod32,e),iMod32,32-iMod32);
            return (strongBits << INT_SIZE-iMod32) | weakBits;
            
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
            s = Integer.toBinaryString(a)+ s;
            
            while(s.length()%32!=0) {
                s = "0"+s;
            }
        }
        return s;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(elements);
    }

    /**
     * A bit vector builder.
     * 
     * @author Adrien Laydu, Michael Tasev
     *
     */
    public final static class Builder{
        private int[] newElements;
        
        /**
         * Creates a bit vector builder.
         * @param size : the size of the bit vector to build.
         * @throws IllegalArgumentException if the size is negative or not divisible by 32.
         */
        public Builder(int size) {
            Preconditions.checkArgument(size%INT_SIZE == 0&&size>0);
            newElements = new int[size/INT_SIZE];
        }
        
        /**
         * Sets the given byte at the given index of the bit vector.
         * @param index : the index of the byte to set.
         * @param value : the 8-bit value to set.
         * @return the builder with the byte value changed.
         * @throws IllegalStateException if the builder is building when the method is called.
         * @throws IndexOutOfBoundsException if the index is bigger than the number of bytes composing the bit vector.
         * @throws IllegalArgumentException if value is not an 8-bit value.
         */
        public Builder setByte(int index, int value) {
            if (newElements == null) throw new IllegalStateException();
            if (index > newElements.length*4) throw new IndexOutOfBoundsException();
            Preconditions.checkBits8(value);
            final int mask = 0b11111111 << 8*(index%4);
            final int antiMask = ~mask;
            final int previousValue = newElements[index/4] & antiMask;
            newElements[index/4] = previousValue|value<<8*(index%4); 
            return this;
        }
        
        /**
         * Sets the given integer at the given index of the bit vector.
         * @param index : the index of the integer to set.
         * @param value : the integer value to set.
         * @return the builder with the integer value changed.
         * @throws IllegalStateException if the builder is building when the method is called.
         * @throws IndexOutOfBoundsException if the index is bigger than the number of integers composing the bit vector.
         */
        public Builder setInt(int index, int value) {
            if (newElements == null) throw new IllegalStateException();
            Preconditions.checkIndex(newElements.length);
            newElements[index]=value;
            return this;
        }
        
        /**
         * Builds the bit vector.
         * @return the built bit vector.
         * @throws IllegalStateException if the index is bigger than the number of bytes composing the bit vector.
         */
        public BitVector build() {
            if (newElements == null) throw new IllegalStateException();
            BitVector bv = new BitVector(newElements);
            newElements = null;
            return bv;
        }
    }
}
