package ch.epfl.gameboj;

import java.util.ArrayList;

import ch.epfl.gameboj.component.Component;

public final class Bus {

    private ArrayList<Component> components = new ArrayList<Component>();

    public void attach(Component component) {
        if (component == null) {
            throw new NullPointerException();
        } else {
            components.add(component);
        }
    }

    public int read(int address) {
        Preconditions.checkBits16(address);
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

    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for (int i = 0; i < components.size(); i++) {
            (components.get(i)).write(address, data);
        }
    }
}
