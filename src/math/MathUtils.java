package math;

/**
 * Some math utilities
 * @author edu
 */
public class MathUtils {
    
    public static final float clamp(float value, float min, float max){
        if(value < min){
            value = min;
        }else if(value > max){
            value = max;
        }
        return value;
    }
    
    public static final int clamp(int value, int min, int max){
        if(value < min){
            value = min;
        }else if(value > max){
            value = max;
        }
        return value;
    }

    public static final float lerp(float start, float end, float alpha){
        return start + alpha * (end-start);
    }
    
}