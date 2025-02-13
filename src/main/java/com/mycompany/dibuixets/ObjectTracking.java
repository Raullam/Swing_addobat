package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import org.opencv.core.Core;
import org.opencv.core.Rect;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.videoio.VideoCapture;
import org.opencv.video.Tracker;
import org.opencv.tracking.TrackerKCF;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Classe per al seguiment d'objectes utilitzant la càmera i OpenCV.
 * <p>
 * Aquesta classe utilitza la càmera per capturar vídeo en temps real i permet a l'usuari seleccionar una regió d'interès (ROI)
 * per fer el seguiment d'un objecte. El seguiment es realitza mitjançant el tracker KCF de OpenCV.
 * </p>
 * 
 * @author Raül, Miquel Angel, Alejandro, Magi
 * @version 1.0
 * @since 2025-02-13
 */
public class ObjectTracking extends JPanel {
    static { System.load(Constants.FILE_PATH); } // Carrega la llibreria OpenCV

    private Rect roi = null; // Regió d'interès (ROI) per al seguiment
    private Point startPoint = null; // Punt inicial per seleccionar la ROI
    private Point endPoint = null; // Punt final per seleccionar la ROI
    private Mat frame = new Mat(); // Matriu per emmagatzemar el fotograma actual
    private boolean running = true; // Controla si l'aplicació està en execució
    private Tracker tracker = null; // Tracker per al seguiment d'objectes
    private boolean trackingActive = false; // Indica si el seguiment està actiu
    private VideoCapture videoCapture; // Captura de vídeo de la càmera

    /**
     * Constructor que inicialitza la càmera, configura els listeners per al seguiment d'objectes i comença a capturar el vídeo.
     * <p>
     * Aquest constructor configura la càmera, permet a l'usuari seleccionar una ROI amb el ratolí i iniciar el seguiment
     * d'un objecte mitjançant el tracker KCF de OpenCV.
     * </p>
     */
    public ObjectTracking() {
        videoCapture = new VideoCapture(0); // Inicialitza la càmera

        if (!videoCapture.isOpened()) {
            System.out.println("Error: No es pot obrir la càmera");
            return;
        }

        setPreferredSize(new Dimension(800, 600)); // Defineix la mida preferida del panell

        // Afegir un listener per detectar clics del ratolí
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startPoint = new Point(e.getX(), e.getY()); // Guarda el punt inicial
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                endPoint = new Point(e.getX(), e.getY()); // Guarda el punt final
                roi = new Rect(
                    Math.min((int) startPoint.x, (int) endPoint.x),
                    Math.min((int) startPoint.y, (int) endPoint.y),
                    Math.abs((int) endPoint.x - (int) startPoint.x),
                    Math.abs((int) endPoint.y - (int) startPoint.y)
                ); // Defineix la ROI
                System.out.println("ROI seleccionada: " + roi);
                
                trackingActive = false; // Reinicia el seguiment
                tracker = TrackerKCF.create(); // Crea un nou tracker
                tracker.init(frame, roi); // Inicialitza el tracker amb la ROI
                trackingActive = true; // Activa el seguiment
            }
        });

        // Fil per llegir els fotogrames de la càmera
        new Thread(() -> {
            while (running) {
                if (!videoCapture.read(frame)) {
                    break; // Si no es pot llegir el fotograma, surt del bucle
                }

                if (trackingActive && roi != null && tracker != null) {
                    boolean success = tracker.update(frame, roi); // Actualitza el tracker
                    if (!success) {
                        System.out.println("Error en el seguiment de l'objecte");
                        trackingActive = false; // Desactiva el seguiment si hi ha un error
                    }
                }

                repaint(); // Repinta el panell per mostrar el fotograma actual

                try {
                    Thread.sleep(30); // Espera 30 ms per a una actualització suau
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            videoCapture.release(); // Allibera la càmera quan el bucle acaba
        }).start();
    }

    /**
     * Dibuixa el fotograma actual al panell.
     * <p>
     * Aquest mètode converteix el fotograma en una imatge i la dibuixa al panell. També dibuixa un rectangle verd al voltant
     * de la regió d'interès (ROI) si el seguiment està actiu.
     * </p>
     * 
     * @param g El context gràfic utilitzat per dibuixar el fotograma.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (frame != null && !frame.empty()) {
            // Converteix el fotograma a una imatge i la dibuixa al panell
            Image img = new ImageIcon(Mat2BufferedImage(frame)).getImage();
            g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
            if (trackingActive && roi != null) {
                g.setColor(Color.GREEN); // Dibuixa un rectangle verd al voltant de la ROI
                g.drawRect(roi.x, roi.y, roi.width, roi.height);
            }
        }
    }

    /**
     * Converteix una matriu OpenCV en una imatge de tipus BufferedImage.
     * <p>
     * Aquesta funció converteix una matriu OpenCV en una imatge compatible amb Swing per poder ser visualitzada en el panell.
     * </p>
     * 
     * @param mat La matriu OpenCV que es vol convertir en una imatge.
     * @return La imatge convertida de tipus BufferedImage.
     */
    public BufferedImage Mat2BufferedImage(Mat mat) {
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
     * Atura el seguiment i allibera els recursos associats.
     * <p>
     * Aquest mètode atura la captura de vídeo, allibera la càmera i els recursos associats.
     * </p>
     */
    public void stop() {
        running = false;
    }

    /*
    // Mètode main per executar l'aplicació de manera independent
    public static void main(String[] args) {
        JFrame frame = new JFrame("Seguiment d'Objectes (Webcam)");
        ObjectTracking objectTrackingPanel = new ObjectTracking();
        frame.add(objectTrackingPanel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Atura el seguiment quan es tanqui la finestra
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                objectTrackingPanel.stop();
            }
        });
    }
    */
}
