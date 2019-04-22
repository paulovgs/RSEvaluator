package recommender.factory;

import recommender.algorithm.Hybrid;
import recommender.algorithm.Recommender;
import utils.User;

/**
 *
 * @author Paulo (04/16/2018)
 */
public class HybridFactory extends RecommenderFactory{
    
    @Override
    public Recommender makeRecommender(int candidates, int neighborhood, User t_user,  int rec_list_length){ 
        return new Hybrid(candidates, neighborhood, t_user, rec_list_length); 
    }


    
}
