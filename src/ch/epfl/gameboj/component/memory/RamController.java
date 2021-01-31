package ch.epfl.gameboj.component.memory;

import ch.epfl.gameboj.component.Component;

import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.*;

/**
 * Classe du contrôlleur de mémoire vive
 *
 * @author Armen Homberger(154511)
 * @author Justin Deschenaux(288424)
 */
public final class RamController implements Component {

    private final int startAdress;
    private final int endAdress;
    private final Ram ram;

    /**
     * construit un RamController avec une mémoire vive, et des adresses de départ et de fin
     *
     * @param ram          : mémoire vive donnée
     * @param startAddress : adresse de départ
     * @param endAddress   : adresse de fin
     * @throws IllegalArgumentException : si startAddress n'est pas une value 16 bits
     * @throws IllegalArgumentException : si endAdress n'est pas une valeur 16 bits
     * @throws IllegalArgumentException : si endAddress est plus grande ou égale à startAdress
     * @throws IllegalArgumentException : si la taille de la ram est supérieure à la plage entre startAddress et endAddress
     * @throws NullPointerException     : si  ram est null
     */
    public RamController(Ram ram, int startAddress, int endAddress) {


        checkArgument(endAddress >= startAddress);
        checkArgument(ram.size() >= endAddress - startAddress);


        this.ram = Objects.requireNonNull(ram);
        this.startAdress = checkBits16(startAddress);
        this.endAdress = checkBits16(endAddress);


    }

    /**
     * Construit un RamController avec une ram et une adresse de départ
     *
     * @param ram          : mémoire vive donnée
     * @param startAdress: adresse de départ
     */
    public RamController(Ram ram, int startAdress) {
        this(ram, startAdress, startAdress + ram.size());
    }


    /**
     * méthode de lecture d'octet
     *
     * @param address : adresse de l'octet à retourner
     * @return l'octet lu, sous forme d'entier
     * @throws IllegalArgumentException : si address n'est pas une valeur 16 bits
     */
    public int read(int address) {
        checkBits16(address);
        if (address < startAdress || address >= endAdress) {
            return NO_DATA;
        }
        return ram.read(address - startAdress);
    }

    /**
     * méthode d'écriture d'octet
     *
     * @param address : adresse de l'octet à écrire
     * @param data    : octet à écrire
     * @throws IllegalArgumentException :   si address n'est pas une valeur 16 bits ou
     * @throws IllegalArgumentException : si data n'est pas une valeur 8 bits
     */
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);
        if (address < startAdress || address >= endAdress) {
            return;
        }
        ram.write(address - startAdress, data);
    }


}
