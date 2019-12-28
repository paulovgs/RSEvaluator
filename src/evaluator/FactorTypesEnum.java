package evaluator;

import database.Evaluation;
import java.sql.SQLException;

/**
 * @author Paulo
 */
public enum FactorTypesEnum{
    
    T_CANDIDATES_SIZE(1), 
    T_NEIGHBOORHOOD_SIZE(2),
    T_RECOMMENDATION_LIST_LENGTH(3),
    T_ALTERNATIVE_RECOMMENDATION(4),
    T_WORKLOAD(5);

    public final int value;
    public String default_value;

    FactorTypesEnum(int value){
        this.value = value;
    }
    
    public void setDefault(String value){ default_value = value; }
    
    
    public static boolean persistFactorTypes() throws SQLException{ 

        FactorTypesEnum[] f_types = FactorTypesEnum.values();
        int i;

        Evaluation evaluation = Evaluation.getInstance();
        int size = evaluation.sizeOfFactorTypes();

        int f_types_size = f_types.length;

        if(size == f_types_size){

            return true;

        }else if(size == 0){ 

            for(i = 0; i < f_types_size; i++)
                    evaluation.insertInFactorTypes(f_types[i].value, f_types[i].toString());

        }else{

            for(i = 0; i < f_types_size; i++){

                try{
                    evaluation.insertInFactorTypes(f_types[i].value, f_types[i].toString());

                }catch(SQLException ex){
                    evaluation.updateFactorTypes(f_types[i].value, f_types[i].toString());
                }
            }

        }
        
        return true;
        
    }
    
    
}
