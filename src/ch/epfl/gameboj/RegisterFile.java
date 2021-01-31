package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

import static ch.epfl.gameboj.Preconditions.checkBits8;

/**
 * Classe représentant un banc de registres
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class RegisterFile<E extends Register> {

    private final byte[] allRegisters;

    public RegisterFile(E[] allRegs) {
        allRegisters = new byte[allRegs.length];

    }

    /**
     * Permet d'obtenir la valeur stockée dans un registre
     *
     * @param reg registre contenant la valeur requise
     * @return valeur contenue dans le registre passé en argument
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(allRegisters[reg.index()]);
    }

    /**
     * Permet de mettre à jour la valeur stockée dans un registre
     *
     * @param reg      registre à modifier
     * @param newValue nouvelle valeur à assigner<
     */
    public void set(E reg, int newValue) {
        checkBits8(newValue);

        allRegisters[reg.index()] = (byte) newValue;

    }

    /**
     * teste si un bit d'indice donné est à un
     *
     * @param reg registre donné
     * @param b   indice du bit à tester
     * @return "true" si le bit d'indice donné est à 1
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(get(reg), b);
    }

    /**
     * Modifie la valeur contenue dans le registre passé, à l'indice passé, en fonction de newValue (true -> 1, false -> 0)
     *
     * @param reg      registre dont le contenu doit être modifié
     * @param bit      indice à modifier
     * @param newValue nouvelle valeur à l'indice
     */
    public void setBit(E reg, Bit bit, boolean newValue) {

        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }

}
