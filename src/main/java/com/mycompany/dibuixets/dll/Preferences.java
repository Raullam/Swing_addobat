package com.mycompany.dibuixets.dll;

import javax.swing.*;
import java.io.*;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Preferences {
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
                if (preferencias.containsKey("opencv") && ((preferencias.get("opencv").endsWith("opencv_java490.dll"))||(preferencias.get("opencv").endsWith("libopencv_java460.so")))) {
                    try {
                        File file = new File(preferencias.get("opencv"));
                        return preferencias.get("opencv");
                    }
                    catch (Exception e){
                        getNewRoute(preferenciasFile);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return getNewRoute(preferenciasFile);
    }
    
    private static String getNewRoute(File preferenciasFile){
        JDialog dialog = new JDialog();
        dialog.setAlwaysOnTop(true);
        JOptionPane.showMessageDialog(dialog, "No se ha encontrado la ruta de OpenCV o no es valida. Por favor, seleccione la carpeta de OpenCV.", "Error", JOptionPane.ERROR_MESSAGE);
        
        // Si no existe la ruta en el archivo, abrir JFileChooser
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecciona la carpeta de OpenCV");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String opencvPath = selectedFile.getAbsolutePath();
            
            // Guardar la nueva preferencia en el archivo
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(preferenciasFile, true))) {
                bw.write("opencv," + opencvPath);
                bw.newLine();
            } catch (IOException ex) {
                Logger.getLogger(Preferences.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            return opencvPath;
        }
        return null;
    }
}