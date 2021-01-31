package ch.epfl.gameboj.component;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class DebugPrintComponent implements Component {

    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        if (address == 0xFF01)
            System.out.print((char) data);
    }
}