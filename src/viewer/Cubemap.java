package viewer;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import math.MathUtils;
import math.Vector3D;

/**
 *
 * @author edu
 */
public class Cubemap {
    
    public static final int POSX = 0;
    public static final int NEGX = 1;
    public static final int POSY = 2;
    public static final int NEGY = 3;
    public static final int POSZ = 4;
    public static final int NEGZ = 5;
    public final BufferedImage images[];
    public final int imageData[][];
    public final int size;
    private final int size_minus_one;

    public Cubemap(BufferedImage posX, BufferedImage negX, BufferedImage posY, BufferedImage negY, BufferedImage posZ, BufferedImage negZ){
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
    
    private int sample2DNearest(int data[], float u, float v) {
        int ru = (int) (u * size);
        int rv = (int) (v * size);
        ru = MathUtils.clamp(ru, 0, size_minus_one);
        rv = MathUtils.clamp(rv, 0, size_minus_one);
        rv = size_minus_one - rv;
        return data[rv * size + ru];
    }

    private int sample2DLinear(int data[], float u, float v) {

        float mu, mv, alpha, beta;
        int u0, u1, v0, v1;
        mu = -0.5f + u * size;
        u0 = (int)Math.floor(mu);
        u1 = u0+1;
        alpha = mu - u0;
        u0 = MathUtils.clamp(u0, 0, size_minus_one);
        u1 = MathUtils.clamp(u1, 0, size_minus_one);
        
        mv = -0.5f + v * size;
        v0 = (int)Math.floor(mv);
        v1 = v0+1;
        beta = mv - v0;
        v0 = MathUtils.clamp(v0, 0, size_minus_one);
        v1 = MathUtils.clamp(v1, 0, size_minus_one);
        v0 = size_minus_one - v0;
        v1 = size_minus_one - v1;
        
        int s00 = data[v0 * size + u0];
        float r00 = (s00 >> 16);
        float g00 = (s00 >> 8) & 0xFF;
        float b00 = (s00 & 0xFF);
        
        int s01 = data[v0 * size + u1];
        float r01 = (s01 >> 16);
        float g01 = (s01 >> 8) & 0xFF;
        float b01 = (s01 & 0xFF);

        float r0 =  MathUtils.lerp(r00, r01, alpha);
        float g0 =  MathUtils.lerp(g00, g01, alpha);
        float b0 =  MathUtils.lerp(b00, b01, alpha);
        
        int s10 = data[v1 * size + u0];
        float r10 = (s10 >> 16);
        float g10 = (s10 >> 8) & 0xFF;
        float b10 = (s10 & 0xFF);
        
        int s11 = data[v1 * size + u1];
        float r11 = (s11 >> 16);
        float g11 = (s11 >> 8) & 0xFF;
        float b11 = (s11 & 0xFF);
        
        float r1 =  MathUtils.lerp(r10, r11, alpha);
        float g1 =  MathUtils.lerp(g10, g11, alpha);
        float b1 =  MathUtils.lerp(b10, b11, alpha);
        
        int r,g,b;
        r = (int)(MathUtils.lerp(r0, r1, beta) + 0.5f);
        g = (int)(MathUtils.lerp(g0, g1, beta) + 0.5f);
        b = (int)(MathUtils.lerp(b0, b1, beta) + 0.5f);
        int color = r << 16 | g << 8 | b;
        return color;
    }

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

    public static Cubemap loadCubemap(String path) throws IOException, Exception {
        BufferedImage imageTmp;
        File posXFile = null, negXFile = null, posYFile = null, negYFile = null, posZFile = null, negZFile = null;
        File cubemapDir = new File(path);
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
            throw new Exception("Cannot find posx");
        }
        if(negXFile == null){
            throw new Exception("Cannot find negx");
        }
        if(posYFile == null){
            throw new Exception("Cannot find posy");
        }
        if(negYFile == null){
            throw new Exception("Cannot find negy");
        }
        if(posZFile == null){
            throw new Exception("Cannot find posz");
        }
        if(negZFile == null){
            throw new Exception("Cannot find negz");
        }
        // Load Positive X
        imageTmp = ImageIO.read(posXFile);
        if(imageTmp.getWidth() != imageTmp.getHeight()){
            throw new Exception("posx is not a square image");
        }
        BufferedImage posXImage = new BufferedImage(imageTmp.getWidth(), imageTmp.getHeight(), BufferedImage.TYPE_INT_RGB);
        posXImage.getGraphics().drawImage(imageTmp, 0, 0, null);
        // Load Negative X
        imageTmp = ImageIO.read(negXFile);
        if(imageTmp.getWidth() != imageTmp.getHeight()){
            throw new Exception("negx is not a square image");
        }
        BufferedImage negXImage = new BufferedImage(imageTmp.getWidth(), imageTmp.getHeight(), BufferedImage.TYPE_INT_RGB);
        negXImage.getGraphics().drawImage(imageTmp, 0, 0, null);
        // Positive Y
        imageTmp = ImageIO.read(posYFile);
        if(imageTmp.getWidth() != imageTmp.getHeight()){
            throw new Exception("posy is not a square image");
        }
        BufferedImage posYImage = new BufferedImage(imageTmp.getWidth(), imageTmp.getHeight(), BufferedImage.TYPE_INT_RGB);
        posYImage.getGraphics().drawImage(imageTmp, 0, 0, null);
        // Negative Y
        imageTmp = ImageIO.read(negYFile);
        if(imageTmp.getWidth() != imageTmp.getHeight()){
            throw new Exception("negy is not a square image");
        }
        BufferedImage negYImage = new BufferedImage(imageTmp.getWidth(), imageTmp.getHeight(), BufferedImage.TYPE_INT_RGB);
        negYImage.getGraphics().drawImage(imageTmp, 0, 0, null);
        // Positive Z
        imageTmp = ImageIO.read(posZFile);
        if(imageTmp.getWidth() != imageTmp.getHeight()){
            throw new Exception("posz is not a square image");
        }
        BufferedImage posZImage = new BufferedImage(imageTmp.getWidth(), imageTmp.getHeight(), BufferedImage.TYPE_INT_RGB);
        posZImage.getGraphics().drawImage(imageTmp, 0, 0, null);
        // Negative Z
        imageTmp = ImageIO.read(negZFile);
        if(imageTmp.getWidth() != imageTmp.getHeight()){
            throw new Exception("negz is not a square image");
        }
        BufferedImage negZImage = new BufferedImage(imageTmp.getWidth(), imageTmp.getHeight(), BufferedImage.TYPE_INT_RGB);
        negZImage.getGraphics().drawImage(imageTmp, 0, 0, null);
        if(posXImage.getWidth() != negXImage.getWidth() || negXImage.getWidth() != posYImage.getWidth() ||
                posYImage.getWidth() != negYImage.getWidth() || negYImage.getWidth() != posZImage.getWidth() ||
                posZImage.getWidth() != negZImage.getWidth()){
            throw new Exception("The 6 images have not the same size");
        }
        return new Cubemap(posXImage, negXImage, posYImage, negYImage, posZImage, negZImage);
    }
    
