package evaluator;

/**
 * @author Paulo
 */
public enum TimeDistEnum {
    
    T_NORMAL_DIST(0), 
    T_ALL_AT_ONCE(1);
    
    public final int value;
    
    TimeDistEnum(int value){
        this.value = value;
    }
    
}
