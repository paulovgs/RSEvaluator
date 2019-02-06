package database;

import database.skeleton.GenericSkeleton;
import static utils.Config.NON_PER_LIMIT;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.User;

/**
 * @author Paulo
 * @date 06/02/2018
 */
public class Generic extends GenericSkeleton{

    public Generic(String dbName) {
        super(dbName);
    }
    
    public static Generic getInstance(String dbName){
        
        if(instance == null)
            instance = new Generic(dbName);
        
        if(instance.dbCon == null)
            instance.dbCon = instance.open();
        
        return (Generic) instance;
    }
    
    // for test purposes
    @Override
    public void executeUpdate(String query) throws SQLException{
        
        Statement stm = dbCon.createStatement();
        stm.executeUpdate(query);
        
    }
    
    /*@Override
    public void deleteItemSimilarityPersonalized(int item_x) throws SQLException{
        Statement stm = dbCon.createStatement();
        stm.executeQuery("delete from item_similarity where item_x = "+item_x+" and item_y not in "
                        + "(select item_y from item_similarity where item_x = "+item_x+" order by similarity desc limit 250)" );
    }*/
    
    @Override
    public ResultSet executeQuery(String query) throws SQLException{
        Statement stm = dbCon.createStatement();
        return stm.executeQuery(query);
    }
    
    @Override
    public ResultSet getAllUsersRandom() throws SQLException {
        
        Statement st = dbCon.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery("select * from users order by random()");
        
    }
    
    @Override
    public ResultSet getAllUsers() throws SQLException{
        
        Statement st = dbCon.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery("select * from users order by user_id");
        
    }
    
    @Override // getAllUserIDsFromUsers
    public ResultSet getAllUserIDs() throws SQLException{
        
        Statement stm = dbCon.createStatement();
        return stm.executeQuery("select "+user_id+" from users");
        
    }

