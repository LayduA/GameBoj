package ch.epfl.gameboj;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

import java.util.ArrayList;
import java.util.Objects;

import ch.epfl.gameboj.component.Component;

/**
 * A Bus linking different Components, able to read and write through them.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
public final class Bus {

    /**
     * The (dynamic) array containing all the bus' components
     */
    private final ArrayList<Component> components = new ArrayList<Component>();

    /**
     * Attach a component to the Bus, by adding it to the components list.
     * @param component, the component to attach.
     * @throws NullPointerException if the component is null.
     */
    public void attach(Component component) {
        Objects.requireNonNull(component);
        components.add(component);
        
    }

    /**
     * Reads through all the bus' components at a given address.
     * @param address , the 16-bit address to read at
     * @return the data stored at the address. If no component stores data at the address, returns 0xFF.
     * @throws IllegalArgumentException if the address is not a 16-bit number.
     */
    public int read(int address) {
        checkBits16(address);
        int value = 0xFF;
        if (components.size() != 0) {
            boolean found = false;
            int i = 0;
            do {
                if ((components.get(i)).read(address) != Component.NO_DATA) {
                    value = (components.get(i)).read(address);
                    found = true; //a value has been found. This is to make the loop stop after one value has been found.
                }
                i++;
            } while (!found && i < components.size());
        }
        return value;
    }

    /**
     * Writes at a given address in all components.
     * @param address , the 16-bit address in which to write
     * @param data , the 8-bit data to store at the address
     * @throws IllegalArgumentException if the address is not a 16-bit number, or the data not an 8-bit one
     */
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        for (int i = 0; i < components.size(); i++) {
            (components.get(i)).write(address, data);
        }
    }
}
