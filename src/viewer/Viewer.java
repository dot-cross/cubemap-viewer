package viewer;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author edu
 */
public class Viewer extends JFrame {

    private CubemapPanel cubemapPanel;
    private JFileChooser openFileChooser;
    private JMenuBar menuBar;
    private JMenu fileMenu;
    private JMenuItem openFile;
    private JMenuItem saveImage;
    private JMenuItem exit;
    private JMenu optionsMenu;
    private JMenuItem showReference;
    private JMenuItem showInfo;
    private JMenuItem nearest;
    private JMenuItem bilinear;
    private JMenuItem resetOrientation;
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
                    try {
                        Cubemap cubemap = Cubemap.loadCubemap(selectedDir.getAbsolutePath());
                        cubemapPanel.setCubemap(cubemap);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(Viewer.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        fileMenu.add(openFile);
        saveImage = new JMenuItem("Save image");
        saveImage.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveImage.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                // Save image
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
        showReference = new JMenuItem("Show reference");
        showReference.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 0));
        showReference.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                CubemapRenderer cubemapRenderer = cubemapPanel.getCubemapRenderer();
                if(cubemapRenderer != null){
                    boolean showReference = cubemapRenderer.isShowReference();
                    cubemapRenderer.showReference(!showReference);
                }
            }
        });

        optionsMenu.add(showReference);
        showInfo = new JMenuItem("Show Info");
        showInfo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, 0));
        showInfo.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean showInfo = cubemapPanel.isShowInfo();
                cubemapPanel.setShowInfo(!showInfo);
            }
            
        });
        optionsMenu.add(showInfo);
        optionsMenu.addSeparator();
        nearest = new JMenuItem("Nearest");
        nearest.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
        nearest.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                CubemapRenderer cubemapRenderer = cubemapPanel.getCubemapRenderer();
                if(cubemapRenderer != null){
                    cubemapRenderer.setLerp(false);
                }
            }
        });
        optionsMenu.add(nearest);
        bilinear = new JMenuItem("Bilinear");
        bilinear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0));
        bilinear.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                CubemapRenderer cubemapRenderer = cubemapPanel.getCubemapRenderer();
                if(cubemapRenderer != null){
                    cubemapRenderer.setLerp(true);
                }
            }
            
        });
        optionsMenu.add(bilinear);
        ButtonGroup bg = new ButtonGroup();
        bg.add(nearest);
        bg.add(bilinear);
        resetOrientation = new JMenuItem("Reset orientation");
        resetOrientation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        resetOrientation.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                cubemapPanel.resetOrientation();
            }
        });
        optionsMenu.addSeparator();
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
        pack();
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