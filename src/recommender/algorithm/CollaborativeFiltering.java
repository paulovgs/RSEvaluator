package recommender.algorithm;

import static utils.Config.STORAGED_CORRELATION;
import com.google.common.collect.Table;
import database.skeleton.CollabFiltDBSkeleton;
import database.factory.CollabFiltDBSkeletonFactory;
import database.factory.ContentBasedDBSkeletonFactory;
import database.factory.GenericSkelFactory;
import database.skeleton.ContentBasedDBSkeleton;
import database.skeleton.GenericSkeleton;
import static java.lang.Math.sqrt;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import utils.FileLogger;
import utils.RSELogger;
import utils.User;
import utils.Utils;

/**
 * @author Paulo
 * todo: adaptação no CF para funcionar também com dados unários.Ver Curso 2 Semana 3 Cursera
 * todo: ver a possibilidade de adicionar explicações nos RS. (porque vc comprou...) Ver Curso 2 Semana 4 Cursera
 */
public abstract class CollaborativeFiltering extends Recommender{
    
    public CollaborativeFiltering(int limit_of_items, int limit_of_users, User t_user,  int rec_list_length){
        super(limit_of_items, limit_of_users, t_user,  rec_list_length);
    }
    
    /**
     * Calcula a similaridade entre todo par user-user e armazena no banco de dados
     * @throws java.sql.SQLException
     * @return
     */
    public static boolean UserUserPearsonSimilarity() throws SQLException{
        
        int user_u, user_v, index = 1, idx = 0, count = 0;
        float avg_u, avg_v;
        String bulk_insert;
                
        Table<Integer, Integer, Float> ratings;
        Map<Integer, Float> ratings_u, ratings_v;
        
        RSELogger logger = new FileLogger("User User Pearson Similarity.log");
  
        CollabFiltDBSkeleton collab = CollabFiltDBSkeletonFactory.getInstance();
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        
        ResultSet usrSet = gen.getAllUsers();
        ratings = gen.getAllRatings();
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss"); 
        Date firstTime = Calendar.getInstance().getTime();
        String tm = sdf.format(firstTime);     
        
        logger.writeEntry("... " + tm);
        System.out.println("... " + tm);

        while(usrSet.next()){
            
            user_u = usrSet.getInt( gen.getUserIDLabel() );
            avg_u = usrSet.getFloat("history_avg_rt");
                        
                //ratings_u = ratings.row(user_u); // todas as ratings dadas pelo user_u
                ResultSet ru = gen.getHistoryFromUser(user_u); 

                ratings_u = new HashMap<>();
                while(ru.next())
                    ratings_u.put(ru.getInt( gen.getItemIDLabel() ), ru.getFloat("rating")); 

                bulk_insert = "INSERT INTO user_similarity(user_x, user_y, similarity) VALUES ";

                usrSet.beforeFirst();
                int cfiller = 0; 
                LinkedHashMap<Integer, Float> k_most_similars = new LinkedHashMap<>();

                while(usrSet.next()){

                    user_v = usrSet.getInt(gen.getUserIDLabel());

                    if(/*user_u >= 20000 &&*/ user_u != user_v){

                        avg_v = usrSet.getFloat("global_avg_rt");
                        ratings_v = ratings.row(user_v); // todas as ratings dadas pelo user_v

                        float sum_num = 0, sum_den1 = 0, sum_den2 = 0;

                        for(Integer movie_id : ratings_u.keySet()){

                            // numerador = soma-se apenas os itens que ambos avaliaram
                            if(ratings_u.get(movie_id) != null && ratings_v.get(movie_id) != null)
                                sum_num += ( (ratings_u.get(movie_id) - avg_u) * (ratings_v.get(movie_id) - avg_v));

                            // denominador parte 1: soma-se todos os itens que user u avaliou
                            double c = ratings_u.get(movie_id) - avg_u;
                            sum_den1 += (c*c);

                        }

                        // denominador parte 2: soma-se todos os itens que user v avaliou
                        for(Integer movie_id : ratings_v.keySet()){
                            double d = ratings_v.get(movie_id) - avg_v;
                            sum_den2 += (d*d);
                        }

                        Float pearson_correlation = (sum_den2 == 0 || sum_den1 == 0) ? 0 : (float) (sum_num / ( sqrt(sum_den1) * sqrt(sum_den2) ));

                     //   if(pearson_correlation > 0){ // limitando para apenas correlações positivas serem salvas

                            if(cfiller < STORAGED_CORRELATION){
                                k_most_similars.put(user_v, pearson_correlation);
                                cfiller++;
                            }else{
                                k_most_similars = Utils.sortByValue(k_most_similars, true);
                                // se pearson é maior do que o menor valor de k most similars
                                if(pearson_correlation > k_most_similars.entrySet().iterator().next().getValue()){ 
                                    int key = k_most_similars.entrySet().iterator().next().getKey();
                                    k_most_similars.remove(key);
                                    k_most_similars.put(user_v, pearson_correlation);
                                }
                            }
                       // }

                    }

                    index++;

               }

               for(Integer key : k_most_similars.keySet() )
                    bulk_insert += " (" + user_u + "," + key + "," + k_most_similars.get(key) + "),";

               bulk_insert = bulk_insert.substring(0, bulk_insert.length() - 1); // retira a virgula do final
               bulk_insert += ";";

               if(!usrSet.isLast() && !k_most_similars.isEmpty())
                        collab.bulkInsSim(bulk_insert);

               idx++;
               usrSet.relative(-index + idx);
               index = 1;
               count++;


               if(count % 100 == 0){
                    Date time = Calendar.getInstance().getTime();
                    String data = sdf.format(time);
                    System.out.println("Similarity of user_x " + user_u + " completed: " + data);
                    logger.writeEntry("Similarity of user_x " + user_u + " completed: " + data);               
                }
           

        }
        
        return true;
        
    }
    
    
    /**
     * Calcula a similaridade entre todo par item-item e armazena no banco de dados
     * @return 
     * @throws java.sql.SQLException 
     */
    public static boolean ItemItemPearsonSimilarity() throws SQLException{
        
        int item_x, item_y, index = 1, count = 0, idx = 0;
        float avg_i, avg_j;
        String bulk_insert;
                
        Table<Integer, Integer, Float> ratings;
        Map<Integer, Float> ratings_i, ratings_j;
        
        FileLogger logger = new FileLogger("Item Item Pearson Similarity.log");
        
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        CollabFiltDBSkeleton collab = CollabFiltDBSkeletonFactory.getInstance();
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ratings = gen.getAllRatings();
        ResultSet itemSet = gen.getAllItems();

        Date firstTime = Calendar.getInstance().getTime();
        String tm = sdf.format(firstTime);          
        logger.writeEntry("... " + tm);
        System.out.println("... " + tm);

        while(itemSet.next()){

            item_x = itemSet.getInt(gen.getItemIDLabel());
            avg_i = itemSet.getFloat("global_avg_rt");
            ratings_i = ratings.row(item_x); // todas as ratings dadas ao item i
            bulk_insert = "INSERT INTO item_similarity(item_x, item_y, similarity) VALUES ";
            
            itemSet.beforeFirst();
            int cfiller = 0;
            //Map<Integer, Float> k_most_similars = new HashMap<>();
            LinkedHashMap<Integer, Float> k_most_similars = new LinkedHashMap<>();

            while(itemSet.next()){
                
                item_y = itemSet.getInt(gen.getItemIDLabel());
                
                if(item_x != item_y){
                
                    avg_j = itemSet.getFloat("global_avg_rt");
                    ratings_j = ratings.row(item_y); // todas as ratings dadas ao item j
                    float sum_num = 0, sum_den1 = 0, sum_den2 = 0;
                    for(Integer user_id : ratings_i.keySet()){
                        // numerador = soma-se apenas se o usuário avaliou ambos os itens
                        if(ratings_i.get(user_id) != null && ratings_j.get(user_id) != null)
                            sum_num += ( (ratings_i.get(user_id) - avg_i) * (ratings_j.get(user_id) - avg_j));
                        // denominador parte 1: soma-se todas as ratings, mesmo que não sejam em comum
                        double c = ratings_i.get(user_id) - avg_i;
                        sum_den1 += (c*c);
                    }

                    // denominador parte 2: soma-se todos os itens que user v avaliou
                    for(Integer user_id : ratings_j.keySet()){
                        double d = ratings_j.get(user_id) - avg_j;
                        sum_den2 += (d*d);
                    }

                    Float pearson_correlation = (sum_den2 == 0 || sum_den1 == 0) ? 0 : (float) (sum_num / ( sqrt(sum_den1) * sqrt(sum_den2) ));
                    
                 //   if(pearson_correlation > 0){ // limitando para apenas correlações positivas serem salvas
                        
                        if(cfiller < STORAGED_CORRELATION){
                            k_most_similars.put(item_y, pearson_correlation);
                            cfiller++;
                        }else{
                            k_most_similars = Utils.sortByValue(k_most_similars, true);
                            // se pearson é maior do que o menor valor de k most similars
                            if(pearson_correlation > k_most_similars.entrySet().iterator().next().getValue()){ 
                                int key = k_most_similars.entrySet().iterator().next().getKey();
                                k_most_similars.remove(key);
                                k_most_similars.put(item_y, pearson_correlation);
                            }
                        }
                  //  }
                    
                }
                
                index++;

           }
            
           for(Integer key : k_most_similars.keySet() )
                bulk_insert += " (" + item_x + "," + key + "," + k_most_similars.get(key) + "),";
           
           bulk_insert = bulk_insert.substring(0, bulk_insert.length() - 1); // retira a virgula do final
           bulk_insert += ";";
           
           if(!itemSet.isLast() && !k_most_similars.isEmpty())
                collab.bulkInsSim(bulk_insert);
           
           idx++;
           itemSet.relative(-index + idx);
           index = 1;
           count++;

            if(count % 25 == 0){
                Date time = Calendar.getInstance().getTime();
                String data = sdf.format(time);
                System.out.println("Similarity of item_x " + item_x + " completed: " + data);
                logger.writeEntry("Similarity of item_x " + item_x + " completed: " + data);
            }

        }
        
        return true;
    }
    
