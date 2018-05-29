package ch.epfl.gameboj;

/**
 * A Register. This interface is meant to be implemented by enum types only.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
public interface Register {
    
    /**
     * @return an element's index in the register.
     */
    public int ordinal();
    
    /**
     * @return an element's index in the register.
     */
    public default int index() {
        return ordinal();
    }
}
