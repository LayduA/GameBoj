package ch.epfl.gameboj.gui;

import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

public class ImageConverter {
    private static int[] colors = {0xFF, 0xD3, 0xA9, 0};
    
    public static Image convert(LcdImage image) {
        WritableImage newImage = new WritableImage(image.width(), image.height());
        for(int i = 0; i<image.width();i++) {
            for (int j = 0; j<image.height();j++) {
                int pixelColor = image.get(i, j);
                int rgb = colors[pixelColor];
                int argb = 0xFF << 24 | rgb << 16 | rgb << 8 | rgb;
                newImage.getPixelWriter().setArgb(i, j, argb);
            }
        }
        return newImage;
    }
}
