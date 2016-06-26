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
import javax.swing.JComponent;
import math.MathUtils;
import math.Matrix33;

/**
 * Component for visualization of cubemaps.
 * @author edu
 */
public class CubemapViewer extends JComponent {

    private CubemapRenderer cubemapRenderer;
    private BufferedImage outputImage;
    private float angleX = 0.0f, angleY = 0.0f;
    private CameraAdapter cameraAdapter;
    private boolean invertMouse;

    /**
     * Creates a new Cubemap Viewer.
     */
    public CubemapViewer(){
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(cubemapRenderer != null){
                    Dimension d = getSize();
                    cubemapRenderer.setRenderSize(d.width, d.height);
                }
            }
        });

        cameraAdapter = new CameraAdapter();
        addMouseListener(cameraAdapter);
        addMouseMotionListener(cameraAdapter);
        addMouseWheelListener(cameraAdapter);
    }
    
    /**
     * Init cubemap renderer.
     */
    public void init() {
        if (cubemapRenderer == null) {
            cubemapRenderer = new CubemapRenderer(this);
            cubemapRenderer.init();
        }
    }
    
    /**
     * Stop cubemap renderer.
     */
    public void release(){
        if(cubemapRenderer != null){
            cubemapRenderer.release();
            cubemapRenderer = null;
        }
    }
    
    /**
     * Get cubemap renderer.
     * @return Cubemap renderer of viewer.
     */
    public CubemapRenderer getCubemapRenderer(){
        return cubemapRenderer;
    }
    
    /**
     * Checks if invert mouse is enabled.
     * @return true if inverted, false otherwise.
     */
    public boolean isInvertMouse(){
        return invertMouse;
    }
    
    /**
     * Set if invert the mouse.
     * @param invert true to invert, false otherwise.
     */
    public void setInvertMouse(boolean invert){
        this.invertMouse = invert;
        if(invert){
            cameraAdapter.invert = -1.0f;
        }else{
            cameraAdapter.invert = 1.0f;
        }
    }
    
    /**
     * Reset orientation to default value.
     */
    public void resetOrientation(){
        angleX = 0.0f;
        angleY = 0.0f;
        if(cubemapRenderer != null){
            cubemapRenderer.setOrientation(Matrix33.identity());
        }
    }
    
    /**
     * Get current cubemap.
     * @return Cubemap image.
     */
    public Cubemap getCubemap(){
        Cubemap cubemap = null;
        if(cubemapRenderer != null){
            cubemap = cubemapRenderer.getCubemap();
        }
        return cubemap;
    }

    /**
     * Set new cubemap to be visualized.
     * @param cubemap Cubemap image
     */
    public void setCubemap(Cubemap cubemap){
        if(cubemapRenderer != null){
            cubemapRenderer.setCubemap(cubemap);
        }
    }

    /**
     * Set reference to rendered image. Only used by Cubemap Renderer.
     * @param outputImage Rendered image.
     */
    public void setOutputImage(BufferedImage outputImage){
        this.outputImage = outputImage;
    }
    
    /**
     * Swing callback to paint the component.
     * Only used to restore component content.
     * @param g Graphics context
     */
    @Override
    public void paintComponent(Graphics g) {
        if (outputImage == null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        } else {
            g.drawImage(outputImage, 0, 0, null);
        }
        
    }

    /**
     * Mouse adapter for control of camera orientation and field of view.
     */
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
            //Disable lerp before start mouse dragging.
            cubemapRenderer.setLerp(false);
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
        public void mouseReleased(MouseEvent e){
            if(cubemapRenderer == null){
                return;
            }
            //Restore lerp on mouse release.
            cubemapRenderer.setLerp(true);
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

