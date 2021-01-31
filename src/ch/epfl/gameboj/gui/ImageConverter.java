package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class ImageConverter {

    private static final int[] COLOR_MAP = new int[] {
            0xFF_FF_FF_FF, 0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00
    };

    /**
     * Convertisseur d'image Game Boy en image JavaFX
     * @param image image à convertir
     * @return image convertie en type JavaFX
     */
    public static Image convert(LcdImage image) {
        Preconditions.checkArgument(image.width() == LcdController.LCD_WIDTH && image.height() == LcdController.LCD_HEIGHT);
        WritableImage i = new WritableImage(image.width(), image.height());
        PixelWriter w = i.getPixelWriter();

        for (int y = 0; y < image.height(); ++y)
            for (int x = 0; x < image.width(); ++x)
                w.setArgb(x, y, COLOR_MAP[image.get(x, y)]);

        return i;
    }

}
