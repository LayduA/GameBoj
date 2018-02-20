package gameboj;

import java.util.ArrayList;

import gameboj.component.Component;

public final class Bus {
    
    private ArrayList<Component> components;
    
    public void attach(Component component) {
        if (component == null) {
            throw new NullPointerException();
        }
        else {
            components.add(component);
        }
    }
    
    public int read(int address) {
        int value = 0xFF;
        
    }
}
