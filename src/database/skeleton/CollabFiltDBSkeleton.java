package database.skeleton;

import database.DataBase;
import static utils.Config.ITEM_ID;
import static utils.Config.ITEM_TABLE;
import static utils.Config.USER_ID;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Paulo
 */
public abstract class CollabFiltDBSkeleton extends DataBase{
    
    protected String user_id;
    protected String item_id;
    protected String item_table;
    
    protected static CollabFiltDBSkeleton instance;
    
    public CollabFiltDBSkeleton(String dbName) {
        super(dbName);
        
        user_id = USER_ID;
        item_id = ITEM_ID;
        item_table = ITEM_TABLE;
        
    }
    
    public abstract ResultSet getTopUserNeighbors(String tset, int user_id, int limit_of_users) throws SQLException;
    
    public abstract ResultSet getCandidateItems(String train_set, int user_id, int limit_of_items) throws SQLException;
    
    public abstract ResultSet getCandidateItemsFromUserHistory(int user_id, int candidates) throws SQLException;
    
    public abstract void bulkInsSim(String bulk_insert) throws SQLException;  
    
    public abstract ResultSet getTopItemNeighbors(int user_id, int item_x, int limit_of_users) throws SQLException;
    
}
