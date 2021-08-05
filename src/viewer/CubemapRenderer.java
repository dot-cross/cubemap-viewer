package viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import math.Matrix33;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import math.MathUtils;
import math.Vector2D;
import math.Vector3D;
import java.awt.Graphics;
import java.text.DecimalFormat;

/**
 * Class for interactive rendering of cubemaps.
 * Performs processing and drawing of new images asynchronously.
 * @author edu
 */
public class CubemapRenderer extends Thread {
    
    /**
     * Inner class for render parameters.
     */
    private static class RenderParams {

        public static final int RP_CUBEMAP = 1;
        public static final int RP_WINDOW_SIZE = 1 << 1;
        public static final int RP_FOV = 1 << 2;
        public static final int RP_ORIENTATION = 1 << 3;
        public static final int RP_REFERENCE = 1 << 4;
        public static final int RP_LERP = 1 << 5;
        public static final int RP_SHOW_INFO = 1 << 6;
        public static final int RP_REF_COLOR = 1 << 7;
        public static final int RP_RENDER_TYPE = 1 << 8;
        public static final int RP_EQUIRECT_OFFSET = 1 << 9;
        public static final int RP_ALL = RP_CUBEMAP | RP_WINDOW_SIZE | RP_FOV | RP_ORIENTATION | RP_REFERENCE | RP_LERP | RP_SHOW_INFO | RP_REF_COLOR | RP_RENDER_TYPE | RP_EQUIRECT_OFFSET;
        public int flags;
        public int renderType;
        public Cubemap cubemap;
        public int width, height;
        public float fov;
        public Matrix33 orientation;
        public boolean showReference;
        public boolean lerp;
        public boolean showInfo;
        public int refColor;
        public float equiRectOffset;
        
        public RenderParams(){
            fov = 75.0f;
            orientation = new Matrix33(1.0f);
            lerp = true;
            showInfo = true;
            refColor = 0x000000FF;
        }

        public RenderParams copy(){
            RenderParams copy = new RenderParams();
            copy.flags = flags;
            copy.cubemap = cubemap;
            copy.width = width;
            copy.height = height;
            copy.fov = fov;
            copy.orientation.assign(orientation);
            copy.showReference = showReference;
            copy.lerp = lerp;
            copy.showInfo = showInfo;
            copy.refColor = refColor;
            copy.equiRectOffset = equiRectOffset;
            return copy;
        }
    }
    
    public static final int RT_PERSPECTIVE = 0;
    public static final int RT_EQUIRECT = 1;
    public static final int RT_UNWRAPPED = 2;
    
    private final CubemapViewer viewer;
    private int renderType;
    private Cubemap cubemap;
    private final RenderParams rp;
    private BufferedImage colorBufferImage;
    private int colorBuffer[];
    private int windowWidth, windowHeight;
    private final float projDistance = 5.0f;
    private float fov, aspectRatio;
    private float windowLeft, windowRight, xRange;
    private float windowBottom, windowTop, yRange;
    private final Matrix33 orientation;
    private boolean showReference, showInfo, lerp;
    private int refColor;
    private float equiRectOffset;
    private boolean alive;
    private int avalaibleProcessors;
    private ImageProcessor processors[];
    private long frames, lastTime;
    private float fps;
    private final Color fontBgColor = new Color(0, 0, 0, 80);
    private final DecimalFormat df = new DecimalFormat("###.##");
    private final Font font = new Font("Tahoma", Font.PLAIN, 11);
    private final int sphereSamples = 16;
    private final Vector3D sphereVec[][];
    
