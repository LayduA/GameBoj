package ch.epfl.gameboj;

public interface Register {
    
    public int ordinal();
    
    public default int index() {
        return ordinal();
    }
}
