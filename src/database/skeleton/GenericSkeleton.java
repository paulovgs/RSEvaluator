package database.skeleton;

import static utils.Config.ITEM_ID;
import static utils.Config.ITEM_TABLE;
import static utils.Config.USER_ID;
import com.google.common.collect.Table;
import database.DataBase;
import java.sql.ResultSet;
import java.sql.SQLException;
import utils.User;

/**
 * @author Paulo (06/02/2018)
 */
public abstract class GenericSkeleton extends DataBase{
    
    protected String user_id;
    protected String item_id;
    protected String item_table;

    protected static GenericSkeleton instance;
    
    public GenericSkeleton(String dbName){
        
        super(dbName);
        
        user_id = USER_ID;
        item_id = ITEM_ID;
        item_table = ITEM_TABLE;
        
    }
    
    // for test purposes
    public abstract void executeUpdate(String query) throws SQLException;
    public abstract ResultSet executeQuery(String query) throws SQLException;
  //  public abstract void deleteItemSimilarityPersonalized(int item_x) throws SQLException;
    

    public String getUserIDLabel(){return user_id;}
    
    public String getItemIDLabel(){return item_id;}
    
    public String getItemTableLabel(){return item_table;}   
    
    public abstract ResultSet getAllUsersRandom() throws SQLException;
    
    public abstract ResultSet getAllUserIDs() throws SQLException;
    
    public abstract int getTotalNOfUsers() throws SQLException;
    
    public abstract void updateHistoryAverage(int user_id) throws SQLException;
    
    public abstract ResultSet getUserTestSet(int user_id) throws SQLException;
    
    public abstract ResultSet getUsersFromRTNotInUsers() throws SQLException;
    
    public abstract void insertInUsers(int user_id) throws SQLException;
    
    public abstract ResultSet getAverageRating(String avg_type) throws SQLException;
    
    public abstract void updateAverageRating(String table, String avg_type, float avg, int id) throws SQLException;
    
    public abstract void setHistoryAverageRating(User user) throws SQLException;
    
    public abstract ResultSet getItemPopularity() throws SQLException;
    
    public abstract ResultSet getAllItems() throws SQLException;
    
    public abstract Table<Integer, Integer, Float> getAllRatings() throws SQLException;
    
    public abstract ResultSet getAllUsers() throws SQLException;
    
    public abstract ResultSet getHistoryFromUser(int user_id) throws SQLException;
    
  //  public abstract ResultSet getHistory2FromUser(int user_id) throws SQLException;
    
    public abstract int getTotalNOfItems() throws SQLException;
    
    public abstract float getOverallAverage() throws SQLException;
    
    public abstract void insertRating(int user_id, int item_id, float rating) throws SQLException;
    
    public abstract void splitRatings75(int user_id) throws SQLException;
    
    public abstract void resetHistory() throws SQLException;
    
    public abstract void updateIsHistory(int user_id, int item_id, boolean is_history) throws SQLException;
    
    public abstract ResultSet getNonPersonalizedScore(int items_qtd,  int user_id) throws SQLException;
    
    public abstract void setNonPersonalizedScore(float score, int item_id) throws SQLException;
    
    public abstract ResultSet getAllFromTable(String table) throws SQLException;
       
    
}
