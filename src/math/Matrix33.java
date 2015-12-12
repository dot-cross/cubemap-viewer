package math;

/**
 * Matrix 3x3
 * @author edu
 */
public final class Matrix33 {

    public float m00, m01, m02;
    public float m10, m11, m12;
    public float m20, m21, m22;

    public Matrix33(){
    }

    public Matrix33(float m00, float m01, float m02,
                    float m10, float m11, float m12,
                    float m20, float m21, float m22) {
        assign(m00, m01, m02, m10, m11, m12, m20, m21, m22);
    }
    
    public Matrix33(Matrix33 mat, boolean transpose){
        if(transpose){
            assignTranspose(mat);
        }else{
            assign(mat);
        }
    }

    public Matrix33(Vector3D vec1, Vector3D vec2, Vector3D vec3, boolean rowOrder){
        if(rowOrder){
            assignRow(vec1, vec2, vec3);
        }else{
            assignColumn(vec1, vec2, vec3);
        }
    }
    
    public Matrix33(float d){
        m00 = d;     m01 = 0.0f;  m02 = 0.0f;
        m10 = 0.0f;  m11 = d;     m12 = 0.0f;
        m20 = 0.0f;  m21 = 0.0f;  m22 = d;
    }
    
    public Matrix33(float d1, float d2, float d3){
        m00 = d1;    m01 = 0.0f;  m02 = 0.0f;
        m10 = 0.0f;  m11 = d2;    m12 = 0.0f;
        m20 = 0.0f;  m21 = 0.0f;  m22 = d3;
    }
    
    public void assign(float m00, float m01, float m02,
                    float m10, float m11, float m12,
                    float m20, float m21, float m22) {
        this.m00 = m00;  this.m01 = m01;  this.m02 = m02;
        this.m10 = m10;  this.m11 = m11;  this.m12 = m12;
        this.m20 = m20;  this.m21 = m21;  this.m22 = m22;
    }
    
    public void assign(Matrix33 mat){
        m00 = mat.m00;  m01 = mat.m01;  m02 = mat.m02;
        m10 = mat.m10;  m11 = mat.m11;  m12 = mat.m12;
        m20 = mat.m20;  m21 = mat.m21;  m22 = mat.m22;
    }
    
    public void assignTranspose(Matrix33 mat){
        m00 = mat.m00;  m01 = mat.m10;  m02 = mat.m20;
        m10 = mat.m01;  m11 = mat.m11;  m12 = mat.m21;
        m20 = mat.m02;  m21 = mat.m12;  m22 = mat.m22;
    }
    
    public void assignRow(Vector3D row1, Vector3D row2, Vector3D row3){
        m00 = row1.x;  m01 = row1.y;  m02 = row1.z;
        m10 = row2.x;  m11 = row2.y;  m12 = row2.z;
        m20 = row3.x;  m21 = row3.y;  m22 = row3.z;
    }
    
    public void assignColumn(Vector3D col1, Vector3D col2, Vector3D col3){
        m00 = col1.x;  m01 = col2.x;  m02 = col3.x;
        m10 = col1.y;  m11 = col2.y;  m12 = col3.y;
        m20 = col1.z;  m21 = col2.z;  m22 = col3.z;
    }
    
    public Vector3D getColumnVec1(){
        return new Vector3D(m00, m10, m20);
    }
    
    public void setColumnVec1(Vector3D vec){
        m00 = vec.x;
        m10 = vec.y;
        m20 = vec.z;
    }
    
    public Vector3D getColumnVec2(){
        return new Vector3D(m01, m11, m21);
    }
    
    public void setColumnVec2(Vector3D vec){
        m01 = vec.x;
        m11 = vec.y;
        m21 = vec.z;
    }
    
    public Vector3D getColumnVec3(){
        return new Vector3D(m02, m12, m22);
    }
    
    public void setColumnVec3(Vector3D vec){
        m02 = vec.x;
        m12 = vec.y;
        m22 = vec.z;
    }
    
    public Vector3D getRowVec1(){
        return new Vector3D(m00, m01, m02);
    }
    
