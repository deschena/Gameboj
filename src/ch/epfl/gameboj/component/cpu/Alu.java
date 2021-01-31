package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

import static ch.epfl.gameboj.Preconditions.*;

/**
 * Classe de méthodes utilitaires utilisées par l'ALU du GameBoy
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class Alu {


    /**
     * Enumération représentant les fanions produits par les opérations de l'ALU
     */
    public enum Flag implements Bit {
        UNUSED_0,
        UNUSED_1,
        UNUSED_2,
        UNUSED_3,
        C,
        H,
        N,
        Z
    }

    /**
     * Enumeration utilisée pour décrire la direction d'une rotation effectuée par l'ALU
     */
    public enum RotDir implements Bit {
        LEFT,
        RIGHT
    }

    /**
     * Constructeur privé afin que la classe soit non-instantiable
     */
    private Alu() {
    }

    /**
     * Crée un fanion ZNHC selon les arguments reçus
     *
     * @param z : valeur du fanion z
     * @param n : valeur du fanion n
     * @param h : valeur du fanion h
     * @param c : valeur du fanion c
     * @return valeur dont les bits correspondants aux différents fanions valent 1 ssi l'argument correspondant est vrai
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int maskZNHC = 0;

        if (z) maskZNHC += Flag.Z.mask();
        if (n) maskZNHC += Flag.N.mask();
        if (h) maskZNHC += Flag.H.mask();
        if (c) maskZNHC += Flag.C.mask();

        return maskZNHC;
    }

    /**
     * Crée un "entier" composé d'une valeur 8 ou 16 bits et de 8 bits (4LSB : 0, 4MSB : fanions)
     *
     * @param value : entier résultant d'une opération
     * @param z     : fanion z
     * @param n     : fanion n
     * @param h     : fanion h
     * @param c     : fanion c
     * @return
     */
    public static int packValueAndFlag(int value, boolean z, boolean n, boolean h, boolean c) {

        return (value << 8) + maskZNHC(z, n, h, c);
    }

    /**
     * Extrait d'un "entier" contenant le résultat d'une opération de l'ALU, la valeur calculée
     *
     * @param valueFlags : paquet valeur/fanion
     * @return valeur contenue dans le paquet valeur/fanion donné
     */
    public static int unpackValue(int valueFlags) {

        return Bits.extract(valueFlags, 8, 23);
    }

    /**
     * Extrait d'un "entier" contenant le résultat d'une opération de l'ALU, les fanions calculés
     *
     * @param valueFlags : paquet valeur/fanion
     * @return fanions contenus dans le paquet valeur/fanion donné
     ***/
    public static int unpackFlags(int valueFlags) {
        return Bits.clip(8, valueFlags) >>> 4 << 4;

    }

    /**
     * Calcul de la somme et des fanions Z0HC
     *
     * @param l  : première valeur 8bits
     * @param r  : deuxième valeur 8bits
     * @param c0 : retenue
     * @return somme des valeurs, retenue initial c0 et fanions Z0HC
     */
    public static int add(int l, int r, boolean c0) {
        checkBits8(l);
        checkBits8(r);

        int result = l + r + booleanToInteger(c0);
        boolean cFlag = result > 0xFF;
        result -= result > 0xFF ? 0x100 : 0;
        boolean zFlag = result == 0;
        boolean hFlag = Bits.clip(4, l) + Bits.clip(4, r) + booleanToInteger(c0) > 0xF;

        return packValueAndFlag(result, zFlag, false, hFlag, cFlag);

    }

    /**
     * Identique à la méthode add préceédente, sans retenue et avec fanions Z0HC
     *
     * @param l : première valeur 8bits à additionner
     * @param r : deuxième valeur 8bits à additionner
     * @return somme des deux valeurs passées en argument
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }

    /**
     * Somme des valeurs et fanions 00HC (addition des 8bits de poids faibles)
     *
     * @param l : valeur 16bits
     * @param r : valeur 16bits
     * @return somme des valeurs et fanions
     */
    public static int add16L(int l, int r) {

        checkBits16(l);
        checkBits16(r);

        int l_8low = Bits.clip(8, l);
        int r_8low = Bits.clip(8, r);
        int l_4low = Bits.clip(4, l);
        int r_4low = Bits.clip(4, r);

        boolean h = (l_4low + r_4low > 0xF);
        boolean c = (l_8low + r_8low > 0xFF);

        return packValueAndFlag(Bits.clip(16, l + r), false, false, h, c);
    }

    /**
     * Similaire à add16L sauf fanions, ici 00HC fanions de l'addition de poids fort
     *
     * @param l : valeur 16bits
     * @param r : valeur 16bits
     * @return somme des valeurs et fanions
     */
    public static int add16H(int l, int r) {
        checkBits16(l);
        checkBits16(r);

        int l_8high = Bits.extract(l, 8, 8);
        int r_8high = Bits.extract(r, 8, 8);
        int l_4high = Bits.extract(l, 8, 4);
        int r_4high = Bits.extract(r, 8, 4);
        //Controle si l'addition des 4 bits les moins significants génère une retenue
        boolean carryOfFirstHalfAddition = (Bits.extract(l, 0, 8) + Bits.extract(r, 0, 8)) > 0xFF;

        boolean h = (l_4high + r_4high + booleanToInteger(carryOfFirstHalfAddition) > 0xF);
        boolean c = (l_8high + r_8high + booleanToInteger(carryOfFirstHalfAddition) > 0xFF);

        return packValueAndFlag(l + r - (l + r > 0xFFFF ? 0x10000 : 0), false, false, h, c);
    }

    /**
     * Soustraction avec 'borrow' si b0 est true
     *
     * @param l  : première valeur 8 bits
     * @param r  : deuxième valeur 8 bits
     * @param b0 : borrow (= "carry" de la soustraction)
     * @return différence entre les valeurs et fanions Z1HC
     */
    public static int sub(int l, int r, boolean b0) {

        checkBits8(l);
        checkBits8(r);

        int result = l - r - booleanToInteger(b0);

        boolean cFlag = Bits.test(result, 8);

        result = Bits.clip(8, result);
        boolean zFlag = result == 0;

        boolean hFlag = Bits.test(Bits.clip(4, l) - Bits.clip(4, r) - (b0 ? 1 : 0), 4);

        return packValueAndFlag(result, zFlag, true, hFlag, cFlag);
    }

    /**
     * Méthode analogue à précédente, sans 'borrow'
     *
     * @param l : première valeur 8 bits
     * @param r : deuxième valeur 8 bits
     * @return différence entre les l et r
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    /**
     * ajuste la valeur 8 bits afin qu'elle soit au format DCB
     *
     * @param v : valeur 8bits
     * @param n : fanion n
     * @param h : fanion h
     * @param c : fanion c
     * @return valeur 8 bits ajustée au format DCB
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {

        checkBits8(v);

        boolean fixL = h || (!n && (Bits.clip(4, v) > 9));
        boolean fixH = c || (!n && (v > 0x99));

        int fix = 0x60 * booleanToInteger(fixH) + 0x06 * booleanToInteger(fixL);

        int va = Bits.clip(8, v + (n ? -fix : fix));

        return packValueAndFlag(va, va == 0, n, false, fixH);

    }

    /**
     * Opération "et" bit à bit
     *
     * @param l : première valeur 8 bits
     * @param r : deuxième valeur 8 bits
     * @return opération bit à bit et fanion Z010
     */
    public static int and(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        int result = l & r;
        boolean zFlag = result == 0;
        return packValueAndFlag(result, zFlag, false, true, false);
    }

    /**
     * Opération "ou inclusif" bit à bit
     *
     * @param l : première valeur 8 bits
     * @param r : deuxième valeur 8 bits
     * @return opération bit à bit et fanion Z000
     */
    public static int or(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        int result = l | r;
        boolean zFlag = result == 0;
        return packValueAndFlag(result, zFlag, false, false, false);
    }

    /**
     * Opération "ou exclusif" bit à bit
     *
     * @param l : première valeur 8 bits
     * @param r : deuxième valeur 8 bits
     * @return opération bit à bit et fanions Z000
     */
    public static int xor(int l, int r) {

        checkBits8(l);
        checkBits8(r);

        int result = l ^ r;
        boolean zFlag = result == 0;
        return packValueAndFlag(result, zFlag, false, false, false);
    }

    /**
     * Décale la valeur d'un bit vers la gauche
     *
     * @param v : valeur 8 bits à décaler
     * @return valeur 8 bits décalée et fanions Z00C (C = bit éjecté par le décalage)
     */
    public static int shiftLeft(int v) {

        checkBits8(v);

        boolean cFlag = Bits.test(v, 7);
        v = Bits.clip(8, v << 1);
        boolean zFlag = v == 0;

        return packValueAndFlag(v, zFlag, false, false, cFlag);
    }

    /**
     * Décale la valeur d'un bit vers la droite (décalage arithmétique)
     *
     * @param v : valeur 8 bits
     * @return valeur décalée et fanions Z00C (C = bit éjecté par le décalage)
     */
    public static int shiftRightA(int v) {

        checkBits8(v);

        boolean cFlag = Bits.test(v, 0);
        v = v >> 1;
        v = Bits.set(v, 7, Bits.test(v, 6));
        boolean zFlag = v == 0;


        return packValueAndFlag(v, zFlag, false, false, cFlag);
    }

    /**
     * Décale la valeur d'un bit vers la droite (décalage logique)
     *
     * @param v : valeur 8 bits
     * @return : valeur décalée et fanions Z00C (C = bit éjecté par le décalage)
     */
    public static int shiftRightL(int v) {

        checkBits8(v);

        boolean cFlag = Bits.test(v, 0);
        v = v >> 1;
        boolean zFlag = v == 0;


        return packValueAndFlag(v, zFlag, false, false, cFlag);
    }

    /**
     * Rotation de 'v' d'un bit
     *
     * @param d : direction de la rotation
     * @param v : valeur 8 bits
     * @return rotation et fanions Z00C (C = bit passé d'une extrémité à l'autre)
     */
    public static int rotate(RotDir d, int v) {

        checkBits8(v);
        checkArgument(d != null);

        int rotateResult;
        boolean cFlag;

        if (d == RotDir.LEFT) {
            cFlag = Bits.test(v, 7);
            rotateResult = Bits.rotate(8, v, 1);

        } else {
            cFlag = Bits.test(v, 0);
            rotateResult = Bits.rotate(8, v, -1);

        }

        boolean zFlag = (v == 0);

        return packValueAndFlag(rotateResult, zFlag, false, false, cFlag);

    }

    /**
     * Rotation à travers la retenue de la combinaison valeur 8 bits et retenue
     *
     * @param d : direction de la rotation
     * @param v : valeur 8 bits à traiter
     * @param c : retenue
     * @return valeur 8 bits à partir de 'v' et 'c' et le fanion C comme nouvelle retenue (MSB)
     */
    public static int rotate(RotDir d, int v, boolean c) {
        checkBits8(v);
        checkArgument(d != null);

        boolean cFlag;
        boolean zFlag;
        if (d == RotDir.LEFT) {
            cFlag = Bits.test(v, 7);
            v = unpackValue(rotate(RotDir.LEFT, v));
            v = Bits.set(v, 0, c);

        } else {
            cFlag = Bits.test(v, 0);
            v = unpackValue(rotate(RotDir.RIGHT, v));
            v = Bits.set(v, 7, c);

        }
        zFlag = (v == 0);

        return packValueAndFlag(v, zFlag, false, false, cFlag);
    }

    /**
     * Echange les 4 bits LSB et les 4 MSB
     *
     * @param v : valeur 8 bits
     * @return valeur echangée et fanion Z000
     */
    public static int swap(int v) {

        checkBits8(v);
        v = Bits.rotate(8, v, 4);
        boolean z = v == 0;

        return packValueAndFlag(v, z, false, false, false);
    }

    /**
     * Vérifie que le bit à l'index donné vaille 1
     *
     * @param v        : valeur 8 bits
     * @param bitIndex : index à vérifier
     * @return 0 et fanions Z010 (Z = 1 ssi bit à l'index vaut 0)
     * @throws IndexOutOfBoundsException si l'index est négatif ou supérieur à 7
     */
    public static int testBit(int v, int bitIndex) {

        checkBits8(v);
        if (bitIndex < 0 || bitIndex > 7)
            throw new IndexOutOfBoundsException("Invalid index value - testBit()");

        return packValueAndFlag(0, !Bits.test(v, bitIndex), false, true, false);

    }

    private static boolean binaryToBoolean(int b) {

        if (b != 0 && b != 1) {
            throw new IllegalArgumentException("Not a single binary value");
        }

        return b == 1;
    }

    /**
     * Convertit un boolean en entier: 1 si true, 0 si false
     *
     * @param b : le boolean passé en argument
     * @return valeur entière du boolean (0 ou 1)
     */
    private static int booleanToInteger(boolean b) {

        return b ? 1 : 0;

    }
}
