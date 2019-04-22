package recommender.factory;

import recommender.algorithm.Recommender;
import utils.User;

/**
 * @author Paulo (09/18/2017)
 */
public abstract class RecommenderFactory {
    
    public abstract Recommender makeRecommender(int candidates, int neighborhood, User t_user, int rec_list_length);
    
}
