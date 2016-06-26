package math;

/**
 * Vector 3D class
 * @author edu
 */
public final class Vector3D {

    public float x;
    public float y;
    public float z;

    public Vector3D(){
    }
    
    public Vector3D(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3D(Vector3D vec) {
        this.x = vec.x;
        this.y = vec.y;
        this.z = vec.z;
    }
    
    public void normalize() {
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        x /= length;
        y /= length;
        z /= length;
    }
    
    public void assign(Vector3D copy){
        this.x = copy.x;
        this.y = copy.y;
        this.z = copy.z;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public Vector3D add(Vector3D vec) {
        Vector3D out = new Vector3D();
        return add(vec, out);
    }
    
    public Vector3D add(Vector3D vec, Vector3D out) {
        out.x = x + vec.x;
        out.y = y + vec.y;
        out.z = z + vec.z;
        return out;
    }

    public Vector3D sub(Vector3D vec) {
        Vector3D out = new Vector3D();
        return sub(vec, out);
    }
    
    public Vector3D sub(Vector3D vec, Vector3D out) {
        out.x = x - vec.x;
        out.y = y - vec.y;
        out.z = z - vec.z;
        return out;
    }
    
    public Vector3D scale(float k){
        Vector3D out = new Vector3D();
        return scale(k, out);
    }
    
    public Vector3D scale(float k, Vector3D out){
        out.x = k * x;
        out.y = k * y;
        out.z = k * z;
        return out;
    }
            
    public float dot(Vector3D vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }
    
    public Vector3D cross(Vector3D vec){
        Vector3D out = new Vector3D();
        return cross(vec, out);
    }
    
    public Vector3D cross(Vector3D vec, Vector3D out){
        out.x = y * vec.z - z * vec.y;
        out.y = z * vec.x - x * vec.z;
        out.z = x * vec.y - y * vec.x;
        return out;
    }
    
    public Vector3D perp() {
        float v[] = new float[]{x, y, z};
        float p[] = new float[3];
        int ma = 0, mi1 = 1, mi2 = 2;
        if(Math.abs(v[1]) > Math.abs(v[ma])){
            mi1 = 0;
            ma = 1;
            mi2 = 2;
        }
        if(Math.abs(v[2]) > Math.abs(v[ma])){
            mi1 = 0;
            mi2 = 1;
            ma = 2;
        }
        p[mi1] = 1.0f;
        p[mi2] = 1.0f;
        p[ma] = (-v[mi1] - v[mi2]) / v[ma];
        return new Vector3D(p[0], p[1], p[2]);
    }

    public static float angle(Vector3D vec0, Vector3D vec1){
        float length0 = vec0.length();
        float length1 = vec1.length();
        float dot = MathUtils.clamp(vec0.dot(vec1) / (length0*length1), -1.0f, 1.0f);
        return (float)Math.acos(dot);
    }
    
    public static Vector3D reflection(Vector3D normalVec, Vector3D incidentVec){
        float normalDotIncident = normalVec.dot(incidentVec);
        Vector3D reflectionVec = incidentVec.sub(normalVec.scale(2.0f * normalDotIncident));
        return reflectionVec;
    }

    @Override
    public String toString() {
        return "["+ x + ", " + y + ", " + z + "]";
    }

}