    public static Cubemap createReferenceCubemap(){
        int size = 512, border = 3;
        Font f = new Font("Arial", Font.BOLD, 100);
        Color bgColor = new Color(255, 255, 255, 0);
        int tx, ty, txtWidth, txtHeight, txtAscent;
        Graphics g;
        FontMetrics m;
        // Positive X
        BufferedImage posXImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        g = posXImage.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, border, size);
        g.fillRect(size - border, 0, border, size);
        g.fillRect(0, 0, size, border);
        g.fillRect(0, size - border, size, border);
        g.setFont(f);
        m = g.getFontMetrics();
        txtWidth = m.stringWidth("+X");
        txtHeight = m.getHeight();
        txtAscent = m.getAscent();
        tx = size / 2 - txtWidth / 2;
        ty = size / 2 - txtHeight / 2 + txtAscent;
        g.drawString("+X", tx, ty);
        // Negative X
        BufferedImage negXImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        g = negXImage.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, border, size);
        g.fillRect(size - border, 0, border, size);
        g.fillRect(0, 0, size, border);
        g.fillRect(0, size - border, size, border);
        g.setFont(f);
        m = g.getFontMetrics();
        txtWidth = m.stringWidth("-X");
        txtHeight = m.getHeight();
        txtAscent = m.getAscent();
        tx = size / 2 - txtWidth / 2;
        ty = size / 2 - txtHeight / 2 + txtAscent;
        g.drawString("-X", tx, ty);
        // Positive Y
        BufferedImage posYImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        g = posYImage.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, border, size);
        g.fillRect(size - border, 0, border, size);
        g.fillRect(0, 0, size, border);
        g.fillRect(0, size - border, size, border);
        g.setFont(f);
        m = g.getFontMetrics();
        txtWidth = m.stringWidth("+Y");
        txtHeight = m.getHeight();
        txtAscent = m.getAscent();
        tx = size / 2 - txtWidth / 2;
        ty = size / 2 - txtHeight / 2 + txtAscent;
        g.drawString("+Y", tx, ty);
        // Negative Y
        BufferedImage negYImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        g = negYImage.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, border, size);
        g.fillRect(size - border, 0, border, size);
        g.fillRect(0, 0, size, border);
        g.fillRect(0, size - border, size, border);
        g.setFont(f);
        m = g.getFontMetrics();
        txtWidth = m.stringWidth("-Y");
        txtHeight = m.getHeight();
        txtAscent = m.getAscent();
        tx = size / 2 - txtWidth / 2;
        ty = size / 2 - txtHeight / 2 + txtAscent;
        g.drawString("-Y", tx, ty);
        // Positive Z
        BufferedImage posZImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        g = posZImage.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, border, size);
        g.fillRect(size - border, 0, border, size);
        g.fillRect(0, 0, size, border);
        g.fillRect(0, size - border, size, border);
        g.setFont(f);
        m = g.getFontMetrics();
        txtWidth = m.stringWidth("+Z");
        txtHeight = m.getHeight();
        txtAscent = m.getAscent();
        tx = size / 2 - txtWidth / 2;
        ty = size / 2 - txtHeight / 2 + txtAscent;
        g.drawString("+Z", tx, ty);
        // Negative Z
        BufferedImage negZImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        g = negZImage.createGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, size, size);
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, border, size);
        g.fillRect(size - border, 0, border, size);
        g.fillRect(0, 0, size, border);
        g.fillRect(0, size - border, size, border);
        g.setFont(f);
        m = g.getFontMetrics();
        txtWidth = m.stringWidth("-Z");
        txtHeight = m.getHeight();
        txtAscent = m.getAscent();
        tx = size / 2 - txtWidth / 2;
        ty = size / 2 - txtHeight / 2 + txtAscent;
        g.drawString("-Z", tx, ty);
        return new Cubemap(posXImage, negXImage, posYImage, negYImage, posZImage, negZImage);
    }
}
