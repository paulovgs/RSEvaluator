package utils;

import database.factory.ContentBasedDBFactory;
import database.skeleton.ContentBasedDBSkeleton;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @date 03/12/2018
 * @author Paulo
 */
public class User {
    
    private final int id;
    private double arrival_time;
    private float history_average_rt;
    
    public User(int id, double arrival_time){
        this.id = id;
        this.arrival_time = arrival_time;
    }
    
    public User(int id){
        this.id = id;
    }
    
    public int getID(){return id;}
    
    public double getArrivalTime(){ return arrival_time; }
    
    public void setArrivalTime(double time){ arrival_time = time; }
    
    public void setHistoryAverageRt(float average_test){ this.history_average_rt = average_test; }
    
    public float getHistoryAverageRt(){ return history_average_rt; }
    
    /**
     * @return 
     * @throws java.sql.SQLException 
     * @todo bulk_insert
     * @todo logger
     * @desc Calcula a média de avaliações de todos os usuários (com 75% das ratings mais antigas = histórico) e armazena no banco
     */
    public static boolean computeHistoryAverageRating() throws SQLException{
        
        int count = 0;    
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet users = gen.getAllUserIDs();

        while(users.next()){
            
            int user_id = users.getInt( gen.getUserIDLabel() );
            
            gen.updateHistoryAverage(user_id);

            Utils.printIf(++count, "Qtd of users updated:", 100);
            
        }

        return true;
        
    }  
    
        /**
     * @param user_or_item_avg
     * @return 
     * @throws java.sql.SQLException 
     * @todo bulk_insert
     * @todo logger
     * @desc Calcula a média de avaliações de todos os itens ou usuários (com 100% das ratings)e armazena no banco
     * @user_or_movie_avg true for user, false for movie
     */
    public static boolean computeAverageRating(boolean user_or_item_avg) throws SQLException{
        
        int count = 0;
        String avg_type, table;
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        
        avg_type = (user_or_item_avg) ? gen.getUserIDLabel() : gen.getItemIDLabel();
        table = (user_or_item_avg) ? "users": gen.getItemTableLabel();
        
        ResultSet avgSet = gen.getAverageRating(avg_type);

        while(avgSet.next()){

            gen.updateAverageRating(table, avg_type, avgSet.getFloat( "avg" ), avgSet.getInt(avg_type));

            Utils.printIf(++count, "Qtd of entries updated:", 1000);
            
        }

        return true;
        
    } 
    
    /**
     * @throws java.sql.SQLException
     * @todo bulk_insert
     * @desc Essa etapa faz parte do setup do sistema. A função faz o insert, caso necessário, de todos os usuários presentes na tabela de ratings
     * @return 
     */
    public static boolean insertUsers() throws SQLException{
        
        int count = 0;
       
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet userSet = gen.getUsersFromRTNotInUsers();

        while(userSet.next()){
            
            gen.insertInUsers(userSet.getInt( gen.getUserIDLabel() ));

            Utils.printIf(++count, "Qtd of users inserted:", 1000);

        }
        
        return true;
    
    }
    
    
    
    public static boolean insertUsersFromTags() throws SQLException{

        int count = 0;
        
        ContentBasedDBSkeleton cbased = ContentBasedDBFactory.getInstance();
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet userSet = cbased.getUsersFromTagsNotInUsers();
        
        while(userSet.next()){
            
            gen.insertInUsers(userSet.getInt( gen.getUserIDLabel() ));
  
            Utils.printIf(++count, "Qtd of users inserted:", 100);

        }

        return true;

    }
    
    public static void splitRatings75(){
        
        try {
            
            GenericSkeleton gen = GenericSkelFactory.getInstance();
            ResultSet users = gen.getAllUsers();
            gen.resetHistory();
            int counter = 0;
            
            System.out.println("inicio");

            while(users.next()){
                gen.splitRatings75(users.getInt(gen.getUserIDLabel()));
                Utils.printIf(++counter, "Users processed:", 10);
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    


        
    
}
