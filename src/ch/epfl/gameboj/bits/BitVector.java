package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class BitVector {

    private final int[] vector;

    /**
     * constructeur privé
     *
     * @param elements tableau contenant les éléments du vecteur
     */
    private BitVector(int[] elements) {
        vector = elements;
    }


    /**
     * construit un vecteur de bits de la taille donnée, dont tous les bits ont la valeur initiale donnée
     *
     * @param size taille en bits
     * @param init valeur initiale
     */
    public BitVector(int size, boolean init) {
        this(construct(size, init));
    }

    /**
     * construit un vecteur de bits de la taille donnée, dont tous les bits sont à zéro
     *
     * @param size taille en bits
     */
    public BitVector(int size) {
        this(size, false);
    }

    /**
     * Méthode auxiliaire de construction qui crée un tableau et valide les arguments
     *
     * @param size taille du tableau
     * @param init valeur de remplissage
     * @return
     */
    private static int[] construct(int size, boolean init) {
        checkArgument(size > 0);
        checkArgument(size % Integer.SIZE == 0);

        int[] elements = new int[size / Integer.SIZE];
        Arrays.fill(elements, (init ? 0xFFFF_FFFF : 0));

        return elements;
    }


    /**
     * @return taille du vecteur de bits
     */
    public int size() {
        return (vector.length) * Integer.SIZE;
    }

    /**
     * Détermine si le bit d'index donné est vrai ou faux
     *
     * @param index index donné
     * @return bit à 1 : vrai ou bit à 0 : faux
     */
    public boolean testBit(int index) {
        checkArgument(index >= 0 && index < size());

        int arrayIndex = index / Integer.SIZE;
        int localIndex = index % Integer.SIZE;

        return Bits.test(vector[arrayIndex], localIndex);
    }

    /**
     * Calcule le complément du vecteur de bits
     *
     * @return complément du vecteur de bits
     */
    public BitVector not() {

        int[] newContent = new int[vector.length];
        UnaryOperator<Integer> not = x -> ~x;

        applyOnVector(not, newContent, vector);
        return new BitVector(newContent);
    }

    /**
     * Calcule la conjonction bit à bit avec un autre vecteur de même taille
     *
     * @param vect vecteur de bits à comparer
     * @return conjonction bit à bit
     */
    public BitVector and(BitVector vect) {

        checkArgument(vect != null);
        checkArgument(vect.size() == this.size());


        int conjunction[] = new int[vector.length];
        BinaryOperator<Integer> and = (x, y) -> x & y;
        applyOnVector(and, conjunction, vector, vect.vector);

        return new BitVector(conjunction);
    }

    /**
     * Calcule la disjonction bit à bit avec un autre vecteur de même taille
     *
     * @param vect vecteur de bits à comparer
     * @return disjonction bit à bit
     */
    public BitVector or(BitVector vect) {

        checkArgument(vect != null);
        checkArgument(vect.size() == this.size());


        int disjunction[] = new int[vector.length];
        BinaryOperator<Integer> or = (x, y) -> x | y;
        applyOnVector(or, disjunction, vect.vector, vector);

        return new BitVector(disjunction);
    }


    /**
     * extrait un vecteur de taille donnée de l'extension par 0 du vecteur
     *
     * @param start index de départ
     * @param size  taille donnée
     * @return vecteur de taille donnée de l'extension par 0 du vecteur
     */
    public BitVector extractZeroExtended(int start, int size) {
        checkArgument(size > 0);
        checkArgument(size % Integer.SIZE == 0);

        int[] newVector = new int[(int) Math.ceil((double) size / (double) Integer.SIZE)];
        int current = start;
        for (int i = 0; i < newVector.length; i++) {
            newVector[i] = extract(current, false);
            current += Integer.SIZE;
        }
        return new BitVector(newVector);
    }

    /**
     * Extrait un vecteur de taille donnée de l'extension par enroulement du vecteur courrant
     *
     * @param start indice de départ du vecteur à retourner
     * @param size  taille du vecteur à retourner
     * @return nouveau vecteur étendu par enroulement du vecteur original (this)
     */
    public BitVector extractWrapped(int start, int size) {
        checkArgument(size > 0);
        checkArgument(size % Integer.SIZE == 0);

        int[] newVector = new int[(int) Math.ceil((double) size / (double) Integer.SIZE)];
        int current = start;
        for (int i = 0; i < newVector.length; i++) {
            newVector[i] = extract(current, true);
            current += Integer.SIZE;
        }
        return new BitVector(newVector);
    }

    /**
     * Effectue un décallage sur un vecteur de bit
     * 'shift' négatif : décallage à droite
     * 'shift' positive : décallage à gauche
     *
     * @param shift distance et direction du décallage
     * @return nouveau vecteur, décallé
     */
    public BitVector shift(int shift) {

        return extractZeroExtended(-shift, size());
    }

    /**
     * Permet de récupérer une valeur de 32 bits du vecteur depuis l'indice passé en argument
     *
     * @param i       indice de départ à récupérer
     * @param wrapped détermine si on utilise un enroulement ou pas
     * @return valeur 32 bits depuis l'indice passé
     */
    private int extract(int i, boolean wrapped) {

        if (Math.floorMod(i, Integer.SIZE) == 0) {
            int arrayIndex = i / Integer.SIZE;
            //Récupère la valeur contenue à l'indice en tenant compte de l'enroulement si nécessaire
            return getLSBs(Integer.SIZE, arrayIndex, wrapped);

        } else {
            int arrayIndex = Math.floorDiv(i, Integer.SIZE);
            /**
             int LSBLength = Integer.SIZE - Math.floorMod(i, Integer.SIZE);
             int MSBLength = Integer.SIZE - LSBLength;
             */
            int LSBLength = Integer.SIZE - Math.floorMod(i, Integer.SIZE);
            int MSBLength = Integer.SIZE - LSBLength;

            int LSB = getMSBs(LSBLength, arrayIndex, wrapped);
            int MSB = getLSBs(MSBLength, arrayIndex + 1, wrapped);

            return (MSB << LSBLength) + LSB;
        }
    }

    /**
     * Récupère les 'size' bits de poids fort d'un entier
     *
     * @param size       taille en bits de la valeur à récupérer
     * @param arrayIndex indice de stockage de la valeur
     * @param wrapped    détermine si on utilise un enroulement ou pas
     * @return bits de poids fort
     */
    private int getMSBs(int size, int arrayIndex, boolean wrapped) {
        //Utilisation de getMSB au lieu de getBits afin d'éviter la copie de code
        return getBits(size, arrayIndex, true, wrapped);
    }

    /**
     * Récupère les 'size' bits de poids faible d'un entier
     *
     * @param size       taille en bits de la valeur à récupérer
     * @param arrayIndex indice de stockage de la valeur
     * @param wrapped    détermine si on utilise un enroulement ou pas
     * @return bits de poids faible
     */
    private int getLSBs(int size, int arrayIndex, boolean wrapped) {
        //Utilisation de getLSB au lieu de getBits afin d'avoir un code plus clair à l'utilisation
        return getBits(size, arrayIndex, false, wrapped);
    }

    /**
     * Méthode auxiliaire pour éviter la duplication inutile de code
     *
     * @param size       taille en bits de la valeur à récupérer
     * @param arrayIndex indice de stockage de la valeur
     * @param MSBs       détermine si on veut récupérer les bits de poids fort ou de poids faible
     * @param wrapped    détermine si on utilise un enroulement ou pas
     * @return bits de poids forts ou faibles
     */
    private int getBits(int size, int arrayIndex, boolean MSBs, boolean wrapped) {

        checkArgument(size > 0 && size <= Integer.SIZE);
        int startIndex;

        if (MSBs) startIndex = Integer.SIZE - size;
        else startIndex = 0;

        if (wrapped) {
            arrayIndex = Math.floorMod(arrayIndex, vector.length);
            return Bits.extract(vector[arrayIndex], startIndex, size);

        } else {

            if (!(arrayIndex < 0 || arrayIndex >= vector.length))
                return Bits.extract(vector[arrayIndex], startIndex, size);
            else return 0;
        }
    }

    /**
     * Méthode de transformation d'un tableau de valeurs
     * @param f opérateur unaire, dont la fonction apply est à redéfinir (lambda)
     * @param newValues tableau de nouvelles valeurs (après apply)
     * @param oldValues tableau de valeurs à transformer
     */
    private void applyOnVector(UnaryOperator<Integer> f, int[] newValues, int[] oldValues) {

        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = f.apply(oldValues[i]);
        }
    }

    /**
     * Méthode de transformation de deux tableaux de valeurs
     * @param f opérateur binaire, dont la fonction apply est à redéfinir (lambda)
     * @param newValues tableau de nouvelles valeurs (apply)
     * @param oldValues0 tableau de valeurs à transformer
     * @param oldValues1 tableau de valeurs à transformer
     */
    private void applyOnVector(BinaryOperator<Integer> f, int[] newValues, int[] oldValues0, int[] oldValues1) {
        for (int i = 0; i < newValues.length; i++) {
            newValues[i] = f.apply(oldValues0[i], oldValues1[i]);
        }
    }

    /**
     * Redéfinition de la méthode toString
     *
     * @return une représentation du vecteur sous la forme d'une chaîne composée uniquement de caractères 0 et 1
     */
    public String toString() {
        StringBuilder b = new StringBuilder();
        for (int i = (vector.length * Integer.SIZE) - 1; i >= 0; --i) {
            char c = this.testBit(i) ? '1' : '0';
            b.append(c);
        }
        return b.toString();
    }

    /**
     * redéfinition de la méthode equals
     * @param o objet à comparer avec this
     * @return true si identique, false sinon
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BitVector)) {
            return false;
        }
        return (Arrays.equals(this.vector, ((BitVector) o).vector));
    }

    /**
     * redéfinition de la méthode hashCode
     * @return la valeur de hachage de l'instance
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(vector);
    }


    public static final class Builder {

        private int[] vector;
        private final int BYTES_PER_INT = 4;

        /**
         * Construit un Builder
         *
         * @param size taille du tableau représentant le vecteur de bits
         */
        public Builder(int size) {
            checkArgument(size > 0);
            checkArgument(size % Integer.SIZE == 0);
            vector = new int[size / Integer.SIZE];
        }

        /**
         * Définit la valeur d'un octet désigné par son index
         *
         * @param byteIndex index de l'octet dans le vecteur de bits
         * @param value     valeur à assigner à l'octet
         * @return le Builder en cours de construction
         */
        public Builder setByte(int byteIndex, int value) {

            checkBits8(value);
            if (vector == null) {
                throw new IllegalStateException();
            }
            if (byteIndex < 0 || byteIndex >= vector.length * (Integer.SIZE / Byte.SIZE)) {
                throw new IndexOutOfBoundsException();
            }

            int arrayIndex = byteIndex / BYTES_PER_INT;
            int byteShift = (byteIndex % ((BYTES_PER_INT * arrayIndex) == 0 ? BYTES_PER_INT : BYTES_PER_INT * arrayIndex)) * Byte.SIZE;
            int zeroMask = ~(0xFF << byteShift);

            // met à zéro les bits du byte à modifier
            vector[arrayIndex] = vector[arrayIndex] & zeroMask;

            // "aligne" la valeur reçue en argument sur la position du byte à modifier
            int setMask = value << byteShift;
            vector[arrayIndex] = vector[arrayIndex] | setMask;

            return this;
        }

        /**
         * Construit le vecteur de bit en appelant le constructeur privé
         *
         * @return le vecteur de bit construit
         */
        public BitVector build() {
            if (vector == null)
                throw new IllegalStateException();

            BitVector v = new BitVector(vector);
            vector = null;
            return v;
        }
    }
}


