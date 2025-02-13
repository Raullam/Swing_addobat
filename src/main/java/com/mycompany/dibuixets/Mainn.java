package com.mycompany.dibuixets;

import com.mycompany.dibuixets.dll.Preferences;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Classe principal que crea el frame i el menú de l'aplicació.
 * Aquesta classe és el punt d'entrada de la GUI i gestiona la navegació entre els diferents panells.
 *
 * @author Raül, Miquel Angel, Alejandro, Magi
 * @version 1.0
 * @since 2025-02-11
 */
public class Mainn {

    /**
     * Mètode principal que inicialitza el programa i carrega la llibreria OpenCV.
     * 
     * @param args Arguments de la línia de comandes (no utilitzats en aquest cas).
     */
    public static void main(String[] args) {
        // Carregar la llibreria OpenCV
        String opencvPath = Preferences.getOpenCVPath();
        if (opencvPath != null) {
            System.load(opencvPath);
        } else {
            JOptionPane.showMessageDialog(null, "No s'ha pogut carregar la llibreria OpenCV.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Crear el frame principal
        JFrame mainFrame = new JFrame("Editor d'Imatges - Menú Principal");
        mainFrame.setLayout(new BorderLayout());
        mainFrame.add(createMainMenu(mainFrame), BorderLayout.CENTER);
        mainFrame.setSize(400, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
    }

    /**
     * Crea el menú principal amb botons per accedir a les diferents funcionalitats.
     *
     * @param mainFrame El frame principal que conté els botons.
     * @return Un JPanel amb els botons del menú principal.
     */
    private static JPanel createMainMenu(JFrame mainFrame) {
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Crear els botons de funcionalitats
        JButton button1 = new JButton("Detector de Cares");
        JButton button2 = new JButton("Croma");
        JButton button3 = new JButton("Guardar Imatge");
        JButton button4 = new JButton("Paint 2030");
        JButton button5 = new JButton("Object Tracking");
        JButton button6 = new JButton("Detector de Text"); // Nuevo botón

        // Establir el tamany preferit dels botons
        Dimension buttonSize = new Dimension(200, 40);
        button1.setPreferredSize(buttonSize);
        button2.setPreferredSize(buttonSize);
        button3.setPreferredSize(buttonSize);
        button4.setPreferredSize(buttonSize);
        button5.setPreferredSize(buttonSize);
        button6.setPreferredSize(buttonSize);  // Tamaño del nuevo botón

        // Acció per al botó de Detector de Cares
        button1.addActionListener(e -> {
            JPanel j = new RealTimeFaceDetection();
            j.setLayout(null);
        });

        // Acció per al botó de Croma
        button2.addActionListener(e -> openCromaPanel(mainFrame));

        // Acció per al botó de Guardar Imatge
        button3.addActionListener(e -> {
            JPanel j = new TextRecognition();
            j.setLayout(null);
        });

        // Acció per al botó Paint 2030
        button4.addActionListener(e -> openDrawingPanel(mainFrame));

        // Acció per al botó Object Tracking
        button5.addActionListener(e -> openObjectTrackingPanel(mainFrame));

        // Acció per al nou botó Detector de Text
        button6.addActionListener(e -> {
            new TextRecognition(); // Crear i mostrar el panell de TextRecognition
        });

        // Afegir els botons al panell
        buttonPanel.add(button1, gbc);
        gbc.gridy++;
        buttonPanel.add(button2, gbc);
        gbc.gridy++;
        buttonPanel.add(button3, gbc);
        gbc.gridy++;
        buttonPanel.add(button4, gbc);
        gbc.gridy++;
        buttonPanel.add(button5, gbc);
        gbc.gridy++;
        buttonPanel.add(button6, gbc);  // Afegir el nou botó

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(buttonPanel, BorderLayout.CENTER);

        return contentPanel;
    }

    /**
     * Obre el panell del Croma en el frame principal.
     *
     * @param mainFrame El frame principal.
     */
    private static void openCromaPanel(JFrame mainFrame) {
        Croma cromaPanel = new Croma();
        mainFrame.getContentPane().removeAll();
        mainFrame.add(cromaPanel, BorderLayout.CENTER);
        

        // Botó per tornar al menú principal
        JButton backButton = new JButton("Tornar");
        backButton.addActionListener(e -> {
            cromaPanel.stopCapture2(); 
            resetToMainMenu(mainFrame);
        });

        JPanel southPanel = new JPanel();
        southPanel.add(backButton);
        mainFrame.add(southPanel, BorderLayout.SOUTH);

        refreshFrame(mainFrame, 650, 600);
    }

    /**
     * Obre el panell de Paint 2030 amb controls per dibuixar sobre una imatge.
     *
     * @param mainFrame El frame principal.
     */
    private static void openDrawingPanel(JFrame mainFrame) {
        String imagePath = "images/,,nk.jpg";
        OpenCVDrawingApp2 drawingPanel = new OpenCVDrawingApp2(imagePath);
        drawingPanel.setPreferredSize(new Dimension(1200, 400));

        mainFrame.getContentPane().removeAll();
        mainFrame.add(drawingPanel, BorderLayout.WEST);

        JPanel controlPanel = OpenCVDrawingApp2.createControlPanel(drawingPanel);

        JButton backButton = new JButton("Tornar");
        backButton.addActionListener(e -> resetToMainMenu(mainFrame));

        controlPanel.add(backButton);
        mainFrame.add(controlPanel, BorderLayout.SOUTH);

        refreshFrame(mainFrame, 1200, 500);
    }

    /**
     * Obre el panell de seguiment d'objectes en el frame principal.
     *
     * @param mainFrame El frame principal.
     */
    private static void openObjectTrackingPanel(JFrame mainFrame) {
        ObjectTracking objectTrackingPanel = new ObjectTracking();
        mainFrame.getContentPane().removeAll();
        mainFrame.add(objectTrackingPanel, BorderLayout.CENTER);

        JButton backButton = new JButton("Tornar");
        backButton.addActionListener(e -> {
            objectTrackingPanel.stop();
            resetToMainMenu(mainFrame);
        });

        JPanel southPanel = new JPanel();
        southPanel.add(backButton);
        mainFrame.add(southPanel, BorderLayout.SOUTH);

        refreshFrame(mainFrame, 800, 600);
    }

    /**
     * Reinicia el frame principal per mostrar el menú principal.
     *
     * @param mainFrame El frame principal.
     */
    private static void resetToMainMenu(JFrame mainFrame) {
        mainFrame.getContentPane().removeAll();
        mainFrame.add(createMainMenu(mainFrame), BorderLayout.CENTER);
        refreshFrame(mainFrame, 400, 400);
    }

    /**
     * Refresca el frame i centra la finestra.
     *
     * @param mainFrame El frame principal.
     * @param width Amplada del frame.
     * @param height Alçada del frame.
     */
    private static void refreshFrame(JFrame mainFrame, int width, int height) {
        mainFrame.setSize(width, height);
        mainFrame.revalidate();
        mainFrame.repaint();
        mainFrame.setLocationRelativeTo(null);
    }
}
