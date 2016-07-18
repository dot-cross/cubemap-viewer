package viewer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Save dialog for screenshots.
 * @author edu
 */
public class SaveDialog extends JDialog {
    
    public static final int RET_CANCEL = 0;
    public static final int RET_OK = 1;
    private JLabel widthLabel;
    private JTextField widthTextField;
    private JLabel heightLabel;
    private JTextField heightTextField;
    private JLabel fovLabel;
    private JTextField fovTextField;
    private JLabel refColorLabel;
    private ColorPicker colorPicker;
    private JCheckBox referenceCheckBox;
    private JCheckBox lerpCheckBox;
    private JButton saveButton;
    private JButton cancelButton;
    private float fov;
    private boolean showReference;
    private boolean lerp;
    private int outputWidth, outputHeight;
    private int refColor;
    private int returnStatus =  RET_CANCEL;
    
    public SaveDialog(JFrame parent, boolean modal, int outputWidth, int outputHeight, float fov,  boolean lerp, boolean showReference, int refColor){
        super(parent, modal);
        setTitle("Save Options");
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.refColor = refColor;
        this.fov = fov;
        this.lerp = lerp;
        this.showReference = showReference;
        setResizable(false);
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setLayout(new GridLayout(0, 2, 3, 3));

        widthLabel = new JLabel("Image Width:");
        panel.add(widthLabel);
        widthTextField = new JTextField();
        widthTextField.setText(String.valueOf(outputWidth));
        panel.add(widthTextField);

        heightLabel = new JLabel("Image Height:");
        panel.add(heightLabel);
        heightTextField = new JTextField();
        heightTextField.setText(String.valueOf(outputHeight));
        panel.add(heightTextField);

        fovLabel = new JLabel("Field of view:");
        panel.add(fovLabel);
        fovTextField = new JTextField();
        fovTextField.setText(String.valueOf(fov));
        panel.add(fovTextField);

        refColorLabel = new JLabel("Ref Color:");
        panel.add(refColorLabel);
        colorPicker = new ColorPicker(new Color(refColor));
        panel.add(colorPicker);
        
        referenceCheckBox = new JCheckBox("Show reference", showReference);
        referenceCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SaveDialog.this.showReference = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        panel.add(referenceCheckBox);

        lerpCheckBox = new JCheckBox("Use lerp", lerp);
        lerpCheckBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                SaveDialog.this.lerp = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        panel.add(lerpCheckBox);

        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (validateInput()) {
                    close(RET_OK);
                }
            }
        });
        panel.add(saveButton);

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                close(RET_CANCEL);
            }
        });
        panel.add(cancelButton);
        add(panel);
        getRootPane().setDefaultButton(saveButton);
        pack();
        setLocation(parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
    }

    private void close(int retStatus) {
        this.returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    public int getReturnStatus() {
        return returnStatus;
    }

    public float getFov() {
        return fov;
    }

    public boolean isShowReference() {
        return showReference;
    }

    public boolean isLerp() {
        return lerp;
    }

    public int getOutputWidth() {
        return outputWidth;
    }

    public int getOutputHeight() {
        return outputHeight;
    }
    
    public int getRefColor(){
        return refColor;
    }
    
    private boolean validateInput(){
        try{
            outputWidth = Integer.parseInt(widthTextField.getText());
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "Invalid value for Image Width", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(outputWidth <= 0){
            JOptionPane.showMessageDialog(this, "Invalid value. Image Width <= 0", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try{
            outputHeight = Integer.parseInt(heightTextField.getText());
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "Invalid value for Image Height", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(outputHeight <= 0){
            JOptionPane.showMessageDialog(this, "Invalid value. Image Height <= 0", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        try{
            fov = Float.parseFloat(fovTextField.getText());
        }catch(NumberFormatException e){
            JOptionPane.showMessageDialog(this, "Invalid value for Field of View", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if(fov < 2.0f || fov > 175.0f){
            JOptionPane.showMessageDialog(this, "Invalid value for Field of View. Valid range [2, 175]", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        refColor = colorPicker.getColor().getRGB();
        return true;
    }
}

class ColorPicker extends JButton implements ActionListener {

    private Color color;

    public ColorPicker(Color color) {
        this.color = color;
        addActionListener(this);
    }

    public void setColor(Color color) {
        this.color = color;
        repaint();
    }

    public Color getColor() {
        return color;
    }

    @Override
    public void paintComponent(Graphics g) {
        if (color != null) {
            g.setColor(color);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Color newColor = JColorChooser.showDialog(this, "Pick a color", color);
        if (newColor != null) {
            color = newColor;
            repaint();
        }
    }

}
