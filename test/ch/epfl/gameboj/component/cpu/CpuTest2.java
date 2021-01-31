package ch.epfl.gameboj.component.cpu;
/**
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static ch.epfl.gameboj.component.cpu.Opcode.*;
import static ch.epfl.gameboj.component.cpu.Opcode.ADC_A_A;
import static ch.epfl.gameboj.component.cpu.Opcode.ADD_A_B;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class CpuTest2 {

    private Cpu cpu = new Cpu();
    private Ram r = new Ram(1024);
    private RamController rm = new RamController(r, 0, 1023);
    private Bus b = new Bus();




    @Test
    void addAR8WorkswithZero() throws IOException {
        Opcode[] os = new Opcode[] {
                ADD_A_B,
                ADD_A_C,
                ADD_A_D,
                ADD_A_E,
                ADD_A_H,
                ADD_A_L,
                ADD_A_A,
                ADC_A_B,
                ADC_A_C,
                ADC_A_D,
                ADC_A_E,
                ADC_A_H,
                ADC_A_L,
                ADC_A_A,
        };
        Cpu cpu = new Cpu();

        for (int i=0; i<os.length; i++) {
            cpu.dispatch(os[i].encoding);
            assertEquals(0, cpu._testGetPcSpAFBCDEHL()[2]);
        }
    }

    @Test
    void addAR8WorkswithValue() throws IOException {
        Opcode o1 = ADD_A_B;
        Opcode o2 = LD_B_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,152);

        cpu.dispatch(o2.encoding); // loads B with content of bus
        cpu.dispatch(o1.encoding); // adds B to A

        assertEquals(152, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void addAR8WorkswithValueandCarry() throws IOException {
        Opcode o1 = ADC_A_C;
        Opcode o2 = LD_C_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0, 203);

        cpu.setInnerFlags(0b00010000); // sets C flag to 1
        cpu.dispatch(o2.encoding); // loads C with content of bus
        cpu.dispatch(o1.encoding); // adds C to A

        assertEquals(204, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void addAR8WorkswithOverflow() throws IOException {
        Opcode o1 = ADC_A_D;
        Opcode o2 = LD_D_HLR;
        Opcode o3 = LD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0, 0xFF);

        cpu.dispatch(o3.encoding); // loads A with content of bus
        cpu.dispatch(o2.encoding); // loads D with content of bus
        cpu.dispatch(o1.encoding); // adds D to A

        assertEquals(254, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0011_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void addAN8WorkswithValue() throws IOException {
        Opcode o1 = ADD_A_N8;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(1,152);

        cpu.dispatch(o1.encoding); // adds value after opcode to A

        assertEquals(152, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    @Test
    void addAN8WorkswithValueandCarry() throws IOException {
        Opcode o1 = ADC_A_N8;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(1,152);

        cpu.setInnerFlags(0b00010000); // sets C flag to 1
        cpu.dispatch(o1.encoding); // adds value after opcode to A

        assertEquals(153, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void addAN8WorkswithOverflow() throws IOException {
        Opcode o1 = ADD_A_N8;
        Opcode o3 = LD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,0xFF);
        bus.write(2, 0xFF);

        cpu.dispatch(o3.encoding); // loads A with content of bus (address 0)
        cpu.dispatch(o1.encoding); // adds value after opcode to A

        assertEquals(254, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0011_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }



    @Test
    void addAHlrWorkswithValue() throws IOException {
        Opcode o1 = ADD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,152);

        cpu.dispatch(o1.encoding); // adds value from bus to A

        assertEquals(152, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    @Test
    void addAHlrWorkswithValueandCarry() throws IOException {
        Opcode o1 = ADC_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,152);

        cpu.setInnerFlags(0b00010000); // sets C flag to 1
        cpu.dispatch(o1.encoding); // adds value from bus to A

        assertEquals(153, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void addAHlrWorkswithOverflow() throws IOException {
        Opcode o1 = ADC_A_HLR;
        Opcode o3 = LD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,0xFF);

        cpu.dispatch(o3.encoding); // loads A with content of bus (address 0)
        cpu.dispatch(o1.encoding); // adds value from bus to A

        assertEquals(254, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0011_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void incR8WorkswithValue() throws IOException {
        Opcode o1 = INC_D;
        Opcode o3 = LD_D_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,152);

        cpu.dispatch(o3.encoding); // loads D with content of bus (address 0)
        cpu.dispatch(o1.encoding); // increments D by 1

        assertEquals(153, cpu._testGetPcSpAFBCDEHL()[6]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

        cpu.setInnerFlags(0b00010000); // sets C flag to 1
        cpu.dispatch(o1.encoding); // increments D by 1
        assertEquals(154, cpu._testGetPcSpAFBCDEHL()[6]);
        assertEquals(0b0001_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    @Test
    void incR8WorkswithOverflow() throws IOException {
        Opcode o1 = INC_D;
        Opcode o3 = LD_D_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,0xFF);

        cpu.dispatch(o3.encoding); // loads D with content of bus (address 0)
        cpu.dispatch(o1.encoding); // increments D by 1

        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[6]);
        assertEquals(0b1010_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void incHlrWorkswithValue() throws IOException {
        Opcode o1 = INC_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0, 152);

        cpu.dispatch(o1.encoding); // increments data at adress HL by 1

        assertEquals(153, bus.read(0));
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

        cpu.setInnerFlags(0b00010000); // sets C flag to 1
        cpu.dispatch(o1.encoding); // increments data at adress HL by 1
        assertEquals(154, bus.read(0));
        assertEquals(0b0001_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }



    @Test
    void incR16SPWorkswithValue() throws IOException {
        Opcode o1 = INC_BC;
        Opcode o3 = LD_BC_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2,0b1001_1000);
        bus.write(1, 0b0111_1001);
        // TODO: to check: most significant are stored in B, least significant in C? (bus reads them in reverse order from Ram Controller)
        cpu.dispatch(o3.encoding); // loads BC with content of bus (address 1 and 2)
        cpu.dispatch(o1.encoding); // increments BC by 1

        assertEquals(0b1001_1000, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(0b0111_1010, cpu._testGetPcSpAFBCDEHL()[5]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

        cpu.setInnerFlags(0b11110000); // sets all flags to 1
        cpu.dispatch(o1.encoding); // increments BC by 1
        assertEquals(0b1001_1000, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(0b0111_1011, cpu._testGetPcSpAFBCDEHL()[5]);
        assertEquals(0b1111_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    @Test
    void incR16SPWorkswithOverflow() throws IOException {
        Opcode o1 = INC_BC;
        Opcode o3 = LD_BC_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2,0xFF);
        bus.write(1, 0xFF);

        cpu.dispatch(o3.encoding); // loads BC with content of bus (address 1 and 2)
        cpu.dispatch(o1.encoding); // increments BC by 1


        assertEquals(0x00, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(0x00, cpu._testGetPcSpAFBCDEHL()[5]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

        cpu.setInnerFlags(0b11110000); // sets all flags to 1
        cpu.dispatch(o1.encoding); // increments BC by 1
        assertEquals(0x00, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(0x01, cpu._testGetPcSpAFBCDEHL()[5]);
        assertEquals(0b1111_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    @Test
    void incR16SPWorkswithValueSP() throws IOException {
        Opcode o1 = INC_SP;
        Opcode o3 = LD_SP_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2,0b1001_1000);
        bus.write(1, 0b0111_1001);
        cpu.dispatch(o3.encoding); // loads SP with content of bus (address 1 and 2)
        cpu.dispatch(o1.encoding); // increments SP by 1

        assertEquals(0b1001_1000_0111_1010, cpu._testGetPcSpAFBCDEHL()[1]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

        cpu.setInnerFlags(0b11110000); // sets all flags to 1
        cpu.dispatch(o1.encoding); // increments SP by 1
        assertEquals(0b1001_1000_0111_1011, cpu._testGetPcSpAFBCDEHL()[1]);
        assertEquals(0b1111_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    @Test
    void addHLr16SpWorkswithValue() throws IOException {
        Opcode o1 = ADD_HL_BC;
        Opcode o2 = LD_BC_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2,0b1001_1000);
        bus.write(1, 0b0111_1001);

        cpu.dispatch(o2.encoding); // loads BC with content of bus
        cpu.dispatch(o1.encoding); // adds BC to content of HL
        assertEquals(0b1001_1000, cpu._testGetPcSpAFBCDEHL()[8]);
        assertEquals(0b0111_1001, cpu._testGetPcSpAFBCDEHL()[9]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void addHLr16SpWorkswithOverflow() throws IOException {
        Opcode o1 = ADD_HL_BC;
        Opcode o2 = LD_BC_N16;
        Opcode o3 = LD_HL_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(5,0x00);
        bus.write(4,0x01);
        bus.write(2,0xFF);
        bus.write(1, 0xFF);

        cpu.dispatch(o2.encoding); // loads BC with content of bus
        cpu.dispatch(o3.encoding); // loads HL with content of bus
        cpu.dispatch(o1.encoding); // adds BC to content of HL

        assertEquals(0x00, cpu._testGetPcSpAFBCDEHL()[8]);
        assertEquals(0x00, cpu._testGetPcSpAFBCDEHL()[9]);
        assertEquals(0b0011_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void addHLr16SpWorkswithValueSP() throws IOException {
        Opcode o1 = ADD_HL_SP;
        Opcode o2 = LD_SP_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2, 0b1001_1000);
        bus.write(1, 0b0111_1001);

        cpu.dispatch(o2.encoding); // loads SP with content of bus

        cpu.dispatch(o1.encoding); // adds SP to content of HL
        assertEquals(0b1001_1000_0111_1001, cpu._testGetPcSpAFBCDEHL()[1]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    @Test
    void addHLr16SpWorkswithValueandCarry() throws IOException {
        Opcode o1 = ADD_HL_BC;
        Opcode o2 = LD_BC_N16;
        Opcode o3 = LD_HL_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(5,0b1001_1001);
        bus.write(4,0b0000_0000);
        bus.write(2,0b1000_1001);
        bus.write(1, 0b0000_0000);

        cpu.setInnerFlags(0b10000000); // sets Z flag to 1
        cpu.dispatch(o2.encoding); // loads BC with content of bus
        cpu.dispatch(o3.encoding); // loads HL with content of bus
        cpu.dispatch(o1.encoding); // adds BC to content of HL

        assertEquals(0b0010_0010, cpu._testGetPcSpAFBCDEHL()[8]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[9]);
        assertEquals(0b1011_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void ldHlSpS8WorkswithpositiveValueSP() throws IOException {
        Opcode o1 = ADD_SP_N;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(1, 0b0111_1111);

        cpu.dispatch(o1.encoding); // adds value after opcode to SP
        assertEquals(0b0111_1111, cpu._testGetPcSpAFBCDEHL()[1]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void ldHlSpS8WorkswithnegativeValueSP() throws IOException {
        Opcode o1 = ADD_SP_N;
        Opcode o2 = LD_SP_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2, 0b0000_0000);
        bus.write(1, 0b0110_0101);
        bus.write(4, 0b1111_1111);

        cpu.dispatch(o2.encoding); // loads SP with value from bus
        cpu.dispatch(o1.encoding); // adds value after opcode to SP
        assertEquals(0b0000_0000_0110_0100, cpu._testGetPcSpAFBCDEHL()[1]);
        assertEquals(0b0011_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void ldHlSpS8WorkswithCarrySP() throws IOException {
        Opcode o1 = ADD_SP_N;
        Opcode o2 = LD_SP_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2, 0b0000_0000);
        bus.write(1, 0b1110_0101);
        bus.write(4, 0b0100_1100);

        cpu.dispatch(o2.encoding); // loads SP with value from bus
        cpu.dispatch(o1.encoding); // adds value after opcode to SP
        assertEquals(0b0000_0001_0011_0001, cpu._testGetPcSpAFBCDEHL()[1]);
        assertEquals(0b0011_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void ldHlSpS8WorkswithpositiveValue() throws IOException {
        Opcode o1 = LD_HL_SP_N8;
        Opcode o2 = LD_SP_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2, 0b0000_0001);
        bus.write(1, 0b1110_0101);
        bus.write(4, 0b000_0001);

        cpu.dispatch(o2.encoding); // loads SP with value from bus
        cpu.dispatch(o1.encoding); // adds value after opcode to SP and stores result in HL
        assertEquals(0b0000_0001, cpu._testGetPcSpAFBCDEHL()[8]);
        assertEquals(0b1110_0110, cpu._testGetPcSpAFBCDEHL()[9]);
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    @Test
    void ldHlSpS8WorkswithnegativeValue() throws IOException {
        Opcode o1 = LD_HL_SP_N8;
        Opcode o2 = LD_SP_N16;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(2, 0b0000_0000);
        bus.write(1, 0b1110_0101);
        bus.write(4, 0b1111_1111);

        cpu.dispatch(o2.encoding); // loads SP with value from bus
        cpu.dispatch(o1.encoding); // adds value after opcode to SP and stores result in HL
        assertEquals(0b0000_0000, cpu._testGetPcSpAFBCDEHL()[8]);
        assertEquals(0b1110_0100, cpu._testGetPcSpAFBCDEHL()[9]);
        assertEquals(0b0011_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }










    @Test
    void SUB_A_R8WorksWithZeros() {

        Opcode[] os = new Opcode[] {
                SUB_A_B,
                SUB_A_C,
                SUB_A_D,
                SUB_A_E,
                SUB_A_H,
                SUB_A_L,
                SUB_A_A,
                SBC_A_B,
                SBC_A_C,
                SBC_A_D,
                SBC_A_E,
                SBC_A_H,
                SBC_A_L,
                SBC_A_A,
        };
        Cpu cpu = new Cpu();

        for (int i=0; i<os.length; i++) {
            cpu.dispatch(os[i].encoding);
            assertEquals(0, cpu._testGetPcSpAFBCDEHL()[2]);
        }

    }

    @Test
    void SubAR8WorksWithValues() {

        Opcode o1 = SUB_A_B;
        Opcode o2 = LD_B_HLR;
        Opcode o3 = LD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,152);

        cpu.dispatch(o3.encoding); // loads B with content of bus
        bus.write(0, 48);
        cpu.dispatch(o2.encoding); // loads A with content of bus
        cpu.dispatch(o1.encoding); // sub B to A

        assertEquals(104, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0100_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    @Test
    void subAR8WorksWithValuesAndCarry() {

        Opcode o1 = SBC_A_C;
        Opcode o2 = LD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);

        bus.write(0, 203);
        cpu.setInnerFlags(0b001_0000); // sets C flag to 1

        cpu.dispatch(o2.encoding); // loads C with content of bus
        cpu.dispatch(o1.encoding); // sub C to A

        assertEquals(202, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0100_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }

    @Test
    void subAR8WorksWithUnderFlow() {

        Opcode o1 = SUB_A_N8;
        Opcode o3 = LD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1000);
        RamController rc = new RamController(ram, 0, 999);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);

        bus.write(0,0x1);
        cpu.dispatch(o3.encoding); // loads A with content of bus (address 0)

        bus.write(o3.totalBytes + 1, 0x2);
        cpu.dispatch(o1.encoding); // sub value after opcode to A

        assertEquals(0b1111_1111, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0111_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }




    //TESTS SUB_A_HLR

    @Test
    void SUB_A_HLRWorksWithZeroValues() {

        Opcode[] os = new Opcode[] {
                SUB_A_HLR,
                SBC_A_HLR
        };
        Cpu cpu = new Cpu();
        Bus b = new Bus();
        Ram r = new Ram(1024);
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        cpu.attachTo(b);

        for (int i=0; i<os.length; i++) {
            cpu.dispatch(os[i].encoding);
            assertEquals(0, cpu._testGetPcSpAFBCDEHL()[2]);
        }

    }


    @Test
    void SUB_A_HLRWorksWithKnownValues() {

        Opcode o1 = SBC_A_HLR;
        Opcode o2 = LD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1024);
        RamController rc = new RamController(ram, 0, 1023);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);
        bus.write(0,175);


        cpu.dispatch(o2.encoding); // loads A with content of bus
        bus.write(0xA, 137);
        cpu.setInnerHLValue(0xA);
        cpu.dispatch(o1.encoding); // sub value at address HL to A

        assertEquals(38, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0100_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }

    @Test
    void SUB_A_HLRWorksWithCarry() {

        Opcode o1 = SBC_A_HLR;
        Opcode o2 = LD_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1024);
        RamController rc = new RamController(ram, 0, 1023);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);

        bus.write(0, 200);
        cpu.dispatch(o2.encoding);
        cpu.setInnerFlags(0b1111_0000);
        bus.write(0, 0);
        cpu.dispatch(o1.encoding);


        assertEquals(199, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0100_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    @Test
    void SUB_A_HLRWorksWithUnderFlow() {

        Opcode o1 = SBC_A_HLR;
        Cpu cpu = new Cpu();
        Ram ram = new Ram(1024);
        RamController rc = new RamController(ram, 0, 1023);
        Bus bus = new Bus();
        bus.attach(rc);
        cpu.attachTo(bus);

        bus.write(0, 1);
        cpu.dispatch(o1.encoding);


        assertEquals(0b1111_1111, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0111_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    // TESTS DEC_R8


    @Test
    void DEC_R8WorksWithZeroValues() {

        Opcode[] os = new Opcode[] {

                DEC_A,
                DEC_B,
                DEC_C,
                DEC_D,
                DEC_E,
                DEC_H,
                DEC_L

        };
        Cpu cpu = new Cpu();

        for (int i=0; i<os.length; i++) {
            cpu.dispatch(os[i].encoding);
            assertEquals(0xFF, cpu._testGetPcSpAFBCDEHL()[2]);
            assertEquals(0b110_0000, cpu._testGetPcSpAFBCDEHL()[3]);
        }

        for (int i=0; i<os.length; i++) {
            cpu.dispatch(os[i].encoding);
            assertEquals(0xFE, cpu._testGetPcSpAFBCDEHL()[2]);
            assertEquals(0b100_0000, cpu._testGetPcSpAFBCDEHL()[3]);
        }

        for (int i=0; i<os.length; i++) {
            cpu.dispatch(os[i].encoding);
            assertEquals(0xFD, cpu._testGetPcSpAFBCDEHL()[2]);
            assertEquals(0b100_0000, cpu._testGetPcSpAFBCDEHL()[3]);
        }

    }

    // TEST DEC_HLR

    @Test
    void testDEC_HLR() {

        Opcode o = DEC_HLR;

        Cpu c = new Cpu();
        Ram r = new Ram(1024);
        RamController rm = new RamController(r, 0, 1023);
        Bus b = new Bus();
        c.attachTo(b);
        rm.attachTo(b);
        b.attach(rm);

        c.setInnerHLValue(0xAA);
        c.dispatch(o.encoding);


        assertEquals(0xFF, rm.read(0xAA));

        c.setInnerHLValue(0xAA);
        rm.write(0xAA, 1);
        c.dispatch(o.encoding);

        assertEquals(0, rm.read(0xAA));

    }

    // TEST CP_A_R8

    @Test
    void CP_A_R8OnSameValuedRegisters() {

        Opcode o = CP_A_B;
        Opcode o1 = LD_A_HLR;
        Opcode o2 = LD_B_HLR;

        Cpu c = new Cpu();
        Ram r = new Ram(1024);
        RamController rm = new RamController(r, 0, 1023);
        Bus b = new Bus();
        c.attachTo(b);
        rm.attachTo(b);
        b.attach(rm);

        rm.write(0, 0xFF);
        c.dispatch(o1.encoding);
        c.dispatch(o2.encoding);
        c.dispatch(o.encoding);

        assertEquals(0b1100_0000, c._testGetPcSpAFBCDEHL()[3]);


    }

    @Test
    void CP_A_R8WorksWithUnderFlow() {

        Opcode o = CP_A_B;
        Opcode o1 = LD_A_HLR;
        Opcode o2 = LD_B_HLR;

        Cpu c = new Cpu();
        Ram r = new Ram(1024);
        RamController rm = new RamController(r, 0, 1023);
        Bus b = new Bus();
        c.attachTo(b);
        rm.attachTo(b);
        b.attach(rm);


        c.dispatch(o1.encoding);
        rm.write(0, 1);
        c.dispatch(o2.encoding);
        c.dispatch(o.encoding);

        assertEquals(0b0111_0000, c._testGetPcSpAFBCDEHL()[3]);
    }



    //Test CP_A_N8


    @Test
    void CP_A_N8WorksOnSameValues() {

        Opcode o1 = CP_A_N8;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(o2.totalBytes + 1, 0xFF);
        cpu.setInnerHLValue(0xAA);
        rm.write(0xAA, 0xFF);
        cpu.dispatch(o2.encoding);

        cpu.dispatch(o1.encoding);

        assertEquals(0b1100_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }

    @Test
    void CP_A_N8WorksWithUnderFlow() {

        Opcode o1 = CP_A_N8;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(o2.totalBytes + 1, 1); //Data to be loaded (n8)
        cpu.setInnerHLValue(0xAA);
        rm.write(0xAA, 0);
        cpu.dispatch(o2.encoding);

        cpu.dispatch(o1.encoding);

        assertEquals(0b0111_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    // TESTS CP_A_HLR

    @Test
    void testCP_A_HLROnSameValues() {

        Opcode o1 = CP_A_HLR;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0xAA, 0xFF);
        cpu.setInnerHLValue(0xAA);
        cpu.dispatch(o2.encoding);

        assertEquals(0xFF, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0xFF, rm.read(0xAA));
        cpu.dispatch(o1.encoding);

        assertEquals(0b1100_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }

    @Test
    void CP_A_HLRWorksWithUnderFlow() {

        Opcode o1 = CP_A_HLR;


        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);


        rm.write(0, 1);

        cpu.dispatch(o1.encoding);


        assertEquals(0b0111_0000, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    @Test
    void DECR16SPTestWithZeroValues() {

        Opcode o = DEC_BC;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        cpu.dispatch(o.encoding);
        assertEquals(0xFF, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(0xFF, cpu._testGetPcSpAFBCDEHL()[5]);

        cpu.dispatch(o.encoding);
        assertEquals(0xFF, cpu._testGetPcSpAFBCDEHL()[4]);
        assertEquals(0xFE, cpu._testGetPcSpAFBCDEHL()[5]);

        o = DEC_SP;

        cpu.dispatch(o.encoding);
        assertEquals(0xFFFF, cpu._testGetPcSpAFBCDEHL()[1]);

        cpu.dispatch(o.encoding);
        assertEquals(0xFFFE, cpu._testGetPcSpAFBCDEHL()[1]);

        cpu.dispatch(o.encoding);
        assertEquals(0xFFFD, cpu._testGetPcSpAFBCDEHL()[1]);

        cpu.dispatch(o.encoding);
        assertEquals(0xFFFC, cpu._testGetPcSpAFBCDEHL()[1]);

        cpu.dispatch(o.encoding);
        assertEquals(0xFFFB, cpu._testGetPcSpAFBCDEHL()[1]);

        cpu.dispatch(o.encoding);
        assertEquals(0xFFFA, cpu._testGetPcSpAFBCDEHL()[1]);

        cpu.dispatch(o.encoding);
        assertEquals(0xFFF9, cpu._testGetPcSpAFBCDEHL()[1]);

        cpu.dispatch(o.encoding);
        assertEquals(0xFFF8, cpu._testGetPcSpAFBCDEHL()[1]);
    }


    //Test AND_A_N8

    @Test
    void AND_A_N8WorksWithFull1s() {

        Opcode o1 = AND_A_N8;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0xFF);
        cpu.dispatch(o2.encoding);


        rm.write(o2.totalBytes + 1, 0xFF);
        cpu.dispatch(o1.encoding);
        assertEquals(0xFF, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x20, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    @Test
    void ADD_A_N8WorksWithFullZeros() {

        Opcode o1 = AND_A_N8;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);



        rm.write( 1, 0);
        cpu.dispatch(o1.encoding);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0xA0, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    @Test
    void ADD_A_N8WorksWithKnowValues() {

        Opcode o1 = AND_A_N8;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0xbf);
        rm.write( o2.totalBytes + 1, 0xa6);
        cpu.dispatch(o2.encoding);

        cpu.dispatch(o1.encoding);
        assertEquals(0xa6, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x20, cpu._testGetPcSpAFBCDEHL()[3]);
    }



    //TEST AND_A_R8

    @Test
    void testAND_A_N8OnKnownValues() {


        Opcode o1 = AND_A_B;
        Opcode o2 = LD_A_HLR;
        Opcode o3 = LD_B_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0b1011_1111);
        cpu.dispatch(o2.encoding);

        rm.write(0, 0b1010_0110);
        cpu.dispatch(o3.encoding);

        cpu.dispatch(o1.encoding);
        assertEquals(0xa6, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x20, cpu._testGetPcSpAFBCDEHL()[3]);


    }


    // TESTS AND A HLR

    @Test
    void testAND_A_HLROnKnownValues() {

        Opcode o1 = AND_A_HLR;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);


        rm.write(0, 0b1011_1111);
        cpu.dispatch(o2.encoding);

        rm.write(0, 0b1010_0110);

        cpu.dispatch(o1.encoding);
        assertEquals(0xa6, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x20, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    //TEST OR_A_N8


    @Test
    void TestOR_A_N8OnFullZeros() {

        Opcode o1 = OR_A_N8;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        cpu.dispatch(o1.encoding);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x80, cpu._testGetPcSpAFBCDEHL()[3]);
    }

    @Test
    void TestOR_A_N8OnFullOnes() {

        Opcode o1 = OR_A_N8;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0xFF);
        cpu.dispatch(o2.encoding);

        rm.write(o2.totalBytes + 1, 0xFF);

        cpu.dispatch(o1.encoding);
        assertEquals(0xFF, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    @Test
    void TestOR_A_N8WorksWithKnownValues() {

        Opcode o1 = OR_A_N8;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0b1000_0100);
        cpu.dispatch(o2.encoding);

        rm.write(o2.totalBytes + 1, 0b0101_1100);

        cpu.dispatch(o1.encoding);
        assertEquals(0b1101_1100, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[3]);

    }

    //Test OR_A_R8

    @Test
    void TestOR_A_R8Works() {

        Opcode o1 = OR_A_B;
        Opcode o2 = LD_A_HLR;
        Opcode o3 = LD_B_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0b0110_1001);
        cpu.dispatch(o2.encoding);

        rm.write(0, 0b1000_0010);
        cpu.dispatch(o3.encoding);

        cpu.dispatch(o1.encoding);
        assertEquals(0b1110_1011, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[3]);
    }



    //Test OR A HLR

    @Test
    void OR_A_HLRWorksOnKnownValues() {

        Opcode o1 = OR_A_HLR;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0b0110_1001);
        cpu.dispatch(o2.encoding);

        rm.write(0, 0b1000_0010);
        cpu.dispatch(o1.encoding);

        assertEquals(0b1110_1011, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    //Tests XOR A N8

    @Test
    void xorAN8WorksWithFull0s() {

        Opcode o1 = XOR_A_N8;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);


        cpu.dispatch(o1.encoding);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x80, cpu._testGetPcSpAFBCDEHL()[3]);


    }

    @Test
    void xorAN8WorksWitFull1s() {

        Opcode o1 = XOR_A_N8;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0xFF);
        cpu.dispatch(o2.encoding);

        rm.write(o2.totalBytes + 1, 0xFF);

        cpu.dispatch(o1.encoding);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0x80, cpu._testGetPcSpAFBCDEHL()[3]);


    }


    @Test
    void xorAN8WorksWithKnownValues() {

        Opcode o1 = XOR_A_N8;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0b10111001);
        cpu.dispatch(o2.encoding);

        rm.write(o2.totalBytes + 1, 0b11001101);

        cpu.dispatch(o1.encoding);
        assertEquals(0b01110100, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    @Test
    void TestxorAR8WithKnownValues() {

        Opcode o1 = XOR_A_B;
        Opcode o2 = LD_A_HLR;
        Opcode o3 = LD_B_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0b1111_0000);
        cpu.dispatch(o2.encoding);

        rm.write(0, 0b1001_1001);
        cpu.dispatch(o3.encoding);

        cpu.dispatch(o1.encoding);
        assertEquals(0b0110_1001, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    @Test
    void XOR_A_HLWorksWithKnownValue() {

        Opcode o1 = XOR_A_HLR;
        Opcode o2 = LD_A_HLR;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        rm.write(0, 0b1111_0000);
        cpu.dispatch(o2.encoding);

        rm.write(0, 0b1001_1001);

        cpu.dispatch(o1.encoding);
        assertEquals(0b0110_1001, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[3]);
    }


    // TESTS COMPLEMENT CPL

    @Test
    void CPLWorksWIthFullZeros() {

        Opcode o1 = CPL;

        Cpu cpu = new Cpu();
        Ram r = new Ram(1024);
        Bus b = new Bus();
        RamController rm = new RamController(r, 0, 1023);
        b.attach(rm);
        rm.attachTo(b);
        cpu.attachTo(b);

        cpu.dispatch(o1.encoding);

        assertEquals(0xFF, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b0110_0000, cpu._testGetPcSpAFBCDEHL()[3]);

        cpu.setInnerFlags(0b1111_0000);
        assertEquals(0b1111_0000, cpu._testGetPcSpAFBCDEHL()[3]);

        cpu.dispatch(o1.encoding);

        assertEquals(0, cpu._testGetPcSpAFBCDEHL()[2]);
        assertEquals(0b1111_0000, cpu._testGetPcSpAFBCDEHL()[3]);

    }


    //RLCA TESTS


    @Test
    void RLCAWorksWithZeros() {

    }


    //SWAP TESTS

    @Test
    void SwapWorksWithZeros() {


    }


    private void resetCpuRamAndCo() {

        cpu = new Cpu();
        r = new Ram(1024);
        rm = new RamController(r, 0, 1023);
        b = new Bus();

        b.attach(rm);
        cpu.attachTo(b);

    }

    private void IPOIRA(Opcode o, int address) {

        rm.write(address, 0xCB);
        rm.write(address + 1, o.encoding);
    }



}
*/