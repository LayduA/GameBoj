package ch.epfl.gameboj.bits;

import java.util.Arrays;

public final class BitVector {

    private final int[] elements;

    private BitVector(int[] elements) {
        this.elements = elements;

    }

    public BitVector(int size, boolean initialValue) {
        if (size % 32 != 0 || size < 0)
            throw new IllegalArgumentException();
        int[] elements = new int[size / 32];
        Arrays.fill(elements, constructInt(initialValue));
        this.elements = elements;
    }

    public BitVector(int size) {
        this(size, false);
    }

    public int size() {
        return elements.length * Integer.SIZE;
    }

    public boolean testBit(int index) {
        if (index >= size())
            throw new IllegalArgumentException();
        return Bits.test(elements[index / 32], index % 32);
    }

    public BitVector not() {
        int[] newElements = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            newElements[i] = ~elements[i];
        }
        return new BitVector(newElements);
    }

    public BitVector and(BitVector other) {
        if (this.size() != other.size())
            throw new IllegalArgumentException();
        int[] newElements = new int[elements.length];
        for (int i = 0; i < elements.length; i++) {
            newElements[i] = this.getElement(i) & other.getElement(i);
        }
        return new BitVector(newElements);
    }

    public BitVector or(BitVector other) {
        if (this.size() != other.size())
            throw new IllegalArgumentException();
        int[] newElements = new int[elements.length];
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
            return 0b11111111_11111111_11111111_11111111;
        else
            return 0;
    }

    private enum Extension {
        ZERO, WRAP
    };


    public BitVector extractZeroExtended(int index, int size) {
        return extract(index,size,Extension.ZERO);
    }

    public BitVector extractWrapped(int index, int size) {
        return extract(index,size, Extension.WRAP);
    }
    private BitVector extract(int index, int size, Extension e) {
        if (size%32 != 0) throw new IllegalArgumentException();
        int elementsNumber = size/32;
        int[] newElements = new int[elementsNumber];
        for(int i = 0;i<newElements.length;i++) {
            newElements[newElements.length-1-i] = getElementFromInfiniteExtension(index +32*i,e);
        }
        System.out.println(Arrays.toString(newElements));
        return new BitVector(newElements);
       
    }

  
    private int getElementFromInfiniteExtension(int index, Extension e) {
        int iOver32 = Math.floorDiv(index, 32);
        if (Math.floorMod(index, 32) == 0) {
            if ((iOver32 < 0|| iOver32 >= elements.length)&&e == Extension.ZERO) {
                return 0;
            } else {
                return getElement(Math.floorMod(iOver32, elements.length));
            }
        }
        else return 0;
    }
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
        int[] val = {424242, -8,2839, 7};
        return new BitVector(val);
    }
}
