package database.skeleton;

import database.DataBase;
import static utils.Config.ITEM_ID;
import static utils.Config.ITEM_TABLE;
import static utils.Config.USER_ID;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Paulo
 * @date 06/02/2018
 */
public abstract class ContentBasedDBSkeleton extends DataBase{
    
    protected String user_id;
    protected String item_id;
    protected String item_table;
    
    protected static ContentBasedDBSkeleton instance;
    
    public ContentBasedDBSkeleton(String dbName) {
        super(dbName);
        user_id = USER_ID;
        item_id = ITEM_ID;
        item_table = ITEM_TABLE;
    }
         
    public abstract ResultSet getCBCandidatesWithVector(int user_id, int limit_of_items) throws SQLException;
    
    public abstract ResultSet getUserVector(int user_id) throws SQLException;
    
    public abstract void insertItemVector(int item_id, String tag, double tfidf) throws SQLException;
    
    public abstract void insertNewItemVector(int item_id, int tag_id, double tfidf) throws SQLException;
        
    public abstract ResultSet getItemVectorSpace(int item_id) throws SQLException;
    
    public abstract boolean hasItemSpace(int item_id, int tag_id) throws SQLException;
    
    public abstract void updateItemVector(int item_id, int tag_id, float relevance) throws SQLException;
    
    public abstract void updateUserVector(int user_id, int tag_id, float relevance) throws SQLException;
    
    public abstract float getMaxValueFromItemVectorSpace2() throws SQLException;
    
    public abstract float getMaxValueFromUserVectorSpace() throws SQLException;
    
    public abstract int hasTag(String tag) throws SQLException;
    
    public abstract int insertTag(String tag, int popularity) throws SQLException;
    
    public abstract void insertTagRelevance(int item_id, int tag_id) throws SQLException;
    
    public abstract ResultSet getTagIDs() throws SQLException;
    
    public abstract ResultSet getUsersFromTagsNotInUsers() throws SQLException;
    
    /*
    * MÉTODOS DE TAG_OLD========================
    */
    
    public abstract void insertUserVector(int item_id, String tag, double tfidf) throws SQLException;
    
    public abstract boolean hasItemAxis(int item_id, String attribute) throws SQLException;
    
    public abstract boolean hasUserAxis(int user_id, String attribute) throws SQLException;
    
    public abstract ResultSet getItemAndTags() throws SQLException;
    
    public abstract ResultSet getUserAndTags() throws SQLException;
    
    public abstract void updateFormatedString(String table, String target_column, String new_target, String pk1, String pk2,
                                              int fpk1, int fpk2, String target) throws SQLException;
    public abstract ResultSet getGlobalTagList() throws SQLException;
    
    
}