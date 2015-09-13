package viewer;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import math.Matrix33;

/**
 *
 * @author edu
 */
public class Viewer extends JFrame {

    private CubemapPanel cubemapPanel;
    private JFileChooser openFileChooser;
    private SaveImageFileChooser saveFileChooser;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openFile;
    private JMenuItem saveImage;
    private JMenuItem exit;
    private JMenu optionsMenu;
    private JCheckBoxMenuItem showReference;
    private JCheckBoxMenuItem showInfo;
    private JMenuItem showUnwrapped;
    private JCheckBoxMenuItem nearest;
    private JCheckBoxMenuItem bilinear;
    private JMenuItem resetOrientation;
    private JCheckBoxMenuItem invertMouse;
    private JMenu helpMenu;
    private JMenuItem about;
    
    public Viewer() {
        setTitle("Cubemap Viewer");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                cubemapPanel.release();
            }
        });
        openFileChooser = new JFileChooser(System.getProperty("user.home"));
        openFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        String userHome = System.getProperty("user.home");
        saveFileChooser = new SaveImageFileChooser(userHome);
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        openFile = new JMenuItem("Open");
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openFile.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                int returnValue = openFileChooser.showOpenDialog(Viewer.this);
                if(returnValue == JFileChooser.APPROVE_OPTION){
                    File selectedDir = openFileChooser.getSelectedFile();
                    loadCubemap(selectedDir);
                }
            }
        });
        fileMenu.add(openFile);
        saveImage = new JMenuItem("Save screenshot");
        saveImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveImage.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                int returnValue = saveFileChooser.showSaveDialog(Viewer.this);
                if(returnValue == JFileChooser.APPROVE_OPTION){
                    File selectedFile = saveFileChooser.getSelectedFile();
                    saveImage(selectedFile);
                }
            }
        });
        fileMenu.add(saveImage);
        exit = new JMenuItem("Exit");
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exit.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                cubemapPanel.release();
                System.exit(0);
            }
        });
        fileMenu.add(exit);
        menuBar.add(fileMenu);
        
        optionsMenu = new JMenu("Options");
        showReference = new JCheckBoxMenuItem("Show reference");
        showReference.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
        showReference.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                CubemapRenderer cubemapRenderer = cubemapPanel.getCubemapRenderer();
                boolean showReference = cubemapRenderer.isShowReference();
                cubemapRenderer.showReference(!showReference);
            }
        });

        optionsMenu.add(showReference);
        showInfo = new JCheckBoxMenuItem("Show Info");
        showInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0));
        showInfo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean showInfo = cubemapPanel.isShowInfo();
                cubemapPanel.setShowInfo(!showInfo);
            }
        });
        optionsMenu.add(showInfo);
        showUnwrapped = new JMenuItem("Show unwrapped image");
        showUnwrapped.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                UnwrappedDialog dialog = new UnwrappedDialog(Viewer.this, true, cubemapPanel.getCubemap());
                dialog.setVisible(true);
            }
        });
        optionsMenu.add(showUnwrapped);
        optionsMenu.addSeparator();
        nearest = new JCheckBoxMenuItem("Nearest");
        nearest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        nearest.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                CubemapRenderer cubemapRenderer = cubemapPanel.getCubemapRenderer();
                cubemapRenderer.setLerp(false);
            }
        });
        optionsMenu.add(nearest);
        bilinear = new JCheckBoxMenuItem("Bilinear");
        bilinear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0));
        bilinear.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                CubemapRenderer cubemapRenderer = cubemapPanel.getCubemapRenderer();
                cubemapRenderer.setLerp(true);
            }
            
        });
        optionsMenu.add(bilinear);
        ButtonGroup bg = new ButtonGroup();
        bg.add(nearest);
        bg.add(bilinear);
        optionsMenu.addSeparator();
        invertMouse = new JCheckBoxMenuItem("Invert mouse");
        invertMouse.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean invertMouse = cubemapPanel.isInvertMouse();
                cubemapPanel.setInvertMouse(!invertMouse);
            }
        });
        optionsMenu.add(invertMouse);
        resetOrientation = new JMenuItem("Reset orientation");
        resetOrientation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        resetOrientation.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                cubemapPanel.resetOrientation();
            }
        });
        optionsMenu.add(resetOrientation);
        menuBar.add(optionsMenu);

        helpMenu = new JMenu("Help");
        about = new JMenuItem("About");
        helpMenu.add(about);
        about.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                JOptionPane.showMessageDialog(Viewer.this, "Eduardo Guerra - 2015", "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menuBar.add(helpMenu);
        setJMenuBar(menuBar);
        cubemapPanel = new CubemapPanel();
        cubemapPanel.init();
        add(cubemapPanel);
        saveImage.setEnabled(false);
        showReference.setEnabled(false);
        showInfo.setEnabled(false);
        showUnwrapped.setEnabled(false);
        nearest.setEnabled(false);
        bilinear.setEnabled(false);
        resetOrientation.setEnabled(false);
        invertMouse.setEnabled(false);
        pack();
    }

    private void loadCubemap(File cubemapDir){
        Cubemap cubemap = null;
        try {
            cubemap = Cubemap.loadCubemap(cubemapDir.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (cubemap == null) {
            JOptionPane.showMessageDialog(this, "Couldn't load cubemap", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Cubemap previousCubemap = cubemapPanel.getCubemap();
        if (previousCubemap == null) {
            CubemapRenderer cubemapRenderer = cubemapPanel.getCubemapRenderer();
            saveImage.setEnabled(true);
            showReference.setEnabled(true);
            showInfo.setEnabled(true);
            showUnwrapped.setEnabled(true);
            nearest.setEnabled(true);
            bilinear.setEnabled(true);
            invertMouse.setEnabled(true);
            resetOrientation.setEnabled(true);
            showReference.setSelected(cubemapRenderer.isShowReference());
            showInfo.setSelected(cubemapPanel.isShowInfo());
            boolean lerp = cubemapRenderer.isLerp();
            nearest.setSelected(!lerp);
            bilinear.setSelected(lerp);
            invertMouse.setSelected(cubemapPanel.isInvertMouse());
        }
        cubemapPanel.setCubemap(cubemap);
    }

    private void saveImage(File file) {
        CubemapRenderer cubemapRenderer = cubemapPanel.getCubemapRenderer();
        Cubemap cubemap = cubemapRenderer.getCubemap();
        int width = cubemapRenderer.getWidth();
        int height = cubemapRenderer.getHeight();
        boolean reference = cubemapRenderer.isShowReference();
        boolean lerp = cubemapRenderer.isLerp();
        Matrix33 orientation = cubemapRenderer.getOrientation();
        float fov = cubemapRenderer.getFov();
        SaveDialog saveDialog = new SaveDialog(Viewer.this, true, width, height, fov, reference, lerp);
        saveDialog.setVisible(true);
        if (saveDialog.getReturnStatus() == SaveDialog.RET_CANCEL) {
            return;
        }
        width = saveDialog.getOutputWidth();
        height = saveDialog.getOutputHeight();
        reference = saveDialog.isShowReference();
        lerp = saveDialog.isLerp();
        BufferedImage outputImage = CubemapRenderer.render(cubemap, orientation, fov, reference, lerp, width, height);
        String format = "jpg";
        String fileName = file.getName().toLowerCase();
        if(fileName.endsWith(".jpg")){
            format = "jpg";
        }else if(fileName.endsWith(".jpeg")){
            format = "jpeg";
        }else if(fileName.endsWith(".png")){
            format = "png";
        }else if(fileName.endsWith(".bmp")){
            format = "bmp";
        }
        try {
            ImageIO.write(outputImage, format, file);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Couldn't save image", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(new WindowsLookAndFeel());
                } catch (UnsupportedLookAndFeelException ex) {
                    Logger.getLogger(Viewer.class.getName()).log(Level.SEVERE, null, ex);
                }
                Viewer viewer = new Viewer();
                viewer.setVisible(true);
            }
        });
    }
    
}