package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.*;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;

/**
 * @author Armen Homberger (154511)
 * @author Justin Deschenaux (288424)
 */
public final class Main extends Application{

    private final HashMap<String, Key> textMap = new HashMap<>();
    private final HashMap<KeyCode, Key> codeMap = new HashMap<>();


    /**
     * Méthode appelant la méthode launch
     * @param args fichier ROM reçu
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    /**
     * Méthode principale du GameBoy, crée l'interface graphique et la met à jour en fonction de l'écoulement du temps
     * @param primaryStage la scène affichée à l'écran
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {

        textMap.put("a", Key.A);
        textMap.put("b", Key.B);
        textMap.put("s", Key.START);
        textMap.put(" ", Key.SELECT);

        codeMap.put(KeyCode.UP, Key.UP);
        codeMap.put(KeyCode.DOWN, Key.DOWN);
        codeMap.put(KeyCode.LEFT, Key.LEFT);
        codeMap.put(KeyCode.RIGHT, Key.RIGHT);



        if(getParameters().getRaw().size() != 1)
            System.exit(1);

        File romFile = new File(getParameters().getRaw().get(0));
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));


        ImageView imageView = new ImageView();
        BorderPane mainPane = new BorderPane(imageView);

        imageView.setFitWidth(gb.lcdController().currentImage().width()*2);
        imageView.setFitHeight(gb.lcdController().currentImage().height()*2);
        imageView.setImage(ImageConverter.convert(gb.lcdController().currentImage()));

        imageView.setOnKeyPressed(e -> {
            Key key = getKey(e);
            if(key != null) {
                gb.joypad().keyPressed(key);
            }
        });


        imageView.setOnKeyReleased(e -> {
            Key key = getKey(e);
            if(key != null) {
                gb.joypad().keyReleased(key);
            }
        });

        long start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = now - start;
                long cycles = (long)(elapsed * GameBoy.CYCLES_PER_NANOSECOND);
                gb.runUntil(cycles);
                imageView.setImage(ImageConverter.convert(gb.lcdController().currentImage()));
            }
        };
        timer.start();

        primaryStage.setScene(new Scene(mainPane));
        primaryStage.show();
        imageView.requestFocus();

    }


    /**
     * Méthode donnant le code ou le texte de la touche pressée ou relâchée, s'il se trouve dans la table de référence
     * @param event événement déclenché par la pression ou le relâchement de touche
     * @return le code ou le texte de la touche
     */
    private Key getKey(KeyEvent event) {

        Key keyText = textMap.get(event.getText());
        Key keyCode = codeMap.get(event.getCode());

        if(keyCode != null) {
            return keyCode;
        } else
            return keyText;

    }

}