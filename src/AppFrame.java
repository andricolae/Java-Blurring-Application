import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class AppFrame extends JFrame {
    private final String infoTitle = "Processing Information";
    private final JFileChooser fileChooser = new JFileChooser("./test_images");
    private final ImagePanel originalImagePanel;
    private final ImagePanel blurredImagePanel;
    private final JTextField kernelDimension;
    private BufferedImage workingImage;
    private String openedImageName;
    private int kernelSize;
    private float[] kernel;
    private FileWriter writeKernel;
    public AppFrame() {
        setTitle("Apply Blurring Filter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1064, 701);

        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new BorderLayout(0, 0));
        setContentPane(contentPane);

        JPanel imagesPanel = new JPanel();
        imagesPanel.setLayout(new BoxLayout(imagesPanel, BoxLayout.X_AXIS));
        originalImagePanel = new ImagePanel();
        blurredImagePanel = new ImagePanel();
        imagesPanel.add(originalImagePanel);
        imagesPanel.add( blurredImagePanel);
        contentPane.add(imagesPanel, BorderLayout.CENTER);

        JPanel ctrlPanel = new JPanel();
        contentPane.add(ctrlPanel, BorderLayout.SOUTH);

        JButton btnOpenImage = new JButton("Open Image");
        btnOpenImage.addActionListener(e -> onOpen());
        ctrlPanel.add(btnOpenImage);

        JLabel labelKernelDimension = new JLabel("Specify the Kernel Dimension:");
        kernelDimension = new JTextField();
        kernelDimension.setPreferredSize(new Dimension(50, 25));
        ctrlPanel.add(labelKernelDimension);
        ctrlPanel.add(kernelDimension);

        JButton btnLoadKernel = new JButton("Load Kernel Info");
        btnLoadKernel.addActionListener(e -> {
            if (kernelDimension.getText().equals("")) {
                File kernelInfo = null;
                JFileChooser chooseKernel = new JFileChooser("./kernels");
                if(chooseKernel.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
                    kernelInfo = chooseKernel.getSelectedFile();
                try {
                    assert kernelInfo != null;
                    Scanner read = new Scanner(kernelInfo);
                    kernelSize = Integer.parseInt(read.nextLine());
                    if (read.hasNextLine()) {
                        kernel = new float[kernelSize * kernelSize];
                        while(read.hasNextLine())
                            for (int i = 0; i < kernelSize * kernelSize; i++)
                                kernel[i] = Float.parseFloat(read.next());
                    }
                    else {
                        kernel = new float[kernelSize * kernelSize];
                        for (int i = 0; i < kernelSize * kernelSize; i++)
                            kernel[i] = 1.0f / (kernelSize * kernelSize);
                    }
                    read.close();
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
            else {
                kernelSize = Integer.parseInt(kernelDimension.getText());
                kernel = new float[kernelSize * kernelSize];
                for (int i = 0; i < kernelSize * kernelSize; i++)
                    kernel[i] = 1.0f / (kernelSize * kernelSize);
            }
        });
        ctrlPanel.add(btnLoadKernel);

        JButton btnSaveKernelInfo = new JButton("Save Kernel Info");
        btnSaveKernelInfo.addActionListener(e -> {
            if (kernelSize == 0 || kernel == null)
                JOptionPane.showMessageDialog(this, "Kernel Doesn't Exist!", "ALERT", JOptionPane.INFORMATION_MESSAGE);
            else {
                StringBuilder kernelInfo = new StringBuilder();
                kernelInfo.append(kernelSize);
                kernelInfo.append("\n");
                int aux = 0;
                for (int i = 0; i < kernelSize * kernelSize; i++) {
                    if (aux == kernelSize) {
                        kernelInfo.append("\n");
                        aux = 0;
                    }
                    kernelInfo.append(kernel[i]).append(" ");
                    aux++;
                }
                try {
                    writeKernel = new FileWriter("./kernels/k" + kernelSize);
                    writeKernel.write(String.valueOf(kernelInfo));
                    writeKernel.close();
                    JOptionPane.showMessageDialog(this, "Kernel Saved!", "WARNING", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        ctrlPanel.add(btnSaveKernelInfo);

        JButton btnBlurSimple = new JButton("Simple Blur");
        btnBlurSimple.addActionListener(e -> {
            if (workingImage == null || kernelSize == 0 || kernel == null)
                JOptionPane.showMessageDialog(this, "Image or Kernel Doesn't Exist!", "ALERT", JOptionPane.INFORMATION_MESSAGE);
            else
                onSimpleBlur(workingImage, new Kernel(kernelSize, kernelSize, kernel));
        });
        ctrlPanel.add(btnBlurSimple);

        JButton btnBlurJavaAPI = new JButton("Java Blur");
        btnBlurJavaAPI.addActionListener(e -> {
            if (workingImage == null || kernelSize == 0 || kernel == null)
                JOptionPane.showMessageDialog(this, "Image or Kernel Doesn't Exist!", "ALERT", JOptionPane.INFORMATION_MESSAGE);
            else
                onJavaBlur(workingImage, new Kernel(kernelSize, kernelSize, kernel));
        });
        ctrlPanel.add(btnBlurJavaAPI);

        JButton btnSaveBlurred = new JButton("Save Blurred Image");
        btnSaveBlurred.addActionListener(e -> {
            if (blurredImagePanel.getImage() == null)
                JOptionPane.showMessageDialog(this, "Blurred Image Doesn't Exist!", "ALERT", JOptionPane.INFORMATION_MESSAGE);
            else
                onSave();
        });
        ctrlPanel.add(btnSaveBlurred);
        openedImageName = "";
    }
    protected void onOpen() {
        File imageFile;
        if(fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            imageFile = fileChooser.getSelectedFile();
            openedImageName = imageFile.getName();
            workingImage = Utils.loadImage(imageFile);
            originalImagePanel.setImage(workingImage);
        }
    }
    protected void onSimpleBlur(BufferedImage inputImage, Kernel kernel) {
        long startTime = System.nanoTime();
        BufferedImage blurredImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        int kernelWidth = kernel.getWidth();
        int kernelRadius = kernelWidth / 2;
        float[] kernelData = kernel.getKernelData(null);
        int kernelDataIndex;

        for (int band = 0; band < inputImage.getRaster().getNumBands() && band < 3; band++)
            for (int y = 0; y < inputImage.getHeight(); y++)
                for (int x = 0; x < inputImage.getWidth(); x++) {
                    float gray = 0;
                    kernelDataIndex = 0;

                    for (int ky = -kernelRadius; ky <= kernelRadius; ky++)
                        for (int kx = -kernelRadius; kx <= kernelRadius; kx++)
                            if ((x + kx) < 0 || (x + kx) > inputImage.getWidth() - 1 || (y + ky) < 0 || (y + ky) > inputImage.getHeight() - 1)
                                gray += 0;
                            else
                                gray += kernelData[kernelDataIndex] * inputImage.getRaster().getSample(x + kx, y + ky, band);
                    blurredImage.getRaster().setSample(x, y, band, Utils.constrain(Math.round(gray)));
                }

        blurredImagePanel.setImage(blurredImage);
        long elapsedTime = System.nanoTime() - startTime;
        String info = "FILTER: lowpass filter\n" +
                "DIMENSION: " + kernelSize + "\n" +
                "VALUES: " + getInfo() +
                "TIME ELAPSED: " + elapsedTime/1000000000 + " seconds/ " + elapsedTime/1000000 + " milliseconds";
        JOptionPane.showMessageDialog(this, info, infoTitle, JOptionPane.INFORMATION_MESSAGE);
    }
    protected void onJavaBlur(BufferedImage inputImage, Kernel kernel) {
        long startTime = System.nanoTime();
        BufferedImage blurredImage = new BufferedImage(inputImage.getWidth(), inputImage.getHeight(), inputImage.getType());
        ConvolveOp convolveOp = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        convolveOp.filter(inputImage, blurredImage);
        blurredImagePanel.setImage(blurredImage);
        long elapsedTime = System.nanoTime() - startTime;
        String info = "FILTER: Java API filter\n" +
                "DIMENSION: " + kernelSize + "\n" +
                "VALUES: " + getInfo() +
                "TIME ELAPSED: " + elapsedTime/1000000000 + " seconds/ " + elapsedTime/1000000 + " milliseconds";
        JOptionPane.showMessageDialog(this, info, infoTitle, JOptionPane.INFORMATION_MESSAGE);
    }
    protected void onSave() {
        String format = openedImageName.substring(openedImageName.lastIndexOf(".") + 1);
        File saveImage = new File("./blurred_images/" + openedImageName);
        try {
            ImageIO.write(blurredImagePanel.getImage(), format, saveImage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        JOptionPane.showMessageDialog(this, "Image Saved!", "WARNING", JOptionPane.INFORMATION_MESSAGE);
    }

    protected StringBuilder getInfo() {
        StringBuilder valori = new StringBuilder();
        int aux = 0;
        valori.append("\n");
        for (int i = 0; i < kernelSize * kernelSize; i++){
            if (aux == kernelSize) {
                valori.append("\n");
                aux = 0;
            }
            valori.append(kernel[i]).append(" | ");
            aux ++;
        }
        valori.append("\n");
        return valori;
    }
    /*
    1. mai multe filtre de netezire
    2. trei variante de tratare a situatiilor in care nucleul depaseste zona imaginii
    */
}