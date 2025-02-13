package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class Croma extends JPanel {
    private Mat frame;
    private BufferedImage bufferedImage;
    private VideoCapture capture;
    private Scalar lowerGreen = new Scalar(35, 50, 50);
    private Scalar upperGreen = new Scalar(85, 255, 255);
    private boolean cromaActive = false;
    private boolean capturing = true;
    private Thread captureThread;
    private Mat backgroundImage = null;

    public Croma() {
        System.load(Constants.FILE_PATH);
        
        capture = new VideoCapture(0);
        frame = new Mat();

        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(this, "No s'ha pogut accedir a la càmera.");
            return;
        }

        JButton cromaButton = new JButton("Activar Croma");
        cromaButton.addActionListener(e -> {
            cromaActive = !cromaActive;
            cromaButton.setText(cromaActive ? "Desactivar Croma" : "Activar Croma");
        });

        JButton selectBackgroundButton = new JButton("Seleccionar Fondo");
        selectBackgroundButton.addActionListener(e -> selectBackgroundImage());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(cromaButton);
        buttonPanel.add(selectBackgroundButton);

        this.setLayout(new BorderLayout());
        this.add(buttonPanel, BorderLayout.SOUTH);

        captureThread = new Thread(() -> {
            while (capturing) {
                capture.read(frame);
                if (!frame.empty()) {
                    if (cromaActive && backgroundImage != null) {
                        applyChromaKeyEffect(frame);
                    }
                    bufferedImage = matToBufferedImage(frame);
                    repaint();
                }
            }
        });
        captureThread.start();
    }

    private void selectBackgroundImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            backgroundImage = Imgcodecs.imread(selectedFile.getAbsolutePath());
            if (backgroundImage.empty()) {
                JOptionPane.showMessageDialog(this, "Error al cargar la imagen de fondo.");
                backgroundImage = null;
            } else {
                Imgproc.resize(backgroundImage, backgroundImage, new Size(frame.width(), frame.height()));
            }
        }
    }

    private void applyChromaKeyEffect(Mat frame) {
        Mat hsvImage = new Mat();
        Imgproc.cvtColor(frame, hsvImage, Imgproc.COLOR_BGR2HSV);
        
        Mat mask = new Mat();
        Core.inRange(hsvImage, lowerGreen, upperGreen, mask);
        
        Mat invMask = new Mat();
        Core.bitwise_not(mask, invMask);
        
        Mat foreground = new Mat();
        frame.copyTo(foreground, invMask);
        
        Mat backgroundResized = new Mat();
        Imgproc.resize(backgroundImage, backgroundResized, frame.size());
        
        Mat background = new Mat();
        backgroundResized.copyTo(background, mask);
        
        Core.add(foreground, background, frame);
        
        hsvImage.release();
        mask.release();
        invMask.release();
        foreground.release();
        backgroundResized.release();
        background.release();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bufferedImage != null) {
            g.drawImage(bufferedImage, 0, 0, this);
        }
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        Mat matRGB = new Mat();

        Imgproc.cvtColor(mat, matRGB, Imgproc.COLOR_BGR2RGB);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[width * height * (int) matRGB.elemSize()];
        matRGB.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);

        return image;
    }

    public void stopCapture() {
        capturing = false;
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        if (captureThread != null) {
            try {
                captureThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }
}
