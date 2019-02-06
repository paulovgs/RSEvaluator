package evaluator;

import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import database.Evaluation;
import static utils.Config.ACCEPTABLE_VALUE;
import com.google.common.collect.HashBasedTable;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static recommender.algorithm.Recommender.NO_RECOMMENDATION;
import utils.Utils;

/**
 *
 * @author Paulo
 */
public class ResponseVariable {
    
    private final ArrayList<Float> response_time;
    private final ArrayList<Float> time_in_queue;
    private Table<Integer, Integer, Float> user_test_ratings;
    
    public ResponseVariable(){
        
        response_time = new ArrayList<>();
        time_in_queue = new ArrayList<>();
        user_test_ratings = HashBasedTable.create();
        
    }
    
    public void addResponseTime(float time){ response_time.add(time); }
    
    public void addTimeInQueue(float time){ time_in_queue.add(time); }
    
    public ArrayList<Float> getResponseTime(){ return response_time; }
    
    public ArrayList<Float> getTimeInQueue(){ return time_in_queue; }
    
    /*
    * Faz um mapeamento das ratings do test set dos usuários que foram testados como carga de trabalho
    */
    public void createUserRatingsTable(List<Integer> real_test_set) throws SQLException{
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        
        for (Integer user_id : real_test_set){
            
            ResultSet result = gen.getUserTestSet(user_id);
            while(result.next())
              user_test_ratings.put(user_id, result.getInt( gen.getItemIDLabel() ), result.getFloat( "rating" ));
        
        }
    }
    
    public float RMSError(Table<Integer, Integer, Float> recommendation_table, List<Integer> real_test_set) throws SQLException{
        
        Map<Integer, Float> recommended_list, user_ratings;
        float rmse = 0, rmse_den = 0;
        
        if(recommendation_table.isEmpty())
                return NO_RECOMMENDATION;
       
        for (Integer user_id : real_test_set){ // para cada user que foi testado
            
            recommended_list = recommendation_table.row(user_id);
            user_ratings = user_test_ratings.row(user_id);
            
            float num = 0, den = 0;
            
            if(user_ratings != null){
                
                Set<Integer> ratings = user_ratings.keySet();
                
                for(Integer item_id : recommended_list.keySet()){
                    
                    if(ratings.contains(item_id)){
                        num += Math.pow(user_ratings.get(item_id) - recommended_list.get(item_id) ,2);
                        den ++;
                    }
                    
                }
                                
            }
            
            float mae = (den != 0) ? num/den : 0; // mean absolute error per user 
            
            if(mae != 0){ // if user rated at least one item in recommendation list
                rmse += (float) Math.sqrt(mae);
                rmse_den ++;
            }
                                
        }
        
        return (rmse_den != 0) ? rmse/rmse_den : NO_RECOMMENDATION;
        
    }
    
    public float throughput(ArrayList<Float> values){
        
        int n = values.size();
        float sum = 0;
        
        for(float v : values)
            sum += v;
        
        return (n == 0) ? NO_RECOMMENDATION : 1000* n / sum; // inverso da média *1000 para dar em segundos
        
    }   
    
    public float precisionOverN(Table<Integer, Integer, Float> recommendation_table, List<Integer> real_test_set) throws SQLException{
        
        Map<Integer, Float> recommended_list, user_ratings;
        float precision = 0;
        int den = 0, den2 = 0;
        
        for(Integer user_id : real_test_set){
            
            recommended_list = recommendation_table.row(user_id);
            user_ratings = user_test_ratings.row(user_id);
            
            if(!recommended_list.isEmpty()){ // se lista é vazia precision incrementa de 0 mas den2 nao incrementa, portanto esse zero nao entra no calculo
                den = recommended_list.size();
                if(user_ratings.size() > 0)
                    den2++;
            }
            
            if(user_ratings != null){
                
                float hit = 0;
                
                for(Integer item_id : recommended_list.keySet()){
                    if(user_ratings.keySet().contains(item_id) && user_ratings.get(item_id) >= ACCEPTABLE_VALUE)
                            hit++;
                }
                         
                precision += (den != 0) ? hit/den : 0; // se precision for 0 pq hit é 0, den2 incrementa e isso entra no calculo
                
            }
        }
        
        // den2 = 0 significa que não existe nenhuma lista
        return (den2 != 0 ) ? 100 * precision/den2 : NO_RECOMMENDATION;
    }
    
    public float recallOverN(Table<Integer, Integer, Float> recommendation_table, List<Integer> real_test_set) throws SQLException{
        
        Map<Integer, Float> recommended_list, user_ratings;
        float recall = 0;
        int den, den2 = 0;
        
        for(Integer user_id : real_test_set){
            
            recommended_list = recommendation_table.row(user_id);
            
            user_ratings = user_test_ratings.row(user_id);
            
            if(user_ratings != null){
                
                float hit = 0;
                den = user_ratings.size();
                
                if(!recommended_list.isEmpty() && den > 0)
                    den2++;
                
                for(Integer item_id : recommended_list.keySet()){
                    if(user_ratings.keySet().contains(item_id) && user_ratings.get(item_id) >= ACCEPTABLE_VALUE)
                            hit++;
                }
                
                // hit = 0 den2 incrementa, portanto contabiliza
                // rec list = 0; não contabiliza
                recall += (den != 0) ? hit/den : 0;
                
            }
        }
        
        // den2 = 0; todas as rec lists vazias
        return (den2 != 0 ) ? 100 * recall/den2 : NO_RECOMMENDATION;
    }
            