    @Override
    public int getTotalNOfUsers() throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet totalSet = st.executeQuery("select count("+user_id+") from users;");
        totalSet.next();
        return totalSet.getInt("count");
        
    }
    
    @Override
    public void updateHistoryAverage(int user_id) throws SQLException{
        
        Statement stm = dbCon.createStatement();
        
        stm.executeUpdate("update users set history_avg_rt = " +
                "(select sum(rating)/count(rating) from ratings where "+this.user_id+" = "+user_id+" and is_history = true)" +
                "where "+this.user_id+" = "+user_id
        );
        
    }

    @Override // getUserRatingsTestSet
    public ResultSet getUserTestSet(int user_id) throws SQLException{
    
        Statement st = dbCon.createStatement();
        return st.executeQuery(
            "select "+item_id+", rating from ratings where "+this.user_id+" = " + user_id+ " and is_history = false "
        );
       

    }
    
    @Override
    public ResultSet getUsersFromRTNotInUsers() throws SQLException{
    
        Statement st = dbCon.createStatement();
        ResultSet userSet = st.executeQuery("select distinct "+user_id+" from ratings where "+user_id+" not in "
                                                + "(select "+user_id+" from users) order by "+user_id+"");
        return userSet;
        
    }
    
    @Override
    public void insertInUsers(int user_id) throws SQLException{

        Statement st = dbCon.createStatement();
        st.executeUpdate("insert into users ("+this.user_id+") values (" + user_id + ")");
        
    }

    @Override
    public ResultSet getAverageRating(String avg_type) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select " + avg_type + ", sum(rating)/count(rating) as avg from ratings group by " + avg_type + " order by " + avg_type);
        
    }
    
    @Override // update the average rating from users or from items
    public void updateAverageRating(String table, String avg_type, float avg, int id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update " + table + " set global_avg_rt = " + avg + " where " + avg_type + " = " + id + ";");
        
    }
    
    @Override
    public void setHistoryAverageRating(User user) throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet uSet = st.executeQuery("select history_avg_rt from users where "+this.user_id+" = " + user.getID());
        uSet.next();       
        user.setHistoryAverageRt(uSet.getFloat("history_avg_rt"));
        
    }

    @Override
    public ResultSet getItemPopularity() throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select "+item_id+", count(rating) from ratings group by "+item_id);
            
    }
    
    @Override // getAllMovies NAO TINHA ORDER BY MOVIE ID*****
    public ResultSet getAllItems() throws SQLException{
        
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet items = st.executeQuery("select * from " + item_table + " order by "+item_id);
        return items;
        
    }
    
    @Override
    public Table<Integer, Integer, Float> getAllRatings() throws SQLException{
        
        Table<Integer, Integer, Float> ratings = HashBasedTable.create();
        
        Statement st = dbCon.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet ratingSet = st.executeQuery("select "+user_id+", "+item_id+", rating from ratings");
        
        while (ratingSet.next())
            ratings.put(ratingSet.getInt(user_id), ratingSet.getInt(item_id), ratingSet.getFloat("rating"));
        
        return ratings;

    }
    
    // maybe esse e getLimitedH podem ser um só
    @Override
    public ResultSet getHistoryFromUser(int user_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select "+item_id+", rating from ratings where "+this.user_id+" = " + user_id + 
                               " and is_history = true" 
        );
        
    }
    
   /* @Override
    public ResultSet getHistory2FromUser(int user_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
               
        return st.executeQuery(
                "select "+item_id+", global_avg_rt from "+item_table+" where "+item_id+" in"
                + " (select "+item_id+" from ratings where "+this.user_id+" = " +user_id+ " and is_history = true)"
                + " order by random()"
        );
        
    }*/
    
    @Override
    public int getTotalNOfItems() throws SQLException{
        
        Statement st = dbCon.createStatement();
        
        ResultSet totalSet = st.executeQuery("select count("+item_id+") from "+item_table);
        totalSet.next();
        return totalSet.getInt("count");
        
    }
    
    @Override
    public float getOverallAverage()throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet ovr = st.executeQuery("select sum(rating)/count(*) as overall from ratings");
        ovr.next();
        return ovr.getFloat("overall");
        
    }
    
    @Override
    public void insertRating(int user_id, int item_id, float rating) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate(
                "insert into ratings("+this.user_id+", "+item_id+", rating)" +
                " values ("+user_id+", "+item_id+", "+rating+")"
        );
        
    }
    
    @Override
    // 25% fica como test set; 75% como histórico
    public void splitRatings75(int user_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update ratings set is_history = false where user_id = "+user_id+" and movie_id in"
            + " (select movie_id from ratings where user_id = "+user_id+" order by timestamp desc limit "
            + " (select round(count(*)*0.25) as counter from ratings where user_id = "+user_id+"))"
        );
        
    }
    
    // seta o historico de todo mundo para true
    @Override
    public void resetHistory() throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update ratings set is_history = true");      
        
    }
    
    
    
    @Override //updateTestSetFromRatings
    public void updateIsHistory(int user_id, int item_id, boolean is_history) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update ratings set is_history = "+is_history+" where "+this.user_id+" = "+user_id+
                         " and "+this.item_id+" = "+item_id);
        
    }
    
    // pega items_qtd dos top non_per_limit ordenados pelo score nao personalizado e excluindo o historico do usuario
    @Override
    public ResultSet getNonPersonalizedScore(int items_qtd, int user_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select "+this.item_id+", non_personalized_score from "+item_table+" where "+this.item_id+" in "
                + "(select "+this.item_id+" from "+item_table+" order by non_personalized_score desc limit " +NON_PER_LIMIT+ ") "
                + "and "+this.item_id+" not in "
                    + "(select "+this.item_id+" from ratings where "+this.user_id+" = "+user_id+" and is_history = true) "
                + "order by random() limit " +items_qtd);
        
    }
    
    @Override // setOverallAverage
    public void setNonPersonalizedScore(float score, int item_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update "+item_table+" set non_personalized_score = " + score + "where "+this.item_id+" = " + item_id);
        
    }
    
   @Override
   public ResultSet getAllFromTable(String table) throws SQLException {
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select * from " + table );
      
    }
   
}
