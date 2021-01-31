package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.memory.Ram;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.UnaryOperator;

import static ch.epfl.gameboj.AddressMap.*;
import static ch.epfl.gameboj.Preconditions.*;
import static ch.epfl.gameboj.bits.Bits.reverse8;
import static ch.epfl.gameboj.component.lcd.LcdController.Registers.*;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class LcdController implements Component, Clocked {

    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private final Cpu belongingCpu;
    private final Ram vRam, OAMRam;
    private final RegisterFile<Registers> regs;
    private final Registers[] ALLREGS = Registers.values();
    private LcdImage currentImage;
    private Bus bus;
    private LcdImage.Builder nextImageBuilder;

    private long nextNonIdleCycle;

    //Constantes utilisées dans LcdController pour éviter les magicNumbers
    private final int REG_LCDC = 0xFF40;
    private final int REG_STAT = 0xFF41;
    private final int REG_LY = 0xFF44;
    private final int REG_LYC = 0xFF45;
    private final int REG_DMA = 0xFF46;

    private final int LY_MAX_VALUE = 154;

    private final int MODE2_CYCLES = 20;
    private final int MODE3_CYCLES = 43;
    private final int MODE0_CYCLES = 51;
    private final int CYCLEPERLINE = MODE2_CYCLES + MODE3_CYCLES + MODE0_CYCLES;

    private final int IMG_SIZE = 256;
    private final int LINES_PER_TILE = 8;
    private final int TILES_PER_LINE = IMG_SIZE / LINES_PER_TILE;
    private final int TILE_SIZE = 16;
    private final int SOURCE_RANGE = 2048;
    private final int TILES_PER_RANGE = 128;
    private final int MAX_SPRITES_PER_LINE = 10;

    private int winY;
    private boolean enableDMA;
    private int DMACount;

    /**
     * Registres du Controlleur
     */
    public enum Registers implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    /**
     * Enumeration représentant les bits du registre LCDC
     */
    private enum LCDCBits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    /**
     * Enumération représentant les bits de registre STAT
     */
    private enum STATBits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC
    }

    /**
     * Enumération représentant les modes du controlleur
     */
    private enum Mode implements Bit {
        M0, M1, M2, M3
    }

    /**
     * Attributs des sprites
     * YPOS : Coordonnée Y à l'écran
     * XPOS : Coordonnée X à l'écran
     * TILEINDEX : indice de la tuile à utiliser pour reprtésenter la sprite
     * BIN : diverse caractéristiques booléennes de la sprite
     */
    private enum Sprite implements Bit {
        YPOS, XPOS, TILEINDEX, BIN;

        public final static int size = Sprite.values().length;
    }

    /**
     * Caractéristiques booléennes (sur 1 bit) de la sprite :
     * PALETTE : palette à utiliser pour transformer les couleurs de la sprite (palette 0 ou 1)
     * FLIP_H : sprite inversée horizontalement
     * FLIP_V : sprite inversée verticalement
     * BEHIND_BG : position par rapport à l'arrière plan
     */
    private enum SpriteCharac implements Bit {
        UNUSED0, UNUSED1, UNUSED2, UNUSED3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG
    }

    public LcdController(Cpu cpu) {
        Objects.requireNonNull(cpu);

        belongingCpu = cpu;
        vRam = new Ram(VIDEO_RAM_SIZE);
        OAMRam = new Ram(OAM_RAM_SIZE);
        nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
        regs = new RegisterFile<>(Registers.values());
        nextNonIdleCycle = Long.MAX_VALUE;
    }

    /**
     * Retourne l'image actuellement affichée à l'écran
     *
     * @return l'image affichée à l'écran
     */
    public LcdImage currentImage() {
        return Objects.requireNonNullElse(currentImage, LcdImage.whiteLCD);
    }


    /**
     * Méthode appelant reallyCycle selon la valeur de cycle
     *
     * @param cycle : cycle d'index
     */
    @Override
    public void cycle(long cycle) {
        if (enableDMA)
            DMAStep();

        if (nextNonIdleCycle == Long.MAX_VALUE) {
            if (regs.testBit(LCDC, LCDCBits.LCD_STATUS)) {
                setMode(Mode.M2);
                nextNonIdleCycle = cycle + MODE2_CYCLES;
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
            }

        } else if (nextNonIdleCycle == cycle) {
            reallyCycle();
        }
    }

    /**
     * Méthode appelée par cycle lorsque le LcdController doit effectuer une action, s'occupe du changement de mode et
     * du dessin des lignes/images à afficher
     */
    private void reallyCycle() {

        Mode currentMode = getMode();
        Mode nextMode = currentMode;
        int currentLine = regs.get(LY);
        int newLine = currentLine;


        if (currentMode == Mode.M2) {
            nextMode = Mode.M3;
            nextNonIdleCycle += MODE3_CYCLES;
            nextImageBuilder.setLine(newLine, computeLine(newLine));
        }

        if (currentMode == Mode.M3) {
            nextMode = Mode.M0;
            nextNonIdleCycle += MODE0_CYCLES;
        }

        if (currentMode == Mode.M0) {
            newLine++;

            if (newLine == LCD_HEIGHT) {
                nextMode = Mode.M1;
                nextNonIdleCycle += CYCLEPERLINE;
                currentImage = nextImageBuilder.build();

            } else {
                nextMode = Mode.M2;
                nextNonIdleCycle += MODE2_CYCLES;
            }
        }


        if (currentMode == Mode.M1) {
            newLine++;

            if (newLine == LY_MAX_VALUE) {
                newLine = 0;
                nextMode = Mode.M2;
                nextNonIdleCycle += MODE2_CYCLES;
                winY = 0;
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);

            } else {
                nextNonIdleCycle += CYCLEPERLINE;
            }
        }

        if (newLine != currentLine) {
            writeLY(newLine);
        }

        if (nextMode != currentMode) {
            setMode(nextMode);
        }
    }

    /**
     * Méthode de lecture des registres ou des RAMS du controlleur
     *
     * @param address : adresse de l'octet à retourner
     * @return l'octet lu, ou NO_DATA si aucune donnée n'est accessible à l'adresse passée
     */
    @Override
    public int read(int address) {
        checkBits16(address);

        if (address >= REGS_LCDC_START && address < REGS_LCDC_END) {
            int index = address - REGS_LCDC_START;
            return regs.get(ALLREGS[index]);
        }

        if (address >= VIDEO_RAM_START && address < VIDEO_RAM_END) {
            address -= VIDEO_RAM_START;
            return vRam.read(address);
        }

        if (address >= OAM_START && address < OAM_END) {
            address -= OAM_START;
            return OAMRam.read(address);
        }
        return NO_DATA;
    }


    /**
     * Méthode d'écriture dans les registres ou RAMS du LcdController
     * N'écrit que si l'adresse passée représente un registre ou une des RAMS
     *
     * @param address : adresse de l'octet à écrire
     * @param data    : octet à écrire
     */
    @Override
    public void write(int address, int data) {
        checkBits8(data);
        checkBits16(address);

        if (address >= REGS_LCDC_START && address < REGS_LCDC_END) {
            switch (address) {

                case REG_LY: {
                }
                break;

                case REG_LCDC: {
                    regs.set(LCDC, data);
                    if (!regs.testBit(LCDC, LCDCBits.LCD_STATUS)) {
                        writeLY(0);
                        nextNonIdleCycle = Long.MAX_VALUE;
                        setMode(Mode.M0);
                    }
                }
                break;

                case REG_STAT: {
                    int weakThree = 0b111 & regs.get(Registers.STAT);
                    int strongFive = 0b1111_1000 & data;
                    int newData = strongFive | weakThree;
                    regs.set(Registers.STAT, newData);
                }
                break;

                case REG_LYC: {
                    writeLYC(data);
                }
                break;

                case REG_DMA: {
                    regs.set(Registers.DMA, data);
                    if (!enableDMA)
                        startDMA();
                }
                break;

                default: {
                    int index = address - REGS_LCDC_START;
                    regs.set(ALLREGS[index], data);
                }
                break;
            }
        }

        if (address >= VIDEO_RAM_START && address < VIDEO_RAM_END) {
            address -= VIDEO_RAM_START;
            vRam.write(address, data);
        }

        if (address >= OAM_START && address < OAM_END) {
            address -= OAM_START;
            OAMRam.write(address, data);
        }
    }

    /**
     * méthode de changement de mode (registre STAT), et de levée d'interruptions correspondantes dans le processeur
     *
     * @param mode mode: 0, 1, 2, 3
     */
    private void setMode(Mode mode) {

        boolean m0 = mode == Mode.M1 || mode == Mode.M3;
        regs.setBit(Registers.STAT, STATBits.MODE0, m0);

        boolean m1 = mode == Mode.M2 || mode == Mode.M3;
        regs.setBit(Registers.STAT, STATBits.MODE1, m1);


        if (mode == Mode.M0 && regs.testBit(Registers.STAT, STATBits.INT_MODE0))
            belongingCpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);


        if (mode == Mode.M1) {
            belongingCpu.requestInterrupt(Cpu.Interrupt.VBLANK);

            if (regs.testBit(Registers.STAT, STATBits.INT_MODE1))
                belongingCpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
        }


        if (mode == Mode.M2 && regs.testBit(Registers.STAT, STATBits.INT_MODE2))
            belongingCpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);
    }

    /**
     * Méthode retournant le mode actuel du Controlleur
     *
     * @return le mode du LcdController
     */
    private Mode getMode() {

        boolean b0 = regs.testBit(Registers.STAT, STATBits.MODE0);
        boolean b1 = regs.testBit(Registers.STAT, STATBits.MODE1);

        if (b0 && b1)
            return Mode.M3;
        else if (b1)
            return Mode.M2;
        else if (b0)
            return Mode.M1;
        else
            return Mode.M0;
    }

    /**
     * Méthode d'écriture dans LY, qui compare ensuite LY avec LYC
     *
     * @param newData octet à écrire
     */
    private void writeLY(int newData) {
        checkBits8(newData);
        regs.set(Registers.LY, newData);
        LYCompareLYC();
    }

    /**
     * Méthode d'écriture dans LYC, qui compare ensuite LY avec LYC
     *
     * @param newData nouvelle valeur à écrire
     */
    private void writeLYC(int newData) {
        checkBits8(newData);
        regs.set(Registers.LYC, newData);
        LYCompareLYC();
    }

    /**
     * Méthode comparant LY avec LYC, met à jour la valeur du bit LYC_EQ_LY de STAT et lève l'interrution LCD_STAT si nécessaire
     */
    private void LYCompareLYC() {

        if (regs.get(Registers.LYC) == regs.get(Registers.LY)) {
            regs.setBit(Registers.STAT, STATBits.LYC_EQ_LY, true);

            if (regs.testBit(Registers.STAT, STATBits.INT_LYC))
                belongingCpu.requestInterrupt(Cpu.Interrupt.LCD_STAT);

        } else if (regs.testBit(Registers.STAT, STATBits.LYC_EQ_LY))
            regs.setBit(Registers.STAT, STATBits.LYC_EQ_LY, false);
    }

    private LcdImageLine computeBGLine(int lineIndex) {
        checkArgument(lineIndex < LCD_HEIGHT);
        LcdImageLine line = LcdImageLine.LCDBlankLine;

        //Calcule la ligne de fond, si celle-ci est activée
        if (regs.testBit(LCDC, LCDCBits.BG)) {

            int SCX = regs.get(Registers.SCX);
            int SCY = regs.get(Registers.SCY);
            int BGArea = BG_DISPLAY_DATA[regs.testBit(Registers.LCDC, LCDCBits.BG_AREA) ? 1 : 0];
            int palette = regs.get(Registers.BGP);

            line = computeSubLine(BGArea, (SCY + lineIndex) % IMG_SIZE).extractWrapped(SCX, LCD_WIDTH).mapColors(palette);
        }

        return line;
    }

    private LcdImageLine computeWindowLine() {
        int WX = WX();
        int palette = regs.get(Registers.BGP);

        int startArea = BG_DISPLAY_DATA[regs.testBit(Registers.LCDC, LCDCBits.WIN_AREA) ? 1 : 0];
        LcdImageLine line = computeSubLine(startArea, winY).extractWrapped(0, LCD_WIDTH).mapColors(palette).shift(WX);
        winY++;
        return line;
    }


    private LcdImageLine computeSubLine(int dispayDataStart, int lineIndex) {

        int lineTileIndexAdress = dispayDataStart + (TILES_PER_LINE * (lineIndex / LINES_PER_TILE));
        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(IMG_SIZE);

        int tileLine = lineIndex % LINES_PER_TILE;
        int tileLSB;
        int tileMSB;
        int tileIndex;

        for (int i = 0; i < TILES_PER_LINE; i++) {

            tileIndex = read(lineTileIndexAdress + i);

            tileLSB = getBGTileByte(tileIndex, tileLine, false);
            tileMSB = getBGTileByte(tileIndex, tileLine, true);
            lineBuilder.setBytes(i, tileMSB, tileLSB);
        }
        return lineBuilder.build();
    }

    private LcdImageLine computeLine(int lineIndex) {
        checkArgument(lineIndex < LCD_HEIGHT);

        LcdImageLine backGround = computeBGLine(lineIndex);
        LcdImageLine result;

        //Conditions pour le calcul d'une ligne de la fenêtre
        int WY = regs.get(Registers.WY);
        int WX = WX();
        boolean winActivated = regs.testBit(LCDC, LCDCBits.WIN);
        boolean wxInRange = WX < LCD_WIDTH;
        boolean wyInRange = lineIndex >= WY;

        //Calcul de la fenêtre si elle est présente
        if (winActivated && wxInRange && wyInRange) {
            LcdImageLine windowLine = computeWindowLine();
            result = backGround.join(WX(), windowLine);
        } else {
            result = backGround;
        }


        //Calcul des lignes de sprites
        LcdImageLine BGSprites = LcdImageLine.LCDBlankLine;
        LcdImageLine FGSprites = LcdImageLine.LCDBlankLine;

        if (regs.testBit(Registers.LCDC, LCDCBits.OBJ)) {

            int[] intersectingLines = spritesIntersectingLine(lineIndex);
            for (int spriteID : intersectingLines) {

                LcdImageLine sprite = oneSpriteLine(spriteID, lineIndex);

                if (sAttribute(spriteID, SpriteCharac.BEHIND_BG))
                    BGSprites = sprite.below(BGSprites);
                else FGSprites = sprite.below(FGSprites);
            }
        }

        // <=> NOT (BGSprites AND NOT result) mais avec un NOT de moins, selon convention gameBoy
        BitVector opacity = BGSprites.opacity().not().or(result.opacity());
        result = BGSprites.below(result, opacity);
        result = result.below(FGSprites);

        return result;
    }

    /**
     * Méthode auxiliaire permettant d'ajuster WX automatiquement
     *
     * @return valeur de WX ajustée selon convention du GameBoy
     */
    private int WX() {
        int wx = regs.get(Registers.WX) - 7;
        wx = Math.max(0, wx);
        return wx;
    }

    private int getTileImageAddress(int index) {

        if (regs.testBit(LCDC, LCDCBits.TILE_SOURCE)) {
            return TILE_SOURCE[1] + (index * TILE_SIZE);
        } else {
            if (index <= 0x7F) {
                return TILE_SOURCE[0] + SOURCE_RANGE + (index * TILE_SIZE);
            } else {
                return TILE_SOURCE[0] + (((index - TILES_PER_RANGE)) * TILE_SIZE);
            }
        }
    }

    public void attachTo(Bus b) {
        b.attach(this);
        bus = b;
    }

    private void startDMA() {
        enableDMA = true;
        DMACount = 0;
    }

    private void DMAStep() {
        int address = Bits.make16(regs.get(Registers.DMA), DMACount);
        int data = bus.read(address);
        OAMRam.write(DMACount, data);

        if (++DMACount == OAMRam.size())
            enableDMA = false;

    }


    /**
     * Permet d'obtenir un attribut d'une Sprite
     *
     * @param spriteID : numéro de la sprite (40 Sprites en mémoire d'objets)
     * @param charac   : caractéristique de la sprite (voir dans l'énumération Sprite)
     * @return
     */
    private int sAttribute(int spriteID, Sprite charac) {
        checkArgument(spriteID < OAMRam.size() / Sprite.size);
        return OAMRam.read(spriteID * Sprite.size + charac.index());
    }

    private boolean sAttribute(int spriteID, SpriteCharac charac) {
        checkArgument(spriteID < OAMRam.size() / Sprite.size);
        int data = sAttribute(spriteID, Sprite.BIN);
        return Bits.test(data, charac);
    }

    private int spriteHeight() {
        return LINES_PER_TILE * (regs.testBit(LCDC, LCDCBits.OBJ_SIZE) ? 2 : 1);
    }


    /**
     * Calcul des sprites en intersection avec la ligne
     *
     * @param lineIndex : ligne que les sprites intersectent
     * @return tableau contenant les 10 premières sprites intersectant la ligne, triée selon con
     */
    private int[] spritesIntersectingLine(final int lineIndex) {

        int[] spritesID = new int[MAX_SPRITES_PER_LINE];
        int nbOfSprites = 0;

        //Ajout de la coordonnée x en vue du tri

        for (int i = 0; i < OAMRam.size() / 4 && nbOfSprites < MAX_SPRITES_PER_LINE; i++) {
            int yPos = sAttribute(i, Sprite.YPOS) - 2 * 8;
            if (lineIndex >= yPos && lineIndex < yPos + spriteHeight()) {
                spritesID[nbOfSprites] = Bits.make16(sAttribute(i, Sprite.XPOS), i);
                nbOfSprites++;
            }
        }

        Arrays.sort(spritesID, 0, nbOfSprites);
        int[] result = new int[nbOfSprites];

        for (int i = 0; i < nbOfSprites; i++) {
            result[i] = Bits.clip(8, spritesID[i]);
        }
        return result;
    }

    private LcdImageLine oneSpriteLine(int spriteId, int lineId) {

        int yPos = sAttribute(spriteId, Sprite.YPOS) - 16; //Ajustement selon convention du gameboy
        int xPos = sAttribute(spriteId, Sprite.XPOS) - 8; //Ajustement selon convention du gameboy
        boolean flipH = sAttribute(spriteId, SpriteCharac.FLIP_H);
        boolean flipV = sAttribute(spriteId, SpriteCharac.FLIP_V);
        Registers OBP = sAttribute(spriteId, SpriteCharac.PALETTE) ? OBP1 : OBP0;

        int palette = regs.get(OBP);
        int tileLine = lineId - yPos;

        if (flipV)
            tileLine = spriteHeight() - tileLine;

        int tileLSB = getSpriteTileByte(spriteId, tileLine, false);
        int tileMSB = getSpriteTileByte(spriteId, tileLine, true);

        if (!flipH) {
            tileLSB = reverse8(tileLSB);
            tileMSB = reverse8(tileMSB);
        }

        LcdImageLine.Builder lineBuilder = new LcdImageLine.Builder(LCD_WIDTH);
        return lineBuilder.setBytes(0, tileMSB, tileLSB).build().shift(xPos).mapColors(palette);
    }

    private int getSpriteTileByte(int tileIndex, int tileLine, boolean MSB) {
        int tileAddress = sAttribute(tileIndex, Sprite.TILEINDEX);
        int address = tileAddress * TILE_SIZE + tileLine * 2 + (MSB ? 1 : 0);

        return vRam.read(address);

    }

    private int getBGTileByte(int tileIndex, int tileLine, boolean MSB) {
        int tileImageAddress = getTileImageAddress(tileIndex);
        return reverse8(read((tileImageAddress + (MSB ? 1 : 0)) + (2 * tileLine)));
    }
}

