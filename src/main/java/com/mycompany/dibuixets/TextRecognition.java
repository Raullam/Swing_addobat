package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgproc.Imgproc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * La clase {@code TextRecognition} permite realizar el reconocimiento de texto en tiempo real
 * desde una cámara web. Utiliza OpenCV para capturar los fotogramas de video y Tesseract OCR
 * para detectar el texto en las imágenes. La interfaz gráfica está construida con Swing.
 * 
 * <p> El sistema captura imágenes de la webcam, procesa las imágenes, y realiza OCR cada 6 segundos
 * para extraer texto. Además, el usuario puede guardar la imagen capturada y cerrar la ventana de captura. </p>
 * 
 * @author Usuario
 */
public class TextRecognition extends JPanel {
    private VideoCapture camera;  // Objeto para capturar video desde la cámara
    private Mat frame;  // Matriz que contiene el fotograma actual capturado desde la cámara
    private BufferedImage bufferedImage;  // Imagen en formato BufferedImage para mostrar en la interfaz gráfica
    private String capturedImagePath = "images/captured_image.jpg";  // Ruta para guardar la imagen capturada
    private JFrame frameWindow;  // Ventana principal de la interfaz gráfica

    /**
     * Constructor de la clase {@code TextRecognition}.
     * Inicializa la cámara, los botones y la ventana para la captura y visualización en tiempo real.
     * También configura el procesamiento de los fotogramas y la ejecución del OCR.
     */
    public TextRecognition() {
        // Cargar OpenCV
        System.load(Constants.FILE_PATH);

        // Inicializar la cámara
        camera = new VideoCapture(0, Videoio.CAP_DSHOW);
        frame = new Mat();

        // Crear botón para guardar imagen
        JButton saveButton = new JButton("Guardar Imagen");
        saveButton.addActionListener(e -> saveCapturedImage());

        // Crear botón para volver (cerrar la ventana)
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

        // Configurar ventana
        frameWindow = new JFrame("Real-Time Text Detection");
        frameWindow.setLayout(new BorderLayout());
        frameWindow.add(this, BorderLayout.CENTER); // Panel con la detección de texto en el centro
        frameWindow.add(buttonPanel, BorderLayout.SOUTH); // Los botones al sur
        frameWindow.setSize(640, 480);
        frameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Centrar la ventana en la pantalla
        frameWindow.setLocationRelativeTo(null);  // Esto centra la ventana en la pantalla
        frameWindow.setVisible(true);

        // Iniciar procesamiento de fotogramas
        new Thread(() -> {
            while (true) {
                if (camera.read(frame)) {
                    Mat grayFrame = new Mat();
                    Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
                    Imgproc.threshold(grayFrame, grayFrame, 100, 255, Imgproc.THRESH_BINARY);

                    // Solo ejecutar OCR cada 6 segundos
                    long startTime = System.currentTimeMillis();
                    if (startTime % 6000 < 100) { // Comprobar si han pasado 6 segundos
                        String detectedText = detectText(grayFrame);
                        if (detectedText != null && !detectedText.isEmpty()) {
                            System.out.println("Texto Detectado: " + detectedText);
                        }
                    }

                    // Convertir a BufferedImage y redibujar
                    bufferedImage = matToBufferedImage(frame);
                    repaint();

                    try {
                        Thread.sleep(100); // Pequeña pausa para reducir carga
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * Sobreescribe el método {@code paintComponent} para dibujar la imagen capturada desde la cámara
     * en el panel.
     * 
     * @param g El objeto {@code Graphics} utilizado para dibujar en el componente.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (bufferedImage != null) {
            g.drawImage(bufferedImage, 0, 0, this);  // Dibuja la imagen en el panel
        }
    }

    /**
     * Convierte un objeto {@code Mat} de OpenCV a un objeto {@code BufferedImage}.
     * Este método maneja tanto imágenes en escala de grises como en color.
     * 
     * @param mat El objeto {@code Mat} que contiene la imagen a convertir.
     * @return Un objeto {@code BufferedImage} equivalente a la imagen contenida en el {@code Mat}.
     */
    private BufferedImage matToBufferedImage(Mat mat) {
        int width = mat.width();
        int height = mat.height();
        int channels = mat.channels();
        BufferedImage image = (channels == 1) 
                ? new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY)
                : new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

        byte[] data = new byte[width * height * channels];
        mat.get(0, 0, data);  // Obtiene los datos de la imagen del objeto Mat
        image.getRaster().setDataElements(0, 0, width, height, data);  // Carga los datos en el BufferedImage

        return image;
    }

    /**
     * Detecta el texto de una imagen usando OCR con Tesseract.
     * 
     * @param frame El objeto {@code Mat} que contiene el fotograma de la cámara a procesar.
     * @return El texto detectado en el fotograma o {@code null} si no se detecta texto.
     */
    private String detectText(Mat frame) {
        ITesseract instance = new Tesseract();
        instance.setDatapath("src/tessdata");
        instance.setLanguage("eng");  // Establece el idioma a inglés

        BufferedImage image = matToBufferedImage(frame);  // Convierte el fotograma a BufferedImage
        try {
            return instance.doOCR(image);  // Realiza el OCR sobre la imagen
        } catch (Exception e) {
            e.printStackTrace();  // Captura cualquier excepción en caso de error durante el OCR
            return null;
        }
    }

    /**
     * Guarda la imagen capturada desde la cámara en el sistema de archivos.
     * El usuario ingresa el nombre del archivo y la imagen se guarda en la carpeta "images".
     */
    private void saveCapturedImage() {
        if (camera.isOpened() && frame != null && !frame.empty()) { // Comprovar que la càmera està oberta i el frame no està buit
            String fileName = JOptionPane.showInputDialog("Introduce el nombre para guardar la imagen:");
            if (fileName != null && !fileName.trim().isEmpty()) {
                File outputFile = new File("images/" + fileName + ".jpg");

                if (Imgcodecs.imwrite(outputFile.getAbsolutePath(), frame)) {
                    JOptionPane.showMessageDialog(this, "Imagen guardada como: " + outputFile.getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this, "Error al guardar la imagen.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "No se ha detectado ninguna imagen.");
        }
    }

    /**
     * Método principal que inicializa la aplicación y muestra la interfaz gráfica.
     * 
     * @param args Los argumentos de la línea de comandos (no utilizados en este caso).
     */
    public static void main(String[] args) {
        new TextRecognition();  // Crea e inicializa la aplicación de reconocimiento de texto
    }
}
