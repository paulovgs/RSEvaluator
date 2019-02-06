package evaluator;

import database.Evaluation;
import java.sql.SQLException;

/**
 * @author Paulo
 * @data 10/26/2017
 */
public enum FactorTypesEnum{ // deve começar de 1 porque 0 indica um SQL null no composed_factor_type
    
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
    
    
    public static boolean persistFactorTypes() throws SQLException{ // salva os factor types no banco, caso ainda não existam

        FactorTypesEnum[] f_types = FactorTypesEnum.values();
        int i;

        Evaluation evaluation = Evaluation.getInstance();
        int size = evaluation.sizeOfFactorTypes();

        int f_types_size = f_types.length;

        if(size == f_types_size){

            return true;

        }else if(size == 0){ // é preciso preencher a tabela com todos os factor types

            for(i = 0; i < f_types_size; i++)
                    evaluation.insertInFactorTypes(f_types[i].value, f_types[i].toString());

        }else{ // existem alguns factors preenchidos, mas nem todos

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