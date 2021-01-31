package ch.epfl.gameboj.component.cpu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
/**
 * @author  Armen Homberger (154511)
 *          Justin Deschenaux (288424)
 */

class AluTest {


    @Test
    void maskZNHC() {

        assertEquals(0xB0, Alu.maskZNHC(true, false, true , true));
        assertEquals(0x00, Alu.maskZNHC(false, false, false, false));
        assertEquals(0xF0, Alu.maskZNHC(true, true, true, true));
        assertEquals(0x50, Alu.maskZNHC(false, true, false, true));
    }

    @Test
    void packValueAndFlag() {

        assertEquals(0xCD2FA0, Alu.packValueAndFlag(0xCD2F, true, false, true, false));
        assertEquals(0x139F00, Alu.packValueAndFlag(0x139F, false, false, false, false));
        assertEquals(0, Alu.packValueAndFlag(0, false, false, false, false));
        assertEquals(0xFFFFF0, Alu.packValueAndFlag(0xFFFF, true, true, true, true));
        assertEquals(0xF0, Alu.packValueAndFlag(0, true, true, true, true));

    }

    @Test
    void unpackValue() {
        //Normal Use
        assertEquals(0xFF, Alu.unpackValue(0xFF70));
        assertEquals(0, Alu.unpackValue(0x00F0));
        assertEquals(0xB5, Alu.unpackValue(0xB5FF)); //With use of the common lasts zeros !
    }

    @Test
    void unpackFlags() {

        //Normal Use
        assertEquals(0x0, Alu.unpackFlags(0xFF07));
        assertEquals(0xF0, Alu.unpackFlags(0xFFF0));
        assertEquals(0xA0, Alu.unpackFlags(  0xA0));

    }

    @Test
    void add() {

        //Addition sans retenue
        int resultWithFlags = Alu.add(0, 0, false);
        assertEquals(0, Alu.unpackValue(resultWithFlags));
        assertEquals(0b1000_0000, Alu.unpackFlags(resultWithFlags));

        //Addition des nombres maximaux sur 8 bits, test si elle est bien modulo 256
        resultWithFlags = Alu.add(0xFF, 0xFF);
        assertEquals(0xFE, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0011_0000, Alu.unpackFlags(resultWithFlags));

        //Addition commune, sans retenue
        resultWithFlags = Alu.add(0x4E, 0x96);
        assertEquals(0xE4, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0010_0000, Alu.unpackFlags(resultWithFlags));

        //Addition commune, avec retenue
        resultWithFlags = Alu.add(0x4E, 0x96, true);
        assertEquals(0xE5, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0010_0000, Alu.unpackFlags(resultWithFlags));

        //Controle la gestion des exeptions
        assertThrows(IllegalArgumentException.class, () -> Alu.add(0xFF1, 0x00));
        assertThrows(IllegalArgumentException.class, () -> Alu.add(0x00, 0xFF1));


    }

