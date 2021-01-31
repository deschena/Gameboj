package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;
import org.junit.jupiter.api.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class LcdImageLineTests {


    private static BitVector createVector(int[] data) {

        BitVector.Builder v = new BitVector.Builder(data.length * 32);

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < 4; j++) {

                v.setByte(i * 4 + j, Bits.extract(data[i], j * 8, 8));
            }
        }
        return v.build();
    }

    public static LcdImageLine createLine(int[] msb, int[] lsb, int[] opacity) {
        return new LcdImageLine(createVector(msb), createVector(lsb), createVector(opacity));

    }

    public static LcdImageLine createLine(int[] msb, int[] lsb) {
        return new LcdImageLine(createVector(msb), createVector(lsb));
    }

    @Test
    public void belowTest() {

        int[] above = {0b11100101110011011111000111111001};
        int[] below = {0b10000111101110011001111001001010};
        int[] result =  {0b11100101_11101001_10111000_01111011};
        int[] opAbove = {0b11100011_01010001_00110110_01111001};
        int[] opBelow = {0b01011010_11010101_00111100_00100010};
        int[] opResult= {0b11111011_11010101_00111110_01111011};

        LcdImageLine l0 = createLine(above, above, opAbove);
        LcdImageLine l1 = createLine(below, below, opBelow);
        LcdImageLine r = createLine(result, result, opResult);

        l1 = l1.below(l0);

        assertEquals(r.msb().toString(), l1.msb().toString());
        assertEquals(r.opacity().toString(), l1.opacity().toString());

        above = new int[]{0b11100101110011011111000111111001};
        below = new int[]{0b10000111101110011001111001001010};
        opAbove = new int[]{0b0};
        opBelow = new int[]{0b0};
        opResult = new int[]{0b0};

        l0 = createLine(above, above, opAbove);
        l1 = createLine(below, below, opBelow);
        r = createLine(below, below, opResult);

        l1 = l1.below(l0);

        assertEquals(r.msb().toString(), l1.msb().toString());
        assertEquals(r.opacity().toString(), l1.opacity().toString());

        above = new int[]{0b11100101110011011111000111111001};
        below = new int[]{0b10000111101110011001111001001010};
        opAbove = new int[]{0xFFFFFFFF};
        opBelow = new int[]{0xFFFFFFFF};
        opResult = new int[]{0xFFFFFFFF};

        l0 = createLine(above, above, opAbove);
        l1 = createLine(below, below, opBelow);
        r = createLine(above, above, opResult);

        l1 = l1.below(l0);

        assertEquals(r.msb().toString(), l1.msb().toString());
        assertEquals(r.opacity().toString(), l1.opacity().toString());

    }

    @Test
    public void mapColorsWorksWithRandomValues() {

        LcdImageLine line = createLine(new int[]{0b00000000_00000000_00000001_00111001}, new int[]{0b00000000_00000000_00000001_10100101},
                new int[]{0b10010101_11010001_11010100_11110000});
        int mapcolors = 0b10010011;

        LcdImageLine newLine = line.mapColors(mapcolors);

        BitVector testMsb = createVector(new int[]{0b11111111111111111111111101100011});
        BitVector testLsb = createVector(new int[]{0b11111111111111111111111001011010});
        BitVector testOpa = createVector(new int[]{0b10010101_11010001_11010100_11110000});

        assertEquals(newLine.msb(), testMsb);
        assertEquals(newLine.lsb(), testLsb);
        assertEquals(newLine.opacity(), testOpa);

    }


    @Test
    public void mapColorsWorksWithNoChange() {

        LcdImageLine line = createLine(new int[]{0b10010110_11111111_10100000_00110000}, new int[]{0b11010101_11101010_00010101_10101101},
                new int[]{0b10010101_11010001_11010100_11110000});
        int mapcolors = 0b11100100;

        LcdImageLine newLine = line.mapColors(mapcolors);

        BitVector testMsb = createVector(new int[]{0b10010110_11111111_10100000_00110000});
        BitVector testLsb = createVector(new int[]{0b11010101_11101010_00010101_10101101});
        BitVector testOpa = createVector(new int[]{0b10010101_11010001_11010100_11110000});

        assertEquals(newLine.msb(), testMsb);
        assertEquals(newLine.lsb(), testLsb);
        assertEquals(newLine.opacity(), testOpa);

    }

    @Test
    public void mapColorsWorksWithAllBlack() {

        LcdImageLine line = createLine(new int[]{0b10010110_11111111_10100000_00110000}, new int[]{0b11010101_11101010_00010101_10101101},
                new int[]{0b10010101_11010001_11010100_11110000});
        int mapcolors = 0b11111111;

        LcdImageLine newLine = line.mapColors(mapcolors);

        BitVector testMsb = createVector(new int[]{0xFFFFFFFF});
        BitVector testLsb = createVector(new int[]{0xFFFFFFFF});
        BitVector testOpa = createVector(new int[]{0b10010101_11010001_11010100_11110000});

        assertEquals(newLine.msb(), testMsb);
        assertEquals(newLine.lsb(), testLsb);
        assertEquals(newLine.opacity(), testOpa);

    }

    @Test
    public void mapColorsWorksWithAllWhite() {

        LcdImageLine line = createLine(new int[]{0b10010110_11111111_10100000_00110000}, new int[]{0b11010101_11101010_00010101_10101101},
                new int[]{0b10010101_11010001_11010100_11110000});
        int mapcolors = 0b00000000;

        LcdImageLine newLine = line.mapColors(mapcolors);

        BitVector testMsb = createVector(new int[]{0x00000000});
        BitVector testLsb = createVector(new int[]{0x00000000});
        BitVector testOpa = createVector(new int[]{0b10010101_11010001_11010100_11110000});

        assertEquals(newLine.msb(), testMsb);
        assertEquals(newLine.lsb(), testLsb);
        assertEquals(newLine.opacity(), testOpa);

    }

    @Test
    public void joinWorksWithRandomValues() {

        int[] msb0 = new int[]{0b11100011_00011000_00110000_11010111};
        int[] lsb0 = new int[]{0b11100000_00110001_00000000_10111001};
        int[] opa0 = new int[]{0b11100011_00111001_00110000_11111111};

        LcdImageLine line1 = createLine(msb0, lsb0, opa0);

        int[] msb1 = new int[]{0b00000011_11000000_01001001_11010111};
        int[] lsb1 = new int[]{0b01100100_00010100_00110000_10111001};
        int[] opa1 = new int[]{0b01100111_11010100_01111001_11111111};
        LcdImageLine line2 = createLine(msb1, lsb1, opa1);

        LcdImageLine newLine = line1.join(12, line2);

        BitVector testMsb = createVector(new int[]{0b00000011_11000000_01000000_11010111});
        BitVector testLsb = createVector(new int[]{0b01100100_00010100_00110000_10111001});
        BitVector testOpa = createVector(new int[]{0b01100111_11010100_01110000_11111111});

        assertEquals(testMsb, newLine.msb());
        assertEquals(testLsb, newLine.lsb());
        assertEquals(testOpa, newLine.opacity());



    }

    @Test
    public void joinWorksWithZero() {

        LcdImageLine line1 = createLine(new int[]{0b11100011_00011000_00110000_11010111},
                new int[]{0b11100000_00110001_00000000_10111001}, new int[]{0b11100000_00110100_00000001_10110011});
        LcdImageLine line2 = createLine(new int[]{0b00000011_11000000_01001001_11010111},
                new int[]{0b01100100_00010100_00110000_10111001}, new int[]{0b11100000_00000111_01000100_10110011});

        LcdImageLine newLine = line1.join(0, line2);

        BitVector testMsb = createVector(new int[]{0b00000011_11000000_01001001_11010111});
        BitVector testLsb = createVector(new int[]{0b01100100_00010100_00110000_10111001});
        BitVector testOpa = createVector(new int[]{0b11100000_00000111_01000100_10110011});

        assertEquals(testMsb, newLine.msb());
        assertEquals(testLsb, newLine.lsb());
        assertEquals(testOpa, newLine.opacity());


    }

    @Test
    public void joinWorksWithThirtyTwo() {

        int[] msb0 = new int[]{0b11100011_00011000_00110000_11010111};
        int[] lsb0 = new int[]{0b11100000_00110001_00000000_10111001};
        int[] opa0 = new int[]{0b11100000_00110100_00000001_10110011};
        LcdImageLine line1 = createLine(msb0, lsb0, opa0);

        int[] msb1 = new int[]{0b00000011_11000000_01001001_11010111};
        int[] lsb1 = new int[]{0b01100100_00010100_00110000_10111001};
        int[] opa1 = new int[]{0b11100000_00000111_01000100_10110011};
        LcdImageLine line2 = createLine(msb1, lsb1, opa1);

        LcdImageLine newLine = line1.join(32, line2);

        BitVector testMsb = createVector(new int[]{0b11100011_00011000_00110000_11010111});
        BitVector testLsb = createVector(new int[]{0b11100000_00110001_00000000_10111001});
        BitVector testOpa = createVector(new int[]{0b11100000_00110100_00000001_10110011});

        assertEquals(testMsb, newLine.msb());
        assertEquals(testLsb, newLine.lsb());
        assertEquals(testOpa, newLine.opacity());

    }

    @Test
    public void lcdImageLineEqualsWork() {
        LcdImageLine line1 = createLine(new int[]{0b11100011_00011000_00110000_11010111},
                new int[]{0b11100000_00110001_00000000_10111001}, new int[]{0b11100000_00110100_00000001_10110011});
        LcdImageLine line2 = createLine(new int[]{0b11100011_00011000_00110000_11010111},
                new int[]{0b11100000_00110001_00000000_10111001}, new int[]{0b11100000_00110100_00000001_10110011});

        assertTrue(line1.equals(line2));
    }

    @Test
    public void lcdImageLineHashCodeWorks() {
        LcdImageLine line1 = createLine(new int[]{0b11100011_00011000_00110000_11010111},
                new int[]{0b11100000_00110001_00000000_10111001}, new int[]{0b11100000_00110100_00000001_10110011});
        LcdImageLine line2 = createLine(new int[]{0b11100011_00011000_00110000_11010111},
                new int[]{0b11100000_00110001_00000000_10111001}, new int[]{0b11100000_00110100_00000001_10110011});

        assertTrue(line1.hashCode() == line2.hashCode());
    }


    @Test
    public void testLcdImageLineBuilder() {

        LcdImageLine.Builder imageBuilder = new LcdImageLine.Builder(32);
        imageBuilder.setBytes(0, 0b10110010, 0b11101001);
        imageBuilder.setBytes(1, 0b10111010, 0b11010100);
        imageBuilder.setBytes(2, 0b00101100, 0b11101010);
        imageBuilder.setBytes(3, 0b00001001, 0b00001010);

        LcdImageLine line = imageBuilder.build();

        BitVector testMsb = createVector(new int[]{0b00001001_00101100_10111010_10110010});
        BitVector testlsb = createVector(new int[]{0b00001010_11101010_11010100_11101001});

        assertTrue(line.msb().equals(testMsb) && line.lsb().equals(testlsb));

    }

    @Test
    public void testLcdImageLineBuilderExceptions() {
        assertThrows(IllegalArgumentException.class, () -> new LcdImageLine.Builder(-1));
        assertThrows(IllegalArgumentException.class, () -> new LcdImageLine.Builder(23));
    }

    @Test
    public void testLcdImageLineSetBytesExceptions() {
        LcdImageLine.Builder imageBuilder = new LcdImageLine.Builder(32);
        assertThrows(IndexOutOfBoundsException.class, () -> imageBuilder.setBytes(-1, 0b11111111, 0b11111111));
        assertThrows(IndexOutOfBoundsException.class, () -> imageBuilder.setBytes(5, 0b11111111, 0b11111111));
        assertThrows(IllegalArgumentException.class, () -> imageBuilder.setBytes(2, 0b11111111_1, 0b11111111));

    }


}
