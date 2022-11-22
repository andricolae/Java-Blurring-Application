import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.io.File;
import java.io.IOException;

public class Utils {
    public static BufferedImage loadImage(File fileName) {
        BufferedImage image;
        try {
            image = ImageIO.read(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return image;
    }
    public static int constrain (int value, int min, int max) { return value > max ? max : (Math.max(value, min)); }
    public static int constrain (int value) {
        return constrain(value, 0, 255);
    }
    public static BufferedImage paddedImage(BufferedImage inputImage, int padding) {
        if (padding == 0)
            return inputImage;
        BufferedImage processedImage = new BufferedImage(
                inputImage.getWidth() + padding * 2,
                inputImage.getHeight() + padding * 2,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graph = (Graphics2D) processedImage.getGraphics();
        graph.drawImage(inputImage, padding, padding, null);
        return processedImage;
    }
}