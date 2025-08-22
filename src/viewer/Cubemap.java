package viewer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import math.MathUtils;
import math.Vector2D;
import math.Vector3D;

/**
 * Class for cubemap images.
 * Internal Format: BufferedImage.TYPE_INT_RGB.
 * @author edu
 */
public class Cubemap {
    
    public static final int POSX = 0;
    public static final int NEGX = 1;
    public static final int POSY = 2;
    public static final int NEGY = 3;
    public static final int POSZ = 4;
    public static final int NEGZ = 5;

    private static final Vector2D SIGN_PLUS_POINTS[] = new Vector2D[] {new Vector2D(0.15f, 0.575f), new Vector2D(0.15f, 0.425f), new Vector2D(0.425f, 0.425f), new Vector2D(0.425f, 0.15f),
        new Vector2D(0.575f, 0.15f), new Vector2D(0.575f, 0.425f), new Vector2D(0.85f, 0.425f), new Vector2D(0.85f, 0.575f), new Vector2D(0.575f, 0.575f), new Vector2D(0.575f, 0.85f), new Vector2D(0.425f, 0.85f),
        new Vector2D(0.425f, 0.575f)};

    private static final Vector2D SIGN_MINUS_POINTS[] = new Vector2D[] {new Vector2D(0.15f, 0.575f), new Vector2D(0.15f, 0.425f), new Vector2D(0.85f, 0.425f), new Vector2D(0.85f, 0.575f)};
    
    private static final Vector2D LETTER_X_POINTS[] = new Vector2D[] {new Vector2D(0.0f, 1.0f), new Vector2D(0.35f, 0.5f), new Vector2D(0.0f, 0.0f), new Vector2D(0.3f, 0.0f), new Vector2D(0.5f, 0.2857f), 
        new Vector2D(0.7f, 0.0f), new Vector2D(1.0f, 0.0f), new Vector2D(0.65f, 0.5f), new Vector2D(1.0f, 1.0f), new Vector2D(0.7f, 1.0f), new Vector2D(0.5f, 0.7143f), new Vector2D(0.3f, 1.0f)};

    private static final Vector2D LETTER_Y_POINTS[] = new Vector2D[]{new Vector2D(0.0f, 1.0f), new Vector2D(0.4f, 0.4f), new Vector2D(0.4f, 0.0f), new Vector2D(0.6f, 0.0f), new Vector2D(0.6f, 0.4f),
        new Vector2D(1.0f, 1.0f), new Vector2D(0.766f, 1.0f), new Vector2D(0.5f, 0.6f), new Vector2D(0.233f, 1.0f)};
    
    private static final Vector2D LETTER_Z_POINTS[] = new Vector2D[]{new Vector2D(0.0f, 1.0f), new Vector2D(0.0f, 0.8f), new Vector2D(0.7f, 0.8f), new Vector2D(0.0f, 0.2f), new Vector2D(0.0f, 0.0f),
        new Vector2D(1.0f, 0.0f), new Vector2D(1.0f, 0.2f), new Vector2D(0.3f, 0.2f), new Vector2D(1.0f, 0.8f), new Vector2D(1.0f, 1.0f)};   
    
    private static final float UMIN, UMAX, VMIN, VMAX;
    
    static {
        float width = 0.15f;
        UMIN = 0.5f - width;
        UMAX = 0.5f + width;
        VMIN = 0.5f - width/2;
        VMAX = 0.5f + width/2;
        for(Vector2D v: SIGN_MINUS_POINTS){
            v.x = v.x*width + 0.5f - width/2 - width/2;
            v.y = v.y*width + 0.5f - width/2;
        }
        for(Vector2D v: SIGN_PLUS_POINTS){
            v.x = v.x*width + 0.5f - width/2 - width/2;
            v.y = v.y*width + 0.5f - width/2;
        }
        for(Vector2D v: LETTER_X_POINTS){
            v.x = v.x*width + 0.5f - width/2 + width/2;
            v.y = v.y*width + 0.5f - width/2;
        }
        for(Vector2D v: LETTER_Y_POINTS){
            v.x = v.x*width + 0.5f - width/2 + width/2;
            v.y = v.y*width + 0.5f - width/2;
        }
        for(Vector2D v: LETTER_Z_POINTS){
            v.x = v.x*width + 0.5f - width/2 + width/2;
            v.y = v.y*width + 0.5f - width/2;
        }
    }

