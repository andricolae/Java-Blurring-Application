import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    private BufferedImage image = null;
    boolean aspectRatio = true;
    boolean fitToScreen = true;
    boolean centerImage = true;
    double scaleValue = 1.0;

    public ImagePanel() {
        super();
    }
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (image == null)
            return;
        if(fitToScreen) {
            if(aspectRatio){
                double scaleWidth = (double) getWidth() / image.getWidth();
                double scaleHeight = (double) getHeight() / image.getHeight();
                scaleValue = Math.min(scaleWidth, scaleHeight);
                int width = (int) (scaleValue * image.getWidth());
                int height = (int) (scaleValue * image.getHeight());
                if(centerImage)
                    g.drawImage(image, (getWidth() - width) / 2, (getHeight() - height) / 2, width, height, null);
                else
                    g.drawImage(image, 0, 0, (int) (scaleValue * image.getWidth()), (int) (scaleValue * image.getHeight()), null);
            }
            else
                g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
        else
            g.drawImage(image,0, 0, image.getWidth(), image.getHeight(), null);
    }
    public BufferedImage getImage() {
        return image;
    }
    public void setImage(BufferedImage image) {
        this.image = image;
        repaint();
    }
}