    public void setRowVec1(Vector3D vec){
        m00 = vec.x;
        m01 = vec.y;
        m02 = vec.z;
    }
    
    public Vector3D getRowVec2(){
        return new Vector3D(m10, m11, m12);
    }
    
    public void setRowVec2(Vector3D vec){
        m10 = vec.x;
        m11 = vec.y;
        m12 = vec.z;
    }
    
    public Vector3D getRowVec3(){
        return new Vector3D(m20, m21, m22);
    }
    
    public void setRowVec3(Vector3D vec){
        m20 = vec.x;
        m21 = vec.y;
        m22 = vec.z;
    }
    
    public float determinant(){
        return m00*m11*m22 + m01*m12*m20 + m02*m10*m21 -
                m20*m11*m02 - m21*m12*m00 - m22*m10*m01;
    }
    
    public Matrix33 inverse(){
        float det = determinant();
        if(det == 0.0f){
            return null;
        }
        Matrix33 inv = new Matrix33();
        inv.m00 = Matrix22.det(m11, m12, m21, m22) / det;   inv.m01 = -Matrix22.det(m01, m02, m21, m22) / det;  inv.m02 = Matrix22.det(m01, m02, m11, m12) / det;
	inv.m10 = -Matrix22.det(m10, m12, m20, m22) / det;  inv.m11 = Matrix22.det(m00, m02, m20, m22) / det;   inv.m12 = -Matrix22.det(m00, m02, m10, m12) / det;
	inv.m20 = Matrix22.det(m10, m11, m20, m21) / det;   inv.m21 = -Matrix22.det(m00, m01, m20, m21) / det;  inv.m22 = Matrix22.det(m00, m01, m10, m11) / det;
        return inv;
    }
    
    public Matrix33 transpose(){
        return new Matrix33(this, true);
    }
    
    public Vector3D mult(Vector3D vec){
        Vector3D output = new Vector3D();
        output.x = m00*vec.x + m01*vec.y + m02*vec.z;
        output.y = m10*vec.x + m11*vec.y + m12*vec.z;
        output.z = m20*vec.x + m21*vec.y + m22*vec.z;
        return output;
    }
    
    public void mult(Vector3D in, Vector3D out){
        out.x = m00*in.x + m01*in.y + m02*in.z;
        out.y = m10*in.x + m11*in.y + m12*in.z;
        out.z = m20*in.x + m21*in.y + m22*in.z;
    }
    
    public Matrix33 mult(Matrix33 in){
        Matrix33 out = new Matrix33();
        out.m00 = m00 * in.m00 + m01 * in.m10 + m02 * in.m20;
        out.m01 = m00 * in.m01 + m01 * in.m11 + m02 * in.m21;
        out.m02 = m00 * in.m02 + m01 * in.m12 + m02 * in.m22;
        out.m10 = m10 * in.m00 + m11 * in.m10 + m12 * in.m20;
        out.m11 = m10 * in.m01 + m11 * in.m11 + m12 * in.m21;
        out.m12 = m10 * in.m02 + m11 * in.m12 + m12 * in.m22;
        out.m20 = m20 * in.m00 + m21 * in.m10 + m22 * in.m20;
        out.m21 = m20 * in.m01 + m21 * in.m11 + m22 * in.m21;
        out.m22 = m20 * in.m02 + m21 * in.m12 + m22 * in.m22;
        return out;
    }

    public Matrix33 mult(Matrix33 in, Matrix33 out){
        out.m00 = m00 * in.m00 + m01 * in.m10 + m02 * in.m20;
        out.m01 = m00 * in.m01 + m01 * in.m11 + m02 * in.m21;
        out.m02 = m00 * in.m02 + m01 * in.m12 + m02 * in.m22;
        out.m10 = m10 * in.m00 + m11 * in.m10 + m12 * in.m20;
        out.m11 = m10 * in.m01 + m11 * in.m11 + m12 * in.m21;
        out.m12 = m10 * in.m02 + m11 * in.m12 + m12 * in.m22;
        out.m20 = m20 * in.m00 + m21 * in.m10 + m22 * in.m20;
        out.m21 = m20 * in.m01 + m21 * in.m11 + m22 * in.m21;
        out.m22 = m20 * in.m02 + m21 * in.m12 + m22 * in.m22;
        return out;
    }
    
