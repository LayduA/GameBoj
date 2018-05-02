package ch.epfl.gameboj.component.lcd;

import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.util.ArrayList;
import java.util.Arrays;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
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

    private Bus bus;

    private static final LcdImageLine EMPTY_LINE = new LcdImageLine(
            new BitVector(LCD_WIDTH, false), new BitVector(LCD_WIDTH, false),
            new BitVector(LCD_WIDTH, false));

    private int winY;
    private long nextNonIdleCycle = Long.MAX_VALUE;
    private Cpu cpu;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage currentImage;

    private int lineCount;

    private int copySource;
    private int copyDest;

    private final Ram videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
    private final Ram objectRam = new Ram(AddressMap.OAM_RAM_SIZE);

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

    private enum Sprite implements Bit {
        UNUSED0, UNUSED1, UNUSED2, UNUSED3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }

    public LcdController(Cpu cpu) {
        this.cpu = cpu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#attachTo(ch.epfl.gameboj.Bus)
     */
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        bus.attach(this);
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
        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            return objectRam.read(address - AddressMap.OAM_START);
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

            switch (reg) {
            case LCDC:
                if (!Bits.test(data, LCDC.LCD_STATUS)) {
                    setMode(0);
                    modifyLYLYC(LcdReg.LY, 0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                } else {
                    file.set(reg, data);
                }
                break;
            case STAT:
                final int value = file.get(LcdReg.STAT);
                final int newValue = Bits.extract(data, 3, 5);
                file.set(LcdReg.STAT, Bits.clip(3, value) | (newValue << 3));
                break;
            case LYC:
                modifyLYLYC(reg, data);
                break;
            case LY:
                return;
            case DMA:
                file.set(reg, data);
                copySource = data;
                copyDest = 0;
                break;
            default:
                file.set(reg, data);
                break;

            }

        }
        if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
        }
        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            objectRam.write(address - AddressMap.OAM_START, data);
        }
    }

    /*
     * Describe the behavior of the controller at a certain cycle.
     * 
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    public void cycle(long cycle) {

        if (nextNonIdleCycle == Long.MAX_VALUE
                && testInReg(LcdReg.LCDC, LCDC.LCD_STATUS)) {
            nextNonIdleCycle = cycle;
            reallyCycle(cycle);
            return;
        }

        if(copyDest < objectRam.size()) {
            objectRam.write(copyDest,bus.read(copySource));
            copyDest ++;
            copySource ++;
        }
        
        if (cycle < nextNonIdleCycle
                || !testInReg(LcdReg.LCDC, LCDC.LCD_STATUS)) {
            return;
        }
        reallyCycle(cycle);
    }

    private void reallyCycle(long cycle) {

        if (file.get(LcdReg.LY) == 0) {
            nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
            winY = 0;
            lineCount = 0;
        }

        switch (getMode()) {
        case 1:
            if (lineCount == 153) {
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
                setMode(2);
                nextNonIdleCycle += 1;
                lineCount = 0;
                winY = 0;
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
        modifyLYLYC(LcdReg.LY, lineCount);
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

    private LcdImageLine computeLine(int index) {
        final LcdImageLine bgLine = computeBGLine(index);
        final int wx = file.get(LcdReg.WX) - 7;
        final LcdImageLine spritesLine = computeSpritesLine(index);

        if (index < file.get(LcdReg.WY) || wx < 0 || wx >= 160
                || !testInReg(LcdReg.LCDC, LCDC.WIN)) {
            return (testInReg(LcdReg.LCDC, LCDC.OBJ) ? bgLine.below(spritesLine)
                    : bgLine);
        }
        
        final LcdImageLine winLine = computeWinLine(winY);
        // System.out.println(wx);

        return bgLine.join(winLine, LCD_WIDTH - 1 - wx);

    }

    private LcdImageLine computeWinOrBGLine(int index, int dataStart,
            int shiftX, int shiftY) {
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(256);

        final int startTileLine = (((index + shiftY) % 256) / 8);
        final int startTile = startTileLine * 32;

        final int lineIndex = index % 8;

        for (int i = 0; i < 32; i++) {
            int tileIndex = read((31-i + startTile) + dataStart);
            final boolean tileSource = testInReg(LcdReg.LCDC, LCDC.TILE_SOURCE);
            int strongBits = getLineFromTile(tileIndex, (2 * lineIndex) + 1,tileSource);
            int weakBits = getLineFromTile(tileIndex, 2 * lineIndex,tileSource);
            lineBuilder.setBytes(i, strongBits, weakBits);
        }
        LcdImageLine l = lineBuilder.build();
        System.out.println(l.lsb());
        
        return l.mapColors(file.get(LcdReg.BGP))
                .extractWrapped(-LCD_WIDTH - shiftX, LCD_WIDTH);
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
        winY = (winY + 1) % 256;
        return computeWinOrBGLine(index, dataStart, -wx, 0);

    }

    private LcdImageLine computeSpritesLine(int index) {
        int[] sprites = spritesIntersectingLine(index);
        LcdImageLine line = EMPTY_LINE;
        if (sprites != null) {
            for (int i = 0; i < sprites.length; i++) {
                line = individualSpriteLine(sprites[i],
                        (index - objectRam.read(sprites[i]))+8 );
            }
        }
        return line;
    }


    private int getLineFromTile(int tileIndex, int lineIndex, boolean condition) {
        int address;
        Preconditions.checkBits8(tileIndex);
        if (condition) {

            address = AddressMap.TILE_SOURCE[1] + tileIndex * 16;
        } else {
            if (tileIndex >= 0x80) {
                address = AddressMap.TILE_SOURCE[1] + tileIndex * 16;
            }else {
            address = 0x9000 + tileIndex * 16;
            }

        }
        
        return Bits.reverse8(read(address + lineIndex));
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

    private int[] spritesIntersectingLine(int index) {
        int[] tiles = new int[10];
        int count = 0;
        int tileIndex = 0;
        while (count < 10 && tileIndex < AddressMap.OAM_RAM_SIZE - 3) {
            int yCoord = objectRam.read(tileIndex);
            if (index >= yCoord - 8 && index < yCoord) {
                
                tiles[count] = objectRam.read(tileIndex + 1) << 8 | tileIndex;
                count++;

            }
            tileIndex += 4;
        }
        if (count == 0) {
            return null;
        }
        Arrays.sort(tiles, 0, count - 1);
        final int[] tilesIndexes = new int[count];
        for (int i = 0; i < count; i++) {
            tilesIndexes[i] = Bits.clip(8, tiles[i]);
        }
        return tilesIndexes;
    }

    private LcdImageLine individualSpriteLine(int tileNumber, int lineIndex) {
        LcdImageLine.Builder builder = new LcdImageLine.Builder(LCD_WIDTH);
        int colors = file
                .get(Bits.test(objectRam.read(tileNumber + 3), Sprite.PALETTE)
                        ? LcdReg.OBP1
                        : LcdReg.OBP0);
        int tile = objectRam.read(tileNumber + 2);
        int weakBits = getLineFromTile(tile, 2 * lineIndex,true);
        int strongBits = getLineFromTile(tile, (2 * lineIndex) + 1,true);
        builder.setBytes(0, strongBits, weakBits);
        return builder.build().mapColors(colors)
                .shift(objectRam.read(tileNumber + 1));
    }

}
