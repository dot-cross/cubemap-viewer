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
        float t00 = mat.m00;
        float t01 = mat.m01;
        float t10 = mat.m10;
        float t11 = mat.m11;
        m00 = t00;  m01 = t10;
        m10 = t01;  m11 = t11;
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
    
    public Matrix22 add(Matrix22 mat){
        Matrix22 out = new Matrix22();
        return add(mat, out);
    }
    
    public Matrix22 add(Matrix22 mat, Matrix22 out){
        out.m00 = m00 + mat.m00;
        out.m01 = m01 + mat.m01;
        out.m10 = m10 + mat.m10;
        out.m11 = m11 + mat.m11;
        return out;
    }

    public Matrix22 sub(Matrix22 mat){
        Matrix22 out = new Matrix22();
        return sub(mat, out);
    }
    
    public Matrix22 sub(Matrix22 mat, Matrix22 out){
        out.m00 = m00 - mat.m00;
        out.m01 = m01 - mat.m01;
        out.m10 = m10 - mat.m10;
        out.m11 = m11 - mat.m11;
        return out;
    }
    
    public Matrix22 mult(float k){
        Matrix22 out = new Matrix22();
        return mult(k, out);
    }
    
    public Matrix22 mult(float k, Matrix22 out){
        out.m00 = k*m00;
        out.m01 = k*m01;
        out.m10 = k*m10;
        out.m11 = k*m11;
        return out;
    }
    
    public Vector2D mult(Vector2D vec){
        Vector2D out = new Vector2D();
        out.x = m00 * vec.x + m01 * vec.y;
        out.y = m10 * vec.x + m11 * vec.y;
        return out;
    }

    public Vector2D mult(Vector2D vec, Vector2D out){
        float x = m00 * vec.x + m01 * vec.y;
        float y = m10 * vec.x + m11 * vec.y;
        out.x = x;  out.y = y;
        return out;
    }
    
    public Matrix22 mult(Matrix22 mat){
        Matrix22 out = new Matrix22();
        out.m00 = m00 * mat.m00 + m01 * mat.m10;
        out.m01 = m00 * mat.m01 + m01 * mat.m11;
        out.m10 = m10 * mat.m00 + m11 * mat.m10;
        out.m11 = m10 * mat.m01 + m11 * mat.m11;
        return out;
    }

    public Matrix22 mult(Matrix22 mat, Matrix22 out){
        float t00 = m00 * mat.m00 + m01 * mat.m10;
        float t01 = m00 * mat.m01 + m01 * mat.m11;
        float t10 = m10 * mat.m00 + m11 * mat.m10;
        float t11 = m10 * mat.m01 + m11 * mat.m11;
        out.m00 = t00;  out.m01 = t01;
        out.m10 = t10;  out.m11 = t11;
        return out;
    }
    
    public Matrix22 inverse(){
	float det = determinant();
        if(Math.abs(det) <= 0.001f){
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
    
    public static Matrix22 rotateAlign(Vector2D fromDir, Vector2D toDir){
        Matrix22 mat = new Matrix22();
        float c = fromDir.dot(toDir);
        float s = fromDir.x*toDir.y-toDir.x*fromDir.y;
        mat.m00 = c;  mat.m01 = -s;
        mat.m10 = s;  mat.m11 = c;
        return mat;
    }

    public static Matrix22 scale(float sx, float sy){
        Matrix22 mat = new Matrix22();
        mat.m00 = sx;    mat.m01 = 0.0f;
        mat.m10 = 0.0f;  mat.m11 = sy;
        return mat;
    }

    public static Matrix22 scaleDir(float k, float x, float y){
        Matrix22 mat = new Matrix22();
        float k_minus_1 = k - 1.0f;
        mat.m00 = k_minus_1 * x * x + 1.0f;  mat.m01 = k_minus_1 * x * y;
        mat.m10 = k_minus_1 * x * y;         mat.m11 = k_minus_1 * y * y + 1.0f;
        return mat;
    }

}
