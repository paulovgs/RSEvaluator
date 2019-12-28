package database;

import database.skeleton.CollabFiltDBSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Paulo 
 */
public class CollaborativeFiltDB extends CollabFiltDBSkeleton{
    
    public CollaborativeFiltDB(String dbName) {
        super(dbName);
    }
    
    public static CollaborativeFiltDB getInstance(String dbName){
        
        if(instance == null)
            instance = new CollaborativeFiltDB(dbName);
        
        if(instance.dbCon == null)
            instance.dbCon = instance.open();
        
        return (CollaborativeFiltDB) instance;
    }
    
    @Override
    public ResultSet getTopUserNeighbors(String tset, int user_id, int neighborhood) throws SQLException{
        
        Statement stm2 = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        
        String wh2 = (tset != null) ? " and user_y not in " + tset : ""; // exclui valores de test set
        
        return stm2.executeQuery("select user_y, similarity, users.global_avg_rt from user_similarity" +
                                 " join users on users."+this.user_id+" = user_similarity.user_y" +
                                 " where user_x = " + user_id + wh2 +
                                 " order by similarity desc limit " + neighborhood
        );
                
    }
       
    @Override
    public ResultSet getCandidateItems(String neighbors, int user_id, int candidates) throws SQLException{
        
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                
        return st.executeQuery(
            "select "+this.item_id+", "+this.user_id+", rating from ratings where "+this.item_id+" in " +
                "(select "+this.item_table+"."+this.item_id+" from "+this.item_table+" inner join ratings on "+this.item_table+"."+this.item_id+" = ratings."+this.item_id+" "
                   + "where "+this.user_id+" in " + neighbors + " and "+this.item_table+"."+this.item_id+" not in "
                       + "(select "+this.item_id+" from ratings where "+this.user_id+" = " + user_id + " and is_history = true) "
                       + "order by random() limit " + candidates + ") " +
              "and "+this.user_id+" in " + neighbors + " order by "+this.item_id+", "+this.user_id+""
        );
        
    }
    
    @Override
    public ResultSet getCandidateItemsFromUserHistory(int user_id, int candidates) throws SQLException{
        
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        
        return st.executeQuery("select item_y, global_avg_rt from item_similarity_new "
                + "join "+this.item_table+" as it on it."+this.item_id+" = item_similarity_new.item_y "
                + "where item_x in " +
                "(select "+this.item_id+" from ratings where "+this.user_id+" = "+user_id+" and is_history = true) " +
                "order by similarity desc limit "+candidates
        );
        
    }
    
    @Override
    public void bulkInsSim(String bulk_insert) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate(bulk_insert);
                
    }
    
    @Override
    public ResultSet getTopItemNeighbors(int user_id, int item_x, int neighborhood) throws SQLException{

        Statement st = dbCon.createStatement();
        return st.executeQuery("select item_y, similarity, "+item_table+".global_avg_rt, r.rating from item_similarity_new" +
                                " join "+item_table+" on "+item_table+"."+this.item_id+" = item_similarity_new.item_y" +
                                " join ratings as r on r."+this.item_id+" = item_similarity_new.item_y" +
                                " and r."+this.user_id+" = " + user_id +
                                " where item_x = " + item_x +
                                " order by similarity desc limit " + neighborhood
        );

    }
    


    
}
