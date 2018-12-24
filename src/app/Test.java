package app;

import com.google.common.collect.Table;
import database.skeleton.ContentBasedDBSkeleton;
import database.factory.ContentBasedDBSkeletonFactory;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Utils;

public class Test {
        
    public static void main(String[] args) {
        
       
    }
    
    public static void auxinterv(float media, float ampic){
        
        float a = media + (ampic*media/200);
        float b = media - (ampic*media/200);
        System.out.println(b + "; " + a);

        
    }
    // retira 10 ratings aleatoriamente para fazer parte do test set, se a pessoa tem mais de 20 e menos de 30, retira quantos puder
    // assim cada user tem no maximo 10 ratings de teste set e na maioria das vezes pelo menos 20 no historico
    public static void createTestSet(){
        
        try {
            GenericSkeleton gen = GenericSkelFactory.getInstance();
            Table<Integer, Integer, Float> ratings = gen.getAllRatings();
            ResultSet users = gen.getAllUsers();
            
            int counter = 0;
            
            while(users.next()){
                
                int user_id = users.getInt( gen.getUserIDLabel() );
                Map<Integer, Float> user_ratings = ratings.row(user_id);
                
                int count = user_ratings.size();
                int qtd = 0;
                
                if((count - 20) > 10) // seta 10 aleatorias como teste
                    qtd = 10;
                else if(count > 20) // retira count - 20 aleatorias como teste
                    qtd = count - 20;
                
                Random generator = new Random();
                
                
                for(int i = 0; i < qtd; i++){
                    
                    Object[] keys = user_ratings.keySet().toArray();
                    Object item_id = keys[generator.nextInt(keys.length)];
                    gen.updateIsHistory(user_id, (int)item_id, false);
                    user_ratings.remove((int)item_id);
                    
                }
                
                Utils.printIf(++counter, " Qtd of users processed: ", 50);
                
            }
            
            
            
        } catch (SQLException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    // scale in a 0 - 1 range
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
    
    // pode ser unida com a query acima
    // precisa ser otimizada
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
