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
        return new Vector3D(x + vec.x, y + vec.y, z + vec.z);
    }

    public Vector3D sub(Vector3D vec){
        return new Vector3D(x - vec.x, y - vec.y, x - vec.z);
    }
    
    public Vector3D scale(float k){
        return new Vector3D(k * x, k * y, k * z);
    }
            
    public float dot(Vector3D vec) {
        return x * vec.x + x * vec.y + y * vec.z;
    }
    
    public Vector3D cross(Vector3D vec){
        return new Vector3D(y * vec.z - z * vec.y, z * vec.x - x * vec.z, x * vec.y - y * vec.x);
    }
    
    public Vector3D normalizedCross(Vector3D vec){
        Vector3D cross = new Vector3D(y * vec.z - z * vec.y, z * vec.x - x * vec.z, x * vec.y - y * vec.x);
        cross.normalize();
        return cross;
    }
    
    public Vector3D getNormal(){
        float v[] = new float[]{x, y, z};
        float n[] = new float[3];
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
        n[mi1] = 1.0f;
        n[mi2] = 1.0f;
        n[ma] = (-v[mi1] - v[mi2]) / v[ma];
        Vector3D normal = new Vector3D(n[0], n[1], n[2]);
        normal.normalize();
        return normal;
    }

    public float angle(Vector3D vec0, Vector3D vec1){
        float length0 = vec0.length();
        float length1 = vec1.length();
        float dot = MathUtils.clamp(vec0.dot(vec1) / (length0*length1), -1.0f, 1.0f);
        return (float)Math.acos(dot);
    }
    
    public Vector3D reflection(Vector3D normalVec, Vector3D incidentVec){
        float normalDotIncident = normalVec.dot(incidentVec);
        Vector3D reflectionVec = incidentVec.sub(normalVec.scale(2.0f * normalDotIncident));
        return reflectionVec;
    }

    @Override
    public String toString() {
        return "["+ x + ", " + y + ", " + z + "]";
    }
    
}
