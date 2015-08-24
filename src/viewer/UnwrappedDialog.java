package viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 *
 * @author edu
 */
public class UnwrappedDialog extends JDialog {

    private final Cubemap cubemap;

    public UnwrappedDialog(JFrame parent, boolean modal, Cubemap cubemap){
        super(parent, modal);
        setTitle("Cubemap Unwrapped");
        this.cubemap = cubemap;
        CubemapUnwrappedPanel panel = new CubemapUnwrappedPanel();
        add(panel);
        setPreferredSize(new Dimension(640, 480));
        pack();
        setLocation(parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
    }

    class CubemapUnwrappedPanel extends JPanel {

        private final Font f = new Font("Arial", Font.BOLD, 50);

        @Override
        protected void paintComponent(Graphics g) {
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, getWidth(), getHeight());
            int panelWidth = getWidth(), panelHeight = getHeight();
            int imageWidth = panelWidth / 4;
            int imageHeight = panelHeight / 3;
            g.setFont(f);
            FontMetrics m = g.getFontMetrics();
            int tx, ty, txtWidth, txtHeight, txtAscent;
            g.setColor(Color.BLUE);
            // Draw negative x
            g.drawImage(cubemap.images[Cubemap.NEGX], 0, imageHeight, imageWidth, imageHeight, null);
            txtWidth = m.stringWidth("-X");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = imageWidth / 2 - txtWidth / 2;
            ty = imageHeight + imageHeight / 2 - txtHeight / 2 + txtAscent;
            g.drawString("-X", tx, ty);
            // Draw positive z
            g.drawImage(cubemap.images[Cubemap.POSZ], imageWidth, imageHeight, imageWidth, imageHeight, null);
            txtWidth = m.stringWidth("+Z");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = imageWidth + imageWidth / 2 - txtWidth / 2;
            ty = imageHeight + imageHeight / 2 - txtHeight / 2 + txtAscent;
            g.drawString("+Z", tx, ty);
            // Draw positive x
            g.drawImage(cubemap.images[Cubemap.POSX], 2 * imageWidth, imageHeight, imageWidth, imageHeight, null);
            txtWidth = m.stringWidth("+X");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = 2 * imageWidth + imageWidth / 2 - txtWidth / 2;
            ty = imageHeight + imageHeight / 2 - txtHeight / 2 + txtAscent;
            g.drawString("+X", tx, ty);
            // Draw negative z
            g.drawImage(cubemap.images[Cubemap.NEGZ], 3 * imageWidth, imageHeight, imageWidth, imageHeight, null);
            txtWidth = m.stringWidth("-Z");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = 3 * imageWidth + imageWidth / 2 - txtWidth / 2;
            ty = imageHeight + imageHeight / 2 - txtHeight / 2 + txtAscent;
            g.drawString("-Z", tx, ty);
            // Draw postive y
            g.drawImage(cubemap.images[Cubemap.POSY], imageWidth, 0, imageWidth, imageHeight, null);
            txtWidth = m.stringWidth("+Y");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = imageWidth + imageWidth / 2 - txtWidth / 2;
            ty = imageHeight / 2 - txtHeight / 2 + txtAscent;
            g.drawString("+Y", tx, ty);
            // Draw negative y
            g.drawImage(cubemap.images[Cubemap.NEGY], imageWidth, 2 * imageHeight, imageWidth, imageHeight, null);
            txtWidth = m.stringWidth("-Y");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = imageWidth + imageWidth / 2 - txtWidth / 2;
            ty = 2 * imageHeight + imageHeight / 2 - txtHeight / 2 + txtAscent;
            g.drawString("-Y", tx, ty);
        }

    }

}
