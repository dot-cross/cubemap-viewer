package math;

/**
 * Vector 2D class
 * @author edu
 */
public final class Vector2D {
 
    public float x;
    public float y;
    
    public Vector2D(){
    }
    
    public Vector2D(float x, float y){
        this.x = x;
        this.y = y;
    }
    
    public Vector2D(Vector2D vec){
        this.x = vec.x;
        this.y = vec.y;
    }
    
    public void normalize() {
        float length = (float) Math.sqrt(x * x + y * y );
        x /= length;
        y /= length;
    }
    
    public void assign(Vector2D copy){
        this.x = copy.x;
        this.y = copy.y;
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public Vector2D add(Vector2D vec) {
        return new Vector2D(x + vec.x, y + vec.y);
    }

    public Vector2D sub(Vector2D vec){
        return new Vector2D(x - vec.x, y - vec.y);
    }
    
    public Vector2D scale(float k){
        return new Vector2D(k * x, k * y);
    }
            
    public float dot(Vector2D vec) {
        return x * vec.x + x * vec.y;
    }
    
    public Vector2D getNormal(){
        Vector2D normal = new Vector2D();
        if(Math.abs(x) > Math.abs(y)){
            normal.x = -y/x;
            normal.y = 1.0f;
        }else{
            normal.x = 1.0f;
            normal.y = -x/y;
        }
        normal.normalize();
        return normal;
    }

    public float angle(Vector2D vec0, Vector2D vec1){
        float length0 = vec0.length();
        float length1 = vec1.length();
        float dot = MathUtils.clamp(vec0.dot(vec1) / (length0*length1), -1.0f, 1.0f);
        return (float)Math.acos(dot);
    }
    
    public Vector2D reflection(Vector2D normalVec, Vector2D incidentVec){
        float normalDotIncident = normalVec.dot(incidentVec);
        Vector2D reflectionVec = incidentVec.sub(normalVec.scale(2.0f * normalDotIncident));
        return reflectionVec;
    }

    @Override
    public String toString() {
        return "["+ x + ", " + y + "]";
    }
}
