package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

import java.util.Objects;

import static ch.epfl.gameboj.AddressMap.BOOT_ROM_END;
import static ch.epfl.gameboj.AddressMap.REG_BOOT_ROM_DISABLE;
import static ch.epfl.gameboj.Preconditions.*;

/**
 * Classe représentant un contrôleur de mémoire de démarrage
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class BootRomController implements Component {

    private final Cartridge cartridge;
    private boolean alreadyBooted;
    private final Rom bootRom;

    /**
     * Construit un un contrôleur de mémoire de démarrage auquel la cartouche donnée est attachée
     *
     * @param cartridge cartouche à attacher au BootRomController
     * @throws NullPointerException si la cartouche donnée est "null"
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);

        this.cartridge = cartridge;
        bootRom = new Rom(BootRom.DATA);

    }

    /**
     * méthode read tant que alreadyBooted est faux (mémoire de démarrage non désactivée)
     *
     * @param address : adresse de l'octet à retourner
     * @return l'octet stocké dans la mémoire de démarrage ou l'octet retourné par la méthode read de la cartouche
     */
    @Override
    public int read(int address) {

        checkBits16(address);

        if (!alreadyBooted && address < BOOT_ROM_END) {

            return bootRom.read(address);
        } else {
            return cartridge.read(address);
        }
    }

    /**
     * méthode write désactive la mémoire de démarrage si écriture à l'adresse FF50 (change alreadyBooted à true). Les autres écritures appellent la méthode write de la cartouche
     *
     * @param address : adresse de l'octet à écrire
     * @param data    : octet à écrire
     */
    @Override
    public void write(int address, int data) {
        checkBits16(16);
        checkBits8(data);

        if (address == REG_BOOT_ROM_DISABLE) {
            alreadyBooted = true;
        } else {
            cartridge.write(address, data);
        }
    }

}
