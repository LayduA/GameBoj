package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

public interface Component {
    
    public final static int NO_DATA = 0x100;
    
    /**
     * Reads the byte of data at the given address in the component, or NO_DATA if nothing is stored at the address.
     * @param address , the 16-bit address at which to read.
     * @return the byte of data stored, or NO_DATA if there is nothing to read.
     * @throws IllegalArgumentException if the address is not a 16-bit number.
     */
    public abstract int read(int address);
    
    /**
     * Stores the given byte of data at the given address in the component. Does nothing if the address is outside of the component.
     * @param address , the 16-bit address at which to store the byte of data.
     * @param data , the byte to store.
     * @throws IllegalArgumentException if the address is not a 16-bit number or if data is not an 8-bit number.
     */
    public abstract void write(int address,int data);
    
    /**
     * Attach the component to a given bus.
     * @param bus , the bus the component must be attched to.
     * 
     * @see ch.epfl.gameboj.Bus.attach()
     */
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }
    
}
