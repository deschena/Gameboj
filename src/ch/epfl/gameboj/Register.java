package ch.epfl.gameboj;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public interface Register {

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


}
