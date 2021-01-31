package ch.epfl.gameboj.bits;

/**
 * Interface implémentée par des énumérations dans le but d'offir des méthodes supplémentaires
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public interface Bit {

    /**
     * Méthode fournie par l'énumération qui représente les bits
     *
     * @return position d'un élément dans l'objet 'enum' implémentant l'interface
     */
    int ordinal();

    /**
     * @return ordinal(), avec un nom plus commun
     */
    default int index() {
        return this.ordinal();
    }

    /**
     * @return masque associé à la position d'une élément dans l'objet 'enum' implémentant l'interface
     */
    default int mask() {
        return Bits.mask(index());
    }
}