    private String name;
    private final BufferedImage images[];
    private final int imageData[][];
    private final int size;
    private final int size_minus_one;

    /**
     * Constructs cubemap.
     * @param name Name for cubemap.
     * @param size Width and height of each image.
     */
    public Cubemap(String name, int size){
        if(name == null){
            throw new NullPointerException("Cubemap name is null");
        }
        if(size <= 0){
            throw new IllegalArgumentException("Invalid cubemap size");
        }
        this.size = size;
        size_minus_one = size-1;
        BufferedImage posX = new BufferedImage(BufferedImage.TYPE_INT_RGB, size, size);
        BufferedImage negX = new BufferedImage(BufferedImage.TYPE_INT_RGB, size, size);
        BufferedImage posY = new BufferedImage(BufferedImage.TYPE_INT_RGB, size, size);
        BufferedImage negY = new BufferedImage(BufferedImage.TYPE_INT_RGB, size, size);
        BufferedImage posZ = new BufferedImage(BufferedImage.TYPE_INT_RGB, size, size);
        BufferedImage negZ = new BufferedImage(BufferedImage.TYPE_INT_RGB, size, size);
        images = new BufferedImage[]{posX, negX, posY, negY, posZ, negZ};
        WritableRaster wr;
        DataBuffer db;
        DataBufferInt dbi;
        imageData = new int[6][];
        for(int i = 0; i < 6; i++){
            wr = images[i].getRaster();
            db = wr.getDataBuffer();
            dbi = (DataBufferInt)db;
            imageData[i] = dbi.getData();
        }
    }
    
    /**
     * Constructs a cubemap from 6 existing images.
     * The 6 images needs to be square and have the same size.
     * The internal format required is BufferedImage.TYPE_INT_RGB.
     * @param name Name for cubemap image.
     * @param posX Image for +X direction.
     * @param negX Image for -X direction.
     * @param posY Image for +Y direction.
     * @param negY Image for -Y direction.
     * @param posZ Image for +Z direction.
     * @param negZ Image for -Z direction.
     */
    public Cubemap(String name, BufferedImage posX, BufferedImage negX, BufferedImage posY, BufferedImage negY, BufferedImage posZ, BufferedImage negZ){
        if(name == null){
            throw new NullPointerException("Cubemap name is null");
        }
        if(posX == null){
            throw new NullPointerException("posX is null");
        }
        if(posX.getType() != BufferedImage.TYPE_INT_RGB){
            throw new IllegalArgumentException("posX has an invalid image type");
        }
        if(posX.getWidth() != posX.getHeight()){
            throw new IllegalArgumentException("posX is not a square image");
        }
        if(negX == null){
            throw new NullPointerException("negX is null");
        }
        if(negX.getType() != BufferedImage.TYPE_INT_RGB){
            throw new IllegalArgumentException("negX has an invalid image type");
        }
        if(negX.getWidth() != negX.getHeight()){
            throw new IllegalArgumentException("negX is not a square image");
        }
        if(posY == null){
            throw new NullPointerException("posY is null");
        }
        if(posY.getType() != BufferedImage.TYPE_INT_RGB){
            throw new IllegalArgumentException("posY has an invalid image type");
        }
        if(posY.getWidth() != posY.getHeight()){
            throw new IllegalArgumentException("posY is not a square image");
        }
        if(negY == null){
            throw new NullPointerException("negY is null");
        }
        if(negY.getType() != BufferedImage.TYPE_INT_RGB){
            throw new IllegalArgumentException("negY has an invalid image type");
        }
        if(negY.getWidth() != negY.getHeight()){
            throw new IllegalArgumentException("negY is not a square image");
        }
        if(posZ == null){
            throw new NullPointerException("posZ is null");
        }
        if(posZ.getType() != BufferedImage.TYPE_INT_RGB){
            throw new IllegalArgumentException("posZ has an invalid image type");
        }
        if(posZ.getWidth() != posZ.getHeight()){
            throw new IllegalArgumentException("posZ is not a square image");
        }
        if(negZ == null){
            throw new NullPointerException("negZ is null");
        }
        if(negZ.getType() != BufferedImage.TYPE_INT_RGB){
            throw new IllegalArgumentException("negZ has an invalid image type");
        }
        if(negZ.getWidth() != negZ.getHeight()){
            throw new IllegalArgumentException("negZ is not a square image");
        }
        if(posX.getWidth() != negX.getWidth() || negX.getWidth() != posY.getWidth() ||
            posY.getWidth() != negY.getWidth() || negY.getWidth() != posZ.getWidth() ||
            posZ.getWidth() != negZ.getWidth()){
            throw new IllegalArgumentException("The 6 images have not the same size");
        }
        this.name = name;
        size = posX.getWidth();
        size_minus_one = size-1;
        images = new BufferedImage[]{posX, negX, posY, negY, posZ, negZ};
        WritableRaster wr;
        DataBuffer db;
        DataBufferInt dbi;
        imageData = new int[6][];
        for(int i = 0; i < 6; i++){
            wr = images[i].getRaster();
            db = wr.getDataBuffer();
            dbi = (DataBufferInt)db;
            imageData[i] = dbi.getData();
        }
    }

