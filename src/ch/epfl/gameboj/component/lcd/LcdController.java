package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;

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

/*
 * A component that controls the screen.
 */
public final class LcdController implements Component, Clocked {
    /*
     * The dimensions of the visible screen.
     */
    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private static final LcdImageLine EMPTY_LINE = new LcdImageLine(
            new BitVector(LCD_WIDTH, false), new BitVector(LCD_WIDTH, false),
            new BitVector(LCD_WIDTH, false));

    private int winY;
    private long nextNonIdleCycle = Long.MAX_VALUE;
    private long lcdOnCycle;
    private Cpu cpu;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;

    private int lineCount;

    private final Ram videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);

    private enum LcdReg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    RegisterFile<LcdReg> file = new RegisterFile<LcdReg>(LcdReg.values());

    private enum LCDC implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    private enum STAT implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC, UNUSED
    }

    public LcdController(Cpu cpu) {
        this.cpu = cpu;
    }

    /*
     * Returns the current image if it exists, or an image filled with empty
     * lines if the current image does not exist.
     */
    public LcdImage currentImage() {
        if (currentImage != null) {
            return currentImage;
        }
        ArrayList<LcdImageLine> lines = new ArrayList<LcdImageLine>(LCD_HEIGHT);
        for (int i = 0; i < LCD_HEIGHT; i++) {
            lines.add(EMPTY_LINE);
        }
        return new LcdImage(LCD_WIDTH, LCD_HEIGHT, lines);
    }

    /*
     * Reads the byte of data at the address given only if the address is of
     * LCDC registers or the video RAM. Returns NO_DATA otherwise.
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
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

    /*
     * Stores the given data at the given address, and modifies some registers
     * when needed.
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            int index = address - AddressMap.REGS_LCDC_START;
            LcdReg reg = LcdReg.values()[index];
            if (reg == LcdReg.LCDC && !Bits.test(data, LCDC.LCD_STATUS)) {
                setMode(0);
                modifyLYLYC(LcdReg.LY, 0);
                nextNonIdleCycle = Long.MAX_VALUE;
            } else if (reg == LcdReg.STAT) {
                final int value = file.get(LcdReg.STAT);
                final int newValue = Bits.extract(data, 3, 5);
                file.set(LcdReg.STAT, Bits.clip(3, value) | (newValue << 3));
            } else if (reg == LcdReg.LY || reg == LcdReg.LYC) {
                modifyLYLYC(reg, data);
            } else {
                file.set(reg, data);
            }
        }
        if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
        }
    }

    /*
     * Describe the behavior of the controller at a certain cycle.
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    public void cycle(long cycle) {
        if (nextNonIdleCycle == Long.MAX_VALUE
                && testInReg(LcdReg.LCDC, LCDC.LCD_STATUS)) {
            nextNonIdleCycle = cycle;
            lcdOnCycle = cycle;
            reallyCycle(cycle);
            return;
        }
        if (cycle < nextNonIdleCycle
                || !testInReg(LcdReg.LCDC, LCDC.LCD_STATUS)) {
            return;
        }
        reallyCycle(cycle);
    }

    private void reallyCycle(long cycle) {

        int cyclesFromLcdOn = (int) (cycle - lcdOnCycle);
        if (cyclesFromLcdOn == 0) {
            nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
            winY = 0;
            lineCount = 0;
        }

        modifyLYLYC(LcdReg.LY, lineCount);
        switch (getMode()) {
        case 1:
            if (lineCount == 153) {
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
                setMode(2);
                nextNonIdleCycle += 1;
                lineCount = 0;
                winY=0;
            } else {
                nextNonIdleCycle += 114;
                lineCount++;
            }
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
            if (lineCount < 144) {
                setMode(2);
                nextNonIdleCycle += 1;
            } else {
                setMode(1);
                currentImage = nextImageBuilder.build();

                nextNonIdleCycle += 114;
                lineCount++;

            }
            break;
        default:
            break;
        }
    }

    private void setMode(int mode) {
        if (mode < 3) {
            if (testInReg(LcdReg.STAT,
                    STAT.values()[STAT.INT_MODE0.index() + mode])) {
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

    private LcdImageLine computeBGLine(int index) {
        if (!testInReg(LcdReg.LCDC, LCDC.BG))
            return EMPTY_LINE;
        final int dataStart = (testInReg(LcdReg.LCDC, LCDC.BG_AREA)
                ? AddressMap.BG_DISPLAY_DATA[1]
                : AddressMap.BG_DISPLAY_DATA[0]);
        final int shiftX = file.get(LcdReg.SCX);
        final int shiftY = file.get(LcdReg.SCY);
        return computeWinOrBGLine(index, dataStart, shiftX, shiftY);
    }

    private LcdImageLine computeWinLine(int index) {
        final int wx = file.get(LcdReg.WX) - 7;
        
        final int dataStart = (testInReg(LcdReg.LCDC, LCDC.WIN_AREA)
                ? AddressMap.BG_DISPLAY_DATA[1]
                : AddressMap.BG_DISPLAY_DATA[0]);
        winY = (winY + 1)%256;
        return computeWinOrBGLine(index, dataStart, -wx, 0);

    }

    private LcdImageLine computeWinOrBGLine(int index, int dataStart,
            int shiftX, int shiftY) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(256);

        final int startTileLine = (((index + shiftY) % 256) / 8);
        final int startTile = startTileLine * 32;

        final int lineIndex = index % 8;

        for (int i = 0; i < 32; ++i) {
            int tileIndex = read((i + startTile) + dataStart);
            int strongBits = getLineFromTile(tileIndex, (2 * lineIndex) + 1);
            int weakBits = getLineFromTile(tileIndex, 2 * lineIndex);
            lineBuilder.setBytes(i, strongBits, weakBits);
        }
        return lineBuilder.build().mapColors(file.get(LcdReg.BGP))
                .extractWrapped(-LCD_WIDTH - shiftX, LCD_WIDTH);
    }

    private LcdImageLine computeLine(int index) {
        final LcdImageLine bgLine = computeBGLine(index);
        final int wx = file.get(LcdReg.WX)-7;
        if (index < file.get(LcdReg.WY) || wx < 0 || wx >= 160 || !testInReg(LcdReg.LCDC, LCDC.WIN)) {
            return bgLine;
        }
        final LcdImageLine winLine = computeWinLine(winY);
        //System.out.println(wx);
        return bgLine.join(winLine,LCD_WIDTH-1-wx);

    }

    private int getTileAddress(int index) {
        Preconditions.checkBits8(index);
        boolean tileSource = testInReg(LcdReg.LCDC, LCDC.TILE_SOURCE);
        if (tileSource) {

            return AddressMap.TILE_SOURCE[1] + index * 16;
        } else {
            if (index >= 0x80)
                return AddressMap.TILE_SOURCE[1] + index * 16;
            return 0x9000 + index * 16;

        }
    }

    private int getLineFromTile(int tileIndex, int lineIndex) {
        return Bits.reverse8(read(getTileAddress(tileIndex) + lineIndex));
    }

    private void modifyLYLYC(LcdReg reg, int data) {
        LcdReg other = (reg == LcdReg.LY ? LcdReg.LYC : LcdReg.LY);
        if (data == file.get(other) && testInReg(LcdReg.STAT, STAT.INT_LYC)) {
            cpu.requestInterrupt(Interrupt.LCD_STAT);
        }
        setInReg(LcdReg.STAT, STAT.LYC_EQ_LY, file.get(other) == data);
        file.set(reg, data);
    }

    private boolean testInReg(LcdReg reg, Bit index) {
        return file.testBit(reg, index);
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
