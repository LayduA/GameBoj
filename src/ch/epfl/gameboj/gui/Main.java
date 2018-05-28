package ch.epfl.gameboj.gui;

import static ch.epfl.gameboj.component.lcd.LcdController.LCD_HEIGHT;
import static ch.epfl.gameboj.component.lcd.LcdController.LCD_WIDTH;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    private static Map<String, KeyCode> textToKeyCodeMap  = Map.of(
            "A",KeyCode.A,
            "B",KeyCode.B,
            "S",KeyCode.S,
            " ",KeyCode.SPACE);
    private static Map<KeyCode, Key> keyMap = Map.of(
            KeyCode.A, Key.A,
            KeyCode.B, Key.B,
            KeyCode.SPACE, Key.SELECT, 
            KeyCode.S, Key.START,
            KeyCode.LEFT,Key.LEFT,
            KeyCode.RIGHT,Key.RIGHT,
            KeyCode.UP,Key.UP,
            KeyCode.DOWN,Key.DOWN);

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start(Stage s) throws IOException {
        if (getParameters().getRaw().size() > 1) {
            System.exit(1);
        }
        File romFile = new File("tetris.gb");
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        ImageView imageView = new ImageView();
        imageView.setFitWidth(2 * LCD_WIDTH);
        imageView.setFitHeight(2 * LCD_HEIGHT);
        BorderPane pane = new BorderPane(imageView);
        Scene scene = new Scene(pane);
        imageView.setOnKeyPressed(e -> {
            KeyCode keycode = textToKeyCodeMap.containsKey(e.getText()) ? textToKeyCodeMap.get(e.getText()) : e.getCode();
            if (keyMap.containsKey(keycode)) {
                gb.joypad().keyPressed(keyMap.get(e.getCode()));
            }
        });
        imageView.setOnKeyReleased(e -> {
            KeyCode keycode = textToKeyCodeMap.containsKey(e.getText()) ? textToKeyCodeMap.get(e.getText()) : e.getCode();
            if (keyMap.containsKey(keycode)) {
                gb.joypad().keyReleased(keyMap.get(e.getCode()));
            }
        });
        s.setTitle("gameboj");
        s.setScene(scene);
        s.show();
        imageView.requestFocus();
        long start = System.nanoTime();
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = now - start;
                gb.runUntil((long) (elapsed * GameBoy.CYCLE_PER_NANOSECOND));
                imageView.setImage(ImageConverter
                        .convert(gb.lcdController().currentImage()));

            }
        };
        timer.start();

    }
}