    /**
     * Get name of cubemap.
     * @return name of cubemap.
     */
    public String getName(){
        return name;
    }
    
    /**
     * Return size of cubemap.
     * @return size of cubemap.
     */
    public int getSize(){
        return size;
    }
    
    /**
     * Return internal array of cubemap images.
     * The order is: +X, -X, +Y, -Y, +Z, -Z.
     * @return array of images.
     */
    public BufferedImage[] getImageArray(){
        return images;
    }
    
    /**
     * Sample an image buffer. No interpolation.
     * @param data Array of pixels
     * @param u Coordinate U
     * @param v Coordinate V
     * @return Color as 32 bit integer
     */
    private int sample2DNearest(int data[], float u, float v) {
        int ru = (int) (u * size);
        int rv = (int) (v * size);
        ru = MathUtils.clamp(ru, 0, size_minus_one);
        rv = MathUtils.clamp(rv, 0, size_minus_one);
        rv = size_minus_one - rv;
        return data[rv * size + ru];
    }

    /**
     * Sample an image buffer. Linear interpolation using fixed point arithmetic.
     * @param data Array of pixels
     * @param u Coordinate U
     * @param v Coordinate V
     * @return Color as 32 bit integer
     */
    private int sample2DLinear(int data[], float u, float v) {
        int mu = (int) ((-0.5f + u * size) * 65536.0f);
        int mv = (int) ((-0.5f + v * size) * 65536.0f);

        int u0 = MathUtils.clamp(mu >> 16, 0, size_minus_one);
        int u1 = MathUtils.clamp(u0 + 1, 0, size_minus_one);
        int alpha = mu & 0xFFFF;

        int v0 = MathUtils.clamp(mv >> 16, 0, size_minus_one);
        int v1 = MathUtils.clamp(v0 + 1, 0, size_minus_one);
        int beta = mv & 0xFFFF;

        // Flip vertical axis
        v0 = size_minus_one - v0;
        v1 = size_minus_one - v1;

        int idx00 = v0 * size + u0;
        int idx01 = v0 * size + u1;
        int idx10 = v1 * size + u0;
        int idx11 = v1 * size + u1;

        int s00 = data[idx00], s01 = data[idx01], s10 = data[idx10], s11 = data[idx11];

        // Compute 16-bit weights
        long w00 = (long) (65536 - alpha) * (65536 - beta);
        long w01 = (long) alpha * (65536 - beta);
        long w10 = (long) (65536 - alpha) * beta;
        long w11 = (long) alpha * beta;

        // Interpolate using 64-bit to avoid overflow
        int r = (int) (((s00 >> 16 & 0xFF) * w00 + (s01 >> 16 & 0xFF) * w01
                + (s10 >> 16 & 0xFF) * w10 + (s11 >> 16 & 0xFF) * w11) >> 32);

        int g = (int) (((s00 >> 8 & 0xFF) * w00 + (s01 >> 8 & 0xFF) * w01
                + (s10 >> 8 & 0xFF) * w10 + (s11 >> 8 & 0xFF) * w11) >> 32);

        int b = (int) (((s00 & 0xFF) * w00 + (s01 & 0xFF) * w01
                + (s10 & 0xFF) * w10 + (s11 & 0xFF) * w11) >> 32);

        return (r << 16) | (g << 8) | b;
    }

