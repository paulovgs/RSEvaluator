package recommender.algorithm;

import static utils.Config.PREDICTION_LIMIT;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import utils.User;

/**
 * @author Paulo
 */
public class UserUserCF extends CollaborativeFiltering{
    
    
    public UserUserCF(int limit_of_items, int limit_of_users, User t_user,  int rec_list_length){
        super(limit_of_items, limit_of_users, t_user,  rec_list_length);

    }
    
    @Override
    public LinkedHashMap<Integer, Float> score() throws SQLException{  
                
        ResultSet uSet, ratingSet;
        String neighbors;
        int item_id, i, vec[];
        ListMultimap<Integer, Float> user_multimap = ArrayListMultimap.create();
        
        gen.setHistoryAverageRating(user);
        
        // escolha da vizinhança U (TOPk). Escolher U usuários dentro de train set mais similares a u 
        uSet = collab.getTopUserNeighbors(testSetToString(), user.getID(), neighborhood);
        
        if(!uSet.last()) // se não existe vizinhança, não é posível fazer a recomendação
                return null;
        
        int row_count = uSet.getRow(); // as vezes retorna menos que a vizinhança então foi preciso mudar pra esse valor no for
        uSet.beforeFirst(); // coloca o cursor na posição correta para começar a iteração
        
        vec = new int[neighborhood];
        
        for (i = 0; i < row_count; i++ ){
            uSet.next();
            int usr = uSet.getInt("user_y");
            vec[i] = usr;
            user_multimap.put(usr, uSet.getFloat("similarity"));
            user_multimap.put(usr, uSet.getFloat("global_avg_rt"));
        }
               
        
        // escolhendo os candidatos e pegando as ratings dadas pelos vizinhos
        neighbors = Arrays.toString(vec).replace('[', '(').replace(']', ')');
        ratingSet = collab.getCandidateItems(neighbors, user.getID(), candidates);
        
        float num = 0, den = 0, pearson;
        int user_id;
        
        LinkedHashMap<Integer, Float> recommendation_list = new LinkedHashMap<>();
        
                
        while (ratingSet.next()){

            item_id = ratingSet.getInt(gen.getItemIDLabel());
            user_id = ratingSet.getInt(gen.getUserIDLabel());
            
            Collection user_values = user_multimap.get(user_id);
            
            if(!user_values.isEmpty()){
                
                pearson = (float) user_values.toArray()[0];
                num += (ratingSet.getFloat( "rating" ) - (float)user_values.toArray()[1]) * pearson; 
                den += Math.abs(pearson);
                
            }
            
            ratingSet.next();
            
            if((!ratingSet.isAfterLast() && item_id != ratingSet.getInt(gen.getItemIDLabel())) || ratingSet.isLast()){ // testa se o proximo id é igual ao atual, se nao for é pq um id foi computado completamente
                // finaliza o score, armazena, e zera
                recommendation_list.put(item_id, user.getHistoryAverageRt() + ((den != 0) ? num/den : 0));
                num = den = 0;
                
            }else{
                ratingSet.previous();
            }
                
         }
        
        return recommendation_list;
                
    }
    
    @Override
    public void alternativeScore(int items_qtd, LinkedHashMap<Integer, Float> recommendation_list) throws SQLException{
        
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
