package recommender.factory;

import recommender.algorithm.Recommender;
import recommender.algorithm.UserUserCF;
import utils.User;

/**
 * @author Paulo
 */
public class UserUserCFFactory extends RecommenderFactory{
    
    @Override
    public Recommender makeRecommender(int candidates, int neighborhood, User t_user, int rec_list_length){ 
        return new UserUserCF(candidates, neighborhood, t_user, rec_list_length); 
    }

}