    /**
     * Sample cubemap with vector.
     * @param dir 3D vector.
     * @param linear true to use bilinear interpolation, false otherwise.
     * @return Color as 32 bits integer. 
     */
    public int sampleCubemap(Vector3D dir, boolean linear) {
        float xuSign, xvSign, xAbs;
        float yuSign, yvSign, yAbs;
        float zuSign, zvSign, zAbs;
        int xIndex, yIndex, zIndex;
        if (dir.x >= 0.0f) {
            xAbs = dir.x;
            xIndex = POSX;
            xuSign = -1.0f;
            xvSign = 1.0f;
        } else {
            xAbs = -dir.x;
            xIndex = NEGX;
            xuSign = 1.0f;
            xvSign = 1.0f;
        }
        if (dir.y >= 0.0f) {
            yAbs = dir.y;
            yIndex = POSY;
            yuSign = 1.0f;
            yvSign = -1.0f;
        } else {
            yAbs = -dir.y;
            yIndex = NEGY;
            yuSign = 1.0f;
            yvSign = 1.0f;
        }
        if (dir.z >= 0.0f) {
            zAbs = dir.z;
            zIndex = POSZ;
            zuSign = 1.0f;
            zvSign = 1.0f;
        } else {
            zAbs = -dir.z;
            zIndex = NEGZ;
            zuSign = -1.0f;
            zvSign = 1.0f;
        }
        int maxIndex = xIndex;
        float u = dir.z, v = dir.y;
        float uSign = xuSign, vSign = xvSign, maxAxis = xAbs;
        if(yAbs > maxAxis){
            maxAxis = yAbs;
            maxIndex = yIndex;
            u = dir.x;
            v = dir.z;
            uSign = yuSign;
            vSign = yvSign;
        }
        if(zAbs > maxAxis){
            maxAxis = zAbs;
            maxIndex = zIndex;
            u = dir.x;
            v = dir.y;
            uSign = zuSign;
            vSign = zvSign;
        }
        int data[] = imageData[maxIndex];
        float nu = 0.5f * uSign * u / maxAxis + 0.5f;
        float nv = 0.5f * vSign * v / maxAxis + 0.5f;
        if(linear){
            return sample2DLinear(data, nu, nv);
        }else{
            return sample2DNearest(data, nu, nv);
        }
    }

    /**
     * Test if a point is inside the given polygon.
     * @param x Coordinate X
     * @param y Coordinate Y
     * @param poly Array of points
     * @return true if inside the polygon, false otherwise.
     */
    private static boolean pointInPolygon(float x, float y, Vector2D poly[]) {
        boolean inside = false;
        Vector2D p0, p1;
        p0 = poly[poly.length-1];
        for(int i = 0; i < poly.length; i++){
            p1 = poly[i];
            if((p0.y < y && p1.y > y) || (p0.y > y && p1.y < y)){
               if((p0.x > x && p1.x > x) || x < ((y-p0.y)*(p1.x-p0.x)/(p1.y-p0.y)+p0.x)){
                   inside = !inside;
               }
            }
            p0 = p1;
        }        
        return inside;
    }
    
