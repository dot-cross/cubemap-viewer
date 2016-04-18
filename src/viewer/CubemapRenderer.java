package viewer;

import math.Matrix33;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import math.MathUtils;
import math.Vector2D;
import math.Vector3D;

/**
 *
 * @author edu
 */
public class CubemapRenderer extends Thread {
    
    private static class RenderParams {

        public static final int RP_CUBEMAP = 1;
        public static final int RP_WINDOW_SIZE = 1 << 1;
        public static final int RP_FOV = 1 << 2;
        public static final int RP_ORIENTATION = 1 << 3;
        public static final int RP_REFERENCE = 1 << 4;
        public static final int RP_LERP = 1 << 5;
        public static final int RP_ALL = RP_CUBEMAP | RP_WINDOW_SIZE | RP_FOV | RP_ORIENTATION | RP_REFERENCE | RP_LERP;
        public int flags;
        public Cubemap cubemap;
        public int width, height;
        public float fov;
        public Matrix33 orientation;
        public boolean showReference;
        public boolean lerp;
        
        public RenderParams(){
            fov = 75.0f;
            orientation = new Matrix33(1.0f);
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
            return copy;
        }
    }
    
    private final CubemapPanel panel;
    private Cubemap cubemap;
    private static final Cubemap cubemapReference = Cubemap.createReferenceCubemap();
    private final RenderParams rp;
    private BufferedImage frontBufferImage, backBufferImage;
    private int frontBuffer[], backBuffer[];
    private int windowWidth, windowHeight;
    private final float projDistance = 5.0f;
    private float fov, aspectRatio;
    private float windowLeft, windowRight, xRange;
    private float windowBottom, windowTop, yRange;
    private final Matrix33 orientation;
    private boolean showReference, lerp;
    private boolean alive;
    private int threadNumber;
    private ImageProcessor processors[];
    
    public CubemapRenderer(CubemapPanel panel){
        super("Cubemap Renderer");
        this.panel = panel;
        rp = new RenderParams();
        fov = 75.0f;
        orientation = new Matrix33(1.0f);
    }
    
    public void init(){
        if (processors == null) {
            threadNumber = Runtime.getRuntime().availableProcessors();
            processors = new ImageProcessor[threadNumber];
            for (int i = 0; i < threadNumber; i++) {
                processors[i] = new ImageProcessor();
                processors[i].start();
            }
            alive = true;
            start();
        }
    }
    
