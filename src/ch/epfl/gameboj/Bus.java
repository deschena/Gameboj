package ch.epfl.gameboj;

import ch.epfl.gameboj.component.Component;

import java.util.ArrayList;
import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

/**
 * Classe représentant un bus
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class Bus {
    private final ArrayList<Component> components;

    /**
     * Constructeur par défaut de la class Bus
     */
    public Bus() {
        components = new ArrayList<>();
    }

    /**
     * Attache le composant donné au bus
     *
     * @param component composant à attacher au bus
     */
    public void attach(Component component) {
        Objects.requireNonNull(component);
        components.add(component);
    }

    /**
     * Lit la valeur contenue à l'adresse donnée ou lève une exception
     *
     * @param address adresse donnée
     * @return valeur contenue à l'adresse par un des composant, ou 0xFF sinon
     * @throws IllegalArgumentException si l'adresse n'est pas au format 16bits
     */
    public int read(int address) {
        checkBits16(address);

        for (Component c : components) {
            int data = c.read(address);

            if (data != Component.NO_DATA)
                return data;
        }
        return 0xFF;
    }

    /**
     * @param address : adresse ou écrire data (dans tous les composants connectés au bus)
     * @param data    : octet de donnée à écrire dans la mémoire des composants connectés
     * @throws IllegalArgumentException si address n'est pas une valeur 16bits ou data une valeur 8bits
     */
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        for(Component c : components)
            c.write(address,data);
    }
}