    /**
     * Creates a new Cubemap Renderer
     * @param viewer The component to draw.
     */
    public CubemapRenderer(CubemapViewer viewer){
        super("Cubemap Renderer");
        if(viewer == null){
            throw new NullPointerException("Cubemap Viewer is null");
        }
        this.viewer = viewer;
        rp = new RenderParams();
        fov = 75.0f;
        orientation = new Matrix33(1.0f);
        showInfo = true;
        lerp = true;
        refColor = 0x000000FF;
        //Precalculate points on the unit sphere
        sphereVec = new Vector3D[sphereSamples][sphereSamples]; 
        float u, v;
        for(int i = 0; i < sphereSamples; ++i) {
            v = (float)i/(float)(sphereSamples-1);
            for(int j = 0; j < sphereSamples; ++j) {
                u = (sphereSamples-1-j+0.5f)/(float)sphereSamples;
                Vector3D dir = new Vector3D();
                dir.x = (float)(Math.cos(2.0*Math.PI*u)*Math.sin(Math.PI*v));
                dir.y = (float)Math.cos(Math.PI*v);
                dir.z = (float)(Math.sin(2.0*Math.PI*u)*Math.sin(Math.PI*v));
                sphereVec[i][j] = dir;
            }
        }
    }
    
    /**
     * Start processing of new parameters and drawing. Ignored if already started.
     */
    public void init(){
        if (processors == null) {
            avalaibleProcessors = Runtime.getRuntime().availableProcessors();
            processors = new ImageProcessor[avalaibleProcessors];
            for (int i = 0; i < avalaibleProcessors; i++) {
                processors[i] = new ImageProcessor();
                processors[i].start();
            }
            alive = true;
            start();
        }
    }
    
    /**
     * Stop processing of new parameters and drawing. Ignored if already stopped.
     */
    public void release(){
        if(isAlive()){
            alive = false;
            try {
                join();
            } catch (InterruptedException ex) {
            }
            for (int i = 0; i < avalaibleProcessors; i++) {
                processors[i].alive = false;
                try {
                    processors[i].join();
                } catch (InterruptedException ex) {
                }
            }
            processors = null;
        }
    }
    
    private void calculateProjection(){
        aspectRatio = (float)windowWidth / (float)windowHeight;
        windowRight = (float) (projDistance * Math.tan(Math.toRadians(fov) / 2.0));
        windowLeft = -windowRight;
        xRange = windowRight * 2.0f;
        windowTop = windowRight / aspectRatio;
        windowBottom = -windowTop;
        yRange = windowTop * 2.0f;
    }
    
    /**
     * Set render type
     * @param type render type
     */
    public void setRenderType(int type) {
        synchronized(rp) {
            rp.flags |= RenderParams.RP_RENDER_TYPE;
            rp.renderType = type;
        }
    }
    
    /**
     * Gets render type
     * @return rende type
     */
    public int getRenderType(){
        synchronized(rp){
            return rp.renderType;
        }
    }
    
    /**
     * Gets offset for equirectangular image
     * @return offset in range [0,1]
     */
    public float getEquirectOffset(){
        synchronized(rp){
            return rp.equiRectOffset;
        }
    }
    
    /**
     * Set horizontal offset for equirectangular image
     * @param offset Offset in range [0,1]
     */
    public void setEquirectOffset(float offset){
        synchronized(rp){
            rp.flags |= RenderParams.RP_EQUIRECT_OFFSET;
            rp.equiRectOffset = offset;
        }
    }
    
    /**
     * Get width from current render parameters.
     * @return Width of rendered image.
     */
    public int getWidth(){
        int value;
        synchronized(rp){
            value = rp.width;
        }
        return value;
    }
    
    /**
     * Get height from current render parameters.
     * @return Height of rendered image.
     */
    public int getHeight(){
        int value;
        synchronized(rp){
            value = rp.height;
        }
        return value;
    }
    
