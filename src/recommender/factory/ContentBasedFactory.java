package recommender.factory;

import recommender.algorithm.ContentBased;
import recommender.algorithm.Recommender;
import utils.User;

/**
 *
 * @author Paulo
 * @date 10/04/2017
 */
public class ContentBasedFactory extends RecommenderFactory{
    
    @Override
    public Recommender makeRecommender(int candidates, int neighborhood, User t_user,  int rec_list_length){ 
        return new ContentBased(candidates, neighborhood, t_user, rec_list_length); 
    }
    
}
