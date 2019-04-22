package recommender.algorithm;

import static utils.Config.PREDICTION_LIMIT;
import database.skeleton.CollabFiltDBSkeleton;
import database.skeleton.ContentBasedDBSkeleton;
import database.skeleton.GenericSkeleton;
import evaluator.Benchmarker;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import utils.User;
import utils.Utils;

/**
 * @author Paulo (09/18/2017)
 */
public abstract class Recommender {
    
    public static final int NO_RECOMMENDATION = -1; // deve ser um número que não está presente em movie_ids e negavito, ou então muito grande. É usado no rmserror tb
    protected int candidates;
    protected int neighborhood;
    protected int rec_list_length;
    protected float score_exec_time;
    protected User user;
    protected boolean alternative_score;
    
    public static GenericSkeleton gen;
    public static CollabFiltDBSkeleton collab;
    public static ContentBasedDBSkeleton cbased;
    
    
    public Recommender(int candidates, int neighborhood, User target_user, int rec_list_length){
        
        this.candidates = candidates;
        this.neighborhood = neighborhood;
        this.user = target_user;
        this.rec_list_length = rec_list_length;
        this.score_exec_time = 0;
        this.alternative_score = false;
        
    }
    
    public static void setGenDB(GenericSkeleton generic){gen = generic;}
    public static void setCollabDB(CollabFiltDBSkeleton collaborative){collab = collaborative;}
    public static void setContentBasedDB(ContentBasedDBSkeleton contentBased){cbased = contentBased;}    
       
    public abstract LinkedHashMap<Integer, Float> score() throws SQLException;
    
    public void setAlternativeScore(String alt){ alternative_score = (alt.equals("true")); }
    
    public abstract void alternativeScore(int items_qtd, LinkedHashMap<Integer, Float> recommendation_list) throws SQLException;
    
    public void setUser(User user){
        this.user = user;
    }
    
    public Map<Integer, Float> recommend() throws SQLException{
        
        if(user == null)
                return null;
        
        LinkedHashMap<Integer, Float> recommendation_list = score();
        
        if(recommendation_list == null)
            recommendation_list = new LinkedHashMap<>();
        
        recommendation_list = Utils.sortByValue(recommendation_list, false);
        recommendation_list = Utils.pruneMap(recommendation_list, rec_list_length, PREDICTION_LIMIT);
        
        if(alternative_score == true){
            int size = recommendation_list.size();
            if( size < rec_list_length) // recomendação alternativa
                alternativeScore(rec_list_length - size, recommendation_list);
            
        }
        
        return (recommendation_list.isEmpty()) ? null : recommendation_list;
        
    }
            
    public String testSetToString(){ // transforma o test set atual em string no formato sql
        
        Benchmarker bcmarker = Benchmarker.getInstance();
        String tset = bcmarker.getTestSet().toString();
        // se estiver vazia vai ter uma string da forma []. Nesse caso retorna a string vazia ""
        return (tset.length() > 2) ? tset.replace('[', '(').replace(']', ')') : null;
        
    }
    
   
    
    
               
}
