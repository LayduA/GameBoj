package ch.epfl.gameboj.gui;

import javafx.application.Application;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import static ch.epfl.gameboj.component.lcd.LcdController.*;

public class Main extends Application{
    public static void main(String[] args) {
        Application.launch(args);
    }

    public void start(Stage s) {
        if(getParameters().getRaw().size() >1) {
            System.exit(1);
        }
        ImageView imageView = new ImageView();
        imageView.setFitWidth(2*LCD_WIDTH);
        imageView.setFitHeight(2*LCD_HEIGHT);
    }
}
