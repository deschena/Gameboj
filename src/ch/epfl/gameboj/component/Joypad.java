package ch.epfl.gameboj.component;

import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

import java.util.Objects;

import static ch.epfl.gameboj.AddressMap.REG_P1;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import static ch.epfl.gameboj.bits.Bits.complement8;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class Joypad implements Component {

    private final Cpu belongingCpu;
    private int P1;
    private int line0;
    private int line1;

    private final int DEFAULT = 0b0000_0000;
    private final int P1MASK = 0b1111_0000;

    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }


    /**
     * Constructeur de Joypad
     * @param cpu le cpu reçu en argument
     */
    public Joypad (Cpu cpu) {
        Objects.requireNonNull(cpu);
        belongingCpu = cpu;
        P1 = DEFAULT;
    }

    /**
     * Méthode donnant accès au registre P1
     * @param address : adresse de l'octet à retourner
     * @return l'octet contenu dans le registre P1
     */
    @Override
    public int read(int address) {
        checkBits16(address);

        if(address == REG_P1) {
            return complement8(P1);
        } else {
            return NO_DATA;
        }
    }

    /**
     * Méthode écrivant dans le registre P1, avec les 4 bits de poids faible en lecture seule
     * @param address : adresse de l'octet à écrire
     * @param data    : octet à écrire
     */
    @Override
    public void write(int address, int data) {
        checkBits16(address);
        checkBits8(data);

        if(address == REG_P1) {
            int oldP1 = P1;
            int weakFour = 0b1111 & P1;
            int strongFour = 0b1111_0000 & Bits.clip(6, complement8(data)); //data clippée pour que les 2 MSB restent inchangés à 1
            P1 = strongFour | weakFour;
            computeP1();
            checkInterrupt(oldP1);
        }

    }


    /**
     * méthode simulant la pression d'une touche, met à jour line0, line1 et P1
     * @param key la touche pressée
     */
    public void keyPressed(Key key) {

        int oldP1 = P1;
        int index = key.ordinal();
        if (index < 4) {
            line0 = Bits.set(line0, index, true);
        } else {
            line1 = Bits.set(line1, index % 4, true);
        }
        computeP1();
        checkInterrupt(oldP1);
    }


    /**
     * méthode simulant le relâchement d'une touche, met à jour line0, line1 et P1
     * @param key la touche relâchée
     */
    public void keyReleased(Key key) {
        int oldP1 = P1;
        int index = key.ordinal();
        if (index < 4) {
            line0 = Bits.set(line0, index, false);
        } else {
            line1 = Bits.set(line1, index % 4, false);
        }
        computeP1();
        checkInterrupt(oldP1);
    }


    /**
     * méthode auxiliaire permettant de calculer la valeur de P1 en fonction des attributs de ligne (touches pressées
     * ou relâchées)
     */
    private void computeP1() {
        if(!Bits.test(P1, 4) && !Bits.test(P1, 5))
            P1 = DEFAULT;
        if(Bits.test(P1, 4) && !Bits.test(P1, 5))
            P1 = line0 | (P1 & P1MASK);
        if(!Bits.test(P1, 4) && Bits.test(P1, 5))
            P1 = line1 | (P1 & P1MASK);
        if(Bits.test(P1, 4) && Bits.test(P1, 5))
            P1 = line0 | line1 | (P1 & P1MASK);
    }

    /**
     * méthode auxiliaire qui vérifie le passage des bits d'état de colonne de 0 à 1
     * @param oldP1 ancienne valeur de P1, à comparer avec la nouvelle valeur
     */
    private void checkInterrupt(int oldP1) {

        int oldStatus = Bits.clip(4, oldP1);
        int newStatus = Bits.clip(4, P1);

        if(!Bits.test(oldStatus, 0) && (Bits.test(newStatus, 0))
                || !Bits.test(oldStatus, 1) && (Bits.test(newStatus, 1))
                || !Bits.test(oldStatus, 2) && (Bits.test(newStatus, 2))
                || !Bits.test(oldStatus, 3) && (Bits.test(newStatus, 3))) {
            belongingCpu.requestInterrupt(Cpu.Interrupt.JOYPAD);
        }

    }

}
