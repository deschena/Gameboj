package ch.epfl.gameboj.component.cartridge;

import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.Rom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static ch.epfl.gameboj.Preconditions.*;

/**
 * Classe représentant une cartouche de jeu
 *
 * @author Armen Homberger(154511)
 * @author Justin Deschenaux(288424)
 */
public final class Cartridge implements Component {

    private static final int MBC_TYPE = 0x147;
    private static final int[] MBC1_RAM_SIZE = {0, 2048, 0x2000, 0x8000};
    private static final int SIMPLE_CARTRIDGE_SIZE = 0x8000;
    private final Component memoryBankController;
    private static final int RAM_SIZE = 0x149;

    /**
     * Constructeur privé de Cartridge, appelé dans la méthode ofFile ci-dessous
     *
     * @param mbc memoryBankController donné
     */
    private Cartridge(Component mbc) {

        this.memoryBankController = mbc;
    }

    /**
     * Crée une cartouche grâce au fichier passé en argument
     *
     * @param romFile fichier à utiliser pour initialiser la cartouche
     * @return cartouche de jeu
     * @throws IOException en cas d'erreur d'entrée-sortie ou si le fichier romFile n'existe pas
     */
    public static Cartridge ofFile(File romFile) throws IOException {

        byte[] gameData = new byte[0];
        try (FileInputStream inputStream = new FileInputStream(romFile)) {


            gameData = new byte[inputStream.available()];

            inputStream.read(gameData);



            if (inputStream.available() != 0)
                throw new IOException("Toutes les données de la ROM n'ont pas été lues !");

        } catch (IOException e) {

            System.out.println("Erreur lors de l'accès au fichier de la rom pour la création de la cartouche : " + e.getLocalizedMessage());
            throw new IOException();
        } catch (Exception e) {

            System.out.println("Erreur lors de la création de la cartouche : " + e.getLocalizedMessage());
        }

        checkArgument(gameData[MBC_TYPE] >= 0 && gameData[MBC_TYPE] <= 3);

        if (gameData[MBC_TYPE] == 0)
            return new Cartridge(new MBC0(new Rom(gameData)));
        else {
            int ramSize = MBC1_RAM_SIZE[gameData[RAM_SIZE]];
            return new Cartridge(new MBC1(new Rom(gameData), ramSize));
        }

    }


    /**
     * Lecture de la cartouche
     *
     * @param address : adresse de l'octet à retourner
     * @return valeur à l'adresse donnée
     */
    @Override
    public int read(int address) {

        return memoryBankController.read(checkBits16(address));
    }

    /**
     * Ecriture de la cartouche
     *
     * @param address : adresse de l'octet à écrire
     * @param data    : octet à écrire
     */
    @Override
    public void write(int address, int data) {

        memoryBankController.write(checkBits16(address), checkBits8(data));
    }


}
