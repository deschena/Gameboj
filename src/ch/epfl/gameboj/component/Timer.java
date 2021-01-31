package ch.epfl.gameboj.component;


import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;

import static ch.epfl.gameboj.AddressMap.*;
import static ch.epfl.gameboj.Preconditions.checkBits16;
import static ch.epfl.gameboj.Preconditions.checkBits8;

/**
 * classe représentant le minuteur associé au processeur du GamBoy
 *
 * @author Armen Homberger(154511)
 * @author Justin Deschenaux(288424)
 */
public final class Timer implements Component, Clocked {

    private final Cpu gameBoyCpu;
    private int mainCounter, TIMACounter, TMARegister, TACRegister;

    /**
     * Construit un minuteur associé au processeur donné
     *
     * @param cpu processeur donné
     * @throws NullPointerException si le processeur donné est "null"
     */
    public Timer(Cpu cpu) {
        if (cpu == null) {
            throw new NullPointerException();
        }
        gameBoyCpu = cpu;
    }

    /**
     * fait évoluer le composant en exécutant les opérations du cycle d'index passé
     *
     * @param cycle : cycle d'index
     */
    @Override
    public void cycle(long cycle) {

        boolean s0 = state();
        mainCounter = Bits.clip(16, mainCounter + 4);
        incIfChange(s0);
    }

    /**
     * incrémente le compteur secondaire ssi l'état passé en argument est vrai et l'état actuel (retourné par state) est faux.
     * @param s0 état précédent
     */
    private void incIfChange(boolean s0) {

        if (s0 && !state()) {

            if (TIMACounter == 0xFF) {

                gameBoyCpu.requestInterrupt(Cpu.Interrupt.TIMER);
                TIMACounter = TMARegister;
            } else {
                ++TIMACounter;
            }

        }
    }

    /**
     * Retourne l'état du minuteur
     *
     * @return état du minuteur: conjonction logique du bit 2 du registre TAC et du bit du compteur principal désigné
     * par les 2 bits de poids faible de ce même registre
     */
    private boolean state() {

        boolean tac = Bits.test(TACRegister, 2);
        boolean div = Bits.test(mainCounter, incIndex());

        return tac & div;
    }

    /**
     * Calcul de l'index à surveiller pour l'incrémentation du compteur secondaire
     *
     * @return index à surveiller
     */
    private int incIndex() {
        int tacValue = Bits.clip(2, TACRegister);

        switch (tacValue) {
            case 0b0:
                return 9;
            case 0b1:
                return 3;
            case 0b10:
                return 5;
            case 0b11:
                return 7;
            default:
                return -1;
        }
    }

    /**
     * Lit l'octet à l'adresse donnée
     *
     * @param address : adresse de l'octet à retourner
     * @return l'octet lu
     */
    @Override
    public int read(int address) {
        checkBits16(address);

        if (address == REG_DIV) return Bits.extract(mainCounter, 8, 8);
        else if (address == REG_TIMA) return TIMACounter;
        else if (address == REG_TMA) return TMARegister;
        else if (address == REG_TAC) return TACRegister;
        else return NO_DATA;

    }

    /**
     * Ecrit un octet à l'adresse donnée
     *
     * @param address : adresse de l'octet à écrire
     * @param data    : octet à écrire
     */
    @Override
    public void write(int address, int data) {
        checkBits8(data);
        checkBits16(address);

        if (address == REG_DIV) {

            boolean s0 = state();
            mainCounter = 0;
            incIfChange(s0);

        } else if (address == REG_TMA) TMARegister = data;
        else if (address == REG_TIMA) TIMACounter = data;
        else if (address == REG_TAC) {

            boolean s0 = state();
            TACRegister = Bits.clip(3, data);
            incIfChange(s0);
        }
    }
}
