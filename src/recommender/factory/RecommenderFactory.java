package recommender.factory;

import recommender.algorithm.Recommender;
import utils.User;

/**
 * @author Paulo
 */
public abstract class RecommenderFactory {
    
    public abstract Recommender makeRecommender(int candidates, int neighborhood, User t_user, int rec_list_length);
    
}
