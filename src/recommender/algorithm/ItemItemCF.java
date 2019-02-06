package recommender.algorithm;

import static utils.Config.PREDICTION_LIMIT;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import utils.User;

/**
 * @author Paulo
 * @date 09/18/2017
 */
public class ItemItemCF extends CollaborativeFiltering{
    
    public ItemItemCF(int limit_of_items, int limit_of_users, User t_user,  int rec_list_length){
        super(limit_of_items, limit_of_users, t_user, rec_list_length);

    }
    

    
    @Override
    /*public LinkedHashMap<Integer, Float> score() throws SQLException{

      //  gen.setHistoryAverageRating(user); // pega os filmes do historico do target_user, em ordem aleatoria
        int user_id = user.getID();
        ResultSet iSet = gen.getHistory2FromUser(user_id);
               
        float num = 0, den = 0, pearson;
        int item_x, counter = 0;
        LinkedHashMap<Integer, Float> recommendation_list = new LinkedHashMap<>();
        String item_id_label = gen.getItemIDLabel();
        
        while (iSet.next() && counter < candidates){ // pega n itens mais similares ao historico do target_user, limitados pela quantidade de candidatos
               
            item_x = iSet.getInt(item_id_label);
            ResultSet jSet = collab.getTopItemNeighbors(user_id, item_x, neighborhood);
            
            while(jSet.next()){
                pearson = jSet.getFloat("similarity");
                num += pearson * (jSet.getFloat("rating") - jSet.getFloat("global_avg_rt"));
                den += Math.abs(pearson);
            }
            
            if(num != 0 && den!= 0){
                recommendation_list.put(item_x, num/den + iSet.getFloat("global_avg_rt"));
                counter ++;
            }
           
            num = den = 0;            
        }
        
        //System.out.println("size = "+recommendation_list.size() + " => "+recommendation_list);
        return recommendation_list; 
        
    }*/
    
    public LinkedHashMap<Integer, Float> score() throws SQLException{

        int user_id = user.getID();
        ResultSet iSet = collab.getCandidateItemsFromUserHistory(user_id, candidates);
               
        float num = 0, den = 0, pearson;
        int item_x;
        LinkedHashMap<Integer, Float> recommendation_list = new LinkedHashMap<>();
        
        while (iSet.next()){ 
               
            item_x = iSet.getInt("item_y"); // sim, é isso mesmo
            ResultSet jSet = collab.getTopItemNeighbors(user_id, item_x, neighborhood);
            
            while(jSet.next()){
                
                pearson = jSet.getFloat("similarity");
                num += pearson * (jSet.getFloat("rating") - jSet.getFloat("global_avg_rt"));
                den += Math.abs(pearson);
            }
            
            if(num != 0 && den!= 0)
                recommendation_list.put(item_x, num/den + iSet.getFloat("global_avg_rt"));
            
            num = den = 0;            
        }
        
        //System.out.println("size = "+recommendation_list.size() + " => "+recommendation_list);
        return recommendation_list; 
        
    }
    
    @Override
    public void alternativeScore(int items_qtd, LinkedHashMap<Integer, Float> recommendation_list) throws SQLException{
        
        //GenericSkeleton gen = GenericSkelFactory.getInstance();
        gen.setHistoryAverageRating(user);
        ResultSet items = gen.getNonPersonalizedScore(items_qtd, user.getID());
        
        while(items.next()){
            
            int item_id = items.getInt( gen.getItemIDLabel() );
            float semi = (user.getHistoryAverageRt() > 0) ? (items.getFloat("non_personalized_score") + user.getHistoryAverageRt()) / 2
                                                     :  items.getFloat("non_personalized_score");
            
            if(!recommendation_list.containsKey(item_id) && semi >= PREDICTION_LIMIT) // semi personalized prediction
                recommendation_list.put(item_id, semi); // insere no final da lista
 
        }
        
    }
    
    
}
