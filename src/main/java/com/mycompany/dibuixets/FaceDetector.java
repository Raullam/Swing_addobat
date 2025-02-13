/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Constants;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 * Clase que implementa un detector de caras utilizando OpenCV.
 * Esta clase carga una imagen, convierte la imagen a escala de grises,
 * aplica un ecualizador de histograma para mejorar el contraste, y luego
 * utiliza un clasificador en cascada para detectar las caras en la imagen.
 * Finalmente, dibuja un rectángulo alrededor de las caras detectadas y guarda
 * la imagen con las caras marcadas.
 *
 * @author Usuario
 */
public class FaceDetector {
    
    /**
     * Método principal que carga la librería OpenCV, lee la imagen desde el disco
     * y llama al método para detectar las caras en la imagen.
     *
     * @param args Argumentos de línea de comandos (no se utilizan en este caso).
     */
    public static void main(String[] args){
        // Cargar la librería de OpenCV
        System.load(Constants.FILE_PATH);
        
        // Leer la imagen desde el disco
        Mat image = Imgcodecs.imread("images/,,nk.jpg");
        
        // Llamar al método de detección de caras
        detectAndSave(image);
    }

    /**
     * Método que detecta las caras en la imagen proporcionada y las marca con rectángulos.
     * Luego guarda la imagen resultante con las caras detectadas en un archivo.
     *
     * @param image Imagen en la que se realizarán las detecciones de caras.
     */
    private static void detectAndSave(Mat image) {
        // Crear un objeto MatOfRect para almacenar las caras detectadas
        MatOfRect faces = new MatOfRect();
        
        // Convertir la imagen original a escala de grises
        Mat grayFrame = new Mat();
        Imgproc.cvtColor(image, grayFrame, Imgproc.COLOR_BGR2GRAY);
        
        // Mejorar el contraste de la imagen en escala de grises
        Imgproc.equalizeHist(grayFrame, grayFrame);
        
        // Establecer el tamaño mínimo de las caras detectables
        int height = grayFrame.height();
        int absoluteFaceSize = 0;
        
        // Si la altura es mayor que un 20% de la imagen, establecer el tamaño mínimo de cara
        if (Math.round(height * 0.2f) > 0) {
            absoluteFaceSize = Math.round(height * 0.2f);
        }
        
        // Crear un objeto CascadeClassifier para cargar el clasificador en cascada
        CascadeClassifier faceCascade = new CascadeClassifier();
        
        // Cargar el archivo entrenado para la detección de caras
        faceCascade.load("data/haarcascade_frontalface_alt2.xml");
        
        // Detectar las caras en la imagen
        faceCascade.detectMultiScale(
            grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, 
            new Size(absoluteFaceSize, absoluteFaceSize), new Size()
        );
        
        // Obtener las caras detectadas como un array de rectángulos
        Rect[] faceArray = faces.toArray();
        
        // Dibujar rectángulos alrededor de las caras detectadas
        for (int i = 0; i < faceArray.length; i++) {
            Imgproc.rectangle(image, faceArray[i], new Scalar(255, 123, 45), 3);
        }
        
        // Guardar la imagen con las caras marcadas en un archivo de salida
        Imgcodecs.imwrite("images/output.jpg", image);
    }
}
