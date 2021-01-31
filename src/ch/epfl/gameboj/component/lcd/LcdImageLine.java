package ch.epfl.gameboj.component.lcd;

import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.checkArgument;
import static ch.epfl.gameboj.Preconditions.checkBits8;
import static java.util.Objects.requireNonNull;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class LcdImageLine {

    public static LcdImageLine LCDBlankLine = blankLine(LcdController.LCD_WIDTH);
    private BitVector msb, lsb, opacity;
    private final int DEFAULT_COLORMAP = 0b11_10_01_00;

    /**
     * Constructeur à trois arguments
     * @param msb le vecteur de bits msb
     * @param lsb le vecteur de bits lsb
     * @param opacity le vecteur de bits d'opacité
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {

        requireNonNull(msb);
        requireNonNull(lsb);
        requireNonNull(opacity);
        checkArgument(msb.size() == lsb.size() && msb.size() == opacity.size());

        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }

    /**
     * Constructeur à deux arguments, appelle le constreucteur à trois arguments en calculant le vecteur de bits d'opacité
     * @param msb le vecteur de bits msb
     * @param lsb le vecteur de bits lsb
     */
    public LcdImageLine(BitVector msb, BitVector lsb) {
        this(msb, lsb, msb.or(lsb));
    }


    /**
     * Accesseur pour la taille de la ligne
     *
     * @return taille de la ligne en bits
     */
    public int size() {
        return msb.size();
    }

    /**
     * Accesseur pour les bits de poids forts de la ligne
     * (On peux renvoyer directement l'objet car il est immuable)
     *
     * @return le vecteur des bits de poids forts
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * Accesseur pour les bits de poids faibles de la ligne
     * (On peux renvoyer directement l'objet car il est immuable)
     *
     * @return le vecteur des bits de poids faible
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * Accesseur pour le vecteur d'opacité de la ligne
     * (On peux renvoyer directement l'objet car il est immuable)
     *
     * @return vecteur d'opacité
     */
    public BitVector opacity() {
        return opacity;
    }

    /**
     * Effectue un décalage sur la ligne
     *
     * @param shift direction/distance du décalage :
     *              'shift' négatif : décalage à droite
     *              'shift' positif : décalage à gauche
     */
    public LcdImageLine shift(int shift) {

        return new LcdImageLine(msb.shift(shift), lsb.shift(shift), opacity.shift(shift));
    }

    /**
     * Extrait de l'extensions infinie par enroulement, à partir d'un pixel donné, une ligne de longueur donnée
     *
     * @param start pixel de départ
     * @param size  taille de la nouvelle ligne
     * @return nouvelle ligne extraite de l'extension infine par enroulement
     */
    public LcdImageLine extractWrapped(int start, int size) {

        return new LcdImageLine(msb.extractWrapped(start, size), lsb.extractWrapped(start, size), opacity.extractWrapped(start, size));
    }

    /**
     * Transforme les couleurs de la ligne en fonction de la palette passée en argument
     *
     * @param mapColors palette de transformation
     * @return
     */
    public LcdImageLine mapColors(int mapColors) {

        checkArgument(mapColors <= 0xFF);
        if (mapColors == DEFAULT_COLORMAP) return this;

        BitVector newMSBs = new BitVector(msb.size(), false);
        BitVector newLSBs = new BitVector(lsb.size(), false);

        BitVector notMSBs = msb.not(); //Calculé une fois pour éviter de devoir le recréer chaque fois qu'on en a besoin
        BitVector notLSBs = lsb.not(); //Idem
        BitVector[] masks = {notLSBs.and(notMSBs), lsb.and(notMSBs), notLSBs.and(msb), lsb.and(msb)};


        for (int i = 0; i < 4; i++) {

            if (Bits.test(mapColors, 2 * i))
                newLSBs = newLSBs.or(masks[i]);
            if (Bits.test(mapColors, 2 * i + 1))
                newMSBs = newMSBs.or(masks[i]);
        }

        return new LcdImageLine(newMSBs, newLSBs, opacity);
    }

    /**
     * Compose la ligne avec une seconde de même longueur placée au dessus d'elle, en utilisant l'opacité
     * de la ligne supérieur pour effectuer la composition
     *
     * @param above ligne supérieure dans la superposition
     * @return nouvelle ligne tenant compte de la supperposition des lignes
     */
    public LcdImageLine below(LcdImageLine above) {

        return below(above, above.opacity);
    }

    /**
     * Compose la ligne avec une seconde de même longueur, placée au-dessus d'elle en utilisant un vecteur d'opacité
     * passé en argument pour effectuer la composition, celui de la ligne supérieure étant ignoré
     *
     * @param above   ligne supérieur dans la superposition
     * @param opacity vecteur pour l'opacité
     * @return nouvelle ligne après superposition en ayant tenu compte de l'opacité passé en argument
     */
    public LcdImageLine below(LcdImageLine above, BitVector opacity) {

        Objects.requireNonNull(above);
        Objects.requireNonNull(opacity);
        checkArgument(above.size() == this.size());

        BitVector notOpacity = opacity.not();

        BitVector aboveMSBs = above.msb.and(opacity);
        BitVector aboveLSBs = above.lsb.and(opacity);
        BitVector aboveOpacity = above.opacity.and(opacity);

        BitVector belowMSBs = msb.and(notOpacity);
        BitVector belowLSBs = lsb.and(notOpacity);
        BitVector belowOpacity = this.opacity.and(notOpacity);


        return new LcdImageLine(aboveMSBs.or(belowMSBs), aboveLSBs.or(belowLSBs), aboveOpacity.or(belowOpacity));
    }

    /**
     * Crée une image dont les 'size' premiers bits sont ceux de cette ligne, les autres ceux de celle passée en argument
     *
     * @param size nombre de bits à garder de cette ligne
     * @param that autre line dont on extrait les derniers bits
     * @return
     */
    public LcdImageLine join(int size, LcdImageLine that) {
        checkArgument(size() == that.size());
        checkArgument(size >= 0);
        checkArgument(size <= size());

        BitVector mask = new BitVector(this.size(), true).shift(size() - size).shift(size - size());
        return that.below(this, mask);

    }

    /**
     * redéfinition de la méthode equals
     * @param that objet à comparer avec this
     * @return true si égal, false sinon
     */
    @Override
    public boolean equals(Object that) {

        if (!(that instanceof LcdImageLine)) return false;

        LcdImageLine lcdThat = (LcdImageLine) that;
        return (msb.equals(lcdThat.msb)) && (lsb.equals(lcdThat.lsb)) && (opacity.equals(lcdThat.opacity));
    }

    /**
     * redéfinition de la méthode hashCode
     * @return la valeur de hachage de l'instance
     */
    @Override
    public int hashCode() {

        return Objects.hash(msb.hashCode(), lsb.hashCode(), opacity.hashCode());
    }


    /**
     * méthode auxiliaire créant une ligne vide
     * @param width taille de la ligne à créer
     * @return la ligne vide de taille width
     */
    public static LcdImageLine blankLine(int width) {
        return new LcdImageLine(new BitVector(width, false), new BitVector(width, false));
    }


    public static final class Builder {

        private BitVector.Builder msbBuilder;
        private BitVector.Builder lsbBuilder;
        private int size;

        /**
         * Constructeur de Builder
         * @param size taille des vecteurs de bits à construire
         */
        public Builder(int size) {

            checkArgument(size > 0);
            checkArgument(size % Integer.SIZE == 0);
            msbBuilder = new BitVector.Builder(size);
            lsbBuilder = new BitVector.Builder(size);
            this.size = size;
        }

        /**
         * Met à jour l'octet à l'indice donné
         *
         * @param byteIndex indice de l'octet à modifier
         * @param MSBs      octet des bits de poids fort
         * @param LSBs      octet des bits de poids faible
         * @return this afin de pouvoir enchainer les appels
         */
        public Builder setBytes(int byteIndex, int MSBs, int LSBs) {

            checkBits8(MSBs);
            checkBits8(LSBs);
            if (msbBuilder == null || lsbBuilder == null) {
                throw new IllegalStateException();
            }
            if (byteIndex < 0 || byteIndex >= size / Byte.SIZE) {
                throw new IndexOutOfBoundsException();
            }

            msbBuilder.setByte(byteIndex, MSBs);
            lsbBuilder.setByte(byteIndex, LSBs);

            return this;
        }

        /**
         * Construit une ligne à partir des octets contenus par le Builder et rend ce dernier inutilisable
         *
         * @return nouvelle ligne à partir des octets en mémoire
         */
        public LcdImageLine build() {

            if (msbBuilder == null || lsbBuilder == null)
                throw new IllegalStateException();

            BitVector msb = msbBuilder.build();
            BitVector lsb = lsbBuilder.build();
            msbBuilder = null;
            lsbBuilder = null;
            return new LcdImageLine(msb, lsb);
        }
    }
}