package evaluator;

import com.google.common.collect.Table;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static recommender.algorithm.Recommender.NO_RECOMMENDATION;

/**
 * @author Paulo
 */
public enum RespVarEnum {
    
    T_RMSE_ERROR(1), 
    T_NOVELTY(2),
    T_PRECISION_OVER_N(3),
    T_RECALL_OVER_N(4),
    T_F1(5),
    T_NORM_DCG(6),
    T_ITEM_COVERAGE(7),
    T_USER_COVERAGE(8),
    T_THROUGHPUT(9),
    T_TIME_W_QUEUE(10),
    T_TIME_NO_QUEUE(11);
    
    public final int value;
    public boolean awake; // diz se a variável será ou não medida
    public String y_axis;
    
    RespVarEnum(int value){
        
        this.value = value;
        this.awake = false;
        
    }
    
    public void setYAxis(){
        
        switch (this) {
            case T_RMSE_ERROR:
                y_axis = "Error";
                break;
                
            case T_PRECISION_OVER_N:
                y_axis = "Precision";
                break;
                
            case T_RECALL_OVER_N:
                y_axis = "Recall";
                break;
                
            case T_F1:
                y_axis = "F1";
                break;
                
            case T_NORM_DCG:
                y_axis = "nDCG";
                break;
                
            case T_ITEM_COVERAGE:
                y_axis = "Coverage (%)";
                break;
                
            case T_USER_COVERAGE:
                y_axis = "Coverage (%)";
                break;
                
            case T_NOVELTY:
                y_axis = "Novelty (%)";
                break;
                
            case T_THROUGHPUT:
                y_axis = "Recommendations per second";
                break;
                
            case T_TIME_W_QUEUE: // o histograma é criado se a variavel de resposta time w queue estiver setada
                
                y_axis = "Time (ms)";
                break;
                
            case T_TIME_NO_QUEUE:
                y_axis = "Time (ms)";
                break;
                
            default:
                y_axis = "Values";
        }
        
    }
    
    public float measure(ResponseVariable response_var, Table<Integer, Integer, Float> recommendation_table, List<Integer> test_set,
                        int workload, int rec_list_length, int total_of_users, Map<Integer, Integer> item_popularity,
                        ArrayList<Float> time_list
                        ) throws SQLException{
        
        float measure = NO_RECOMMENDATION;
        
        switch (this) {
            case T_RMSE_ERROR:
                measure = response_var.RMSError(recommendation_table, test_set);
                break;
                
            case T_PRECISION_OVER_N:
                measure = response_var.precisionOverN(recommendation_table, test_set);
                break;
                
            case T_RECALL_OVER_N:
                measure = response_var.recallOverN(recommendation_table, test_set);
                break;
                
            case T_F1:
                float precision = response_var.precisionOverN(recommendation_table, test_set);
                float recall = response_var.recallOverN(recommendation_table, test_set);
                measure = response_var.F1(precision, recall);
                break;
                
            case T_NORM_DCG:
                measure = response_var.normDiscountCumulativeGain(recommendation_table, test_set);
                break;
                
            case T_ITEM_COVERAGE:
                measure = response_var.catalogCoverage(recommendation_table, workload, rec_list_length);
                break;
                
            case T_USER_COVERAGE:
                measure = response_var.userCoverage(recommendation_table, workload);
                break;
                
            case T_NOVELTY:
                measure = response_var.novelty(recommendation_table, test_set, total_of_users, item_popularity);
                break;
                
            case T_THROUGHPUT:
                measure = response_var.throughput(time_list);
                break;
                
            case T_TIME_W_QUEUE: // o histograma é criado se a variavel de resposta time w queue estiver setada
                
                 Benchmarker bk = Benchmarker.getInstance();
                float time = bk.Mean( response_var.getResponseTime() );
                if(time > 0)
                    measure = time;
                TimeDist.createTimeClasses(response_var.getTimeInQueue(), bk.Mean( time_list ), bk.getTimeClasses());
                
                break;
                
            case T_TIME_NO_QUEUE:
                Benchmarker bkm = Benchmarker.getInstance();
                float t = bkm.Mean( time_list );
                if(t > 0)
                    measure = t;
                break;
                
            default:
                throw new RuntimeException("Response variable not found.");
        }
        
        return measure;
        
    }
    
}
