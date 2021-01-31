package ch.epfl.gameboj.component.cpu;

import static ch.epfl.gameboj.component.cpu.Opcode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Assembler.Program;


public class ControlInstrutionTestsNew {

    /**public static Cpu cpu = new Cpu();
    public static Ram ram = new Ram(65535);
    public static RamController rc = new RamController(ram, 0, 65534);
    public static Bus bus = new Bus();

    public static void reset() {

        cpu = new Cpu();
        ram = new Ram(65535);
        rc = new RamController(ram, 0, 65534);
        bus = new Bus();

        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.attach(cpu);
        bus.attach(rc);
    }


    @Test
    void JP_N16WorkswithValue() {
        reset();
        Opcode o1 = JP_N16;
        bus.write(2,0b1111_1000);
        bus.write(1, 0b1000_1111);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2)
        assertEquals(0b1111_1000_1000_1111, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void JP_NZ_N16WorkswithZtrue() {
        reset();
        Opcode o1 = JP_NZ_N16;
        bus.write(2,0b1011_1000);
        bus.write(1, 0b1100_1100);
        cpu.setTestFlags(0b1000_0000);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2) if cc true
        assertEquals(3, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void JP_NZ_N16WorkswithZfalse() {
        reset();
        Opcode o1 = JP_NZ_N16;
        bus.write(2,0b1000_1000);
        bus.write(1, 0b1111_1100);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2) if cc true
        assertEquals(0b1000_1000_1111_1100, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void JP_Z_N16WorkswithZtrue() {
        reset();
        Opcode o1 = JP_Z_N16;
        bus.write(2,0b1010_1001);
        bus.write(1, 0b1101_1100);
        cpu.setTestFlags(0b1000_0000);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2) if cc true
        assertEquals(0b1010_1001_1101_1100, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void JP_Z_N16WorkswithZfalse() {
        reset();
        Opcode o1 = JP_Z_N16;
        bus.write(2,0b1010_1001);
        bus.write(1, 0b1101_1100);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2) if cc true
        assertEquals(3, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void JP_NC_N16WorkswithCtrue() {
        reset();
        Opcode o1 = JP_NC_N16;
        bus.write(2,0b1001_1000);
        bus.write(1, 0b1000_1101);
        cpu.setTestFlags(0b0001_0000);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2) if cc true
        assertEquals(3, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void JP_NC_N16WorkswithCfalse() {
        reset();
        Opcode o1 = JP_NC_N16;
        bus.write(2,0b1001_1000);
        bus.write(1, 0b1000_1101);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2) if cc true
        assertEquals(0b1001_1000_1000_1101, cpu._testGetPcSpAFBCDEHL()[0]);
    }


    @Test
    void JP_C_N16WorkswithCtrue() {
        reset();
        Opcode o1 = JP_C_N16;
        bus.write(2,0b1001_0001);
        bus.write(1, 0b1001_1100);
        cpu.setTestFlags(0b0001_0000);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2) if cc true
        assertEquals(0b1001_0001_1001_1100, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void JP_C_N16WorkswithCfalse() {
        reset();
        Opcode o1 = JP_C_N16;
        bus.write(2,0b1001_0001);
        bus.write(1, 0b1001_1100);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o1.encoding); // loads PC with content of bus (adress 1 and 2) if cc true
        assertEquals(3, cpu._testGetPcSpAFBCDEHL()[0]);
    }


    @Test
    void JP_HLWorkswithValues() {
        reset();
        Opcode o1 = JP_HL;
        Opcode o2 = LD_HL_N16;
        bus.write(2,0b1110_1010);
        bus.write(1, 0b1001_1100);
        cpu.dispatch(o2.encoding); // loads HL with values of bus (adress 1 and 2)
        cpu.dispatch(o1.encoding); // sets PC equals to HL
        assertEquals(0b1110_1010_1001_1100, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void JR_E8WorkswithPositiveValues() {
        reset();
        Opcode o1 = JR_E8;
        bus.write(1, 0b0101_1100);
        cpu.dispatch(o1.encoding); // adds e (signed value) to the next instruction adress and stores result in PC
        assertEquals(0b0101_1110, cpu._testGetPcSpAFBCDEHL()[0]);

    }

    @Test
    void JR_E8WorkswithNegativeValues() {
        reset();
        Opcode o1 = JR_E8;
        bus.write(1, 0b1111_1100);
        cpu.dispatch(o1.encoding); // adds e (signed value) to the next instruction adress and stores result in PC
        assertEquals(0b1111_1111_1111_1110, cpu._testGetPcSpAFBCDEHL()[0]);

    }

    @Test
    void JR_NZ_E8WorkswithZtrue() {
        reset();
        Opcode o1 = JR_NZ_E8;
        bus.write(1, 0b010_1100);
        cpu.setTestFlags(0b1000_0000);
        cpu.dispatch(o1.encoding); // adds e (signed value) to the next instruction adress and stores result in PC if condition is true
        assertEquals(2, cpu._testGetPcSpAFBCDEHL()[0]);

    }

    @Test
    void JR_NZ_E8WorkswithZfalse() {
        reset();
        Opcode o1 = JR_NZ_E8;
        bus.write(1, 0b010_1100);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o1.encoding); // adds e (signed value) to the next instruction adress and stores result in PC if condition is true
        assertEquals(0b010_1110, cpu._testGetPcSpAFBCDEHL()[0]);

    }

    @Test
    void JR_NC_E8WorkswithCtrue() {
        reset();
        Opcode o1 = JR_NC_E8;
        bus.write(1, 0b010_1100);
        cpu.setTestFlags(0b0001_0000);
        cpu.dispatch(o1.encoding); // adds e (signed value) to the next instruction adress and stores result in PC if condition is true
        assertEquals(2, cpu._testGetPcSpAFBCDEHL()[0]);

    }

    @Test
    void JR_NC_E8WorkswithCfalse() {
        reset();
        Opcode o1 = JR_NC_E8;
        bus.write(1, 0b010_1100);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o1.encoding); // adds e (signed value) to the next instruction adress and stores result in PC if condition is true
        assertEquals(0b010_1110, cpu._testGetPcSpAFBCDEHL()[0]);

    }

    @Test
    void JR_Z_E8WorkswithZtrue() {
        reset();
        Opcode o1 = JR_Z_E8;
        bus.write(1, 0b010_1010);
        cpu.setTestFlags(0b1000_0000);
        cpu.dispatch(o1.encoding); // adds e (signed value) to the next instruction adress and stores result in PC if condition is true
        assertEquals(0b010_1100, cpu._testGetPcSpAFBCDEHL()[0]);

    }

    @Test
    void JR_Z_E8WorkswithZfalse() {
        reset();
        Opcode o1 = JR_Z_E8;
        bus.write(1, 0b010_1010);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o1.encoding); // adds e (signed value) to the next instruction adress and stores result in PC if condition is true
        assertEquals(2, cpu._testGetPcSpAFBCDEHL()[0]);

    }

    @Test
    void CALL_N16Works() {
        reset();
        Opcode o1 = CALL_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFD, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP decremented by 2
        assertEquals(6, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction saved at address SP
        assertEquals(0b0000_1110_0000_0010, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void CALL_NZ_N16WorkswithZtrue() {
        reset();
        Opcode o1 = CALL_NZ_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.setTestFlags(0b1000_0000);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFF, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP NOT decremented by 2
        assertEquals(0, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction NOT saved at address SP
        assertEquals(6, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void CALL_NZ_N16WorkswithZfalse() {
        reset();
        Opcode o1 = CALL_NZ_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFD, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP decremented by 2
        assertEquals(6, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction saved at address SP
        assertEquals(0b0000_1110_0000_0010, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void CALL_NC_N16WorkswithCtrue() {
        reset();
        Opcode o1 = CALL_NC_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.setTestFlags(0b0001_0000);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFF, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP NOT decremented by 2
        assertEquals(0, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction NOT saved at address SP
        assertEquals(6, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void CALL_NC_N16WorkswithCfalse() {
        reset();
        Opcode o1 = CALL_NC_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFD, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP decremented by 2
        assertEquals(6, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction saved at address SP
        assertEquals(0b0000_1110_0000_0010, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void CALL_Z_N16WorkswithZtrue() {
        reset();
        Opcode o1 = CALL_Z_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.setTestFlags(0b1000_0000);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFD, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP decremented by 2
        assertEquals(6, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction saved at address SP
        assertEquals(0b0000_1110_0000_0010, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void CALL_Z_N16WorkswithZfalse() {
        reset();
        Opcode o1 = CALL_Z_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFF, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP NOT decremented by 2
        assertEquals(0, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction NOT saved at address SP
        assertEquals(6, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void CALL_C_N16WorkswithCtrue() {
        reset();
        Opcode o1 = CALL_C_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.setTestFlags(0b0001_0000);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFD, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP decremented by 2
        assertEquals(6, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction saved at address SP
        assertEquals(0b0000_1110_0000_0010, cpu._testGetPcSpAFBCDEHL()[0]);
    }

    @Test
    void CALL_C_N16WorkswithCfalse() {
        reset();
        Opcode o1 = CALL_C_N16;
        Opcode o2 = LD_SP_N16;
        bus.write(2,0xFF);
        bus.write(1, 0xFF);
        bus.write(5,0b0000_1110);
        bus.write(4, 0b0000_0010);
        cpu.setTestFlags(0b0000_0000);
        cpu.dispatch(o2.encoding); // loads SP with FFFF
        cpu.dispatch(o1.encoding); // push next instruction to stack pile, stores nn into program counter
        assertEquals(0xFFFF, cpu._testGetPcSpAFBCDEHL()[1]); // ok: SP NOT decremented by 2
        assertEquals(0, bus.read(cpu._testGetPcSpAFBCDEHL()[1])); // ok, adress of next instruction NOT saved at address SP
        assertEquals(6, cpu._testGetPcSpAFBCDEHL()[0]);
    }
    */

}
