package ch.epfl.gameboj.component.memory;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;

/**
 * Classe représentant une mémoire vive
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class Ram {

    private final byte[] data;

    /**
     * Construit une Ram de taille donnée
     *
     * @param size : taille de la RAM (tableau), défini une seule fois
     * @throws IllegalArgumentException : si size est une valeur négative
     */
    public Ram(int size) {
        checkArgument(size >= 0);
        data = new byte[size];
    }

    /**
     * Lit un octet à une adresse donnée
     * @param index : index de l'octet à lire
     * @return l'octet non signé à lire
     * @throws IndexOutOfBoundsException : si l'indice n'existe pas dans la RAM
     */
    public int read(int index) {

        return Byte.toUnsignedInt(data[index]);
    }

    /**
     * Ecrit un octet à une adresse donnée
     * @param index : index de l'octet à écrire
     * @param value : valeur à attribuer à l'octet spécifié
     * @throws IllegalArgumentException  : si value n'est pas une valeur 8 bits
     * @throws IndexOutOfBoundsException : si l'indice n'exite pas dans la RAM
     */
    public void write(int index, int value) {
        checkBits8(value);
        data[index] = (byte) value;
    }

    /**
     * Getter de la taille de la RAM
     * @return la taille de la RAM
     */
    public int size() {
        return data.length;
    }

}
