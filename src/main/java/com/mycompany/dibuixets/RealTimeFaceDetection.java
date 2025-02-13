package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import static com.mycompany.dibuixets.dll.Preferences.getOpenCVPath;
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

/**
 * Classe que implementa la detecció de rostres en temps real utilitzant OpenCV i la càmera.
 * <p>
 * Aquesta classe captura vídeo en temps real des de la càmera, converteix les imatges a escala de grisos,
 * i aplica un classificador de cascades per detectar rostres. Els rostres detectats es dibuixen al voltant de la
 * regió del rostre i es poden desar com a imatges amb els rostres detectats.
 * </p>
 * 
 * @author Raül, Miquel Angel, Alejandro, Magi
 * @version 1.0
 * @since 2025-02-13
 */
public class RealTimeFaceDetection extends JPanel {
    
    private VideoCapture camera; // Objecte per capturar vídeo de la càmera
    private Mat frame; // Matriu per emmagatzemar el fotograma actual
    private BufferedImage bufferedImage; // Imatge per mostrar en el panell
    private CascadeClassifier faceCascade; // Classificador per detectar rostres
    private String capturedImagePath = "images/captured_image.jpg"; // Ruta on es desarà la imatge capturada
    private JFrame frameWindow; // Finestra on es mostrarà el vídeo

    /**
     * Constructor que inicialitza la càmera i el sistema de detecció de rostres.
     * <p>
     * Aquest constructor configura la càmera, carrega el classificador Haar per detectar rostres i 
     * crea la finestra amb els botons per guardar la imatge o tancar la finestra.
     * </p>
     */
    public RealTimeFaceDetection() {
        System.load(getOpenCVPath()); // Carrega la llibreria OpenCV

        camera = new VideoCapture(0); // Inicialitza la càmera
        frame = new Mat(); // Matriu per emmagatzemar els fotogrames capturats
        
        faceCascade = new CascadeClassifier(); // Inicialitza el classificador de rostres
        faceCascade.load("data/haarcascade_frontalface_alt2.xml"); // Carrega el model Haar per detectar rostres

        // Botó per guardar la imatge capturada
        JButton saveButton = new JButton("Guardar Imatge");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveCapturedImage(); // Crida al mètode per guardar la imatge
            }
        });

        // Botó per tancar la finestra
        JButton backButton = new JButton("Volver");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Cierra la finestra sense tancar l'aplicació completa
                camera.release();
                frameWindow.dispose();
            }
        });

        // Panell per als botons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS)); // Layout horitzontal
        buttonPanel.add(Box.createHorizontalGlue()); // Alinea els botons al centre
        buttonPanel.add(saveButton);
        buttonPanel.add(Box.createHorizontalStrut(20)); // Espai entre botons
        buttonPanel.add(backButton);
        buttonPanel.add(Box.createHorizontalGlue());

        // Crea la finestra
        frameWindow = new JFrame("Detecció de Rostres en Temps Real");
        frameWindow.setLayout(new BorderLayout());
        frameWindow.add(this, BorderLayout.CENTER); // Panell amb la detecció de rostres
        frameWindow.add(buttonPanel, BorderLayout.SOUTH); // Col·loca els botons a la part inferior
        frameWindow.setSize(640, 480);
        frameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Centra la finestra en la pantalla
        frameWindow.setLocationRelativeTo(null);
        frameWindow.setVisible(true);

        // Fil per capturar els fotogrames de la càmera i detectar rostres
        new Thread(() -> {
            while (true) {
                if (camera.read(frame)) {
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY); // Converteix el fotograma a escala de grisos

                    // Matriu per emmagatzemar els rostres detectats
                    MatOfRect faces = new MatOfRect();
                    faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0, new Size(30, 30), new Size());

                    // Dibuixa rectangles al voltant dels rostres detectats
                    for (Rect rect : faces.toArray()) {
                        Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(255, 0, 0), 3);
                    }

                    // Converteix el fotograma amb els rostres detectats a BufferedImage per mostrar-lo
                    bufferedImage = matToBufferedImage(frame);
                    repaint(); // Repinta el panell
                }
            }
        }).start();
    }

    /**
     * Sobreescriu el mètode paintComponent per mostrar el vídeo processat amb els rostres detectats.
     * 
     * @param g Objecte Graphics utilitzat per dibuixar en el panell.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bufferedImage != null) {
            g.drawImage(bufferedImage, 0, 0, this); // Dibuixa la imatge processada
        }
    }

    /**
     * Converteix un objecte Mat d'OpenCV a un BufferedImage per mostrar-lo a Swing.
     * <p>
     * Aquest mètode és necessari perquè els objectes Mat d'OpenCV no es poden mostrar directament en Swing,
     * per tant es converteixen a BufferedImage.
     * </p>
     * 
     * @param mat Matriu d'OpenCV a convertir.
     * @return BufferedImage resultant.
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
     * Guarda la imatge capturada amb els rostres detectats en un fitxer.
     * <p>
     * El mètode mostra una finestra de diàleg per introduir el nom del fitxer. La imatge es desa a la carpeta "images".
     * </p>
     */
    private void saveCapturedImage() {
        if (frame != null) {
            String fileName = JOptionPane.showInputDialog("Introdueix el nom per guardar la imatge:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                File outputFile = new File("images/" + fileName + ".jpg");
                Imgcodecs.imwrite(outputFile.getAbsolutePath(), frame); // Desa la imatge
                JOptionPane.showMessageDialog(this, "Imatge guardada com: " + outputFile.getAbsolutePath());
            }
        } else {
            JOptionPane.showMessageDialog(this, "No s'ha detectat cap imatge.");
        }
    }

    /**
     * Mètode principal que inicia l'aplicació de detecció de rostres.
     * 
     * @param args Arguments de la línia de comandes.
     */
    public static void main(String[] args) {
        new RealTimeFaceDetection(); // Inicia la detecció de rostres en temps real
    }
}