    public void release(){
        if(isAlive()){
            alive = false;
            try {
                join();
            } catch (InterruptedException ex) {
            }
            for (int i = 0; i < threadNumber; i++) {
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
    
    public int getWidth(){
        int value;
        synchronized(rp){
            value = rp.width;
        }
        return value;
    }
    
    public int getHeight(){
        int value;
        synchronized(rp){
            value = rp.height;
        }
        return value;
    }
    
    public void setWindowSize(int width, int height){
        if(width <= 0 || height <= 0){
            throw new IllegalArgumentException("Invalid window size");
        }
        synchronized(rp){
            rp.flags |= RenderParams.RP_WINDOW_SIZE;
            rp.width = width;
            rp.height = height;
        }
    }
    
    public Matrix33 getOrientation(){
        Matrix33 mat = new Matrix33();
        synchronized(rp){
            mat.assign(rp.orientation);
        }
        return mat;
    }
    
    public void setOrientation(Matrix33 orientation){
        if(orientation == null){
            throw new NullPointerException();
        }
        synchronized(rp){
            rp.flags |= RenderParams.RP_ORIENTATION;
            rp.orientation.assign(orientation);
        }
    }
    
    public float getFov(){
        float value;
        synchronized(rp){
            value = rp.fov;
        }
        return value;
    }
    
    public void setFov(float fov){
        synchronized(rp){
            rp.flags |= RenderParams.RP_FOV;
            rp.fov = MathUtils.clamp(fov, 2.0f, 175.0f);
        }
    }
    
    public boolean isShowReference(){
        boolean value;
        synchronized(rp){
            value = rp.showReference;
        }
        return value;
    }
    
    public void showReference(boolean showReference){
        synchronized(rp){
            rp.flags |= RenderParams.RP_REFERENCE;
            rp.showReference = showReference;
        }
    }
    
    public boolean isLerp(){
        boolean value;
        synchronized(rp){
            value = rp.lerp;
        }
        return value;
    }
    
    public void setLerp(boolean lerp){
        synchronized(rp){
            rp.flags |= RenderParams.RP_LERP;
            rp.lerp = lerp;
        }
    }
    
    public Cubemap getCubemap(){
        Cubemap image;
        synchronized(rp){
            image = rp.cubemap;
        }
        return image;
    }
    
    public void setCubemap(Cubemap cubemap){
        if(cubemap == null){
            throw new NullPointerException();
        }
        synchronized(rp){
            rp.flags |= RenderParams.RP_CUBEMAP;
            rp.cubemap = cubemap;
        }
    }

    @Override
    public void run() {
        while (alive) {
            RenderParams newRP = null;
            synchronized (rp) {
                if(rp.flags != 0){
                    newRP = rp.copy();
                    rp.flags = 0;
                }
            }
            if (newRP != null) {
                // Update internal state
                boolean updateProjection = false;
                if((newRP.flags & RenderParams.RP_CUBEMAP) != 0){
                    cubemap = newRP.cubemap;
                }
                if ((newRP.flags & RenderParams.RP_WINDOW_SIZE) != 0 && (windowWidth != newRP.width || windowHeight != newRP.height)) {
                    WritableRaster wr;
                    DataBuffer db;
                    DataBufferInt dbi;
                    windowWidth = newRP.width;
                    windowHeight = newRP.height;
                    frontBufferImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
                    wr = frontBufferImage.getRaster();
                    db = wr.getDataBuffer();
                    dbi = (DataBufferInt) db;
                    frontBuffer = dbi.getData();
                    backBufferImage = new BufferedImage(windowWidth, windowHeight, BufferedImage.TYPE_INT_RGB);
                    wr = backBufferImage.getRaster();
                    db = wr.getDataBuffer();
                    dbi = (DataBufferInt) db;
                    backBuffer = dbi.getData();
                    updateProjection = true;
                    int batch = windowHeight / threadNumber;
                    if (threadNumber * batch < windowHeight) {
                        batch++;
                    }
                    int startRow = 0, endRow;
                    for (int i = 0; i < threadNumber; i++) {
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
                if((newRP.flags & RenderParams.RP_LERP) != 0){
                    lerp = newRP.lerp;
                }
                if (updateProjection) {
                    calculateProjection();
                }
                if (cubemap == null || frontBuffer == null || backBuffer == null) {
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                // Start render
                for (int i = 0; i < threadNumber; i++) {
                    processors[i].render = true;
                }
                for (int i = 0; i < threadNumber; i++) {
                    while (processors[i].render) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                        }
                    }
                }
                //Swap buffers
                BufferedImage tmpImage = frontBufferImage;
                int tmpBuffer[] = frontBuffer;
                frontBufferImage = backBufferImage;
                frontBuffer = backBuffer;
                backBufferImage = tmpImage;
                backBuffer = tmpBuffer;
                panel.setOutputImage(frontBufferImage);
            } else {
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    class ImageProcessor extends Thread {

        private int startRow, endRow;
        private boolean alive, render;
        
        public ImageProcessor(){
            super("Image Processor");
            alive = true;
        }
        
        public void setRowRange(int startRow, int endRow){
            this.startRow = startRow;
            this.endRow = endRow;
        }
        
        @Override
        public void run() {
            while (alive) {
                if (render) {
                    int width = backBufferImage.getWidth();
                    int height = backBufferImage.getHeight();
                    Vector3D inDir = new Vector3D();
                    Vector2D nc = new Vector2D();
                    inDir.z = projDistance;
                    Vector3D outDir = new Vector3D();
                    float oneOverWidth = 1.0f / width, oneOverHeight = 1.0f / height;
                    if (!showReference) {
                        for (int y = startRow; y < endRow; y++) {
                            nc.y = ((height - 1 - y) + 0.5f) * oneOverHeight;
                            inDir.y = windowBottom + yRange * nc.y;
                            for (int x = 0; x < width; x++) {
                                nc.x = (x + 0.5f) * oneOverWidth;
                                inDir.x = windowLeft + xRange * nc.x;
                                orientation.mult(inDir, outDir);
                                int color = cubemap.sampleCubemap(outDir, lerp);
                                backBuffer[y * width + x] = color;
                            }
                        }
                    } else {
                        for (int y = startRow; y < endRow; y++) {
                            nc.y = ((height - 1 - y) + 0.5f) * oneOverHeight;
                            inDir.y = windowBottom + yRange * nc.y;
                            for (int x = 0; x < width; x++) {
                                nc.x = (x + 0.5f) * oneOverWidth;
                                inDir.x = windowLeft + xRange * nc.x;
                                orientation.mult(inDir, outDir);
                                int reference = cubemapReference.sampleCubemap(outDir, false);
                                if(((reference >> 24) & 0xFF) != 0){
                                    backBuffer[y * width + x] = reference;
                                }else{
                                    int color = cubemap.sampleCubemap(outDir, lerp);
                                    backBuffer[y * width + x] = color;
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

    public static final BufferedImage render(Cubemap cubemap, Matrix33 orientation, float fov, boolean showReference, boolean lerp, int width, int height){
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
            for (int y = 0; y < height; y++) {
                nc.y = ((height - 1 - y) + 0.5f) * oneOverHeight;
                inDir.y = windowBottom + yRange * nc.y;
                for (int x = 0; x < width; x++) {
                    nc.x = (x + 0.5f) * oneOverWidth;
                    inDir.x = windowLeft + xRange * nc.x;
                    orientation.mult(inDir, outDir);
                    int color = cubemap.sampleCubemap(outDir, lerp);
                    buffer[y * width + x] = color;
                }
            }
        } else {
            for (int y = 0; y < height; y++) {
                nc.y = ((height - 1 - y) + 0.5f) * oneOverHeight;
                inDir.y = windowBottom + yRange * nc.y;
                for (int x = 0; x < width; x++) {
                    nc.x = (x + 0.5f) * oneOverWidth;
                    inDir.x = windowLeft + xRange * nc.x;
                    orientation.mult(inDir, outDir);
                    int reference = cubemapReference.sampleCubemap(outDir, false);
                    if(((reference >> 24) & 0xFF) != 0){
                        buffer[y * width + x] = reference;
                    }else{
                        int color = cubemap.sampleCubemap(outDir, lerp);
                        buffer[y * width + x] = color;
                    }
                }
            }
        }
        return outputImage;
    }

}