    /**
     * Test if a given point is inside cubemap reference.
     * @param u Coordinate U
     * @param v Coordinate V
     * @param cubemapFace Index of cubemap face
     * @return true if inside the reference, false otherwise
     */
    private static boolean reference(float u, float v, int cubemapFace){
        //Test if point is inside the border
        if(u <= 0.01f || u >= 0.99f || v <= 0.01f || v >= 0.99f){
            return true;
        }
        //Reject if outside of bounding box
        if(u < UMIN || u > UMAX || v < VMIN || v > VMAX){
            return false;
        }
        boolean ref = false;
        //Test if point is inside polygons
        switch(cubemapFace){
            case Cubemap.POSX:
                ref = pointInPolygon(u, v, SIGN_PLUS_POINTS) || pointInPolygon(u, v, LETTER_X_POINTS);
                break;
            case Cubemap.NEGX:
                ref = pointInPolygon(u, v, SIGN_MINUS_POINTS) || pointInPolygon(u, v, LETTER_X_POINTS);
                break;
            case Cubemap.POSY:
                ref = pointInPolygon(u, v, SIGN_PLUS_POINTS) || pointInPolygon(u, v, LETTER_Y_POINTS);
                break;
            case Cubemap.NEGY:
                ref = pointInPolygon(u, v, SIGN_MINUS_POINTS) || pointInPolygon(u, v, LETTER_Y_POINTS);
                break;
            case Cubemap.POSZ:
                ref = pointInPolygon(u, v, SIGN_PLUS_POINTS) || pointInPolygon(u, v, LETTER_Z_POINTS);
                break;
            case Cubemap.NEGZ:
                ref = pointInPolygon(u, v, SIGN_MINUS_POINTS) || pointInPolygon(u, v, LETTER_Z_POINTS);
                break;
        }
        return ref;
    }

    /**
     * Sample cubemap and reference with vector.
     * @param dir 3D vector.
     * @param linear true to use bilinear interpolation, false otherwise.
     * @param ref Color for reference as 32 bits integer
     * @return Color as 32 bits integer. 
     */
    public int sampleCubemapRef(Vector3D dir, boolean linear, int ref) {
        float xuSign, xvSign, xAbs;
        float yuSign, yvSign, yAbs;
        float zuSign, zvSign, zAbs;
        int xIndex, yIndex, zIndex;
        if (dir.x >= 0.0f) {
            xAbs = dir.x;
            xIndex = POSX;
            xuSign = -1.0f;
            xvSign = 1.0f;
        } else {
            xAbs = -dir.x;
            xIndex = NEGX;
            xuSign = 1.0f;
            xvSign = 1.0f;
        }
        if (dir.y >= 0.0f) {
            yAbs = dir.y;
            yIndex = POSY;
            yuSign = 1.0f;
            yvSign = -1.0f;
        } else {
            yAbs = -dir.y;
            yIndex = NEGY;
            yuSign = 1.0f;
            yvSign = 1.0f;
        }
        if (dir.z >= 0.0f) {
            zAbs = dir.z;
            zIndex = POSZ;
            zuSign = 1.0f;
            zvSign = 1.0f;
        } else {
            zAbs = -dir.z;
            zIndex = NEGZ;
            zuSign = -1.0f;
            zvSign = 1.0f;
        }
        int maxIndex = xIndex;
        float u = dir.z, v = dir.y;
        float uSign = xuSign, vSign = xvSign, maxAxis = xAbs;
        if(yAbs > maxAxis){
            maxAxis = yAbs;
            maxIndex = yIndex;
            u = dir.x;
            v = dir.z;
            uSign = yuSign;
            vSign = yvSign;
        }
        if(zAbs > maxAxis){
            maxAxis = zAbs;
            maxIndex = zIndex;
            u = dir.x;
            v = dir.y;
            uSign = zuSign;
            vSign = zvSign;
        }
        int data[] = imageData[maxIndex];
        float nu = 0.5f * uSign * u / maxAxis + 0.5f;
        float nv = 0.5f * vSign * v / maxAxis + 0.5f;

        if(reference(nu, nv, maxIndex)){
            return ref;
        }              
        if(linear){
            return sample2DLinear(data, nu, nv);
        }else{
            return sample2DNearest(data, nu, nv);
        }
    }
    
