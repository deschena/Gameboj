package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;

/**
 * interface contenant les méthodes que les composants du GameBoy doivent offrir
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */

public interface Component {
    /**
     * constante retournée en cas d'absence de donnée
     */
    public static final int NO_DATA = 0x100;

    /**
     * méthode de lecture d'octet
     *
     * @param address : adresse de l'octet à retourner
     * @return l'octet lu, sous forme d'entier
     */
    public abstract int read(int address);

    /**
     * méthode d'écriture d'octet
     *
     * @param address : adresse de l'octet à écrire
     * @param data    : octet à écrire
     * @throws IllegalArgumentException, si address n'est pas
     *                                   une valeur 16 bits, ou data une valeur 8 bits
     */
    public abstract void write(int address, int data);

    /**
     * méthode permettant d'attacher le component à un bus de données
     *
     * @param bus : bus auquel est attaché le component
     */
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }

}