    public static Matrix33 identity(){
        return new Matrix33(1.0f);
    }
    
    public static Matrix33 rotate(float angle, float x, float y, float z) {
        float c = (float) Math.cos(angle);
        float one_minus_c = 1.0f - c;
        float s = (float) Math.sin(angle);
        Matrix33 mat = new Matrix33();
        mat.m00 = x * x * one_minus_c + c;      mat.m01 = x * y * one_minus_c - z * s;  mat.m02 = x * z * one_minus_c + y * s;
        mat.m10 = x * y * one_minus_c + z * s;  mat.m11 = y * y * one_minus_c + c;      mat.m12 = y * z * one_minus_c - x * s;
        mat.m20 = x * z * one_minus_c - y * s;  mat.m21 = y * z * one_minus_c + x * s;  mat.m22 = z * z * one_minus_c + c;
        return mat;
    }
    
    public static Matrix33 rotateAlign(Vector3D fromDir, Vector3D toDir){
        Vector3D axis = fromDir.cross(toDir);
        float c = fromDir.dot(toDir);
        float k = 1.0f / (1.0f + c);
        float x = axis.x;
        float y = axis.y;
        float z = axis.z;
        Matrix33 mat = new Matrix33();
        mat.m00 = x * x * k + c;  mat.m01 = x * y * k - z;  mat.m02 = x * z * k + y;
        mat.m10 = x * y * k + z;  mat.m11 = y * y * k + c;  mat.m12 = y * z * k - x;
        mat.m20 = x * z * k - y;  mat.m21 = y * z * k + x;  mat.m22 = z * z * k + c;
        return mat;
    }

    public static Matrix33 rotateX(float angle){
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);
        Matrix33 mat = new Matrix33();
        mat.m00 = 1.0f;  mat.m01 = 0.0f;  mat.m02 = 0.0f;
        mat.m10 = 0.0f;  mat.m11 = c;     mat.m12 = -s;
        mat.m20 = 0.0f;  mat.m21 = s;     mat.m22 = c;
        return mat;
    }
    
    public static Matrix33 rotateY(float angle){
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);
        Matrix33 mat = new Matrix33();
        mat.m00 = c;     mat.m01 = 0.0f;  mat.m02 = s;
        mat.m10 = 0.0f;  mat.m11 = 1.0f;  mat.m12 = 0.0f;
        mat.m20 = -s;    mat.m21 = 0.0f;  mat.m22 = c;
        return mat;
    }
    
    public static Matrix33 rotateZ(float angle){
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);
        Matrix33 mat = new Matrix33();
        mat.m00 = c;     mat.m01 = -s;    mat.m02 = 0.0f;
        mat.m10 = s;     mat.m11 = c;     mat.m12 = 0.0f;
        mat.m20 = 0.0f;  mat.m21 = 0.0f;  mat.m22 = 1.0f;
        return mat;
    }
    
    public static Matrix33 scale(float sx, float sy, float sz){
        Matrix33 mat = new Matrix33();
        mat.m00 = sx;    mat.m01 = 0.0f;  mat.m02 = 0.0f;
        mat.m10 = 0.0f;  mat.m11 = sy;    mat.m12 = 0.0f;
        mat.m20 = 0.0f;  mat.m21 = 0.0f;  mat.m22 = sz;
        return mat;
    }

    public static Matrix33 scaleDir(float k, float x, float y, float z){
        Matrix33 mat = new Matrix33();
        float k_minus_1 = k - 1.0f;
        mat.m00 = k_minus_1 * x * x + 1.0f;  mat.m01 = k_minus_1 * x * y;         mat.m02 = k_minus_1 * x * z;
        mat.m10 = k_minus_1 * x * y;         mat.m11 = k_minus_1 * y * y + 1.0f;  mat.m12 = k_minus_1 * y * z;
        mat.m20 = k_minus_1 * x * z;         mat.m21 = k_minus_1 * y * z;         mat.m22 = k_minus_1 * z *z + 1.0f;
        return mat;
    }

}