    /**
     * Scale in a 0 - 1 range
     * @throws SQLException 
     */
    public static void normalizeItemVectorSpace()throws SQLException{
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet itens = gen.getAllItems();
        
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        
        int counter = 0;
        float scale_factor = cbased.getMaxValueFromItemVectorSpace2();
        System.out.println(scale_factor);
        
        while(itens.next()){
            
            int item_id = itens.getInt( gen.getItemIDLabel() );
            
            ResultSet item_vector = cbased.getItemVectorSpace(item_id);
            
            item_vector.beforeFirst();
                 
            if(item_vector.next()){ // at least one entry to insert
                
                item_vector.beforeFirst();

                //normaliza e salva 
                while(item_vector.next()){
                    cbased.updateItemVector(item_id, item_vector.getInt("tag_id"), item_vector.getFloat("relevance")/scale_factor);
                }

            }
            
           
           counter++;
           if(counter % 5000 == 0)
                System.out.println(counter + " items were normalized.");
            
        }
    }
    
    // pode ser unida com a query acima; precisa ser otimizada
    public static void normalizeUserVectorSpace()throws SQLException{
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet users = gen.getAllUsers();
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        
        int counter = 0;
        float scale_factor = cbased.getMaxValueFromUserVectorSpace();
        System.out.println(scale_factor);
        
        while(users.next()){
            
            int user_id = users.getInt( gen.getUserIDLabel() );
            
            ResultSet user_vector = cbased.getUserVector(user_id);
            
            user_vector.beforeFirst();
                 
            if(user_vector.next()){ // at least one entry to insert
                
                user_vector.beforeFirst();

                //normaliza e salva 
                while(user_vector.next()){
                    cbased.updateUserVector(user_id, user_vector.getInt("tag_id"), user_vector.getFloat("relevance")/scale_factor);
                }

            }
            
           
           counter++;
           if(counter % 50 == 0)
                System.out.println(counter + " users were normalized.");
            
        }
    }
    
    
}
