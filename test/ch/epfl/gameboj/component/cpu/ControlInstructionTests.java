package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;
import org.testng.annotations.Test;
import static ch.epfl.gameboj.component.cpu.Opcode.*;

public class ControlInstructionTests {

    public static Cpu cpu = new Cpu();
    public static Ram ram = new Ram(0xFFFF);
    public static RamController rc = new RamController(ram, 0, 0xFFFE);
    public static Bus bus = new Bus();

    public static void reset() {

        cpu = new Cpu();
        ram = new Ram(0xFFFF);
        rc = new RamController(ram, 0, 0xFFFE);
        bus = new Bus();

        cpu.attachTo(bus);
        rc.attachTo(bus);
        bus.attach(cpu);
        bus.attach(rc);
    }


    @Test
    public static void testRam() {
        Ram r = new Ram(0xFFFF);
        r.read(65493);

    }


    @Test
    public static void fibTest() {

        reset();

        int[] instructions = new int[] {
                0x31, 0xFF, 0xFF, 0x3E,
                0x0B, 0xCD, 0x0A, 0x00,
                0x76, 0x00, 0xFE, 0x02,
                0xD8, 0xC5, 0x3D, 0x47,
                0xCD, 0x0A, 0x00, 0x4F,
                0x78, 0x3D, 0xCD, 0x0A,
                0x00, 0x81, 0xC1, 0xC9,
        };

        for(int i = 0; i < instructions.length; i++) {

            rc.write(i, instructions[i]);
        }

        int j = 0;

        System.out.println("TESTS FIBONACCI");
        System.out.println("--------------");
        while(cpu._testGetPcSpAFBCDEHL()[0] != 8) {

            cpu.cycle(j);
            j++;

            System.out.println("Etat des registres au cycle " + (j-1) + " : ");

            System.out.println("Registre PC" + " : " + cpu._testGetPcSpAFBCDEHL()[0] );
            System.out.println("Registre SP" + " : " + cpu._testGetPcSpAFBCDEHL()[1] );
            System.out.println("Registre A" + "  : " +  cpu._testGetPcSpAFBCDEHL()[2]);
            System.out.println("Registre F" + "  : " +  cpu._testGetPcSpAFBCDEHL()[3]);
            System.out.println("Registre B" + "  : " +  cpu._testGetPcSpAFBCDEHL()[4]);
            System.out.println("Registre C" + "  : " +  cpu._testGetPcSpAFBCDEHL()[5]);
            System.out.println("Registre D" + "  : " +  cpu._testGetPcSpAFBCDEHL()[6]);
            System.out.println("Registre E" + "  : " +  cpu._testGetPcSpAFBCDEHL()[7]);
            System.out.println("Registre H" + "  : " +  cpu._testGetPcSpAFBCDEHL()[8]);
            System.out.println("Registre L" + "  : " +  cpu._testGetPcSpAFBCDEHL()[9]);
            System.out.println("valeur en ram :" + ram.read(65493));

            System.out.println("--------------------------");
            System.out.println("");
            System.out.println("");

        }

    }




    @Test
    public static void testGameboy() {

        int[] instructions = {LD_SP_N16.encoding, 0xFF, 0xFF, LD_BC_N16.encoding, 0x34, 0x12, LD_DE_N16.encoding, 0x78, 0x56, PUSH_BC.encoding, PUSH_DE.encoding, POP_BC.encoding, POP_DE.encoding, HALT.encoding};

        GameBoy g = new GameBoy(null);
        g.runUntil(50);
        reset();
        ram = new Ram(65536);
        rc = new RamController(ram, 0, 65535);
        g.bus().attach(rc);
        for(int i = 0; i < instructions.length; i++) {

            ram.write(i, instructions[i]);

        }
    }


    @Test
    public static void InterruptTests() {

        reset();

        //ram.write(0, );


    }
}
