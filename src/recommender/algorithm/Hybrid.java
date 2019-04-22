package recommender.algorithm;

import static utils.Config.PREDICTION_LIMIT;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import utils.User;

/**
 * @author Paulo (04/16/2018)
 */
public class Hybrid extends Recommender{
    
    public Hybrid(int limit_of_items, int limit_of_users, User t_user,  int rec_list_length){
        super(limit_of_items, limit_of_users, t_user, rec_list_length);
    }
    
    @Override
    public LinkedHashMap<Integer, Float> score() throws SQLException{
        
        LinkedHashMap<Integer, Float> rec_list = new LinkedHashMap<>();
        Map<Integer, Float> l1,l2;
        
        UserUserCF user_cf = new UserUserCF(candidates, neighborhood, user, rec_list_length);
        l1 = user_cf.score();
        
        ItemItemCF item_cf = new ItemItemCF(candidates, neighborhood, user, rec_list_length);
        l2 = item_cf.score();
        
        if(l1 != null)
                rec_list.putAll(l1);
        
        if(l2 != null)
                rec_list.putAll(l2);
        
        return rec_list;
                
    }

    @Override
    public void alternativeScore(int items_qtd, LinkedHashMap<Integer, Float> recommendation_list) throws SQLException{
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
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