    /**
     * Load an image, validate if it is square, and convert to BufferedImage.TYPE_INT_RGB if necessary.
     * @param imageFile The image file
     * @return The read image
     * @throws IOException If couldn't open the file or if not a square image.
     */
    private static BufferedImage readImageAsRgb(File imageFile) throws IOException {
        String fileName = imageFile.getName();
        BufferedImage image = ImageIO.read(imageFile);
        if(image == null){
            throw new IOException("Couldn't open file: "+ fileName);
        }
        if(image.getWidth() != image.getHeight()){
            throw new IOException(fileName + " is not a square image");
        }
        if(image.getType() != BufferedImage.TYPE_INT_RGB) {
            BufferedImage imageRgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = imageRgb.createGraphics();
            g.drawImage(image, 0, 0, null);
            g.dispose();
            image = imageRgb;
        }
        return image;
    }
        
    /**
     * Load images from specified directory and returns a cubemap object.
     * @param path Path to valid cubemap directory.
     * @return Cubemap object
     * @throws IOException
     */
    public static Cubemap loadCubemap(String path) throws IOException {
        if(path == null){
            throw new NullPointerException("path is null");
        }
        File posXFile = null, negXFile = null, posYFile = null, negYFile = null, posZFile = null, negZFile = null;
        File cubemapDir = new File(path);
        if(!cubemapDir.isDirectory()){
            throw new IOException("Path " + cubemapDir.getName() + " is not a valid directory");
        }
        File files[] = cubemapDir.listFiles();
        for(File f: files){
            if(f.isFile()){
                String fileName = f.getName().toLowerCase();
                if(fileName.startsWith("posx") || fileName.startsWith("right")){
                    posXFile = f;
                }else if(fileName.startsWith("negx") || fileName.startsWith("left")){
                    negXFile = f;
                }else if(fileName.startsWith("posy") || fileName.startsWith("top")){
                    posYFile = f;
                }else if(fileName.startsWith("negy") || fileName.startsWith("bottom")){
                    negYFile = f;
                } else if(fileName.startsWith("posz") || fileName.startsWith("front")){
                    posZFile = f;
                }else if(fileName.startsWith("negz") || fileName.startsWith("back")){
                    negZFile = f;
                }
            }
        }
        if(posXFile == null){
            throw new IOException("Couldn't find neither posx nor right");
        }
        if(negXFile == null){
            throw new IOException("Couldn't find neither negx nor left");
        }
        if(posYFile == null){
            throw new IOException("Couldn't find neither posy nor top");
        }
        if(negYFile == null){
            throw new IOException("Couldn't find neither negy nor bottom");
        }
        if(posZFile == null){
            throw new IOException("Couldn't find neither posz nor front");
        }
        if(negZFile == null){
            throw new IOException("Couldn't find neither negz nor back");
        }
        //Load images and construct cubemap
        BufferedImage posXImage, negXImage, posYImage, negYImage, posZImage, negZImage;
        posXImage = readImageAsRgb(posXFile);
        negXImage = readImageAsRgb(negXFile);
        posYImage = readImageAsRgb(posYFile);
        negYImage = readImageAsRgb(negYFile);
        posZImage = readImageAsRgb(posZFile);
        negZImage = readImageAsRgb(negZFile);
        if(posXImage.getWidth() != negXImage.getWidth() || negXImage.getWidth() != posYImage.getWidth() ||
           posYImage.getWidth() != negYImage.getWidth() || negYImage.getWidth() != posZImage.getWidth() ||
           posZImage.getWidth() != negZImage.getWidth()){
            throw new IOException("The 6 images have not the same size");
        }
        return new Cubemap(cubemapDir.getName(), posXImage, negXImage, posYImage, negYImage, posZImage, negZImage);
    }

}