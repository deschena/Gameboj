package ch.epfl.gameboj.component;

/**
 * interface implémentée par les composants simulant la notion de cycle d'horloge
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */

public interface Clocked {


    /**
     * demande au composant d'évoluer en exécutant toutes les opérations qu'il doit exécuter durant le cycle d'index donné en argument
     *
     * @param cycle : cycle d'index
     * @return pas de retour
     */

    public abstract void cycle(long cycle);

}
