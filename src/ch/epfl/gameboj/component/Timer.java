package ch.epfl.gameboj.component;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import static ch.epfl.gameboj.bits.Bits.*;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

public final class Timer implements Component, Clocked {

    private Cpu cpu;

    private int clock;
    private int TIMA;
    private int TMA;
    private int TAC;

    public Timer(Cpu cpu) {
        if (cpu == null)
            throw new NullPointerException();
        this.cpu = cpu;
    }

    public int read(int address) {
        checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV:
            return Bits.extract(clock, 8, 8);
        case AddressMap.REG_TIMA:
            return TIMA;
        case AddressMap.REG_TMA:
            return TMA;
        case AddressMap.REG_TAC:
            return TAC;
        default:
            return NO_DATA;
        }
    }

    public void write(int address, int data) {
        checkBits16(address);
        switch (address) {
        case AddressMap.REG_DIV:
            clock = 0;
        case AddressMap.REG_TIMA:
            checkBits8(data);
            TIMA = data;
        case AddressMap.REG_TMA:
            checkBits8(data);
            TMA = data;
        case AddressMap.REG_TAC:
            checkBits8(data);
            boolean state0 = state();
            TAC = data;
            TIMA = incIfChange(TIMA, state0);
            checkTIMA();
        default:
            return;
        }
    }

    public void cycle(long cycle) {
        if (clock == 0xFFFF) {
            clock = 0;
        } else {
            boolean state0 = state();
            clock += 1;
            TIMA = incIfChange(TIMA, state0);
            checkTIMA();
        }

    }

    private int getIndex() {
        int temp = clip(2, TAC);
        switch (temp) {
        case 0:
            return 9;
        case 1:
            return 3;
        case 2:
            return 5;
        case 3:
            return 7;
        default:
            return 0;
        }
    }
    private void checkTIMA() {
        if (TIMA == 0x100) {
            cpu.requestInterrupt(Interrupt.TIMER);
            TIMA = TMA;
        }
    }

    private boolean state() {
        return test(TAC, 2) && test(clock, getIndex());
    }
    private int incIfChange(int value, boolean previousState) {
        if(previousState && !state()) {
            return value+1;
        }else {
            return value;
        }
    }
}