    @Test
    void add16L() {

        //Addition
        int resultWithFlags = Alu.add16L(0, 0);
        assertEquals(0, Alu.unpackValue(resultWithFlags));
        assertEquals(0, Alu.unpackFlags(resultWithFlags));

        //Addition des nombres maximaux sur 16 bits, test si elle est bien modulo 0xFFFF
        resultWithFlags = Alu.add16L(0xFFFF, 0xFFFF);
        assertEquals(0xFFFE, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0011_0000, Alu.unpackFlags(resultWithFlags));

        //Addition commune
        resultWithFlags = Alu.add16L(0xBA4E, 0x891);
        assertEquals(0xC2DF, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0000_0000, Alu.unpackFlags(resultWithFlags));


        //Controle la gestion des exeptions
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(0x10000, 0x00));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(0x00, 0x10000));

    }

    @Test
    void add16H() {

        //Addition
        int resultWithFlags = Alu.add16H(0, 0);
        assertEquals(0, Alu.unpackValue(resultWithFlags));
        assertEquals(0, Alu.unpackFlags(resultWithFlags));

        //Addition des nombres maximaux sur 16 bits, test si elle est bien modulo 0xFFFF
        resultWithFlags = Alu.add16H(0xFFFF, 0xFFFF);
        assertEquals(0xFFFE, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0011_0000, Alu.unpackFlags(resultWithFlags));

        //Addition commune
        resultWithFlags = Alu.add16H(0xBA4E, 0x891);
        assertEquals(0xC2DF, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0010_0000, Alu.unpackFlags(resultWithFlags));


        //Controle la gestion des exeptions
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(0x10000, 0x00));
        assertThrows(IllegalArgumentException.class, () -> Alu.add16L(0x00, 0x10000));
    }

    @Test
    void sub() {

        //Addition sans retenue
        int resultWithFlags = Alu.sub(0, 0, false);
        assertEquals(0, Alu.unpackValue(resultWithFlags));
        assertEquals(0b1100_0000, Alu.unpackFlags(resultWithFlags));

        //Soustraction avec résultat négatif
        resultWithFlags = Alu.sub(0x00, 0xFF);
        assertEquals(0x01, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0111_0000, Alu.unpackFlags(resultWithFlags));

        //Soustraction commune, sans retenue
        resultWithFlags = Alu.sub(0x4E, 0x96);
        assertEquals(0xB8, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0101_0000, Alu.unpackFlags(resultWithFlags));

        //Soustraction commune, avec retenue
        resultWithFlags = Alu.sub(0x01, 0x01, true);
        assertEquals(0xFF, Alu.unpackValue(resultWithFlags));
        assertEquals(0x70, Alu.unpackFlags(resultWithFlags));

        resultWithFlags = Alu.sub(0x10, 0x80);
        assertEquals(0x90, Alu.unpackValue(resultWithFlags));
        assertEquals(0x50, Alu.unpackFlags(resultWithFlags));

        resultWithFlags = Alu.sub(0x10, 0x10);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0xC0, Alu.unpackFlags(resultWithFlags));

        //Controle la gestion des exeptions
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(0xFF1, 0x00));
        assertThrows(IllegalArgumentException.class, () -> Alu.sub(0x00, 0xFF1));
    }


    @Test
    void bcdAdjust() {
        // test avec une valeur arbitraire
        int resultWithFlags = Alu.bcdAdjust(0x6D, false, false, false);
        assertEquals(0x73, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0000_0000, Alu.unpackFlags(resultWithFlags));

        // test avec une valeur arbitraire
        resultWithFlags = Alu.bcdAdjust(0x0F, true, true, false);
        assertEquals(0x09, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0100_0000, Alu.unpackFlags(resultWithFlags));

        // test avec zéro
        resultWithFlags = Alu.bcdAdjust(0x00, false, false, false);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0b1000_0000, Alu.unpackFlags(resultWithFlags));

        // Contrôle de la gestion des exceptions
        assertThrows(IllegalArgumentException.class, () -> Alu.bcdAdjust(0x100, false, false, false));
    }

    @Test
    void and() {
        // test avec une valeur arbitraire
        int resultWithFlags = Alu.and(0x53, 0xA7);
        assertEquals(0x03, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0010_0000, Alu.unpackFlags(resultWithFlags));

        // test avec zéro
        resultWithFlags = Alu.and(0x00, 0x00);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0b1010_0000, Alu.unpackFlags(resultWithFlags));

        // test avec 255
        resultWithFlags = Alu.and(0xFF, 0xFF);
        assertEquals(0xFF, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0010_0000, Alu.unpackFlags(resultWithFlags));

        // Contrôle de la gestion des exceptions
        assertThrows(IllegalArgumentException.class, () -> Alu.and(0x100, 0xF));
        assertThrows(IllegalArgumentException.class, () -> Alu.and(0xF, 0x100));

    }

    @Test
    void or() {
        // test avec une valeur arbitraire
        int resultWithFlags = Alu.or(0x53, 0xA7);
        assertEquals(0xF7, Alu.unpackValue(resultWithFlags));
        assertEquals(0x00, Alu.unpackFlags(resultWithFlags));

        // test avec zéro
        resultWithFlags = Alu.or(0x00, 0x00);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0b1000_0000, Alu.unpackFlags(resultWithFlags));

        // test avec 255
        resultWithFlags = Alu.or(0xFF, 0xFF);
        assertEquals(0xFF, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0000_0000, Alu.unpackFlags(resultWithFlags));

        // Contrôle de la gestion des exceptions
        assertThrows(IllegalArgumentException.class, () -> Alu.or(0x100, 0xF));
        assertThrows(IllegalArgumentException.class, () -> Alu.or(0xF, 0x100));

    }

    @Test
    void xor() {
        // test avec une valeur arbitraire
        int resultWithFlags = Alu.xor(0x53, 0xA7);
        assertEquals(0xF4, Alu.unpackValue(resultWithFlags));
        assertEquals(0x00, Alu.unpackFlags(resultWithFlags));

        // test avec zéro
        resultWithFlags = Alu.xor(0x00, 0x00);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0b1000_0000, Alu.unpackFlags(resultWithFlags));

        // test avec 255
        resultWithFlags = Alu.xor(0xFF, 0xFF);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0b1000_0000, Alu.unpackFlags(resultWithFlags));

        // Contrôle de la gestion des exceptions
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(0x100, 0xF));
        assertThrows(IllegalArgumentException.class, () -> Alu.xor(0xF, 0x100));

    }

    @Test
    void shiftLeft() {

        int resultWithFlags = Alu.shiftLeft(0x80);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0x90, Alu.unpackFlags(resultWithFlags));

        resultWithFlags = Alu.shiftLeft(0);
        assertEquals(0, Alu.unpackValue(resultWithFlags));
        assertEquals(0x80, Alu.unpackFlags(resultWithFlags));

        resultWithFlags = Alu.shiftLeft(0xFF);
        assertEquals(0xFE, Alu.unpackValue(resultWithFlags));
        assertEquals(0x10, Alu.unpackFlags(resultWithFlags));

        assertThrows(IllegalArgumentException.class, () -> Alu.shiftLeft(0x100));
    }

    @Test
    void shiftRightA() {

        int resultWithFlags = Alu.shiftRightA(0x80);
        assertEquals(0xC0, Alu.unpackValue(resultWithFlags));
        assertEquals(0x00, Alu.unpackFlags(resultWithFlags));

        resultWithFlags = Alu.shiftRightA(0xFF);
        assertEquals(0xFF, Alu.unpackValue(resultWithFlags));
        assertEquals(0x10, Alu.unpackFlags(resultWithFlags));

        resultWithFlags = Alu.shiftRightA(0x00);
        assertEquals(0x0, Alu.unpackValue(resultWithFlags));
        assertEquals(0x80, Alu.unpackFlags(resultWithFlags));

        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightA(0x100));


    }

    @Test
    void shiftRightL() {

        int resultWithFlags = Alu.shiftRightL(0x80);
        assertEquals(0x40, Alu.unpackValue(resultWithFlags));
        assertEquals(0x00, Alu.unpackFlags(resultWithFlags));

        resultWithFlags = Alu.shiftRightL(0xFF);
        assertEquals(0x7F, Alu.unpackValue(resultWithFlags));
        assertEquals(0x10, Alu.unpackFlags(resultWithFlags));

        resultWithFlags = Alu.shiftRightL(0x00);
        assertEquals(0x0, Alu.unpackValue(resultWithFlags));
        assertEquals(0x80, Alu.unpackFlags(resultWithFlags));

        assertThrows(IllegalArgumentException.class, () -> Alu.shiftRightL(0x100));

    }

    @Test
    void rotate() {
        // test avec une valeur arbitraire, rotation gauche
        int resultWithFlags = Alu.rotate(Alu.RotDir.LEFT, 0x80);
        assertEquals(0x01, Alu.unpackValue(resultWithFlags));
        assertEquals(0x10, Alu.unpackFlags(resultWithFlags));

        // test avec une valeur arbitraire, rotation droite
        resultWithFlags = Alu.rotate(Alu.RotDir.RIGHT, 0x01);
        assertEquals(0x80, Alu.unpackValue(resultWithFlags));
        assertEquals(0x10, Alu.unpackFlags(resultWithFlags));

        // contrôle de la gestion des exceptions
        assertThrows(IllegalArgumentException.class, () -> Alu.rotate(null, 0x80));
        assertThrows(IllegalArgumentException.class, () -> Alu.rotate(Alu.RotDir.LEFT, 0x100));
    }

    @Test
    void rotate1() {
        // test avec une valeur arbitraire, rotation gauche
        int resultWithFlags = Alu.rotate(Alu.RotDir.LEFT, 0x80, false);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0x90, Alu.unpackFlags(resultWithFlags));

        // test avec une valeur arbitraire, rotation gauche
        resultWithFlags = Alu.rotate(Alu.RotDir.LEFT, 0x00, true);
        assertEquals(0x01, Alu.unpackValue(resultWithFlags));
        assertEquals(0x00, Alu.unpackFlags(resultWithFlags));

        // test avec une valeur arbitraire, rotation droite
        resultWithFlags = Alu.rotate(Alu.RotDir.RIGHT, 0x01, false);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0x90, Alu.unpackFlags(resultWithFlags));

        // test avec une valeur arbitraire, rotation droite
        resultWithFlags = Alu.rotate(Alu.RotDir.RIGHT, 0x01, true);
        assertEquals(0x80, Alu.unpackValue(resultWithFlags));
        assertEquals(0x10, Alu.unpackFlags(resultWithFlags));

        // contrôle de la gestion des exceptions
        assertThrows(IllegalArgumentException.class, () -> Alu.rotate(null, 0x80, true));
        assertThrows(IllegalArgumentException.class, () -> Alu.rotate(Alu.RotDir.LEFT, 0x100, true));
    }

    @Test
    void swap() {
        // test avec une valeur arbitraire
        int resultWithFlags = Alu.swap(0xF0);
        assertEquals(0xF, Alu.unpackValue(resultWithFlags));
        assertEquals(0x00, Alu.unpackFlags(resultWithFlags));

        // test avec zéro
        resultWithFlags = Alu.swap(0x00);
        assertEquals(0x00, Alu.unpackValue(resultWithFlags));
        assertEquals(0b1000_0000, Alu.unpackFlags(resultWithFlags));

        // test avec 255
        resultWithFlags = Alu.swap(0xFF);
        assertEquals(0xFF, Alu.unpackValue(resultWithFlags));
        assertEquals(0b0000_0000, Alu.unpackFlags(resultWithFlags));

        // contrôle de la gestion des exceptions
        assertThrows(IllegalArgumentException.class, () -> Alu.swap(0x100));

    }


    @Test
    void binaryToBoolean() {
        // test avec zéro
        //assertEquals(false, Alu.binaryToBoolean(0));

        // test avec un
        //assertEquals(false, Alu.binaryToBoolean(0));

        // contrôle de la gestion des exceptions
        //assertThrows(IllegalArgumentException.class, () -> Alu.binaryToBoolean(3));
    }

    @Test
    void booleanToInteger() {
        // test avec zéro
        //assertEquals(0, Alu.booleanToInteger(false));

        // test avec un
        //assertEquals(1, Alu.booleanToInteger(true));

    }

}