package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

public interface Component {
    
    public final static int NO_DATA = 0x100;
    
    public abstract int read(int address);
    
    public abstract void write(int address,int data);
    
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }
    
}
