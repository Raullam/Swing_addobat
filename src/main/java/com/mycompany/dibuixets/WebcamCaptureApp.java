package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.opencv.imgproc.Imgproc;

/**
 * La clase WebcamCaptureApp permite capturar imágenes desde la webcam en tiempo real
 * y guardarlas en el sistema de archivos. Utiliza la biblioteca OpenCV para capturar
 * el video y Swing para mostrar la imagen en una interfaz gráfica de usuario.
 * 
 * <p> La interfaz gráfica consiste en una ventana con un botón para capturar una imagen 
 * y un área para mostrar el video capturado. </p>
 * 
 * <p> El flujo de trabajo consiste en iniciar la aplicación, visualizar el video en tiempo real,
 * y al presionar el botón "Capturar", se guarda la imagen en el disco con el nombre ingresado
 * por el usuario. </p>
 * 
 * @author Usuario
 */
public class WebcamCaptureApp extends JFrame {
    private JLabel imageLabel;  // Etiqueta para mostrar la imagen capturada
    private VideoCapture capture;  // Objeto que maneja la captura de video
    private Mat frame;  // Matriz que contiene el cuadro de video actual
    private boolean capturing = false;  // Estado de la captura de video

    /**
     * Constructor de la clase WebcamCaptureApp.
     * Inicializa la interfaz gráfica y configura el botón para capturar imágenes.
     */
    public WebcamCaptureApp() {
        setTitle("Captura d'Imatges de la Webcam");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear un JLabel para mostrar la imagen capturada
        imageLabel = new JLabel();
        add(imageLabel, BorderLayout.CENTER);

        // Crear un botón para capturar la imagen
        JButton captureButton = new JButton("Capturar");
        captureButton.addActionListener(e -> captureImage());
        add(captureButton, BorderLayout.SOUTH);
    }

    /**
     * Captura la imagen actual de la webcam y la guarda en un archivo.
     * 
     * <p> El usuario es solicitado para ingresar el nombre del archivo,
     * luego se guarda la imagen en la carpeta 'images'. </p>
     */
    private void captureImage() {
        String fileName = JOptionPane.showInputDialog(this, "Introdueix el nom del fitxer:");
        if (fileName != null && !fileName.trim().isEmpty()) {
            // Crear el archivo de salida y guardar la imagen
            File outputFile = new File("images/" + fileName + ".jpg");
            Imgcodecs.imwrite(outputFile.getAbsolutePath(), frame);
            JOptionPane.showMessageDialog(this, "Imatge desada com: " + outputFile.getAbsolutePath());
        }
    }

    /**
     * Inicia la captura de video desde la cámara.
     * 
     * <p> Este método carga la biblioteca de OpenCV, configura la cámara
     * y empieza a capturar los cuadros de video en un hilo de fondo. </p>
     */
    public void start() {
        // Cargar la librería de OpenCV
        System.load(Constants.FILE_PATH);
        capture = new VideoCapture(0);  // Inicia la captura de video
        frame = new Mat();

        // Verificar si la cámara está disponible
        if (!capture.isOpened()) {
            JOptionPane.showMessageDialog(this, "No s'ha pogut obrir la webcam.");
            return;
        }

        capturing = true;
        new Thread(() -> {
            while (capturing) {
                capture.read(frame);  // Capturar el cuadro actual
                if (!frame.empty()) {
                    BufferedImage img = matToBufferedImage(frame);  // Convertir el Mat a BufferedImage
                    ImageIcon icon = new ImageIcon(img);  // Crear un icono para mostrar la imagen
                    imageLabel.setIcon(icon);  // Actualizar la etiqueta con la nueva imagen
                    imageLabel.repaint();  // Redibujar la etiqueta
                }
            }
        }).start();
    }

    /**
     * Convierte un objeto {@code Mat} de OpenCV a un objeto {@code BufferedImage}.
     * Este método utiliza la codificación de imagen JPEG para convertir los datos
     * en formato de bytes y luego crea un BufferedImage.
     * 
     * @param mat El objeto Mat que contiene la imagen de OpenCV.
     * @return Un objeto BufferedImage equivalente a la imagen contenida en el Mat.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", mat, matOfByte);  // Codificar la imagen a formato JPEG
        byte[] byteArray = matOfByte.toArray();  // Convertir a array de bytes
        BufferedImage img = null;
        try {
            // Convertir el array de bytes a BufferedImage
            img = ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (IOException e) {
            e.printStackTrace();  // Capturar excepciones si ocurre un error durante la conversión
        }
        return img;
    }

    /**
     * Método principal que inicia la aplicación y muestra la ventana.
     * 
     * @param args Los argumentos de la línea de comandos (no utilizados en este caso).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            WebcamCaptureApp app = new WebcamCaptureApp();  // Crear la instancia de la aplicación
            app.setVisible(true);  // Hacer visible la ventana
            app.start();  // Iniciar la captura de video
        });
    }
}
