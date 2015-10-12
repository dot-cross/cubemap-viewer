package viewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import javax.swing.JPanel;
import math.MathUtils;
import math.Matrix33;

/**
 *
 * @author edu
 */
public class CubemapPanel extends JPanel {

    private CubemapRenderer cubemapRenderer;
    private BufferedImage outputImage;
    private float angleX = 0.0f, angleY = 0.0f;
    private long frames, lastTime;
    private float fps;
    private final Color fontBgColor = new Color(0, 0, 0, 80);
    private final DecimalFormat df = new DecimalFormat("###.##");
    private boolean showInfo = true;
    private CameraAdapter cameraAdapter;
    private boolean invertMouse;

    public CubemapPanel(){
        frames = 0;
        lastTime = 0;
        fps = 0.0f;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(cubemapRenderer != null){
                    Dimension d = getSize();
                    cubemapRenderer.setWindowSize(d.width, d.height);
                }
            }
        });
        cameraAdapter = new CameraAdapter();
        addMouseListener(cameraAdapter);
        addMouseMotionListener(cameraAdapter);
        addMouseWheelListener(cameraAdapter);
    }
    
    public void init() {
        if (cubemapRenderer == null) {
            cubemapRenderer = new CubemapRenderer(this);
            cubemapRenderer.init();
        }
    }
    
    public void release(){
        if(cubemapRenderer != null){
            cubemapRenderer.release();
            cubemapRenderer = null;
        }
    }
    
    public CubemapRenderer getCubemapRenderer(){
        return cubemapRenderer;
    }

    public boolean isShowInfo(){
        return showInfo;
    }
    
    public void setShowInfo(boolean showInfo){
        this.showInfo = showInfo;
        repaint();
    }
    
    public boolean isInvertMouse(){
        return invertMouse;
    }
    
    public void setInvertMouse(boolean invert){
        this.invertMouse = invert;
        if(invert){
            cameraAdapter.invert = -1.0f;
        }else{
            cameraAdapter.invert = 1.0f;
        }
    }
    
    public void resetOrientation(){
        angleX = 0.0f;
        angleY = 0.0f;
        if(cubemapRenderer != null){
            cubemapRenderer.setOrientation(Matrix33.identity());
        }
    }
    
    public Cubemap getCubemap(){
        Cubemap cubemap = null;
        if(cubemapRenderer != null){
            cubemap = cubemapRenderer.getCubemap();
        }
        return cubemap;
    }

    public void setCubemap(Cubemap cubemap){
        if(cubemapRenderer != null){
            cubemapRenderer.setCubemap(cubemap);
        }
    }

    public void setOutputImage(BufferedImage outputImage){
        this.outputImage = outputImage;
        fps();
        repaint();
    }
    
    private void fps(){
        frames++;
        long time = System.currentTimeMillis();
        long diff = time - lastTime;
        if(diff >= 1000){
            fps = frames * 1000.0f / diff;
            frames = 0;
            lastTime = time;
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        if (cubemapRenderer == null || outputImage == null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }
        g.drawImage(outputImage, 0, 0, null);
        if(showInfo){
            float fov = cubemapRenderer.getFov();
            g.setColor(fontBgColor);
            g.fillRect(5, 5, 110, 55);
            g.setColor(Color.WHITE);
            g.drawString("FPS: "+df.format(fps), 15, 20);
            g.drawString("FOV: "+df.format(fov), 15, 35);
            g.drawString("FILTER: "+ (cubemapRenderer.isLerp() ? "Bilinear": "Nearest"), 15, 50);
        }
    }

    class CameraAdapter extends MouseAdapter {

        private int startX, startY;
        public float invert = 1.0f;
        
        @Override
        public void mousePressed(MouseEvent e) {
            if(cubemapRenderer == null){
                return;
            }
            startX = e.getX();
            startY = e.getY();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if(cubemapRenderer == null){
                return;
            }
            int endX = e.getX(), endY = e.getY();
            int windowWidth = getWidth(), windowHeight = getHeight();
            float aspectRatio = (float)windowWidth / (float)windowHeight;
            float fov = cubemapRenderer.getFov();
            float diffX = endX - startX, diffY = endY - startY;
            diffX *= invert; diffY *= invert;
            startX = endX; startY = endY;
            angleY += fov * (float) diffX / (float) windowWidth;
            angleX += (fov / aspectRatio) * (float) diffY / (float) windowHeight;
            angleX = MathUtils.clamp(angleX, -90.0f, 90.0f);
            Matrix33 rotX = Matrix33.rotateX((float) Math.toRadians(angleX));
            Matrix33 rotY = Matrix33.rotateY((float) Math.toRadians(angleY));
            Matrix33 orientation = rotY.mult(rotX);
            cubemapRenderer.setOrientation(orientation);
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if(cubemapRenderer == null){
                return;
            }
            float fov = cubemapRenderer.getFov();
            fov += e.getWheelRotation();
            cubemapRenderer.setFov(fov);
        }
    }

}

