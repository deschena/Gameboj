package ch.epfl.gameboj.component.cpu;

public class InnerCPUTests {

    /**
     * EVERYTHING IS IN COMMENT IN ORDER TO COMPILE PROPERLY
     *
     *  @Test
    public void testsSetRegsAndFlags(){

    //Set Regs and Flags tests
    int result = 0b1010_1111_1001_0000;
    setRegFromAlu(Reg.A, result);
    assertEquals(0b1010_1111, registerFile_8.get(Reg.A));
    setRegFromAlu(Reg.B, result);
    assertEquals(0b1010_1111, registerFile_8.get(Reg.B));
    setRegFromAlu(Reg.C, result);
    assertEquals(0b1010_1111, registerFile_8.get(Reg.C));
    setRegFromAlu(Reg.D, result);
    assertEquals(0b1010_1111, registerFile_8.get(Reg.D));
    setRegFromAlu(Reg.E, result);
    assertEquals(0b1010_1111, registerFile_8.get(Reg.E));
    setRegFromAlu(Reg.F, result);
    assertEquals(0b1010_1111, registerFile_8.get(Reg.F));
    setRegFromAlu(Reg.H, result);
    assertEquals(0b1010_1111, registerFile_8.get(Reg.H));
    setRegFromAlu(Reg.L, result);
    assertEquals(0b1010_1111, registerFile_8.get(Reg.L));
    setFlags(result);
    assertEquals(0b1001_0000, registerFile_8.get(Reg.F));

    resetRegisters();

    //Set regs and flag simultaneously

    result = 0b1000_0100_1011_0000;

    setRegFlags(Reg.A, result);
    assertEquals(0b1000_0100, registerFile_8.get(Reg.A));
    assertEquals(0b1011_0000, registerFile_8.get(Reg.F));

    setRegFlags(Reg.B, result);
    assertEquals(0b1000_0100, registerFile_8.get(Reg.B));
    assertEquals(0b1011_0000, registerFile_8.get(Reg.F));

    result = 0b1001_1011_1011_0000;

    setRegFlags(Reg.C, result);
    assertEquals(0b1001_1011, registerFile_8.get(Reg.C));
    assertEquals(0b1011_0000, registerFile_8.get(Reg.F));

    result = 0b1111_1111_1111_0000;

    setRegFlags(Reg.D, result);
    assertEquals(0b1111_1111, registerFile_8.get(Reg.D));
    assertEquals(0b1111_0000, registerFile_8.get(Reg.F));

    result = 0;
    setRegFlags(Reg.E, result);
    assertEquals(0, registerFile_8.get(Reg.E));
    assertEquals(0, registerFile_8.get(Reg.F));

    resetRegisters();



    }
     @Test
     public void testHlMethods() {

     Cpu c = new Cpu();
     Bus b = new Bus();
     Ram r = new Ram(1024);
     RamController rm = new RamController(r, 0, 1024);
     b.attach(rm);
     c.attachTo(b);

     int data;

     b.write(0x190, 0x12);
     c.setReg16(Reg16.HL, 0x190);
     assertEquals(0x12, c.read8AtHl());

     b.write(0xAF, 0xAA);
     c.setReg16(Reg16.HL, 0xAF);
     assertEquals(0xAA, c.read8AtHl());

     data = 0xFFF0;
     setReg16(Reg16.HL, 0xAF);
     c.write8AtHlAndSetFlags(data);
     assertEquals(0xFF, b.read(0xAF));
     assertEquals(c.registerFile_8.get(Reg.F), 0xF0);


     }

     @Test
     private void testResultFlagCombination() {

     Cpu c = new Cpu();
     Bus b = new Bus();
     Ram r = new Ram(1024);
     RamController rm = new RamController(r, 0, 1024);
     b.attach(rm);
     c.attachTo(b);

     int data = 0b1100_1001_1101_0000;
     Reg reg = Reg.F;

     c.combineAluFlags(data, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0, FlagSrc.V1);
     assertEquals(0b0001_0000, c.registerFile_8.get(reg));

     c.combineAluFlags(data, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.V1, FlagSrc.CPU);
     assertEquals(0b1111_0000, c.registerFile_8.get(reg));

     c.combineAluFlags(0, FlagSrc.CPU, FlagSrc.CPU, FlagSrc.CPU, FlagSrc.CPU);
     assertEquals(0b1111_0000, c.registerFile_8.get(reg));

     c.resetRegisters();

     c.combineAluFlags(0xFF, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU, FlagSrc.ALU);
     assertEquals(0b1111_0000, c.registerFile_8.get(reg));



     }


     private void resetRegisters() {

     registerFile_8.set(Reg.A, 0);
     registerFile_8.set(Reg.B, 0);
     registerFile_8.set(Reg.C, 0);
     registerFile_8.set(Reg.D, 0);
     registerFile_8.set(Reg.E, 0);
     registerFile_8.set(Reg.F, 0);
     registerFile_8.set(Reg.H, 0);
     registerFile_8.set(Reg.L, 0);
     registerFile_8.set(Reg.F, 0);
     }
     */
}
