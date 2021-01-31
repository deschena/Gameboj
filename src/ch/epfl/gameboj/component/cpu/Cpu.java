package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Ram;

import static ch.epfl.gameboj.AddressMap.*;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import static ch.epfl.gameboj.bits.Bits.*;
import static ch.epfl.gameboj.component.cpu.Alu.*;
import static ch.epfl.gameboj.component.cpu.Alu.rotate;
import static java.lang.Integer.lowestOneBit;
import static java.lang.Integer.numberOfLeadingZeros;

/**
 * Classe représentant le processeur du GameBoy
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class Cpu implements Component, Clocked {

    private final int PREFIX = 0xCB;

    private enum FlagSrc {
        V0, V1, ALU, CPU
    }

    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD

    }

    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }


    private enum Reg16 {
        AF(Reg.A, Reg.F),
        BC(Reg.B, Reg.C),
        DE(Reg.D, Reg.E),
        HL(Reg.H, Reg.L);

        private final Reg firstReg, secondReg;

        /**
         * Constructeur l'enum Reg16, enregistre une référence aux deux registres le composant
         *
         * @param firstReg  premier registre du nom de la pair de registres
         * @param secondReg second registre du nom de la pair de registres
         */
        Reg16(Reg firstReg, Reg secondReg) {
            this.firstReg = firstReg;
            this.secondReg = secondReg;
        }
    }

    private final Ram highRam;
    private Bus bus;

    private int programCounter, stackPointer;
    private boolean interruptMasterEnable;
    private int interruptEnable, interruptFlags;
    private final RegisterFile<Register> regFile;
    private long nextNonIdleCycle;

    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(Opcode.Kind.PREFIXED);
    private static final Reg[] REGISTERS_8 = {Reg.B, Reg.C, Reg.D, Reg.E, Reg.H, Reg.L, null, Reg.A};
    private static final Reg16[] REGISTERS_16 = {Reg16.BC, Reg16.DE, Reg16.HL, Reg16.AF};


    public Cpu() {
        regFile = new RegisterFile<>(Reg.values());
        highRam = new Ram(HIGH_RAM_SIZE);
    }


    /**
     * méthode permettant de stocker la référence au bus passé
     *
     * @param bus : référence du bus à stocker
     */
    public void attachTo(Bus bus) {
        this.bus = bus;
        bus.attach(this);
    }


    /**
     * méthode de lecture d'octet, redéfinition de l'interface Component
     *
     * @param address : adresse de l'octet à retourner
     * @return l'octet lu, sous forme d'entier
     * @throws IllegalArgumentException : si l'address n'est pas une valeur 16 bits
     */
    public int read(int address) {
        checkBits16(address);

        if (address == REG_IE) return interruptEnable;
        else if (address == REG_IF) return interruptFlags;
        else if (address >= HIGH_RAM_START && address < HIGH_RAM_END) {
            return highRam.read(address - HIGH_RAM_START);
        }

        return NO_DATA;
    }

    /**
     * méthode d'écriture d'octet, redéfinition de l'interface Component
     *
     * @param address : adresse 16 bits de l'octet à écrire
     * @param data    : valeur 8 bits à écrire
     * @throws IllegalArgumentException : si data n'est pas une valeur 8 bits
     * @throws IllegalArgumentException : si address n'est pas une valeur 16 bits
     */
    public void write(int address, int data) {

        checkBits8(data);
        checkBits16(address);

        if (address == REG_IE) interruptEnable = data;
        else if (address == REG_IF) interruptFlags = data;
        else if (address >= HIGH_RAM_START && address < HIGH_RAM_END) {
            highRam.write(address - HIGH_RAM_START, data);
        }

    }

    /**
     * Crée un tableau contenant les opcodes du type passé en argument
     *
     * @param opcodeKind type d'opcode à filtrer
     * @return tableau d'Opcodes du type
     */
    private static Opcode[] buildOpcodeTable(Opcode.Kind opcodeKind) {
        Opcode[] resultOpcodeArray = new Opcode[Opcode.values().length];

        for (Opcode o : Opcode.values()) {
            if (o.kind == opcodeKind) {
                resultOpcodeArray[o.encoding] = o;

            }
        }
        return resultOpcodeArray;
    }

    /**
     * méthode de facilitation de tests
     *
     * @return un tableau contenant, dans l'ordre, la valeur des registres programCounter, stackPointer, A, F, B, C, D, E, H et L.
     */
    public int[] _testGetPcSpAFBCDEHL() {

        int[] tab = new int[10];
        tab[0] = programCounter;
        tab[1] = stackPointer;
        tab[2] = regFile.get(Reg.A);
        tab[3] = regFile.get(Reg.F);
        tab[4] = regFile.get(Reg.B);
        tab[5] = regFile.get(Reg.C);
        tab[6] = regFile.get(Reg.D);
        tab[7] = regFile.get(Reg.E);
        tab[8] = regFile.get(Reg.H);
        tab[9] = regFile.get(Reg.L);
        return tab;
    }

    /**
     * Contrôle l'état des interruptions
     *
     * @return une entier dont certains bits sont égaux à 1 ssi une interruption doit être levée
     */
    private int interruptState() {

        return clip(5, interruptEnable) & clip(5, interruptFlags);
    }


    /**
     * fait évoluer le composant en exécutant les opérations du cycle d'index passé
     *
     * @param cycle : cycle d'index
     */
    public void cycle(long cycle) {

        if (cycle == nextNonIdleCycle) reallyCycle();
        else {
            if ((nextNonIdleCycle == Long.MAX_VALUE) && (interruptState() != 0)) {

                nextNonIdleCycle = cycle;
                reallyCycle();

            }
        }

    }

    /**
     * vérifie si les interruptions sont activées et si une interruption est en attente, sinon exécute normalement la prochaine instruction
     */
    private void reallyCycle() {

        if (interruptMasterEnable && interruptState() != 0) {

            interruptMasterEnable = false;
            int lowestOneBit = lowestOneBit(interruptState());
            int index = Integer.SIZE - 1 - numberOfLeadingZeros(lowestOneBit);
            interruptFlags = set(interruptFlags, index, false);
            push16(programCounter);
            programCounter = INTERRUPTS[index];
            nextNonIdleCycle += 5;

        } else {
            dispatch(read8(programCounter));
        }
    }


    /**
     * exécute l'instruction correspondant à la famille Opcode reçu en argument
     *
     * @param opcode : opcode donné
     */
    private void dispatch(int opcode) {

        Opcode o;

        o = (opcode == PREFIX) ? PREFIXED_OPCODE_TABLE[read8AfterOpcode()] : DIRECT_OPCODE_TABLE[opcode];

        Reg reg;
        Reg16 reg16;
        int value;
        int index;
        int nextPC;

        nextPC = clip(16, o.totalBytes + programCounter);
        long nbOfCycles = o.cycles;

        switch (o.family) {
            case NOP: {
            }
            break;
            case LD_R8_HLR: {
                reg = extractReg(o, 3);
                value = read8AtHl();
                regFile.set(reg, value);
            }
            break;

            case LD_A_HLRU: {
                reg = Reg.A;
                value = read8AtHl();
                regFile.set(reg, value);
                incrementHl(extractHlIncrement(o));
            }
            break;

            case LD_A_N8R: {
                reg = Reg.A;
                value = read8(REGS_START + read8AfterOpcode());
                regFile.set(reg, value);
            }
            break;

            case LD_A_CR: {
                reg = Reg.A;
                value = read8(REGS_START + regFile.get(Reg.C));
                regFile.set(reg, value);
            }
            break;

            case LD_A_N16R: {
                reg = Reg.A;
                value = read8(read16AfterOpcode());
                regFile.set(reg, value);
            }
            break;

            case LD_A_BCR: {
                reg = Reg.A;
                value = read8(reg16(Reg16.BC));
                regFile.set(reg, value);
            }
            break;

            case LD_A_DER: {
                reg = Reg.A;
                value = read8(reg16(Reg16.DE));
                regFile.set(reg, value);
            }
            break;

            case LD_R8_N8: {
                reg = extractReg(o, 3);
                value = read8AfterOpcode();
                regFile.set(reg, value);
            }
            break;

            case LD_R16SP_N16: {
                reg16 = extractReg16(o);
                value = read16AfterOpcode();
                setReg16SP(reg16, value);
            }
            break;

            case POP_R16: {
                reg16 = extractReg16(o);
                setReg16(reg16, pop16());
            }
            break;

            case LD_HLR_R8: {
                reg = extractReg(o, 0);
                value = regFile.get(reg);
                write8AtHl(value);
            }
            break;

            case LD_HLRU_A: {
                reg = Reg.A;
                value = regFile.get(reg);
                write8AtHl(value);
                incrementHl(extractHlIncrement(o));
            }
            break;

            case LD_N8R_A: {
                reg = Reg.A;
                value = regFile.get(reg);
                write8(REGS_START + read8AfterOpcode(), value);
            }
            break;

            case LD_CR_A: {
                reg = Reg.A;
                value = regFile.get(reg);
                write8(REGS_START + regFile.get(Reg.C), value);
            }
            break;

            case LD_N16R_A: {
                reg = Reg.A;
                value = regFile.get(reg);
                write8(read16AfterOpcode(), value);
            }
            break;

            case LD_BCR_A: {
                write8(reg16(Reg16.BC), regFile.get(Reg.A));
            }
            break;

            case LD_DER_A: {
                reg = Reg.A;
                value = regFile.get(reg);
                write8(reg16(Reg16.DE), value);
            }
            break;

            case LD_HLR_N8: {
                write8AtHl(read8AfterOpcode());
            }
            break;

            case LD_N16R_SP: {
                write16(read16AfterOpcode(), stackPointer);
            }
            break;

            case LD_R8_R8: {
                reg = extractReg(o, 3);
                Reg regToCopy = extractReg(o, 0);

                if (reg != regToCopy)
                    regFile.set(reg, regFile.get(regToCopy));
            }
            break;

            case LD_SP_HL: {
                stackPointer = reg16(Reg16.HL);
            }
            break;

            case PUSH_R16: {
                push16(reg16(extractReg16(o)));
            }
            break;

            // Add
            case ADD_A_R8: {
                reg = Reg.A;
                Reg regToAdd = extractReg(o, 0);
                value = add(regFile.get(reg), regFile.get(regToAdd), getCarry(o, 3));


                setRegFlags(reg, value);
            }
            break;

            case ADD_A_N8: {
                reg = Reg.A;
                value = add(regFile.get(reg), read8AfterOpcode(), getCarry(o, 3));

                setRegFlags(reg, value);

            }
            break;

            case ADD_A_HLR: {
                reg = Reg.A;
                value = add(regFile.get(reg), read8AtHl(), getCarry(o, 3));

                setRegFlags(reg, value);
            }
            break;

            case INC_R8: {
                reg = extractReg(o, 3);
                value = add(regFile.get(reg), 1);
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);
            }
            break;

            case INC_HLR: {
                value = add(read8AtHl(), 1);
                write8AtHl(unpackValue(value));
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.CPU);

            }
            break;

            case INC_R16SP: {
                reg16 = extractReg16(o);
                value = add16H(reg16SP(reg16), 1);
                setReg16SP(reg16, unpackValue(value));

            }
            break;

            case ADD_HL_R16SP: {
                reg16 = extractReg16(o);

                value = add16H(reg16SP(reg16), reg16(Reg16.HL));
                setReg16SP(Reg16.HL, unpackValue(value));
                combineAluFlags(value, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
            }
            break;

            case LD_HLSP_S8: {
                value = add16L(stackPointer, clip(16, signExtend8(read8AfterOpcode())));

                if (test(o.encoding, 4)) {

                    setReg16(Reg16.HL, unpackValue(value));
                    combineAluFlags(value, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);
                } else {
                    stackPointer = clip(16, unpackValue(value));
                    combineAluFlags(value, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU, FlagSrc.ALU);

                }
            }
            break;

            // Subtract
            case SUB_A_R8: {
                reg = Reg.A;
                Reg regToSub = extractReg(o, 0);

                value = sub(regFile.get(reg), regFile.get(regToSub), getCarry(o, 3));

                setRegFlags(reg, value);
            }
            break;

            case SUB_A_N8: {
                reg = Reg.A;
                value = sub(regFile.get(reg), read8AfterOpcode(), getCarry(o, 3));

                setRegFlags(reg, value);
            }
            break;

            case SUB_A_HLR: {
                reg = Reg.A;
                value = sub(regFile.get(reg), read8AtHl(), getCarry(o, 3));

                setRegFlags(reg, value);
            }
            break;

            case DEC_R8: {

                reg = extractReg(o, 3);


                value = sub(regFile.get(reg), 1);
                setRegFromAlu(reg, value);

                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
            }
            break;

            case DEC_HLR: {
                value = sub(read8AtHl(), 1);
                write8AtHl(unpackValue(value));
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU, FlagSrc.CPU);
            }
            break;

            case CP_A_R8: {
                reg = Reg.A;
                Reg regToSub = extractReg(o, 0);
                value = sub(regFile.get(reg), regFile.get(regToSub));

                setFlags(value);
            }
            break;

            case CP_A_N8: {
                reg = Reg.A;
                value = sub(regFile.get(reg), read8AfterOpcode());
                setFlags(value);
            }
            break;

            case CP_A_HLR: {
                reg = Reg.A;
                value = sub(regFile.get(reg), read8AtHl());

                setFlags(value);
            }
            break;

            case DEC_R16SP: {
                reg16 = extractReg16(o);

                setReg16SP(reg16, clip(16, (reg16SP(extractReg16(o)) - 1)));

            }
            break;

            // And, or, xor, complement
            case AND_A_N8: {
                value = and(regFile.get(Reg.A), read8AfterOpcode());
                setRegFlags(Reg.A, value);

            }
            break;

            case AND_A_R8: {
                reg = extractReg(o, 0);
                value = and(regFile.get(Reg.A), regFile.get(reg));
                setRegFlags(Reg.A, value);
            }
            break;

            case AND_A_HLR: {
                value = and(regFile.get(Reg.A), read8AtHl());
                setRegFlags(Reg.A, value);
            }
            break;

            case OR_A_R8: {
                reg = extractReg(o, 0);
                value = or(regFile.get(Reg.A), regFile.get(reg));
                setRegFlags(Reg.A, value);
            }
            break;

            case OR_A_N8: {
                value = or(regFile.get(Reg.A), read8AfterOpcode());
                setRegFlags(Reg.A, value);
            }
            break;

            case OR_A_HLR: {
                value = or(regFile.get(Reg.A), read8AtHl());
                setRegFlags(Reg.A, value);
            }
            break;

            case XOR_A_R8: {
                reg = extractReg(o, 0);
                value = xor(regFile.get(Reg.A), regFile.get(reg));
                setRegFlags(Reg.A, value);
            }
            break;

            case XOR_A_N8: {
                value = xor(regFile.get(Reg.A), read8AfterOpcode());
                setRegFlags(Reg.A, value);
            }
            break;

            case XOR_A_HLR: {
                value = xor(regFile.get(Reg.A), read8AtHl());
                setRegFlags(Reg.A, value);
            }
            break;

            case CPL: {
                value = complement8(regFile.get(Reg.A));
                regFile.set(Reg.A, value);
                combineAluFlags(value, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1, FlagSrc.CPU);
            }
            break;

            // Rotate, shift
            case ROTCA: {
                reg = Reg.A;
                value = rotate(rotdir(o), regFile.get(reg));
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case ROTA: {
                reg = Reg.A;
                value = rotate(rotdir(o), regFile.get(reg), test(regFile.get(Reg.F), 4));
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case ROTC_R8: {
                reg = extractReg(o, 0);
                value = rotate(rotdir(o), regFile.get(reg));
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case ROT_R8: {
                reg = extractReg(o, 0);
                value = rotate(rotdir(o), regFile.get(reg), test(regFile.get(Reg.F), 4));
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case ROTC_HLR: {
                value = rotate(rotdir(o), read8AtHl());
                write8AtHl(unpackValue(value));
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case ROT_HLR: {
                value = rotate(rotdir(o), read8AtHl(), test(regFile.get(Reg.F), 4));
                write8AtHl(unpackValue(value));
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case SWAP_R8: {
                reg = extractReg(o, 0);
                value = swap(regFile.get(reg));
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
            }
            break;

            case SWAP_HLR: {
                value = swap(read8AtHl());
                write8AtHl(unpackValue(value));
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0);
            }
            break;

            case SLA_R8: {
                reg = extractReg(o, 0);
                value = shiftLeft(regFile.get(reg));
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case SRA_R8: {
                reg = extractReg(o, 0);
                value = shiftRightA(regFile.get(reg));
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case SRL_R8: {
                reg = extractReg(o, 0);
                value = shiftRightL(regFile.get(reg));
                setRegFromAlu(reg, value);
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case SLA_HLR: {
                value = shiftLeft(read8AtHl());
                write8AtHl(unpackValue(value));
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case SRA_HLR: {
                value = shiftRightA(read8AtHl());
                write8AtHl(unpackValue(value));
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            case SRL_HLR: {
                value = shiftRightL(read8AtHl());
                write8AtHl(unpackValue(value));
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
            }
            break;

            // Bit test and set
            case BIT_U3_R8: {
                int result = testBit(regFile.get(extractReg(o, 0)), extractIndexFromOpcode(o));
                combineAluFlags(result, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);

            }
            break;

            case BIT_U3_HLR: {
                int result = testBit(read8AtHl(), extractIndexFromOpcode(o));
                combineAluFlags(result, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.CPU);
            }
            break;

            case CHG_U3_R8: {

                Reg r = extractReg(o, 0);
                int i = extractIndexFromOpcode(o);

                if (test(o.encoding, 6)) {
                    //TODO : correct this by using Bits.test
                    int result = regFile.get(r) | mask(i);
                    regFile.set(r, result);
                } else {
                    //TODO : same here
                    int result = regFile.get(r) & ~mask(i);

                    regFile.set(r, result);
                }

            }
            break;

            case CHG_U3_HLR: {

                value = read8AtHl();
                index = extractIndexFromOpcode(o);
                if (test(o.encoding, 6)) {

                    value = or(value, 1 << index);
                    write8AtHl(unpackValue(value));
                } else {

                    value = and(value, complement8(1 << index));
                    write8AtHl(unpackValue(value));
                }
            }
            break;

            // Misc. ALU
            case DAA: {
                int FVesult = regFile.get(Reg.F);
                int AValue = regFile.get(Reg.A);
                value = bcdAdjust(AValue, test(FVesult, Flag.N), test(FVesult, Flag.H), test(FVesult, Flag.C));
                setRegFromAlu(Reg.A, value);
                combineAluFlags(value, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU);

            }
            break;

            case SCCF: {

                if (test(o.encoding, 3)) {
                    value = complement8(regFile.get(Reg.F));
                    combineAluFlags(value, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU);
                } else {
                    combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0, FlagSrc.V1);
                }

            }
            break;

            // Jumps
            case JP_HL: {
                nextPC = reg16(Reg16.HL);
            }
            break;

            case JP_N16: {
                nextPC = read16AfterOpcode();

            }
            break;

            case JP_CC_N16: {
                if (isFlagEnabledAfterOpcode(o)) {
                    nbOfCycles += o.additionalCycles;
                    nextPC = read16AfterOpcode();
                }
            }
            break;

            case JR_E8: {
                nextPC += clip(16, signExtend8(read8AfterOpcode()));
                nextPC = clip(16, nextPC);

            }
            break;

            case JR_CC_E8: {
                if (isFlagEnabledAfterOpcode(o)) {
                    nbOfCycles += o.additionalCycles;
                    nextPC += clip(16, signExtend8(read8AfterOpcode()));
                    nextPC = clip(16, nextPC);
                }
            }
            break;

            // Calls and returns
            case CALL_N16: {
                push16(nextPC);
                nextPC = read16AfterOpcode();
            }
            break;

            case CALL_CC_N16: {
                if (isFlagEnabledAfterOpcode(o)) {
                    push16(nextPC);
                    nextPC = read16AfterOpcode();
                    nbOfCycles += o.additionalCycles;
                }
            }
            break;

            case RST_U3: {
                push16(nextPC);
                nextPC = RESETS[extract(o.encoding, 3, 3)];
            }
            break;

            case RET: {
                nextPC = pop16();
            }
            break;

            case RET_CC: {
                if (isFlagEnabledAfterOpcode(o)) {
                    nextPC = pop16();
                    nbOfCycles += o.additionalCycles;
                }
            }
            break;

            // Interrupts
            case EDI: {

                interruptMasterEnable = test(o.encoding, 3);

            }
            break;

            case RETI: {
                interruptMasterEnable = true;
                nextPC = pop16();
            }
            break;

            // Misc control
            case HALT: {
                nextNonIdleCycle = Long.MAX_VALUE;
            }
            break;
            case STOP:
                throw new Error("STOP is not implemented");

        }


        nextNonIdleCycle += nbOfCycles;
        programCounter = nextPC;
        programCounter = clip(16, programCounter);
    }


    /**
     * lit la valeur 8 bits à l'adresse depuis le bus
     *
     * @param address : addresse de la donnée
     * @return valeur 8 bits lue
     * @throws IllegalArgumentException :si address n'est pas une valeur 16 bits
     */
    private int read8(int address) {

        return bus.read(checkBits16(address));
    }


    /**
     * lit la valeur 8 bits à l'adresse contenue dans HL
     *
     * @return valeur 8 bits lue
     */
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }


    /**
     * lit la valeur 8 bits contenue dans l'adresse suivant celle contenue dans programCounter
     *
     * @return valeur 8 bits lue
     */
    private int read8AfterOpcode() {

        return read8(clip(16, programCounter + 1));
    }


    /**
     * lit depuis le bus la valeur 16 bits à l'adresse donnée
     *
     * @return valeur 16 bits
     * @throws IllegalArgumentException : si address n'est pas une valeur 16 bits
     */
    private int read16(int address) {

        int lsb = read8(address);
        int msb = read8(address + 1);

        return make16(msb, lsb);

    }


    /**
     * lit depuis le bus la valeur 16 bits à l'adresse suivant celle contenue dans programCounter
     *
     * @return valeur 16 bits sur le bus
     */
    private int read16AfterOpcode() {
        return read16(programCounter + 1);
    }


    /**
     * Ecrit sur le bus la valeur 8 bits à l'adresse donnée
     *
     * @param address : adresse où stocker la valeur
     * @param v       : valeur 8 bits à écrire
     * @throws IllegalArgumentException : si address n'est pas une valeur 16 bits
     * @throws IllegalArgumentException : si v n'est pas une valeur 8 bits
     */
    private void write8(int address, int v) {

        bus.write(address, v);
    }


    /**
     * Ecrit sur le bus la valeur 16 bits à l'adresse donnée
     *
     * @param address : adresse où stocker la valeur
     * @param v       : valeur 16 bits à écrire
     * @throws IllegalArgumentException : si address n'est pas une valeur 16 bits
     * @throws IllegalArgumentException : si la valeur n'est pas une valeur 16 bits
     */
    private void write16(int address, int v) {
        int lsb = clip(8, v);
        int msb = extract(v, 8, 8);
        write8(address, lsb);
        write8(address + 1, msb);

    }


    /**
     * Ecrit sur le bus à l'adresse contenue dans la pair HL
     *
     * @param v valeur 8 bits à écrire
     * @throws IllegalArgumentException : si v n'est pas une valeur 8 bits
     */
    private void write8AtHl(int v) {

        int address = reg16(Reg16.HL);
        write8(address, v);
    }


    /**
     * Décrémente de 2 l'adresse contenue dans la pair de registres stackPointer puis écrit à la valeur de stackPointer
     *
     * @param v : valeur 16 bits à écrire à la nouvelle adresse
     * @throws IllegalArgumentException : si v n'est pas une valeur 16 bits
     */
    private void push16(int v) {

        stackPointer -= 2;
        stackPointer = clip(16, stackPointer);
        write16(stackPointer, v);
    }


    /**
     * retourne la valeur 16 bits à l'adresse contenue dans la pair de registres stackPointer, puis incrément stackPointer de 2 unités
     *
     * @return valeur à l'adresse contenue dans stackPointer
     */
    private int pop16() {

        int result = read16(stackPointer);
        stackPointer = clip(16, stackPointer + 2);
        return result;

    }


    /**
     * retourne la valeur contenue dans la paire de registres donnée
     *
     * @param r paire de registres donnée
     * @return valeur 16 bits contenue dans la paire de registres
     */
    private int reg16(Reg16 r) {

        int v1 = regFile.get(r.firstReg);
        int v2 = regFile.get(r.secondReg);
        return make16(v1, v2);
    }

    /**
     * retourne la valeur contenue dans la paire de registres donnée, sauf dans le cas où la paire est AF, auquel cas la valeur contenue dans le registre stackPointer est modifiée
     *
     * @param r paire de registres donnée
     * @return valeur 16 bits contenue dans la paire de registres
     */
    private int reg16SP(Reg16 r) {

        if (r == Reg16.AF) {
            return stackPointer;
        } else {
            return reg16(r);
        }

    }


    /**
     * modifie la valeur contenue dans la paire de registres donnée, en faisant attention de mettre à 0 les bits de poids faible si la paire en question est AF
     *
     * @param r    paire de registres donnée
     * @param newV nouvelle valeur
     * @throws IllegalArgumentException : si newV n'est pas une valeur 16 bits
     */
    private void setReg16(Reg16 r, int newV) {

        checkBits16(newV);

        if (r == Reg16.AF) newV = newV >>> 4 << 4;

        int lsb = clip(8, newV);
        int msb = extract(newV, 8, 8);

        regFile.set(r.firstReg, msb);
        regFile.set(r.secondReg, lsb);
    }


    /**
     * fait la même chose que setReg16 sauf dans le cas où la paire passée est AF, auquel cas le registre stackPointer est modifié en lieu et place de la paire AF.
     *
     * @param r    : paire de registres donnée
     * @param newV : nouvelle valeur
     * @throws IllegalArgumentException : si newV n'est pas une valeur 16 bits
     */
    private void setReg16SP(Reg16 r, int newV) {

        checkBits16(newV);

        if (r == Reg16.AF) {
            stackPointer = newV;
        } else {
            setReg16(r, newV);
        }
    }


    /**
     * extrait et retourne l'identité d'un registre 8 bits de l'encodage de l'opcode donné, à partir du bit d'index donné
     *
     * @param opcode   opcode donné
     * @param startBit bit d'index
     * @return identité d'un registre 8 bits de l'encodage de l'opcode
     */
    private Reg extractReg(Opcode opcode, int startBit) {

        int reg = extract(opcode.encoding, startBit, 3);
        return REGISTERS_8[reg];
    }


    /**
     * fait la même chose que extractReg mais pour les paires de registres (ne prend pas le paramètre startBit car il vaut 4 pour toutes les instructions du processeur)
     *
     * @param opcode opcode donné
     * @return identité d'une paire de registres 16 bits de l'encodage de l'opcode
     */
    private Reg16 extractReg16(Opcode opcode) {

        int reg = extract(opcode.encoding, 4, 2);
        return REGISTERS_16[reg];
    }


    /**
     * qui retourne -1 ou +1 en fonction du bit d'index 4, qui est utilisé pour encoder l'incrémentation ou la décrémentation de la paire HL dans différentes instructions. Si le bit vaut 1, retourne -1
     *
     * @param opcode opcode donné
     * @return -1 ou +1 en fonction du bit d'index 4
     */
    private int extractHlIncrement(Opcode opcode) {

        boolean bit = test(opcode.encoding, 4);
        return bit ? -1 : 1;
    }

    /**
     * Incrémente la pair de registres HL du nombre passé en argument
     *
     * @param value valeur de l'incrémentation (ou décrémentation si négatif)
     */
    private void incrementHl(int value) {

        int hlValue = reg16(Reg16.HL);
        hlValue = clip(16, hlValue + value);

        setReg16(Reg16.HL, hlValue);
    }

    /**
     * Extrait la valeur stockée dans vf et la place dans le registre donné
     *
     * @param r  registre à modifier
     * @param vf couple valeur / fanions
     */
    private void setRegFromAlu(Reg r, int vf) {
        regFile.set(r, unpackValue(vf));
    }

    /**
     * Extrait les fanions et les place dans le registre F
     *
     * @param valueFlags couple valeur / fanions
     */
    private void setFlags(int valueFlags) {
        regFile.set(Reg.F, unpackFlags(valueFlags));
    }

    /**
     * Enregistre la valeur de vf dans le registre donné et les fanions dans le registre F
     *
     * @param r  registre à modifier
     * @param vf couple valeur / fanions
     */
    private void setRegFlags(Reg r, int vf) {
        setRegFromAlu(r, vf);
        setFlags(vf);
    }


    /**
     * Met à jour le registre F en fonction des FlagSrc
     *
     * @param vf fanion fourni par l'ALU (Contenu dans vf)
     * @param z  permet de déterminer l'origine du fanion Z
     * @param n  permet de déterminer l'origine du fanion N
     * @param h  permet de déterminer l'origine du fanion H
     * @param c  permet de déterminer l'origine du fanion C
     */
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {

        int newFlag = getFlag(vf, z, 7) + getFlag(vf, n, 6) + getFlag(vf, h, 5) + getFlag(vf, c, 4);
        regFile.set(Reg.F, newFlag);

    }

    /**
     * teste la valeur du fanion et retourne 0, 1, la valeur de l'ALU ou la valeur du CPU
     *
     * @param vf        valeur donnée en argument
     * @param f         fanion à tester
     * @param znhcShift indice de décalage
     * @return 0, 1, la valeur de l'ALU ou la valeur du CPU
     */
    private int getFlag(int vf, FlagSrc f, int znhcShift) {

        switch (f) {
            case V0: {
                return 0;
            }
            case V1: {
                return 1 << znhcShift;
            }
            case ALU: {
                return (test(vf, znhcShift) ? 1 : 0) << znhcShift;
            }
            case CPU: {
                return (test(regFile.get(Reg.F), znhcShift) ? 1 : 0) << znhcShift;
            }
        }

        return Integer.MAX_VALUE;
    }

    /**
     * teste si l'addition ou la soustraction de l'instruction doit être faite avec carry ou non
     *
     * @param o           opcode reçu en argument
     * @param opcodeIndex indice du bit de l'opcode à tester
     * @return true si le registre F contient un carry et si l'opcode d'addition ou de soustraction contient
     * un bit d'instruction carry
     */
    private boolean getCarry(Opcode o, int opcodeIndex) {

        return test(regFile.get(Reg.F), 4) && test(o.encoding, opcodeIndex);
    }


    /**
     * Méthode retournant la direction de la rotation à effectuer, en fonction du bit d'index 3 de l'opcode
     *
     * @param o: opcode reçu
     * @return direction de rotation, de type RotDir
     */
    private RotDir rotdir(Opcode o) {
        if (test(o.encoding, 3)) {
            return RotDir.RIGHT;
        } else {
            return RotDir.LEFT;
        }
    }

    /**
     * Méthode permettant d'extraire un indice d'un opcode, pour les instructions BIT, RES et SET
     *
     * @param o opcode
     * @return indice stocké entre les bits 3 et 5 dans l'opcode
     */
    private int extractIndexFromOpcode(Opcode o) {
        return extract(o.encoding, 3, 3);
    }

    /**
     * met à 1 le fanion correspondant à l'argument passé, ce qui équivaut â lever une interruption si celle-ci est activée
     *
     * @param i élément de l'énumération Interrupt
     */
    public void requestInterrupt(Interrupt i) {

        interruptFlags |= i.mask();

    }

    /**
     * Vérifie la valeur des fanions en fonction de l'opcode pour exécuter les instructions conditionnelles
     *
     * @param condition opcode indiquant quelle condition doie être vraie
     * @return "état" de la condition
     */
    private boolean isFlagEnabledAfterOpcode(Opcode condition) {

        int cc = extract(condition.encoding, 3, 2);
        int FValue = regFile.get(Reg.F);
        switch (cc) {
            case 0:
                return !test(FValue, Flag.Z);

            case 1:
                return test(FValue, Flag.Z);

            case 2:
                return !test(FValue, Flag.C);

            case 3:
                return test(FValue, Flag.C);

            default:
                return false;
        }

    }

}
