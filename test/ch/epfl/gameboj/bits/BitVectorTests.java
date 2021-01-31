package ch.epfl.gameboj.bits;


import org.testng.annotations.Test;

import java.util.function.BiFunction;
import java.util.function.Function;

import static junit.framework.Assert.assertEquals;

public class BitVectorTests {

    private static BitVector createVector(int[] data) {

        BitVector.Builder v = new BitVector.Builder(data.length * 32);

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < 4; j++) {

                v.setByte(i * 4 + j, Bits.extract(data[i], j * 8, 8));
            }
        }
        return v.build();
    }

    private static void testEachCell(int[] exceptedValues, BitVector vector) {

        int j = 0;
        int k = 0;

        for (int i = 0; i < vector.size(); i++) {

            assertEquals("Error at index " + i, Bits.test(exceptedValues[j], k), vector.testBit(i));

            k++;
            if (k == 32) {
                k = 0;
                j++;
            }
        }


    }

    @Test
    public static void creatorTest() {

        int values[] = {0xABCD, 0xFFEF, 0x08FA, 0x1234, 0x0808};

        BitVector v = createVector(values);

        testEachCell(values, v);

    }

    private static void apply2(Function<Integer, Integer> function, int[] array) {

        for (int i = 0; i < array.length; i++) {

            array[i] = function.apply(array[i]);
        }
    }

    private static int[] apply3(BiFunction<Integer, Integer, Integer> f, int[] array1, int[] array2) {
        int result[] = new int[array2.length];
        for (int i = 0; i < array1.length; i++) {

            result[i] = f.apply(array1[i], array2[i]);
        }

        return result;
    }

    @Test
    public static void notTest() {

        int[] values = {0xABCD, 0xFFEF, 0x08FA, 0x1234, 0x0808};
        BitVector v = createVector(values);
        v = v.not();
        Function<Integer, Integer> inverse = (x) -> ~x;
        apply2(inverse, values);
        testEachCell(values, v);


        values = new int[]{0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF};
        v = createVector(values);
        v = v.not();
        apply2(inverse, values);

        testEachCell(values, v);
    }


    @Test
    public static void andTest() {

        int values1[];
        int values2[];
        int conjunction[];
        BitVector v0;
        BitVector v1;
        BiFunction<Integer, Integer, Integer> f = (x, y) -> x & y;
        for (int i = 0; i < 100; i++) {

            values1 = new int[]{i - 3, i + 1, i + 2, i + 3, i + 4, i + 5, i + 3, i - 6};
            values2 = new int[]{i - 7, i - 4, i * 3, i + 2, i / 6, i + 4, i + 3, i + 9};
            conjunction = apply3(f, values1, values2);
            v0 = createVector(values1);
            v1 = createVector(values2);
            v0 = v0.and(v1);
            testEachCell(conjunction, v0);
        }

        values1 = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        values2 = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        conjunction = apply3(f, values1, values2);
        v0 = createVector(values1);
        v1 = createVector(values2);
        v0 = v0.and(v1);

        testEachCell(conjunction, v0);

        values1 = new int[]{0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF};
        values2 = new int[]{0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF};

        conjunction = apply3(f, values1, values2);
        v0 = createVector(values1);
        v1 = createVector(values2);
        v0 = v0.and(v1);

        testEachCell(conjunction, v0);
    }

    @Test
    public static void orTest() {

        int values1[];
        int values2[];
        int disjunction[];
        BitVector v0;
        BitVector v1;
        BiFunction<Integer, Integer, Integer> f = (x, y) -> x | y;
        for (int i = 0; i < 100; i++) {

            values1 = new int[]{i - 3, i + 1, i + 2, i + 3, i + 4, i + 5, i + 3, i - 6};
            values2 = new int[]{i - 7, i - 4, i * 3, i + 2, i / 6, i + 4, i + 3, i + 9};
            disjunction = apply3(f, values1, values2);
            v0 = createVector(values1);
            v1 = createVector(values2);
            v0 = v0.or(v1);
            testEachCell(disjunction, v0);
        }

        values1 = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        values2 = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

        disjunction = apply3(f, values1, values2);
        v0 = createVector(values1);
        v1 = createVector(values2);
        v0 = v0.or(v1);

        testEachCell(disjunction, v0);

        values1 = new int[]{0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF};
        values2 = new int[]{0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF, 0xFFFF};

        disjunction = apply3(f, values1, values2);
        v0 = createVector(values1);
        v1 = createVector(values2);
        v0 = v0.or(v1);

        testEachCell(disjunction, v0);

    }

    @Test
    public static void extractZeroExtendedTests() {

        int[] values = {0xABCD00FF, 0x1212ABCD};
        BitVector v0 = createVector(values);
        BitVector v1 = v0.extractZeroExtended(12, 32);
        BitVector v2;
        compareStringAndValue(v1.toString(), 0xbcdabcd0);

        v1 = v0.extractZeroExtended(-28, 32);
        compareStringAndValue(v1.toString(), 0xf0000000);

        v1 = v0.extractZeroExtended(-32, 32);
        compareStringAndValue(v1.toString(), 0);

        v1 = v0.extractZeroExtended(52, 64);
        compareStringAndValue(v1.toString(), 0x121);

        values = new int[]{0xabcdabcd, 0x00001212};
        v1 = createVector(values);

        v2 = v0.extractZeroExtended(16, 64);
        assertEquals(v1.toString(), v2.toString());
    }

    @Test
    public static void wrappedExtensionTest() {

        int[] values = {0xABCD00FF, 0x1212ABCD};
        BitVector v0 = createVector(values);
        BitVector v1 = v0.extractWrapped(12, 32);
        BitVector v2;
        compareStringAndValue(v1.toString(), 0xbcdabcd0);

        v1 = v0.extractWrapped(-8, 32);
        compareStringAndValue(v1.toString(), 0xcd00ff12);

        v1 = v0.extractWrapped(12, 128);

        values = new int[]{0xBCDABCD0, 0x0FF1212A, 0xBCDABCD0, 0x0FF1212A};
        v2 = createVector(values);

        assertEquals(v2.toString(), v1.toString());


    }

    @Test
    public static void shiftTest() {

        int values[] = {0XABCD00FF, 0x1212ABCD};

        BitVector v0 = createVector(values);
        values = new int[]{0x00ff0000, 0xabcdabcd};
        BitVector v1 = v0.shift(16);
        BitVector v2 = createVector(values);
        assertEquals(v1.toString(), v2.toString());

        values = new int[]{0, 0};

        v1 = v0.shift(64);
        v2 = createVector(values);
        assertEquals(v2.toString(), v1.toString());

        v1 = v0.shift(-8);
        values = new int[]{0xcdabcd00, 0x1212ab};
        v2 = createVector(values);
        assertEquals(v2.toString(), v1.toString());


    }

    private static void compareStringAndValue(String str, int value) {


        String strValue = Integer.toBinaryString(value);
        if (strValue.length() < str.length()) {
            StringBuilder prefix = new StringBuilder();

            for (int i = 0; i < str.length() - strValue.length(); i++) {
                prefix.append('0');
            }
            strValue = prefix.toString() + strValue;
        }
        assertEquals("Error : excepted " + strValue + ", given : " + str, strValue, str);
    }


}