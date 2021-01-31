package ch.epfl.gameboj.component.cartridge;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits16;

/**
 * Contrôleur de ROM pour les cartouches de jeu simples
 *
 * @author Armen Homberger(154511)
 * @author Justin Deschenaux(288424)
 */
public final class MBC0 implements Component {

    private final Rom rom;
    private static final int SIMPLE_CARTRIDGE_SIZE = 0x8000;

    /**
     * Construit un MBC0 avec la rom donnée
     *
     * @param rom rom donnée
     * @throws NullPointerException si la rom donnée est "null"
     */
    public MBC0(Rom rom) {

        if (rom == null) {
            throw new NullPointerException("Given rom object is null");
        }
        checkArgument(rom.size() == SIMPLE_CARTRIDGE_SIZE);

        this.rom = rom;


    }

    /**
     * méthode de lecture de la rom
     *
     * @param address : adresse de l'octet à retourner
     * @return l'octet lu, ou NO_DATA
     */
    @Override
    public int read(int address) {
        checkBits16(address);

        if (address < rom.size()) {
            return rom.read(address);
        } else {
            return NO_DATA;
        }
    }

    /**
     * méthode d'écriture de la rom, n'écrit évidemment rien
     *
     * @param address : adresse de l'octet à écrire
     * @param data    : octet à écrire
     */
    @Override
    public void write(int address, int data) {
    }

}
