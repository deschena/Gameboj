package ch.epfl.gameboj;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.DebugPrintComponent;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;

import java.io.File;
import java.io.IOException;

public final class DebugMain {
    public static void main(String[] args) throws IOException {


        args = new String[2];
        args[0] = "01-special.gb";
        args[1] = "30000000";

        File romFile = new File(args[0]);
        long cycles = Long.parseLong(args[1]);

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        Component printer = new DebugPrintComponent();
        printer.attachTo(gb.bus());
        while (gb.cycles() < cycles) {
            long nextCycles = Math.min(gb.cycles() + 17556, cycles);
            gb.runUntil(nextCycles);
            gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
        }
    }
}