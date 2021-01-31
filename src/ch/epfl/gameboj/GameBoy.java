package ch.epfl.gameboj;

import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

import java.util.Objects;

import static ch.epfl.gameboj.AddressMap.*;
import static ch.epfl.gameboj.Preconditions.checkArgument;

/**
 * Classe principale représentant le GameBoy
 *
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class GameBoy {

    private final Cpu cpu;
    private final Bus bus;
    private final Timer timer;
    private final LcdController lcdController;
    private final Joypad joypad;

    private long nextCycleId = 0;

    public static final long CYCLES_PER_SECOND = 0x100000;
    public static final double CYCLES_PER_NANOSECOND = CYCLES_PER_SECOND / 1e9;

    /**
     * Construit un GameBoy avec la cartouche donnée
     *
     * @param cartridge cartouche donnée
     * @throws NullPointerException si la cartouche donnée est "null"
     */
    public GameBoy(Cartridge cartridge) {

        Objects.requireNonNull(cartridge);

        cpu = new Cpu();
        bus = new Bus();
        timer = new Timer(cpu);
        Ram workRam = new Ram(WORK_RAM_SIZE);
        RamController workRamController = new RamController(workRam, WORK_RAM_START);
        RamController echoRamController = new RamController(workRam, ECHO_RAM_START, ECHO_RAM_END);
        BootRomController bootRomController = new BootRomController(cartridge);
        lcdController = new LcdController(cpu);
        joypad = new Joypad(cpu);

        timer.attachTo(bus);
        workRamController.attachTo(bus);
        echoRamController.attachTo(bus);
        bootRomController.attachTo(bus);
        lcdController.attachTo(bus);
        joypad.attachTo(bus);
        cpu.attachTo(bus);
    }

    /**
     * @return adresse en mémoire du bus principal
     */
    public Bus bus() {
        return bus;
    }

    /**
     * @return addresse en mémoire du processeur
     */
    public Cpu cpu() {
        return cpu;
    }

    /**
     * Simule le fonctionnement du Gameboy jusqu'au cycle donné moins 1
     *
     * @param cycles cycle limite du fonctionnement à simuler
     */
    public void runUntil(long cycles) {

        checkArgument(cycles >= nextCycleId);


        while (nextCycleId < cycles) {

            timer.cycle(nextCycleId);
            lcdController.cycle(nextCycleId);
            cpu.cycle(nextCycleId);

            nextCycleId++;
        }
    }

    /**
     * @return nombre de cycles déjà exécutés
     */
    public long cycles() {
        return nextCycleId;
    }

    /**
     * méthode donnant accès au minuteur
     *
     * @return l'attribut timer
     */
    public Timer timer() {
        return timer;
    }

    /**
     * méthode donnant accès au controleur d'écran
     *
     * @return l'attribut lcdController
     */
    public LcdController lcdController() {
        return lcdController;
    }


    /**
     * méthode donnant accès au joypad
     * @return le joypad
     */
    public Joypad joypad() {
        return joypad;
    }

}