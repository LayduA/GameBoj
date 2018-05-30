package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * A simulated, functional GameBoy.
 * 
 * @author Adrien Laydu, Michael Tasev
 *
 */
public final class GameBoy implements AddressMap {

    // A couple useful constants.
    public static final long CYCLES_PER_SECOND = 1L << 20;
    public static final double CYCLES_PER_NANOSECOND = (double)(CYCLES_PER_SECOND /1e9);

    private final Bus bus;
    private final Cpu cpu;

    private final Timer timer;
    private final LcdController lcdController;
    private final Joypad joypad;

    private int cycles;

    /**
     * Creates a new GameBoy with the given cartridge.
     * 
     * @param cartridge
     *            : the initial cartridge of the GameBoy
     */
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);

        // Creating the work RAM.
        final Ram ram = new Ram(WORK_RAM_SIZE);

        // The echo RAM has the same reference as the work RAM to be able to
        // write in one and read in the other.
        final Ram echoRam = ram;

        // The two RAM controllers. Note that the echo RAM is de facto smaller
        // than the work RAM.
        final RamController ramController = new RamController(ram,
                WORK_RAM_START);
        final RamController echoRamController = new RamController(echoRam,
                ECHO_RAM_START, ECHO_RAM_END);

        // Initialising all the components.
        cpu = new Cpu();
        bus = new Bus();
        timer = new Timer(cpu);
        lcdController = new LcdController(cpu);
        joypad = new Joypad(cpu);

        // The boot rom controller of the GameBoy.
        final BootRomController BRC = new BootRomController(cartridge);

        // Attaching our components.
        ramController.attachTo(bus);
        echoRamController.attachTo(bus);

        BRC.attachTo(bus);
        timer.attachTo(bus);

        cpu.attachTo(bus);
        lcdController.attachTo(bus);
        joypad.attachTo(bus);

    }

    /**
     * Simulates the GameBoy until a given cycle (excluded).
     * 
     * @param cycle
     *            : a long, the cycle until which the GameBoy is to be run.
     * @throws IllegalArgumentException
     *             if the given cycle has already been processed.
     */
    public void runUntil(long cycle) {
        Preconditions.checkArgument(cycles <= cycle);
        while (cycles < cycle) {
            timer.cycle(cycles);
            cpu.cycle(cycles);
            lcdController.cycle(cycles);
            cycles += 1;
        }
    }

    /**
     * Gets the GameBoy's bus.
     * 
     * @return the GameBoy's bus.
     */
    public Bus bus() {
        return bus;
    }

    /**
     * Gets the GameBoy's cpu
     * 
     * @return the GameBoy's cpu.
     */
    public Cpu cpu() {
        return cpu;
    }

    /**
     * Gets the number of cycle the GameBoy has processed yet.
     * 
     * @return an integer, the number of cycles processed.
     */
    public int cycles() {
        return cycles;
    }

    /**
     * Gets the GameBoy's Timer
     * 
     * @return the GameBoy's timer.
     */
    public Timer timer() {
        return timer;
    }

    /**
     * Gets the GameBoy's lcd controller.
     * 
     * @return the GameBoy's lcd controller.
     */
    public LcdController lcdController() {
        return lcdController;
    }

    /**
     * Gets the GameBoy's joypad.
     * 
     * @return the GameBoy's joypad.
     */
    public Joypad joypad() {
        return joypad;
    }
}
