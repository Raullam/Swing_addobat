package com.mycompany.dibuixets.dll;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Preferences {
    // Ruta predeterminada de OpenCV (modifica esto según tu instalación)
    private static final String DEFAULT_OPENCV_PATH = "C:\\Users\\Rulox\\Downloads\\Nueva carpeta (83)\\dibuxets222\\dibuixets\\src\\main\\java\\com\\mycompany\\dibuixets\\dll\\opencv_java490.dll";

    public static String getOpenCVPath() {
        File preferenciasFolder = new File("data");
        if (!preferenciasFolder.exists()) {
            preferenciasFolder.mkdir();
        }
        File preferenciasFile = new File("data/preferencias.txt");
        HashMap<String, String> preferencias = new HashMap<>();

        if (preferenciasFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(preferenciasFile))) {
                while (br.ready()) {
                    String line = br.readLine();
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        preferencias.put(parts[0], parts[1]);
                    }
                }
                if (preferencias.containsKey("opencv") && new File(preferencias.get("opencv")).exists()) {
                    return preferencias.get("opencv");
                }
            } catch (Exception ex) {
                Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Si no hay una ruta válida, intenta usar la ruta predeterminada
        if (new File(DEFAULT_OPENCV_PATH).exists()) {
            saveOpenCVPath(preferenciasFile, DEFAULT_OPENCV_PATH);
            return DEFAULT_OPENCV_PATH;
        }

        // Si la ruta predeterminada no existe, pedir al usuario
        return getNewRoute(preferenciasFile);
    }

    private static String getNewRoute(File preferenciasFile) {
        JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(dialog, "No se ha encontrado la ruta de OpenCV o no es válida. Por favor, seleccione la librería OpenCV.", "Error", JOptionPane.ERROR_MESSAGE);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona la librería OpenCV");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String opencvPath = selectedFile.getAbsolutePath();
            
            saveOpenCVPath(preferenciasFile, opencvPath);
            return opencvPath;
        }
        return null;
    }

    private static void saveOpenCVPath(File preferenciasFile, String opencvPath) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(preferenciasFile, false))) { // Sobreescribir archivo
            bw.write("opencv," + opencvPath);
            bw.newLine();
        } catch (IOException ex) {
            Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