    public float F1(float precision, float recall){
        
        if(precision == NO_RECOMMENDATION || recall == NO_RECOMMENDATION)
            return NO_RECOMMENDATION;
        
        float den = precision + recall;
        return (den != 0 ) ? 2*precision*recall/den : 0;
    }
    
    public float normDiscountCumulativeGain(Table<Integer, Integer, Float> recommendation_table, List<Integer> real_test_set) throws SQLException{
        
        Map<Integer, Float> recommended_list, user_ratings;
        float dcg = 0, dcg_acc = 0;
        int counter = 0;
        
        for(Integer user_id : real_test_set){
            
            recommended_list = recommendation_table.row(user_id);
            user_ratings = user_test_ratings.row(user_id);
            
            LinkedHashMap<Integer, Float> DCG = new LinkedHashMap<>();
            
            if(user_ratings != null){
                for(Integer item_id : recommended_list.keySet()){
                    if(user_ratings.keySet().contains(item_id))
                        DCG.put(item_id, user_ratings.get(item_id));
                }
                
            }
           
            // será considerado o cálculo para users que tenham pelo menos 3 hits
            if(DCG.size() >= 3){

                int position = 0;
                for(Integer item_id : DCG.keySet()){
                    position ++;
                    dcg += DCG.get(item_id)/discount(position);
                }
                
                dcg /= perfectDiscountCumulativeGain(DCG);
                counter++;
                dcg_acc += dcg;
                dcg = 0;
            }
            
            
        }
        
        if(counter > 0) System.out.println("ndcg cter: "+counter);
        
        // se counter = 0; nenhum user com pelo menos 3 hits alcansado
        return (counter != 0) ? 100 * dcg_acc/counter : NO_RECOMMENDATION;
    }
    
    private float discount(int position){
        return (float) ((position <= 2) ? 1 : Math.log(position) / Math.log(2));
    }
    
    private float perfectDiscountCumulativeGain(LinkedHashMap<Integer, Float> DCG_list){
        
        LinkedHashMap<Integer, Float> sortedDCG = Utils.sortByValue(DCG_list, false); // lista ordenada em ordem dec
        float dcg = 0;
        int position = 0;
        
        for(Integer item_id : sortedDCG.keySet()){
            position ++;
            dcg += sortedDCG.get(item_id)/discount(position);
        }
        
        return dcg;
    }
    
    public float catalogCoverage(Table<Integer, Integer, Float> recommendation_table, int current_workload, int rec_list_length){
        
        // o tamanho da rec_table é a soma de todos os itens recomendados para todos os usuarios
        // se potential items = 0; ou não existe carga ou não existem comprimento > 0. Coverage não pode ser calculada
        float potential_items = rec_list_length * current_workload;
        return (potential_items != 0 ) ? 100 * recommendation_table.size() / potential_items : NO_RECOMMENDATION;

    }
    
    public float userCoverage(Table<Integer, Integer, Float> recommendation_table, int current_workload){
        
        ArrayList<Integer> user_list = new ArrayList<>(); // lista de usuarios distintos atendidos
        
        // se está na recommendation_table significa que possui ao menos um item recomendado
        for (Cell<Integer, Integer, Float> cell: recommendation_table.cellSet()){

            int user = cell.getRowKey();
            if(!user_list.contains(user))
                        user_list.add(user);
            
        }
        
                
        return (current_workload != 0 ) ? 100 *  user_list.size() / current_workload : NO_RECOMMENDATION;

    }
           
   
    public float novelty(Table<Integer, Integer, Float> recommendation_table, List<Integer> real_test_set,
                        int total_of_users, Map<Integer, Integer> item_pop) throws SQLException{
        
        Map<Integer, Float> recommended_list;
        float users_with_list = 0, pop = 0;
        
        for(Integer user_id : real_test_set){
            float acc = 0;
            recommended_list = recommendation_table.row(user_id);
            
            if(recommended_list != null && !recommended_list.isEmpty()){
                
                users_with_list ++;
                for(Integer item_id : recommended_list.keySet()){
                    acc += ( total_of_users != 0 && item_pop.containsKey(item_id)) ? (float) item_pop.get(item_id)/total_of_users : 0;
                }
                acc /= (float)recommended_list.size(); // average popularity for one list
                pop += acc;
                
            }
        }

        if(users_with_list == 0){
            return NO_RECOMMENDATION;
        }else{
            float popularity_total = (users_with_list != 0 ) ? (pop/users_with_list) : 0;
            return (1 - popularity_total)*100;
        }
        
        
    }
    
      
    public static boolean persistResponseVariables() throws SQLException{
               
        RespVarEnum[] rvar_enum = RespVarEnum.values();
        
        int i;
        Evaluation evaluation = Evaluation.getInstance();
        int size = evaluation.sizeOfResponseVariables();
        int rvar_size = rvar_enum.length;

        if(size == rvar_size){

            return true;

        }else if(size == 0){ // é preciso preencher a tabela com todos as response variables

            for(i = 0; i < rvar_size; i++){
                rvar_enum[i].setYAxis();
                evaluation.insertInResponseVariables(rvar_enum[i].value, rvar_enum[i].toString(), rvar_enum[i].y_axis);
            }
            
        }else{ // existem algumas response variables preenchidas, mas nem todas

            for(i = 0; i < rvar_size; i++){
                
                rvar_enum[i].setYAxis();

                try{
                    
                    evaluation.insertInResponseVariables(rvar_enum[i].value, rvar_enum[i].toString(), rvar_enum[i].y_axis);

                }catch(SQLException ex){
                    
                    evaluation.updateResponseVariables(rvar_enum[i].value, rvar_enum[i].toString(), rvar_enum[i].y_axis);
                    
                }
            }

        }           

        return true;
        
    }
    
}
