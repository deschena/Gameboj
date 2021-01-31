package ch.epfl.gameboj;

/**
 * Interface offrant des méthodes auxilliaires permettant de vérifier la validité des arguments reçus par les différentes méthodes du projet
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public interface Preconditions {

    /**
     * @param b : expression booléenne censée être 'true'
     */
    static void checkArgument(boolean b) {
        if (!b) {
            throw new IllegalArgumentException("Bad argument used");
        }
    }

    /**
     * @param v : entier censé être dans l'interval [0x00 ; 0xFF]
     * @return l'entier reçu (v)
     */
    static int checkBits8(int v) {

        checkArgument(v <= 0xFF);
        checkArgument(v >= 0);
        return v;
    }

    /**
     * @param v : entier à censé être dans l'interval [0x0000 ; 0xFFFF]
     * @return l'entier reçu (v)
     */
    static int checkBits16(int v) {

        checkArgument(v >= 0);
        checkArgument(v <= 0xFFFF);

        return v;
    }
}
