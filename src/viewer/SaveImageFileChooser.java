package viewer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicFileChooserUI;

/**
 *
 * @author edu
 */
public class SaveImageFileChooser extends JFileChooser {

    public SaveImageFileChooser(String currentDirectoryPath) {
        super(currentDirectoryPath);
        setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filters[] = getChoosableFileFilters();
        for (FileFilter filter : filters) {
            removeChoosableFileFilter(filter);
        }
        FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPG Image", "jpg");
        addChoosableFileFilter(jpgFilter);
        FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("PNG Image", "png");
        addChoosableFileFilter(pngFilter);
        FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter("BMP Image", "bmp");
        addChoosableFileFilter(bmpFilter);
        setFileFilter(jpgFilter);
        addPropertyChangeListener(JFileChooser.FILE_FILTER_CHANGED_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                FileNameExtensionFilter filter = (FileNameExtensionFilter) evt.getNewValue();
                String extensions[] = filter.getExtensions();
                String currentName = ((BasicFileChooserUI) getUI()).getFileName();
                int index = currentName.indexOf(".");
                if (index != -1) {
                    currentName = currentName.substring(0, index);
                }
                currentName += "." + extensions[0];
                setSelectedFile(new File(currentName));
            }
        });
    }

    @Override
    public void approveSelection() {
        File file = getSelectedFile();
        String path = file.getAbsolutePath().toLowerCase();
        FileNameExtensionFilter filter = (FileNameExtensionFilter) getFileFilter();
        String extensions[] = filter.getExtensions();
        if (!path.endsWith(".png") && !path.endsWith(".jpg") && !path.endsWith(".jpeg") && !path.endsWith(".bmp")) {
            path += "." + extensions[0];
            file = new File(path);
            setSelectedFile(file);
        }
        if (file.exists()) {
            int optionSelected = JOptionPane.showConfirmDialog(this, "Do you want to overwrite the existing file?", file.getName() + " already exists", JOptionPane.YES_NO_OPTION);
            switch (optionSelected) {
                case JOptionPane.YES_OPTION:
                    super.approveSelection();
                    return;
                case JOptionPane.NO_OPTION:
                    return;
                case JOptionPane.CLOSED_OPTION:
                    cancelSelection();
                    return;
            }
        }
        super.approveSelection();
    }
}