package ch.epfl.gameboj;

import java.util.ArrayList;

import ch.epfl.gameboj.component.Component;
import static ch.epfl.gameboj.Preconditions.*;

public final class Bus {

    /**
     * The (dynamic) array containing all the bus' components
     */
    private ArrayList<Component> components = new ArrayList<Component>();

    /**
     * Attach a component to the Bus, by adding it to the components list.
     * @param component, the component to attach.
     * @throws NullPointerException if the component is null.
     */
    public void attach(Component component) {
        if (component == null) {
            throw new NullPointerException();
        } else {
            components.add(component);
        }
    }

    /**
     * Reads through all the bus' components at a given address.
     * @param address , the 16-bit address to read at
     * @return the data stored at the address. If no component stores data at the address, returns 0xFF.
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
                    found = true;
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
     */
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        for (int i = 0; i < components.size(); i++) {
            (components.get(i)).write(address, data);
        }
    }
}
