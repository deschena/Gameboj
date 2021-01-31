package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.bits.BitVector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class LcdImage {

    private final List<LcdImageLine> lines;
    private final int width;
    private final int height;

    public static final LcdImage whiteLCD = new LcdImage.Builder(LcdController.LCD_WIDTH, LcdController.LCD_HEIGHT).build();

    /**
     * Construit une image Game Boy
     *
     * @param lines  liste des lignes
     * @param width  largeur de l'image
     * @param height hauteur de l'image
     */
    public LcdImage(List<LcdImageLine> lines, int width, int height) {

        checkArgument(width >= 0 && height >= 0 && lines.size() == height);
        Objects.requireNonNull(lines);

        for(LcdImageLine line : lines){
            checkArgument(line.size() == width);
        }

        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
        this.width = width;
        this.height = height;
    }


    /**
     * Accesseur de la largeur de l'image
     *
     * @return largeur
     */
    public int width() {
        return width;
    }

    /**
     * Accesseur de la hauteur de l'image
     *
     * @return hauteur
     */
    public int height() {
        return height;
    }

    /**
     * Méthode donnant la couleur d'un pixel d'index donné
     *
     * @param x position verticale du pixel
     * @param y position horizontale du pixel
     * @return couleur du pixel
     */
    public int get(int x, int y) {

        checkArgument(x >= 0 && x < width);
        checkArgument(y >= 0 && y < height);
        boolean msb = lines.get(y).msb().testBit(x);
        boolean lsb = lines.get(y).lsb().testBit(x);

        if (!msb && !lsb) return 0;
        else if (!msb) return 0b01;
        else if (!lsb) return 0b10;
        else return 0b11;
    }

    /**
     * redéfinition de la méthode equals: compare deux images LCD entre elles
     *
     * @param that image à comparer avec l'instance courante
     * @return true si les images sont identiques, false sinon
     */
    @Override
    public boolean equals(Object that) {

        if (!(that instanceof LcdImage)) return false;

        LcdImage thatImage = (LcdImage) that;
        if (this.width != thatImage.width || this.height != thatImage.height) {
            return false;
        }

        for (int i = 0; i < lines.size(); ++i) {
            if (!this.lines.get(i).equals(thatImage.lines.get(i)))
                return false;
        }

        return true;
    }


    /**
     * redéfinition de la méthode hashCode
     *
     * @return la valeur de hachage de l'image de Game Boy
     */
    @Override
    public int hashCode() {

        return lines.hashCode();
    }

    public static final class Builder {

        private final int width;
        private final int height;
        private List<LcdImageLine> lines;


        /**
         * Construit une image à bâtir dont tous les pixels ont la couleur 0
         *
         * @param height hauteur de l'image
         * @param width  largeur de l'image
         */
        public Builder(int width, int height) {

            checkArgument(width >= 0 && height >= 0);

            this.width = width;
            this.height = height;
            lines = new ArrayList<>(height);
            LcdImageLine blankLine = LcdImageLine.blankLine(width);

            for (int i = 0; i < height; ++i) {
                lines.add(blankLine);
            }

        }

        /**
         * Méthode permettant de changer la ligne d'index donné
         *
         * @param index index de la ligne à changer
         * @param line  ligne à remplacer
         */
        public void setLine(int index, LcdImageLine line) {
            requireNonNull(line);
            checkArgument(line.size() == width);

            lines.set(index, line);
        }


        /**
         * Construit l'image en cours de construction
         *
         * @return l'image construite
         */
        public LcdImage build() {
            return new LcdImage(lines, width, height);
        }


    }
}
