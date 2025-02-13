package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.util.Stack;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

/**
 * La clase {@code OpenCVDrawingApp2} permite dibujar sobre una imagen cargada usando OpenCV,
 * con varias formas y opciones de edición como deshacer, rehacer, borrado y dibujo libre.
 * La interfaz gráfica está construida con Swing y permite realizar diferentes operaciones de dibujo
 * sobre la imagen cargada, así como guardar los cambios realizados.
 *
 * <p>La clase también implementa un panel adicional que se puede hacer visible o invisible según lo desee el usuario.</p>
 *
 * @author Usuario
 */
public class OpenCVDrawingApp2 extends JPanel {
    private Mat image;  // Imagen cargada para realizar dibujos sobre ella
    private BufferedImage bufferedImage;  // Imagen en formato BufferedImage para mostrar en la interfaz gráfica
    private Point lastPoint;  // Última posición del ratón durante el dibujo
    private Color currentColor = Color.RED;  // Color actual para el dibujo
    private int strokeWidth = 2;  // Ancho de la línea de dibujo
    private boolean isErasing = false;  // Bandera para saber si estamos borrando
    private boolean isFreeDrawing = false;  // Bandera para saber si estamos dibujando libremente
    private String currentShape = "LINE";  // Forma seleccionada para el dibujo actual

    private Stack<Mat> undoStack = new Stack<>();  // Pila para deshacer cambios
    private Stack<Mat> redoStack = new Stack<>();  // Pila para rehacer cambios

    private JPanel myPanel;  // Panel adicional que se puede hacer visible o invisible

