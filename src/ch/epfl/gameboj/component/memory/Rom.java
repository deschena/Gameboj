package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.util.Objects;

/**
 * Classe représentant une mémoire morte
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class Rom {

    private final byte[] data;

    /**
     * Construit une Rom avec le tableau d'octet donné
     *
     * @param data : Tableau d'octets, contenu de la ROM
     */
    public Rom(byte[] data) {

        Objects.requireNonNull(data);

        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Getter de la taille de la ROM
     * @return taille de la ROM
     */
    public int size() {
        return data.length;
    }

    /**
     * Lit un octet à un index donné
     * @param index : index de l'octet de la ROM à lire
     * @return contenu de la ROM à l'index passé en argument
     */
    public int read(int index) {
        return Byte.toUnsignedInt(data[index]);
    }
}
