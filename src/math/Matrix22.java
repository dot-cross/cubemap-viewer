package math;

/**
 * Matrix 2x2
 * @author edu
 */
public final class Matrix22 {
    
    public float m00, m01;
    public float m10, m11;
    
    public Matrix22(){
    }
    
    public Matrix22(float m00, float m01, float m10, float m11){
        assign(m00, m01, m10, m11);
    }
    
    public Matrix22(Matrix22 mat, boolean transpose){
        if(transpose){
            assignTranspose(mat);
        }else{
            assign(mat);
        }
    }
    
    public Matrix22(Vector2D vec1, Vector2D vec2, boolean rowOrder){
        if(rowOrder){
            assignRow(vec1, vec2);
        }else{
            assignColumn(vec1, vec2);
        }
    }
    
    public void assign(float m00, float m01, float m10, float m11){
        this.m00 = m00;  this.m01 = m01;
        this.m10 = m10;  this.m11 = m11;
    }
    
    public void assign(Matrix22 mat){
        m00 = mat.m00;  m01 = mat.m01;
        m10 = mat.m10;  m11 = mat.m11;
    }

    public void assignTranspose(Matrix22 mat){
        m00 = mat.m00;  m01 = mat.m10;
        m10 = mat.m01;  m11 = mat.m11;
    }
    
    public void assignRow(Vector2D row1, Vector2D row2){
        m00 = row1.x;  m01 = row1.y;
        m10 = row2.x;  m11 = row2.y;
    }
    
    public void assignColumn(Vector2D col1, Vector2D col2){
        m00 = col1.x;  m01 = col2.x;
        m10 = col1.y;  m11 = col2.y;
    }
    
    public Vector2D getRow1(){
        return new Vector2D(m00, m01);
    }
    
    public void setRow1(Vector2D vec){
        m00 = vec.x;
        m01 = vec.y;
    }
    
    public Vector2D getRow2(){
        return new Vector2D(m10, m11);
    }
    
    public void setRow2(Vector2D vec){
        m10 = vec.x;
        m11 = vec.y;
    }
    
    public Vector2D getColumn1(){
        return new Vector2D(m00, m10);
    }
    
    public void setColumn1(Vector2D vec){
        m00 = vec.x;
        m10 = vec.y;
    }
    
    public Vector2D getColumn2(){
        return new Vector2D(m10, m11);
    }
    
    public void setColumn2(Vector2D vec){
        m10 = vec.x;
        m11 = vec.y;
    }
    
    public Matrix22 transpose(){
        return new Matrix22(this, true);
    }
    
    public Vector2D mult(Vector2D in){
        Vector2D out = new Vector2D();
        out.x = m00 * in.x + m01 * in.y;
        out.y = m10 * in.x + m11 * in.y;
        return out;
    }

    public void mult(Vector2D in, Vector2D out){
        out.x = m00 * in.x + m01 * in.y;
        out.y = m10 * in.x + m11 * in.y;
    }
    
    public Matrix22 mult(Matrix22 in){
        Matrix22 out = new Matrix22();
        out.m00 = m00 * in.m00 + m01 * in.m10;
        out.m01 = m00 * in.m01 + m01 * in.m11;
        out.m10 = m10 * in.m00 + m11 * in.m10;
        out.m11 = m10 * in.m01 + m11 * in.m11;
        return out;
    }

    public void mult(Matrix22 in, Matrix22 out){
        out.m00 = m00 * in.m00 + m01 * in.m10;
        out.m01 = m00 * in.m01 + m01 * in.m11;
        out.m10 = m10 * in.m00 + m11 * in.m10;
        out.m11 = m10 * in.m01 + m11 * in.m11;
    }
    
    public Matrix22 inverse(){
	float det = determinant();
        if(det == 0.0f){
            return null;
        }
        Matrix22 inv = new Matrix22();
	inv.m00 = m11 / det;
	inv.m10 = -m10 / det;
	inv.m01 = -m01 / det;
	inv.m11 = m00 / det;
        return inv;
    }
    
    public static float det(float m00, float m01, float m10, float m11){
        return m00*m11 - m10*m01;
    }
    
    public float determinant(){
        return m00*m11 - m10*m01;
    }
    
    public static Matrix22 rotate(float angle){
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);
        Matrix22 mat = new Matrix22();
        mat.m00 = c;  mat.m01 = -s;
        mat.m10 = s;  mat.m11 = c;
        return mat;
    }

}
