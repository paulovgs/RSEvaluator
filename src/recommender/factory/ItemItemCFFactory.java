package recommender.factory;

import recommender.algorithm.ItemItemCF;
import recommender.algorithm.Recommender;
import utils.User;

/**
 * @author Paulo (09/18/2017)
 */
public class ItemItemCFFactory extends RecommenderFactory{
    
    @Override
    public Recommender makeRecommender(int candidates, int neighborhood, User t_user, int rec_list_length){ 
        return new ItemItemCF(candidates, neighborhood, t_user, rec_list_length); 
    }

}
