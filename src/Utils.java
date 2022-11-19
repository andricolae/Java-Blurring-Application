import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

    public static int constrain (int value, int min, int max) {
        return value > max ? max : (Math.max(value, min));
    }

    public static int constrain (int value) {
        return constrain(value, 0, 255);
    }
}
