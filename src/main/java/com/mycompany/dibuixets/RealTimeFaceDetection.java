package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

public class RealTimeFaceDetection extends JPanel {
    private VideoCapture camera;
    private Mat frame;
    private BufferedImage bufferedImage;
    private CascadeClassifier faceCascade;
    private String capturedImagePath = "images/captured_image.jpg";
    private JFrame frameWindow;

    /**
     * Constructor de la classe que inicialitza la càmera i el sistema de detecció de rostres.
     */
    public RealTimeFaceDetection() {
        System.load(Constants.FILE_PATH);

        camera = new VideoCapture(0);
        frame = new Mat();
        
        faceCascade = new CascadeClassifier();
        faceCascade.load("data/haarcascade_frontalface_alt2.xml");

        JButton saveButton = new JButton("Guardar Imatge");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCapturedImage();
            }
        });

        JButton backButton = new JButton("Volver");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cierra solo la ventana de la cámara (no la aplicación completa)
                camera.release();
                frameWindow.dispose();
            }
        });

        // Panel con los botones
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));  // Layout para los botones en línea horizontal
        buttonPanel.add(Box.createHorizontalGlue()); // Esto coloca los botones al centro
        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Espacio entre los botones
        buttonPanel.add(backButton);
        buttonPanel.add(Box.createHorizontalGlue());

        frameWindow = new JFrame("Detecció de Rostres en Temps Real");
        frameWindow.setLayout(new BorderLayout());
        frameWindow.add(this, BorderLayout.CENTER); // Panel con la detección de rostros en el centro
        frameWindow.add(buttonPanel, BorderLayout.SOUTH); // Los botones al sur
        frameWindow.setSize(640, 480);
        frameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Centrar la ventana en la pantalla
        frameWindow.setLocationRelativeTo(null);  // Esto centra la ventana en la pantalla
        frameWindow.setVisible(true);

        new Thread(() -> {
            while (true) {
                if (camera.read(frame)) {
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);

                    MatOfRect faces = new MatOfRect();
                    faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0, new Size(30, 30), new Size());

                    for (Rect rect : faces.toArray()) {
                        Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(255, 0, 0), 3);
                    }

                    bufferedImage = matToBufferedImage(frame);
                    repaint();
                }
            }
        }).start();
    }

    /**
     * Sobreescriu el mètode paintComponent per mostrar el vídeo processat.
     * 
     * @param g Objecte Graphics per pintar el component.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bufferedImage != null) {
            g.drawImage(bufferedImage, 0, 0, this);
        }
    }

    /**
     * Converteix un Mat d'OpenCV a un BufferedImage per mostrar-lo en Swing.
     * 
     * @param mat Mat d'OpenCV.
     * @return BufferedImage equivalent.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] data = new byte[width * height * (int) mat.elemSize()];
        mat.get(0, 0, data);
        image.getRaster().setDataElements(0, 0, width, height, data);
        return image;
    }

    /**
     * Guarda la imatge amb els rostres detectats.
     */
    private void saveCapturedImage() {
        if (frame != null) {
            String fileName = JOptionPane.showInputDialog("Introdueix el nom per guardar la imatge:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                File outputFile = new File("images/" + fileName + ".jpg");
                Imgcodecs.imwrite(outputFile.getAbsolutePath(), frame);
                JOptionPane.showMessageDialog(this, "Imatge guardada com: " + outputFile.getAbsolutePath());
            }
        } else {
            JOptionPane.showMessageDialog(this, "No s'ha detectat cap imatge.");
        }
    }

    /**
     * Mètode principal que inicia l'aplicació.
     * 
     * @param args Arguments de la línia de comandes.
     */
    public static void main(String[] args) {
        new RealTimeFaceDetection();
    }
}