    /**
     * Set new render size to be processed.
     * @param width Width of rendered image.
     * @param height Height of rendered image.
     */
    public void setRenderSize(int width, int height){
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException("Invalid render size");
        }
        synchronized(rp){
            rp.flags |= RenderParams.RP_WINDOW_SIZE;
            rp.width = width;
            rp.height = height;
        }
    }
    
    /**
     * Get orientation from current render parameters.
     * @return Orientation matrix
     */
    public Matrix33 getOrientation(){
        Matrix33 mat = new Matrix33();
        synchronized(rp){
            mat.assign(rp.orientation);
        }
        return mat;
    }
    
    /**
     * Set new orientation matrix to be processed.
     * @param orientation Orientation matrix.
     */
    public void setOrientation(Matrix33 orientation){
        if(orientation == null){
            throw new NullPointerException();
        }
        synchronized(rp){
            rp.flags |= RenderParams.RP_ORIENTATION;
            rp.orientation.assign(orientation);
        }
    }
    
    /**
     * Get field of view from current render parameters.
     * @return Field of view.
     */
    public float getFov(){
        float value;
        synchronized(rp){
            value = rp.fov;
        }
        return value;
    }
    
    /**
     * Set new field of view to be processed.
     * @param fov New Field of View. Clamped to [2, 175].
     */
    public void setFov(float fov){
        synchronized(rp){
            rp.flags |= RenderParams.RP_FOV;
            rp.fov = MathUtils.clamp(fov, 2.0f, 175.0f);
        }
    }
    
    /**
     * Checks if cubemap reference is enabled from current render parameters.
     * @return true if showing reference, false otherwise.
     */
    public boolean isShowReference(){
        boolean value;
        synchronized(rp){
            value = rp.showReference;
        }
        return value;
    }
    
    /**
     * Enable/Disable drawing of cubemap reference on the rendered image.
     * @param showReference True to show reference, false otherwise.
     */
    public void showReference(boolean showReference){
        synchronized(rp){
            rp.flags |= RenderParams.RP_REFERENCE;
            rp.showReference = showReference;
        }
    }
    
    /**
     * Checks if linear interpolation is enabled from current render parameters.
     * @return true if linear interpolation is enabled, false otherwise.
     */
    public boolean isLerp(){
        boolean value;
        synchronized(rp){
            value = rp.lerp;
        }
        return value;
    }
    
    /**
     * Enable/Disable use of linear interpolation.
     * @param lerp true if linear interpolation will be used, false otherwise.
     */
    public void setLerp(boolean lerp){
        synchronized(rp){
            rp.flags |= RenderParams.RP_LERP;
            rp.lerp = lerp;
        }
    }
    
    /**
     * Get cubemap from current render parameters.
     * @return Cubemap image.
     */
    public Cubemap getCubemap(){
        Cubemap image;
        synchronized(rp){
            image = rp.cubemap;
        }
        return image;
    }
    
    /**
     * Set new cubemap to be processed.
     * @param cubemap Cubemap image. If null a black image will be drawn.
     */
    public void setCubemap(Cubemap cubemap){
        synchronized(rp){
            rp.flags |= RenderParams.RP_CUBEMAP;
            rp.cubemap = cubemap;
        }
    }
    
    /**
     * Checks if text info (fps, fov, filter) is enabled from current render parameters.
     * @return true if showing info, false otherwise
     */
    public boolean isShowInfo(){
        boolean value;
        synchronized(rp){
            value = rp.showInfo;
        }
        return value;
    }
    
    /**
     * Enable/Disable drawing of text info (fps, fov, filter) on rendered image.
     * @param showInfo true to show info, false otherwise.
     */
    public void setShowInfo(boolean showInfo){
        synchronized(rp){
            rp.flags |= RenderParams.RP_SHOW_INFO;
            rp.showInfo = showInfo;
        }
    }

    /**
     * Get reference color from current render parameters.
     * @return Reference color as 32 bits integer
     */
    public int getRefColor(){
        int color;
        synchronized(rp){
            color = rp.refColor;
        }
        return color;
    }
    
    /**
     * Set new reference color to be processed.
     * @param color Reference color as 32 bit integer
     */
    public void setRefColor(int color){
        synchronized(rp){
            rp.flags |= RenderParams.RP_REF_COLOR;
            rp.refColor = color;
        }
    }
    
    /**
     * Update frames per second.
     */
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
    
    /**
     * Draw info on rendered image: fps, fov, and interpolation method.
     * @param renderType Render type
     */
    protected void drawInfo(int renderType) {
        if(showInfo &&  cubemap != null && (renderType == CubemapRenderer.RT_PERSPECTIVE || renderType == CubemapRenderer.RT_EQUIRECT) ){
            Graphics g = colorBufferImage.createGraphics();
            g.setFont(font);
            g.setColor(fontBgColor);
            g.fillRect(5, 5, 110, 55);
            g.setColor(Color.WHITE);
            g.drawString("FPS: "+df.format(fps), 15, 20);
            g.drawString("FOV: "+df.format(fov), 15, 35);
            g.drawString("FILTER: "+ (lerp ? "Bilinear": "Nearest"), 15, 50);
            g.dispose();
        }
    }
    
    /**
     * Sample point on unit sphere from uv coordinates
     * @param u Coordinate in the range [0,1]
     * @param v Coordinate in the range [0,1]
     * @param vec Point on the unit sphere
     */
    private void sampleSphere(float u, float v, Vector3D vec){
        float mu, mv, alpha, beta;
        int u0, u1, v0, v1;
        mu = -0.5f + u * sphereSamples;
        u0 = (int)Math.floor(mu);
        u1 = u0+1;
        alpha = mu - u0;
        u0 = u0 & (sphereSamples-1);
        u1 = u1 & (sphereSamples-1);
        
        mv = v * (sphereSamples-1);
        v0 = (int)Math.floor(mv);
        v1 = v0+1;
        beta = mv - v0;
        v0 = MathUtils.clamp(v0, 0, sphereSamples-1);
        v1 = MathUtils.clamp(v1, 0, sphereSamples-1);
        
        Vector3D vec00 = sphereVec[v0][u0];
        Vector3D vec01 = sphereVec[v0][u1];
        Vector3D vec10 = sphereVec[v1][u0];
        Vector3D vec11 = sphereVec[v1][u1];
        
        float x0 = MathUtils.lerp(vec00.x, vec01.x, alpha);
        float y0 = MathUtils.lerp(vec00.y, vec01.y, alpha);
        float z0 = MathUtils.lerp(vec00.z, vec01.z, alpha);
        
        float x1 = MathUtils.lerp(vec10.x, vec11.x, alpha);
        float y1 = MathUtils.lerp(vec10.y, vec11.y, alpha);
        float z1 = MathUtils.lerp(vec10.z, vec11.z, alpha);
        
        vec.x = MathUtils.lerp(x0, x1, beta);
        vec.y = MathUtils.lerp(y0, y1, beta);
        vec.z = MathUtils.lerp(z0, z1, beta);
    }
    
    /**
     * Generates an image mapping the cubemap to a rentangular image
     * @param cubemap Cubemap image
     * @param showReference If the cubemap reference will be drawn on rendered image.
     * @param refColor Color of cubemap reference
     * @param lerp If linear interpolation will be used.
     * @param width Width of rendered image
     * @param height Height of rendered image
     * @param pixelBuffer buffer of pixels
     */
    private static void drawEquirect(Cubemap cubemap, boolean showReference, int refColor, boolean lerp, int width, int height, float offset, int[] pixelBuffer) {

        Vector3D dir = new Vector3D();
        float u, v;
        for(int i = 0; i < height; ++i) {
            v = i/(float)(height-1);
            for(int j = 0; j < width; ++j) {
                u = (width-1-j+0.5f)/(float)width + offset;
                dir.x = (float)(Math.cos(2.0*Math.PI*u)*Math.sin(Math.PI*v));
                dir.y = (float)Math.cos(Math.PI*v);
                dir.z = (float)(Math.sin(2.0*Math.PI*u)*Math.sin(Math.PI*v));
                pixelBuffer[i*width+j] = showReference ? cubemap.sampleCubemapRef(dir, lerp, refColor) : cubemap.sampleCubemap(dir, lerp);
            }
        }
    }
   
    /**
     * Draw the cubemap unwrapped.
     * @param cubemap Cubemap image
     * @param showReference If the cubemap reference will be drawn on rendered image.
     * @param refColor Color of cubemap reference
     * @param width Width of rendered image.
     * @param height height Height of rendered image.
     * @param outputImage Output Image
     */
    private static void drawUnwrapped(Cubemap cubemap, boolean showReference, int refColor, int width, int height, BufferedImage outputImage){
        Graphics g = outputImage.createGraphics();
        BufferedImage imageArray[] = cubemap.getImageArray();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, width, height);
        int imageSize = 0;
        if(width < height) {
            imageSize = width/4;
        } else {
            imageSize = height/3;
        }
        imageSize = (int)(imageSize * 0.9f);
        int hMargin = width -  4*imageSize;
        int vMargin = height - 3*imageSize;
        g.drawImage(imageArray[Cubemap.NEGX], hMargin/2              , vMargin/2 + imageSize    , imageSize, imageSize, null);
        g.drawImage(imageArray[Cubemap.POSZ], hMargin/2 +   imageSize, vMargin/2 + imageSize    , imageSize, imageSize, null);
        g.drawImage(imageArray[Cubemap.POSX], hMargin/2 + 2*imageSize, vMargin/2 + imageSize    , imageSize, imageSize, null);
        g.drawImage(imageArray[Cubemap.NEGZ], hMargin/2 + 3*imageSize, vMargin/2 + imageSize    , imageSize, imageSize, null);
        g.drawImage(imageArray[Cubemap.POSY], hMargin/2 + imageSize  , vMargin/2                , imageSize, imageSize, null);
        g.drawImage(imageArray[Cubemap.NEGY], hMargin/2 + imageSize  , vMargin/2 + 2 * imageSize, imageSize, imageSize, null);
        if(showReference) {
            FontMetrics m = g.getFontMetrics();
            Font f = new Font("Arial", Font.BOLD, 50);
            g.setFont(f);
            int tx, ty, txtWidth, txtHeight, txtAscent;
            g.setColor(new Color(refColor));
            // Draw negative x
            txtWidth = m.stringWidth("-X");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = hMargin/2 + imageSize / 2 - txtWidth / 2;
            ty = vMargin/2 + imageSize + imageSize / 2 - txtHeight / 2 + txtAscent;
            g.drawString("-X", tx, ty);
            // Draw positive z
            txtWidth = m.stringWidth("+Z");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = hMargin/2 + imageSize + imageSize / 2 - txtWidth / 2;
            ty = vMargin/2 + imageSize + imageSize / 2 - txtHeight / 2 + txtAscent;
            g.drawString("+Z", tx, ty);
            // Draw positive x
            txtWidth = m.stringWidth("+X");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = hMargin/2 + 2 * imageSize + imageSize / 2 - txtWidth / 2;
            ty = vMargin/2 + imageSize + imageSize / 2 - txtHeight / 2 + txtAscent;
            g.drawString("+X", tx, ty);
            // Draw negative z
            txtWidth = m.stringWidth("-Z");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = hMargin/2 + 3 * imageSize + imageSize / 2 - txtWidth / 2;
            ty = vMargin/2 + imageSize + imageSize / 2 - txtHeight / 2 + txtAscent;
            g.drawString("-Z", tx, ty);
            // Draw postive y
            txtWidth = m.stringWidth("+Y");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = hMargin/2 + imageSize + imageSize / 2 - txtWidth / 2;
            ty = vMargin/2 + imageSize / 2 - txtHeight / 2 + txtAscent;
            g.drawString("+Y", tx, ty);
            // Draw negative y
            txtWidth = m.stringWidth("-Y");
            txtHeight = m.getHeight();
            txtAscent = m.getAscent();
            tx = hMargin/2 + imageSize + imageSize / 2 - txtWidth / 2;
            ty = vMargin/2 + 2 * imageSize + imageSize / 2 - txtHeight / 2 + txtAscent;
            g.drawString("-Y", tx, ty);
        }
        g.dispose();
    }
    
    @Override
    public void run() {
        while (alive) {
            RenderParams newRP = null;
            // Check for new parameters
            synchronized (rp) {
                if(rp.flags != 0){
                    newRP = rp.copy();
                    rp.flags = 0;
                }
            }
            if (newRP != null) {
                // Process the new parameters and update internal state
                boolean updateProjection = false;
                if((newRP.flags & RenderParams.RP_RENDER_TYPE) != 0) {
                    renderType = rp.renderType;
                }
                if((newRP.flags & RenderParams.RP_CUBEMAP) != 0){
                    cubemap = newRP.cubemap;
                }
                if ((newRP.flags & RenderParams.RP_WINDOW_SIZE) != 0 && (windowWidth != newRP.width || windowHeight != newRP.height)) {
                    windowWidth = newRP.width;
                    windowHeight = newRP.height;
                    colorBufferImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
                    WritableRaster wr = colorBufferImage.getRaster();
                    DataBuffer db = wr.getDataBuffer();
                    DataBufferInt dbi = (DataBufferInt) db;
                    colorBuffer = dbi.getData();
                    updateProjection = true;
                    int batch = windowHeight / avalaibleProcessors;
                    if (avalaibleProcessors * batch < windowHeight) {
                        batch++;
                    }
                    int startRow = 0, endRow;
                    for (int i = 0; i < avalaibleProcessors; i++) {
                        endRow = startRow + batch;
                        processors[i].setRowRange(startRow, Math.min(endRow, windowHeight));
                        startRow = endRow;
                    }
                }
                if ((newRP.flags & RenderParams.RP_FOV) != 0 && fov != newRP.fov) {
                    fov = newRP.fov;
                    updateProjection = true;
                }
                if((newRP.flags & RenderParams.RP_ORIENTATION) != 0){
                    orientation.assign(newRP.orientation);
                }
                if((newRP.flags & RenderParams.RP_REFERENCE) != 0){
                    showReference = newRP.showReference;
                }
                if((newRP.flags & RenderParams.RP_REF_COLOR) != 0){
                    refColor = newRP.refColor;
                }
                if((newRP.flags & RenderParams.RP_LERP) != 0){
                    lerp = newRP.lerp;
                }
                if((newRP.flags & RenderParams.RP_SHOW_INFO) != 0){
                    showInfo = newRP.showInfo;
                }
                if((newRP.flags & RenderParams.RP_EQUIRECT_OFFSET) != 0){
                    equiRectOffset = newRP.equiRectOffset;
                }
                if (updateProjection) {
                    calculateProjection();
                }
                if (colorBufferImage == null) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                // Draw image
                if(cubemap != null){
                    switch(renderType) {
                        case RT_PERSPECTIVE:
                            // Start render threads
                            for (int i = 0; i < avalaibleProcessors; i++) {
                                processors[i].setRenderType(CubemapRenderer.RT_PERSPECTIVE);
                                processors[i].render = true;
                            }
                            // Wait for render threads to finish
                            for (int i = 0; i < avalaibleProcessors; i++) {
                                while (processors[i].render) {
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException e) {
                                    }
                                }
                            }
                            break;
                        case RT_EQUIRECT:
                            //drawEquirectTest(cubemap, lerp, windowWidth, windowHeight, colorBuffer);
                            // Start render threads
                            for (int i = 0; i < avalaibleProcessors; i++) {
                                processors[i].setRenderType(CubemapRenderer.RT_EQUIRECT);
                                processors[i].render = true;
                            }
                            // Wait for render threads to finish
                            for (int i = 0; i < avalaibleProcessors; i++) {
                                while (processors[i].render) {
                                    try {
                                        Thread.sleep(1);
                                    } catch (InterruptedException e) {
                                    }
                                }
                            }
                            break;
                        case RT_UNWRAPPED:
                            drawUnwrapped(cubemap, showReference, refColor, windowWidth, windowHeight, colorBufferImage);
                            break;
                    }
                }else{
                    // If cubemap is null, draw a black image.
                    Graphics gi = colorBufferImage.createGraphics();
                    gi.setColor(Color.BLACK);
                    gi.fillRect(0, 0, windowWidth, windowHeight);
                    gi.dispose();
                }
                // Update fps
                fps();
                // Draw info over image
                drawInfo(renderType);
                // Write color buffer directly to graphics context. (Active Rendering).
                Graphics gv = viewer.getGraphics();
                if (gv != null) {
                    gv.drawImage(colorBufferImage, 0, 0, null);
                    gv.dispose();
                }
                // Pass image to cubemap viewer. This is necessary to restore component content in the paintComponent callback.
                viewer.setOutputImage(colorBufferImage);

            } else {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    /**
     * Thread for drawing a part of the image.
     */
    class ImageProcessor extends Thread {

        private int startRow, endRow;
        private boolean alive, render;
        private int renderType;
        
        /**
         * Creates a new Image Processor.
         */
        public ImageProcessor(){
            super("Image Processor");
            alive = true;
            renderType = CubemapRenderer.RT_PERSPECTIVE;
        }
        
        /**
         * Specify the row range.
         * @param startRow Start row.
         * @param endRow End row. Not inclusive.
         */
        public void setRowRange(int startRow, int endRow){
            this.startRow = startRow;
            this.endRow = endRow;
        }
        
        public void setRenderType(int renderType){
            this.renderType = renderType;
        }
        
        @Override
        public void run() {
            while (alive) {
                if (render) {
                    if(renderType == CubemapRenderer.RT_PERSPECTIVE){                    
                        int width = colorBufferImage.getWidth();
                        int height = colorBufferImage.getHeight();
                        Vector3D inDir = new Vector3D();
                        Vector2D nc = new Vector2D();
                        inDir.z = projDistance;
                        Vector3D outDir = new Vector3D();
                        float oneOverWidth = 1.0f / width, oneOverHeight = 1.0f / height;
                        if (!showReference) {
                            for (int y = startRow; y < endRow; ++y) {
                                nc.y = ((height - 1 - y) + 0.5f) * oneOverHeight;
                                inDir.y = windowBottom + yRange * nc.y;
                                for (int x = 0; x < width; ++x) {
                                    nc.x = (x + 0.5f) * oneOverWidth;
                                    inDir.x = windowLeft + xRange * nc.x;
                                    orientation.mult(inDir, outDir);
                                    colorBuffer[y * width + x] = cubemap.sampleCubemap(outDir, lerp);
                                }
                            }
                        } else {
                            for (int y = startRow; y < endRow; ++y) {
                                nc.y = ((height - 1 - y) + 0.5f) * oneOverHeight;
                                inDir.y = windowBottom + yRange * nc.y;
                                for (int x = 0; x < width; ++x) {
                                    nc.x = (x + 0.5f) * oneOverWidth;
                                    inDir.x = windowLeft + xRange * nc.x;
                                    orientation.mult(inDir, outDir);
                                    colorBuffer[y * width + x] = cubemap.sampleCubemapRef(outDir, lerp, refColor);
                                }
                            }
                        }
                    } else if (renderType == CubemapRenderer.RT_EQUIRECT) {
                        int width = colorBufferImage.getWidth();
                        int height = colorBufferImage.getHeight();
                        Vector3D dir = new Vector3D();
                        float u, v;
                        if (!showReference) {
                            for (int y = startRow; y < endRow; ++y) {
                                v = (y+0.5f) /(float)height;
                                for (int x = 0; x < width; ++x) {
                                    u = (x+0.5f)/(float)width + equiRectOffset;
                                    sampleSphere(u, v, dir);
                                    colorBuffer[y*width+x] = cubemap.sampleCubemap(dir, lerp);
                                }
                            }
                        }else{
                            for (int y = startRow; y < endRow; ++y) {
                                v = (y+0.5f) /(float)height;
                                for (int x = 0; x < width; ++x) {
                                    u = (x+0.5f)/(float)width + equiRectOffset;
                                    sampleSphere(u, v, dir);
                                    colorBuffer[y*width+x] = cubemap.sampleCubemapRef(dir, lerp, refColor);
                                }
                            }
                        }
                    }
                    render = false;
                }else{
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }

    /**
     * Render an image from given parameters.
     * The type of the image returned is: BufferedImage.TYPE_INT_RGB.
     * @param cubemap Cubemap image.
     * @param orientation Orientation matrix.
     * @param fov Field of view.
     * @param showReference If the cubemap reference will be drawn on rendered image.
     * @param refColor Color of cubemap reference
     * @param lerp If linear interpolation will be used.
     * @param width Width of rendered image.
     * @param height Height of rendered image.
     * @return rendered image 
     */
    public static final BufferedImage render(Cubemap cubemap, Matrix33 orientation, float fov, boolean showReference, int refColor, boolean lerp, int width, int height){
        if(cubemap == null){
            throw new NullPointerException();
        }
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException("Invalid window size");
        }
        if(orientation == null){
            throw new NullPointerException();
        }
        if(fov <= 2.0f || fov >= 175.0f){
            throw new IllegalArgumentException("Invalid fov");
        }
        float windowLeft, windowRight, xRange;
        float windowBottom, windowTop, yRange;
        float projDistance = 5.0f;
        // Calculate projection
        float aspectRatio = (float)width / (float)height;
        windowRight = (float) (projDistance * Math.tan(Math.toRadians(fov) / 2.0));
        windowLeft = -windowRight;
        xRange = windowRight * 2.0f;
        windowTop = windowRight / aspectRatio;
        windowBottom = -windowTop;
        yRange = windowTop * 2.0f;
        // Allocate image
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster wr = outputImage.getRaster();
        DataBuffer db = wr.getDataBuffer();
        DataBufferInt dbi = (DataBufferInt) db;
        int buffer[] = dbi.getData();
        //Render
        Vector3D inDir = new Vector3D();
        Vector2D nc = new Vector2D();
        inDir.z = projDistance;
        Vector3D outDir = new Vector3D();
        float oneOverWidth = 1.0f / width, oneOverHeight = 1.0f / height;
        if (!showReference) {
            for (int y = 0; y < height; ++y) {
                nc.y = ((height - 1 - y) + 0.5f) * oneOverHeight;
                inDir.y = windowBottom + yRange * nc.y;
                for (int x = 0; x < width; ++x) {
                    nc.x = (x + 0.5f) * oneOverWidth;
                    inDir.x = windowLeft + xRange * nc.x;
                    orientation.mult(inDir, outDir);
                    buffer[y * width + x] = cubemap.sampleCubemap(outDir, lerp);
                }
            }
        } else {
            for (int y = 0; y < height; ++y) {
                nc.y = ((height - 1 - y) + 0.5f) * oneOverHeight;
                inDir.y = windowBottom + yRange * nc.y;
                for (int x = 0; x < width; ++x) {
                    nc.x = (x + 0.5f) * oneOverWidth;
                    inDir.x = windowLeft + xRange * nc.x;
                    orientation.mult(inDir, outDir);
                    buffer[y * width + x] = cubemap.sampleCubemapRef(outDir, lerp, refColor);
                }
            }
        }
        return outputImage;
    }
    
    /**
     * Renders an unwrapped image.
     * The type of the image returned is: BufferedImage.TYPE_INT_RGB.
     * @param cubemap Cubemap image
     * @param showReference If the cubemap reference will be drawn on rendered image.
     * @param refColor Color of cubemap reference
     * @param width Width of rendered image.
     * @param height Height of rendered image.
     * @return rendered image
     */
    public static BufferedImage renderUnWrapped(Cubemap cubemap, boolean showReference, int refColor, int width, int height) {
        if(cubemap == null){
            throw new NullPointerException();
        }
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException("Invalid window size");
        }
        // Allocate image
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        drawUnwrapped(cubemap, showReference, refColor, width, height, outputImage);
        return outputImage;
    }

    /**
     * Renders an equirectangular image.
     * The type of the image returned is: BufferedImage.TYPE_INT_RGB.
     * @param cubemap Cubemap image
     * @param showReference If the cubemap reference will be drawn on rendered image.
     * @param refColor Color of cubemap reference
     * @param lerp If linear interpolation will be used.
     * @param width Width of rendered image.
     * @param height Height of rendered image.
     * @return 
     */
    public static BufferedImage renderEquirect(Cubemap cubemap, boolean showReference, int refColor, boolean lerp, int width, int height, float offset) {
        if(cubemap == null){
            throw new NullPointerException();
        }
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException("Invalid window size");
        }
        // Allocate image
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        WritableRaster wr = outputImage.getRaster();
        DataBuffer db = wr.getDataBuffer();
        DataBufferInt dbi = (DataBufferInt) db;
        int buffer[] = dbi.getData();
        drawEquirect(cubemap, showReference, refColor, lerp, width, height, offset, buffer);
        return outputImage;
    }
    
}
