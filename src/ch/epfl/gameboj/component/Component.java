package ch.epfl.gameboj.component;

public interface Component {
    
    public final static int NO_DATA = 0x100;
    
    public abstract int read(int address);
    
    public abstract void write(int address,int data);
    
}
