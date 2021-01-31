package ch.epfl.gameboj.bits;


import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;

/**
 * Classe non-instanciable offrant des méthodes utilitaires de manipulation de bits
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */

public final class Bits {
    private Bits() {
    }

    /**
     * retourne un entier qui ne contient qu'un '1' dans sa forme binaire
     *
     * @param index : position dans l'entier
     * @return entier composé d'un unique '1' à la position index
     */
    public static int mask(int index) {

        Objects.checkIndex(index, Integer.SIZE);
        return 0b1 << index;
    }

    /**
     * Vérifie si on trouve '1' à l'index donné dans l'entier donné sous sa forme binaire
     *
     * @param bits  : entier binaire
     * @param index
     * @return résultat du test (booléen)
     * @throws IndexOutOfBoundsException si l'index est négatif ou supérieur à 32 bits
     */
    public static boolean test(int bits, int index) {
        if (index < 0 || index >= Integer.SIZE) {
            throw new IndexOutOfBoundsException();
        }

        int mask = mask(index);
        return (bits & mask) == mask;

    }

    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }

    /**
     * retourne une valeur dont tous les bits sont égaux à ceux de bits, sauf celui d'index qui est égal à newValue
     *
     * @param bits     : chaine à modifier
     * @param index    : index du bit à modifier
     * @param newValue : true: 1, false: 0
     * @return la nouvelle valeur modifiée
     */
    public static int set(int bits, int index, boolean newValue) {
        Objects.checkIndex(index, Integer.SIZE);
        return newValue ? (bits | mask(index)) : bits & ~mask(index);

    }

    /**
     * retourne une valeur dont les size bits de poids faible sont égaux à ceux de bits, les autres valant 0
     *
     * @param size : nombre de bits de poids faible à copier dans la nouvelle valeur
     * @param bits : chaine à modifier
     * @return la nouvelle valeur modifiée
     */
    public static int clip(int size, int bits) {
        checkArgument(size >= 0 && size <= Integer.SIZE);
        if (size == 0) return 0;
        // si size est égal à zéro, l'entier retourné est constitué uniquement de zéros
        return (bits << (Integer.SIZE - size) >>> (Integer.SIZE - size));
    }

    /**
     * @param bits  : valeur dont est extrait la valeur de retour
     * @param start : position de départ (incluse, depuis la droite)
     * @param size  : longueur de la valeur à renvoyer
     * @return : valeur dont les size LS bits sont extraits de bits à partir de l'indice start
     * @throws IndexOutOfBoundsException si la position de départ est supérieure ou égale à 32 bits, ou si elle est
     *                                   négative, ou si la somme de la position de départ et la longueur de la valeur est supérieure à 32 bits
     */
    public static int extract(int bits, int start, int size) {

        if ((start >= Integer.SIZE || start < 0) || start + size > Integer.SIZE) {
            throw new IndexOutOfBoundsException();
        }
        if (size == 0) return 0;
        return Bits.clip(start + size, bits) >>> start;
    }

    /**
     * Effectue une rotation sur une valeur sous sa forme binaire
     *
     * @param size     : longueur en bits de la valeur
     * @param bits     : entier sur lequel effectuer la rotation
     * @param distance : nombre de décalage (positif : vers la gauche, négatif : vers la droite)
     * @return une valeur dont les 'size' LS sont une rotation des 'size' LS de bits
     */
    public static int rotate(int size, int bits, int distance) {
        checkArgument(size > 0 && size <= Integer.SIZE && ((bits >> size == 0) || size == Integer.SIZE));

        distance = Math.floorMod(distance, size);

        return clip(size, (bits << distance)) | (bits >>> size - distance);

    }


    /**
     * étend le signe de la valeur 8-bit donnée
     *
     * @param b : valeur dont il faut étendre le signe
     * @return la nouvelle valeur modifiée
     */
    public static int signExtend8(int b) {
        checkBits8(b);
        return (byte) b;
    }

    /**
     * retourne une valeur égale à celle donnée, en inversant les bits de poids faible,
     *
     * @param b : valeur dont il faut inverser les bits
     * @return la nouvelle valeur modifiée
     */
    public static int reverse8(int b) {

        checkBits8(b);

        int[] a = new int[]{
                0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0,
                0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0,
                0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8,
                0x18, 0x98, 0x58, 0xD8, 0x38, 0xB8, 0x78, 0xF8,
                0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4, 0x64, 0xE4,
                0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4,
                0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC,
                0x1C, 0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC,
                0x02, 0x82, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2,
                0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2,
                0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA,
                0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA,
                0x06, 0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6,
                0x16, 0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6,
                0x0E, 0x8E, 0x4E, 0xCE, 0x2E, 0xAE, 0x6E, 0xEE,
                0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E, 0xFE,
                0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1,
                0x11, 0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1,
                0x09, 0x89, 0x49, 0xC9, 0x29, 0xA9, 0x69, 0xE9,
                0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9, 0x79, 0xF9,
                0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65, 0xE5,
                0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5,
                0x0D, 0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED,
                0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD, 0x7D, 0xFD,
                0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3,
                0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73, 0xF3,
                0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB,
                0x1B, 0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB,
                0x07, 0x87, 0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7,
                0x17, 0x97, 0x57, 0xD7, 0x37, 0xB7, 0x77, 0xF7,
                0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F, 0xEF,
                0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF,
        };

        return a[b];
    }

    /**
     * retourne une valeur égale à celle donnée, en inversant les 8 bits de poids faible bit à bit,
     *
     * @param b : valeur dont il faut inverser les bits
     * @return la nouvelle valeur modifiée
     */
    public static int complement8(int b) {

        checkBits8(b);
        return (0xFF ^ b);
    }

    /**
     * retourne une valeur 16 bits dont les 8 bits de poids forts sont les 8 bits de poids faible de highB, et dont les 8 bits de poids faible sont ceux de lowB
     *
     * @param highB : valeur des 8 bits forts de la valeur à retourner
     * @param lowB  : valeur des 8 bits faibles de la valeur à retourner
     * @return la nouvelle valeur modifiée
     */
    public static int make16(int highB, int lowB) {

        checkBits8(highB);
        checkBits8(lowB);

        return (highB << 8) | lowB;
    }
}