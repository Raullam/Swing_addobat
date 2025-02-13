package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import static com.mycompany.dibuixets.dll.Preferences.getOpenCVPath;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Classe que implementa un panell de vídeo amb efecte de croma per substituir el fons de la imatge.
 * <p>
 * Aquesta classe captura imatges de la càmera, aplica un efecte de croma (substituir un fons verd per una imatge seleccionada)
 * i permet mostrar-les en un panell gràfic.
 * </p>
 * 
 * @author Raül, Miquel Angel, Alejandro, Magi
 * @version 1.0
 * @since 2025-02-13
 */
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

    /**
     * Constructor que inicialitza la càmera i els botons per activar el croma i seleccionar el fons.
     * <p>
     * Aquest constructor carrega la configuració inicial per capturar vídeo des de la càmera,
     * activa el suport per l'efecte de croma i crea els botons per interactuar amb l'usuari.
     * </p>
     */
    public Croma() {
        System.load(getOpenCVPath());

        capture = new VideoCapture(0);
        frame = new Mat();

        // Verifica si la càmera s'ha obert correctament
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(this, "No s'ha pogut accedir a la càmera.");
            return;
        }

        // Botó per activar/desactivar l'efecte de croma
        JButton cromaButton = new JButton("Activar Croma");
        cromaButton.addActionListener(e -> {
            cromaActive = !cromaActive;
            if (cromaActive && backgroundImage == null) {
                backgroundImage = new Mat(frame.size(), CvType.CV_8UC3, new Scalar(0, 255, 0));
            }
            cromaButton.setText(cromaActive ? "Desactivar Croma" : "Activar Croma");
        });

        // Botó per seleccionar una imatge de fons
        JButton selectBackgroundButton = new JButton("Seleccionar Fondo");
        selectBackgroundButton.addActionListener(e -> selectBackgroundImage());

        // Botó per tancar la finestra
        //JButton backButton = new JButton("Tornar");
        //backButton.addActionListener(e -> stopCapture2());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(cromaButton);
        buttonPanel.add(selectBackgroundButton);
        //buttonPanel.add(backButton);

        this.setLayout(new BorderLayout());
        this.add(buttonPanel, BorderLayout.SOUTH);

        // Thread per capturar contínuament frames de la càmera
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

    /**
     * Permet a l'usuari seleccionar una imatge per utilitzar-la com a fons per l'efecte de croma.
     * <p>
     * Aquest mètode obre un selector de fitxers perquè l'usuari esculli una imatge des del sistema de fitxers.
     * Si la imatge és vàlida, es redimensiona per ajustar-se a la mida del frame actual.
     * </p>
     */
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

    /**
     * Aplica l'efecte de croma per substituir el fons verd per una altra imatge.
     * <p>
     * Aquesta funció utilitza una matriu HSV per identificar la gamma de colors verds i substituir-los
     * per una imatge de fons seleccionada prèviament.
     * </p>
     * 
     * @param frame El frame actual de la càmera a modificar.
     */
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

        // Alliberar matrius per evitar pèrdues de memòria
        hsvImage.release();
        mask.release();
        invMask.release();
        foreground.release();
        backgroundResized.release();
        background.release();
    }

    /**
     * Dibuixa el frame actual en el panell.
     * <p>
     * Aquest mètode dibuixa la imatge convertida en BufferedImage sobre el panell gràfic.
     * </p>
     * 
     * @param g El context gràfic en el qual es dibuixa la imatge.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bufferedImage != null) {
            g.drawImage(bufferedImage, 0, 0, this);
        }
    }

    /**
     * Converteix una matriu OpenCV en una imatge de tipus BufferedImage.
     * <p>
     * Aquesta funció converteix una imatge representada com una matriu OpenCV en una imatge compatible amb Swing,
     * que es pot mostrar en el panell gràfic.
     * </p>
     * 
     * @param mat La matriu OpenCV a convertir.
     * @return La imatge de tipus BufferedImage.
     */
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

    /**
     * Atura la captura de vídeo i tanca la finestra.
     * <p>
     * Aquest mètode atura la captura de vídeo, allibera els recursos associats a la càmera
     * i tanca la finestra de l'aplicació.
     * </p>
     */
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
    
   public void stopCapture2() {
    capturing = false; // Detener la captura de vídeo
    if (capture != null && capture.isOpened()) {
        capture.release(); // Liberar los recursos de la cámara
    }

    // Detener el hilo de captura si está en ejecución
    if (captureThread != null && captureThread.isAlive()) {
        try {
            captureThread.join(); // Esperar a que el hilo termine
        } catch (InterruptedException e) {
            // Manejar cualquier excepción que pueda ocurrir cuando se interrumpe el hilo
            e.printStackTrace();
        }
    }
    
    // Aquí no cerramos la ventana, solo paramos la captura
}


}
