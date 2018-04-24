package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.memory.Ram;

public final class LcdController implements Component, Clocked {
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 140;

    private long nextNonIdleCycle;
    private long lcdOnCycle;
    private Cpu cpu;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;

    private int lineCount;

    public LcdController(Cpu cpu) {
        this.cpu = cpu;
    }

    private final Ram videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);

    private enum LcdReg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    private enum LCDC implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum STAT implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED
    }

    public LcdImage currentImage() {
        if (currentImage != null) {
            return currentImage;
        }
        BitVector v = new BitVector(LCD_WIDTH, false);
        ArrayList<LcdImageLine> lines = new ArrayList<LcdImageLine>(LCD_HEIGHT);
        for (int i = 0; i < LCD_HEIGHT; i++) {
            lines.add(new LcdImageLine(v, v, v));
        }
        return new LcdImage(LCD_WIDTH, LCD_HEIGHT, lines);
    }

    RegisterFile<LcdReg> file = new RegisterFile<LcdReg>(LcdReg.values());

    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address < AddressMap.REGS_LCDC_END
                && address >= AddressMap.REGS_LCDC_START) {
            int index = address - AddressMap.REGS_LCDC_START;
            return file.get(LcdReg.values()[index]);
        }
        if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            return videoRam.read(address - AddressMap.VIDEO_RAM_START);
        }
        return NO_DATA;
    }

    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            int index = address - AddressMap.REGS_LCDC_START;
            LcdReg reg = LcdReg.values()[index];
            if (reg == LcdReg.LCDC && !Bits.test(data, LCDC.LCD_STATUS)) {
                setMode(0);
                file.set(LcdReg.LY, 0);
                nextNonIdleCycle = Long.MAX_VALUE;
            }
            if (reg == LcdReg.STAT) {
                final int value = file.get(LcdReg.STAT);
                final int newValue = Bits.extract(data, 3, 5);
                file.set(LcdReg.STAT, Bits.clip(3, value) | (newValue << 3));
            }
            if (reg == LcdReg.LY || reg == LcdReg.LYC) {
                modifyLYLYC(reg, data);
            }
        } else if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
        }
    }

    public void cycle(long cycle) {
        if (nextNonIdleCycle == Long.MAX_VALUE
                && testInReg(LcdReg.LCDC, LCDC.LCD_STATUS)) {
            nextNonIdleCycle = cycle;
            lcdOnCycle = cycle;
            reallyCycle(cycle);
            return;
        }
        if (cycle < nextNonIdleCycle)
            return;
        reallyCycle(cycle);
    }

    public void reallyCycle(long cycle) {
        int cyclesFromImageStart = (int) (cycle - lcdOnCycle) % (154 * 114);
        if (cyclesFromImageStart == 0) {
            nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
            setMode(1);
        }
        
        switch (getMode()) {
        case 1:
            setMode(2);
            nextNonIdleCycle += 20;
            break;
        case 2:
            setMode(3);
            nextImageBuilder.setLine(lineCount, computeLine(lineCount));
            nextNonIdleCycle += 43;
            break;
        case 3:
            setMode(0);
            lineCount++;
            nextNonIdleCycle += 51;
            break;
        case 0:
            setMode(1);
            currentImage = nextImageBuilder.build();
            nextNonIdleCycle += 10 * 114;
            lineCount = 0;
            break;
        default:
            break;
        }
        if (cyclesFromImageStart % 114 == 0)

        {
            file.set(LcdReg.LY, lineCount);
        }

    }

    private void setMode(int mode) {
        if (mode < 3) {
            if (testInReg(LcdReg.STAT, STAT.INT_MODE0.index() + mode)) {
                cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
            if (mode == 1) {
                cpu.requestInterrupt(Interrupt.VBLANK);
            }
        }
        setInReg(LcdReg.STAT, STAT.MODE0, Bits.test(mode, 0));
        setInReg(LcdReg.STAT, STAT.MODE1, Bits.test(mode, 1));

    }

    private int getMode() {
        int strongBit = testInReg(LcdReg.STAT, STAT.MODE1) ? 1 : 0;
        int weakBit = testInReg(LcdReg.STAT, STAT.MODE0) ? 1 : 0;
        return strongBit << 1 | weakBit;
    }

    private LcdImageLine computeLine(int index) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(LCD_WIDTH);
        final int dataStart = (testInReg(LcdReg.LCDC, LCDC.BG_AREA)
                ? AddressMap.BG_DISPLAY_DATA[1]
                : AddressMap.BG_DISPLAY_DATA[0]);
        final int startTileIndexInData = (index + file.get(LcdReg.SCY) / 8) * 32
                + (file.get(LcdReg.SCX) / 8);
        final int startTile = dataStart + startTileIndexInData;
        final int lineIndex = index % 8;

        for (int i = 0; i < 20; ++i) {
            int tileIndex = read(i + startTile);
            // System.out.println(Integer.toHexString(i+startTile) + " " +
            // tileIndex + " " + index);
            int strongBits = getLineFromTile(tileIndex, 2 * lineIndex+1);
            int weakBits = getLineFromTile(tileIndex, 2 * lineIndex);
            if (strongBits != 0)
                // System.out.println(Integer.toBinaryString(strongBits));
                lineBuilder.setBytes(i, strongBits, weakBits);
        }
        return lineBuilder.build();
    }

    private int getTileAddress(int index) {
        boolean tileSource = testInReg(LcdReg.LCDC, LCDC.TILE_SOURCE);
        if (index != 0 && index != 256)
            System.out.println(index);
        if (tileSource) {
            return AddressMap.TILE_SOURCE[1] + index * 16;
        } else {
            if (index >= 0x80)
                return AddressMap.TILE_SOURCE[1] + index * 16;
            return 0x9000 + index * 16;

        }
    }

    private int getLineFromTile(int tileIndex, int lineIndex) {

        return read(getTileAddress(tileIndex) + lineIndex);
    }

    private void modifyLYLYC(LcdReg reg, int data) {
        LcdReg other = (reg == LcdReg.LY ? LcdReg.LYC : LcdReg.LY);
        if (data == file.get(other) && testInReg(LcdReg.STAT, STAT.INT_LYC)) {
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
        setInReg(LcdReg.STAT, STAT.LYC_EQ_LY, file.get(other) == data);
    }

    private boolean testInReg(LcdReg reg, int index) {
        return Bits.test(file.get(reg), index);
    }

    private boolean testInReg(LcdReg reg, Bit index) {
        return Bits.test(file.get(reg), index.index());
    }

    private void setInReg(LcdReg reg, int index, boolean newBitValue) {
        final int value = file.get(reg);
        final int newValue = Bits.set(value, index, newBitValue);
        file.set(reg, newValue);
    }

    private void setInReg(LcdReg reg, Bit index, boolean newBitValue) {
        setInReg(reg, index.index(), newBitValue);
    }

}