    /**
     * Constructor de la clase {@code OpenCVDrawingApp2}.
     * Inicializa la imagen, el panel y las configuraciones de los eventos del ratón.
     * 
     * @param imagePath La ruta de la imagen que se desea cargar y modificar.
     */
    public OpenCVDrawingApp2(String imagePath) {
        // Inicialización de la imagen y demás
        System.load(Constants.FILE_PATH);
        image = Imgcodecs.imread(imagePath);
        resizeImage();
        bufferedImage = matToBufferedImage(image);

        setPreferredSize(new Dimension(image.width(), image.height()));
        undoStack.push(image.clone());

        // Crear el panel adicional que estará oculto por defecto
        myPanel = new JPanel();
        myPanel.setBackground(Color.CYAN);
        myPanel.setPreferredSize(new Dimension(200, 200));
        myPanel.setVisible(false); // Establecer el panel como no visible por defecto

        // Agregar un MouseListener y MouseMotionListener para gestionar el dibujo
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint = e.getPoint();
                undoStack.push(image.clone());
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!isFreeDrawing && !isErasing && lastPoint != null) {
                    drawShape(e.getPoint(), true);
                    bufferedImage = matToBufferedImage(image);
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint != null) {
                    if (isFreeDrawing) {
                        drawFreeDraw(e.getPoint());
                    } else if (isErasing) {
                        erase(e.getPoint());
                    } else {
                        drawShape(e.getPoint(), false);
                    }
                    bufferedImage = matToBufferedImage(image);
                    repaint();
                }
            }
        });
    }

    /**
     * Redimensiona la imagen para ajustarse a un tamaño máximo de 800x800 píxeles
     * mientras mantiene la proporción original de la imagen.
     */
    private void resizeImage() {
        int maxWidth = 800;
        int maxHeight = 800;
        int width = image.width();
        int height = image.height();

        if (width > maxWidth || height > maxHeight) {
            double aspectRatio = (double) width / height;
            int newWidth = maxWidth;
            int newHeight = (int) (newWidth / aspectRatio);
            if (newHeight > maxHeight) {
                newHeight = maxHeight;
                newWidth = (int) (newHeight * aspectRatio);
            }

            Mat resizedImage = new Mat();
            Imgproc.resize(image, resizedImage, new org.opencv.core.Size(newWidth, newHeight));
            image = resizedImage;
            bufferedImage = matToBufferedImage(image);
            setPreferredSize(new Dimension(newWidth, newHeight));
        }
    }

    /**
     * Dibuja una figura (como un círculo, rectángulo, flecha o línea) entre dos puntos
     * sobre la imagen cargada.
     * 
     * @param currentPoint El punto donde termina la figura.
     * @param finalize Si es {@code true}, guarda el cambio en la pila de deshacer.
     */
    private void drawShape(Point currentPoint, boolean finalize) {
        image = undoStack.peek().clone();
        Scalar color = new Scalar(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());

        switch (currentShape) {
            case "CIRCLE":
                int radius = (int) lastPoint.distance(currentPoint);
                Imgproc.circle(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y), radius, color, strokeWidth);
                break;
            case "RECTANGLE":
                Imgproc.rectangle(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                        new org.opencv.core.Point(currentPoint.x, currentPoint.y), color, strokeWidth);
                break;
            case "ARROW":
                Imgproc.arrowedLine(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                        new org.opencv.core.Point(currentPoint.x, currentPoint.y), color, strokeWidth);
                break;
            case "LINE":
                Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                        new org.opencv.core.Point(currentPoint.x, currentPoint.y), color, strokeWidth);
                break;
        }
        if (finalize) {
            undoStack.push(image.clone());
        }
    }

    /**
     * Dibuja libremente sobre la imagen cargada, sin formas predeterminadas.
     * 
     * @param currentPoint El punto al que se va a dibujar.
     */
    private void drawFreeDraw(Point currentPoint) {
        Scalar color = new Scalar(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
        Imgproc.line(image, new org.opencv.core.Point(lastPoint.x, lastPoint.y),
                new org.opencv.core.Point(currentPoint.x, currentPoint.y), color, strokeWidth);
        lastPoint = currentPoint;
    }

    /**
     * Borra sobre la imagen cargada utilizando un círculo blanco (simulando un borrador).
     * 
     * @param currentPoint El punto en el que se va a borrar.
     */
    private void erase(Point currentPoint) {
        Scalar eraserColor = new Scalar(255, 255, 255);
        Imgproc.circle(image, new org.opencv.core.Point(currentPoint.x, currentPoint.y), strokeWidth * 2, eraserColor, -1);
        lastPoint = currentPoint;
    }

    /**
     * Sobrescribe el método {@code paintComponent} para dibujar la imagen cargada
     * en el panel.
     * 
     * @param g El objeto {@code Graphics} utilizado para dibujar en el componente.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(bufferedImage, 0, 0, this);
    }

    /**
     * Convierte una imagen {@code Mat} de OpenCV a un objeto {@code BufferedImage}.
     * 
     * @param mat La imagen {@code Mat} a convertir.
     * @return Un objeto {@code BufferedImage} equivalente a la imagen {@code Mat}.
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
     * Crea el panel de controles para la aplicación, permitiendo al usuario elegir
     * opciones como formas, colores y grosor de la línea, así como las acciones de deshacer y rehacer.
     * 
     * @param panel El panel principal de dibujo.
     * @return Un {@code JPanel} con los controles necesarios.
     */
    public static JPanel createControlPanel(OpenCVDrawingApp2 panel) {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        // Agregar botones de control
        JButton lineButton = new JButton("Línea");
        lineButton.addActionListener(e -> setDrawingMode(panel, "LINE", false, false));
        controlPanel.add(lineButton);

        JButton circleButton = new JButton("Círculo");
        circleButton.addActionListener(e -> setDrawingMode(panel, "CIRCLE", false, false));
        controlPanel.add(circleButton);

        JButton rectangleButton = new JButton("Rectángulo");
        rectangleButton.addActionListener(e -> setDrawingMode(panel, "RECTANGLE", false, false));
        controlPanel.add(rectangleButton);

        JButton arrowButton = new JButton("Flecha");
        arrowButton.addActionListener(e -> setDrawingMode(panel, "ARROW", false, false));
        controlPanel.add(arrowButton);

        JButton freeDrawButton = new JButton("Dibujo libre");
        freeDrawButton.addActionListener(e -> setDrawingMode(panel, "", true, false));
        controlPanel.add(freeDrawButton);

        JButton eraseButton = new JButton("Borrar");
        eraseButton.addActionListener(e -> setDrawingMode(panel, "", false, true));
        controlPanel.add(eraseButton);

        JButton undoButton = new JButton("Deshacer");
        undoButton.addActionListener(e -> panel.undo());
        controlPanel.add(undoButton);

        JButton redoButton = new JButton("Rehacer");
        redoButton.addActionListener(e -> panel.redo());
        controlPanel.add(redoButton);

        JButton colorButton = new JButton("Color");
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(panel, "Seleccionar Color", panel.currentColor);
            if (newColor != null) {
                panel.currentColor = newColor;
            }
        });
        controlPanel.add(colorButton);

        JSlider thicknessSlider = new JSlider(1, 20, panel.strokeWidth);
        thicknessSlider.addChangeListener(e -> panel.strokeWidth = thicknessSlider.getValue());
        controlPanel.add(thicknessSlider);

        JButton saveButton = new JButton("Guardar");
        saveButton.addActionListener(e -> panel.saveImage());
        controlPanel.add(saveButton);

        return controlPanel;
    }

    /**
     * Configura el modo de dibujo seleccionado, ya sea para formas, dibujo libre o borrado.
     * 
     * @param panel El panel de dibujo al que se le aplica el modo.
     * @param shape La forma seleccionada para dibujar.
     * @param freeDraw Si se está activando el dibujo libre.
     * @param erase Si se está activando el borrado.
     */
    private static void setDrawingMode(OpenCVDrawingApp2 panel, String shape, boolean freeDraw, boolean erase) {
        panel.currentShape = shape;
        panel.isFreeDrawing = freeDraw;
        panel.isErasing = erase;
    }

    /**
     * Deshace la última acción de dibujo realizada.
     */
    private void undo() {
        if (!undoStack.isEmpty()) {
            redoStack.push(image.clone());
            image = undoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    /**
     * Rehace la última acción de dibujo deshecha.
     */
    private void redo() {
        if (!redoStack.isEmpty()) {
            undoStack.push(image.clone());
            image = redoStack.pop();
            bufferedImage = matToBufferedImage(image);
            repaint();
        }
    }

    /**
     * Guarda la imagen actual en el sistema de archivos en formato JPEG.
     */
    private void saveImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Guardar imagen");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Archivos JPEG", "jpg", "jpeg"));
        int userChoice = fileChooser.showSaveDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            Imgcodecs.imwrite(file.getAbsolutePath(), image);
        }
    }
}
