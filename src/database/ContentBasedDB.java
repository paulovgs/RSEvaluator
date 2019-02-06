package database;

import database.skeleton.ContentBasedDBSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static recommender.algorithm.Recommender.NO_RECOMMENDATION;

/**
 * @author Paulo
 * @date 06/02/2018
 */
public class ContentBasedDB extends ContentBasedDBSkeleton{
    
    public ContentBasedDB(String dbName) {
        super(dbName);
    }
    
    public static ContentBasedDB getInstance(String dbName){
        
        if(instance == null)
            instance = new ContentBasedDB(dbName);
        
        if(instance.dbCon == null)
            instance.dbCon = instance.open();
        
        return (ContentBasedDB) instance;
    }
    
    @Override
    public ResultSet getCBCandidatesWithVector(int user_id, int candidates) throws SQLException{
        
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery("select "+this.item_id+", tag_id, relevance from item_vector where "+this.item_id+" in " +
                    "(select "+this.item_id+" from "+this.item_table+" where "+this.item_id+" not in " // n itens aleatorios
                        + "(select "+this.item_id+" from ratings where "+this.user_id+" = "+user_id+" and is_history = true) " + // exclui o historio do usuário
                    " order by random() limit "+candidates+" ) order by "+this.item_id
        );
        
    }
    
        
    @Override
    public ResultSet getUserVector(int user_id) throws SQLException{
        
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery("select * from user_vector where "+this.user_id+" = "+user_id);         
        
    }
    
    @Override
    public void insertItemVector(int item_id, String tag, double tfidf) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("insert into item_vector("+this.item_id+", tag_id, relevance) values ("+item_id+", '"+tag+"', "+tfidf+");");
        
    }
    
    @Override
    public void insertNewItemVector(int item_id, int tag_id, double tfidf) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("insert into item_vector values ("+item_id+", '"+tag_id+"', "+tfidf+");");
        
    }
    
    @Override // FOI JUNTADO COM GETITEMVECTORSPACE2 ************
    public ResultSet getItemVectorSpace(int item_id) throws SQLException{
        
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet vector = st.executeQuery("select tag_id, relevance from item_vector where "+this.item_id+" = " + item_id);
        return vector;
        
    }
    
    @Override
    public boolean hasItemSpace(int item_id, int tag_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet rSet = st.executeQuery("select * from item_vector where "+this.item_id+" = "+item_id+
                " and tag_id = "+tag_id);
        
        return rSet.next();
    }
    
    @Override
    public void updateItemVector(int item_id, int tag_id, float relevance) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update item_vector set relevance = "+relevance+ " where "+this.item_id+" = "+item_id+""
                         + " and tag_id = "+tag_id);
        
    }
    
    @Override
    public void updateUserVector(int user_id, int tag_id, float relevance) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update user_vector set relevance = "+relevance+ " where "+this.user_id+" = "+user_id+""
                         + " and tag_id = "+tag_id);
        
    }
    
    @Override
    public float getMaxValueFromItemVectorSpace2() throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet val = st.executeQuery("select max(relevance) from item_vector");
        val.next();
        return val.getFloat("max");
        
    }
    
    @Override
    public float getMaxValueFromUserVectorSpace() throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet val = st.executeQuery("select max(relevance) from user_vector");
        val.next();
        return val.getFloat("max");
        
    }
    
    @Override
    public int hasTag(String tag) throws SQLException{
        
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet r = st.executeQuery("select tag_id from tags where tag = '"+tag+"'");
        
        if(r.first())
            return r.getInt("tag_id");
        
        return NO_RECOMMENDATION;
        
        
    }
    
    @Override
    public int insertTag(String tag, int popularity) throws SQLException{
        
        if(tag.equals("(no genres listed)")) return NO_RECOMMENDATION;
        
        Statement st = dbCon.createStatement();
        ResultSet id = st.executeQuery("select tag_id from tags order by tag_id desc limit 1");
        id.next();
        
        int tag_id = id.getInt("tag_id") + 1;
        
        ResultSet tagSet = st.executeQuery("insert into tags(tag_id, tag, tag_popularity) "
                                         + "values ("+tag_id+", '"+tag+"', "+popularity+") returning tag_id;");
        tagSet.next();
        return tagSet.getInt("tag_id");
        
    }
    
    // o valor baixo de relevance é para tentar mesclar generos com as tags genome, mas priorizando as suas principais tags.
    // As tags de genero serão mais importantes nos itens que não tiverem nenhuma tag genome
    @Override
    public void insertTagRelevance(int item_id, int tag_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet tagSet = st.executeQuery("insert into item_vector("+this.item_id+", tag_id, relevance) "
                                         + "values ("+item_id+", '"+tag_id+"', 0.02) returning tag_id;");
        
    }
    
    @Override
    public ResultSet getTagIDs() throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select tag_id, tag from tags");
        
    }
    
    @Override
    public ResultSet getUsersFromTagsNotInUsers() throws SQLException{
    
        Statement st = dbCon.createStatement();
        ResultSet userSet = st.executeQuery("select distinct user_id from tags_old where user_id not in (select user_id from users) order by user_id");
        return userSet;
        
    }
    
    /*                                          
    * MÉTODOS DA FORMA ALTERNATIVA DE CRIAR TAGS
    */
    @Override
    public void insertUserVector(int item_id, String tag, double tfidf) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("insert into user_vector_space values ("+item_id+", '"+tag+"', "+tfidf+");");
        
    }
    
    @Override
    public boolean hasItemAxis(int item_id, String attribute) throws SQLException{
        
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet r = st.executeQuery("select "+this.item_id+" from item_vector where "+this.item_id+" = "+item_id+" and relevance = '"+attribute+"'");
        return r.first();
        
    }
    
    @Override
    public boolean hasUserAxis(int user_id, String attribute) throws SQLException{
        Statement st = dbCon.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        ResultSet r = st.executeQuery("select "+this.user_id+" from user_vector_space where "+this.user_id+" = "+user_id+" and attribute = '"+attribute+"'");
        return r.first();
    }
    
    @Override
    public ResultSet getItemAndTags() throws SQLException{
        
        Statement st = dbCon.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery("select "+this.item_id+", tag from tags_old order by "+this.item_id);

    }
    
    @Override
    public ResultSet getUserAndTags() throws SQLException{
        
        Statement st = dbCon.createStatement( ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        return st.executeQuery("select "+this.user_id+", tag from tags_old order by "+this.user_id+"");

    }
    
    @Override
    public void updateFormatedString(String table, String target_column, String new_target, String pk1, String pk2,
                                     int fpk1, int fpk2, String target) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update " + table + " set " + target_column + " = '" + new_target + "' " +
                        "where " + pk1 + " = " + fpk1 + " and " + pk2 + " = " + fpk2 +
                        " and " + target_column + " = '" + target + "'"
        );
        
    }
    
    @Override
    public ResultSet getGlobalTagList() throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select * from tags_old order by tag_id");
        
    }
    


}
