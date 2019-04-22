package utils;

import database.skeleton.ContentBasedDBSkeleton;
import database.factory.ContentBasedDBSkeletonFactory;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static recommender.algorithm.Recommender.NO_RECOMMENDATION;

/**
 * Strings and tags manipulation. Some operations were adapted from <a href="http://www.catalysoft.com/articles/StrikeAMatch.html">How to Strike a Match</a>
 * @author Paulo
 */
public class Tags {
    
    protected static float SIMILARITY_CRETERIA = (float)0.7;


    /**
     * Two strings are compared each other and the similarity between them is measured.
     * @param str1 String one to be compared
     * @param str2 String two to be compared
     * @return lexical similarity value in the range [0,1].
     */
    public static double compareStrings(String str1, String str2) {
        
        str1 = str1.toUpperCase().replaceAll("\\s+", " ");// normalize spaces
        str2 = str2.toUpperCase().replaceAll("\\s+", " ");
        
        if(str1.length() == 1 && str2.length() == 1)
          return (str1.equals(str2)) ? 1 : 0;
        
        ArrayList pairs1 = wordLetterPairs(str1);
        ArrayList pairs2 = wordLetterPairs(str2);

        int intersection = 0;
        int union = pairs1.size() + pairs2.size();
        
        for (int i = 0; i < pairs1.size(); i++) {
            Object pair1 = pairs1.get(i);
            for(int j = 0; j < pairs2.size(); j++) {
                Object pair2 = pairs2.get(j);
                if (pair1.equals(pair2)) {
                    intersection++;
                    pairs2.remove(j);
                    break;
                }   
            }
        }
    
        return (2.0*intersection)/union;
    }

    /**
     * Auxiliary and internal method
     * @param str Input string
     * @return An ArrayList of 2-character Strings.
     */
    private static ArrayList wordLetterPairs(String str) {
        
        ArrayList allPairs = new ArrayList();
        // Tokenize the string and put the tokens/words into an array
        String[] words = str.split("\\s");
        
        // For each word
        for (int w=0; w < words.length; w++) {
        
            // Find the pairs of characters
            String[] pairsInWord = letterPairs(words[w]);
            for (int p=0; p < pairsInWord.length; p++)
                allPairs.add(pairsInWord[p]);
        
        }
        return allPairs;
    }
    
    /**
    * Auxiliary and internal method.
    * @param str Input string
    * @return An array of adjacent letter pairs contained in the input string.
    */
    private static String[] letterPairs(String str) {
        
        int numPairs = str.length()-1;  
        String[] pairs = new String[numPairs];
        
        for (int i = 0; i < numPairs; i++)
            pairs[i] = str.substring(i,i+2);
       
        return pairs;
        
    }
    
    /**
     * Given a table, formats all of your strings and updates the database. It requires two primary keys plus a target column.
     * @param table Table name
     * @param pk1 Primary key one
     * @param pk2 Primary key two
     * @param target_column Column to be formated
     * @return True if success, false if fails.
     */
    public static boolean formatString(String table, String pk1, String pk2, String target_column) {
        
        int counter = 0;
        
        try {
                        
            ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
            GenericSkeleton gen = GenericSkelFactory.getInstance();
            ResultSet rSet = gen.getAllFromTable(table);
            
            while(rSet.next()){
                
                String tgt = rSet.getString(target_column);
                String new_target = tgt.replaceAll("\\s+"," ");
                new_target =  new_target.replaceAll("'", "''"); // format quotes from postgreSQL
                tgt = tgt.replaceAll("'", "''");
                
                cbased.updateFormatedString(table, target_column, new_target, pk1, pk2, rSet.getInt(pk1), rSet.getInt(pk2), tgt);
                Utils.printIf(++counter, "Formated Strings:", 1000);
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Tags.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            
        }
        
        return true;
        
    }
    
    /**
     * 
     * @param tagSet
     * @param gen
     * @param space_vec
     * @return
     * @throws SQLException 
     */
    public static Map< Integer, ArrayList<String> > reduce(ResultSet tagSet, GenericSkeleton gen, String space_vec) throws SQLException{
        
        int current_id, item_change = NO_RECOMMENDATION;
        Map< Integer, ArrayList<String> > tagMap = new HashMap<>();
        ArrayList<String> list = new ArrayList();
        
        String col = (space_vec.equals("item")) ? gen.getItemIDLabel() : gen.getUserIDLabel();
        
        while(tagSet.next()){

            current_id = tagSet.getInt( col );

            if(item_change != current_id || tagSet.isLast()){
                
                if(item_change != NO_RECOMMENDATION)
                    tagMap.put(item_change, list);
               
                if(!tagSet.isLast())
                    list = new ArrayList();
               
                item_change = current_id;
            
            }
            
            list.add(tagSet.getString("tag_id"));
            
            if(tagSet.isLast()) // adds the last element
                tagMap.put(item_change, list);
        }
        
        return tagMap;
        
    }
    
    /**
     * Generates a list of global tags, which represents similar words. For example, the global tag <b>computer</b> may represent both computer and computers
     * @return The global tags list
     * @throws SQLException 
     */
    public static ArrayList<String> getGlobalTagList() throws SQLException{
        
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        ResultSet tags = cbased.getItemAndTags();
        ArrayList<String> global_tag_list = new ArrayList();
        int count = 0;
        
        while(tags.next()){
            
            String tag = tags.getString( "tag" );//.replace("'", "''");

            if(!checkInGlobalTagList(tag, global_tag_list))
                global_tag_list.add(tag);

            Utils.printIf(++count, "Tags processed:", 500);
            
        }
        
        return global_tag_list;

    }
    
    /**
     * Given a distinct tag list, generates a list of global tags which represents similar words. 
     * @return The global tags list
     * @throws SQLException 
     */
    public static ArrayList<String> getGlobalTagList2() throws SQLException{
        
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        ResultSet tags = cbased.getGlobalTagList();
        ArrayList<String> global_tag_list = new ArrayList();
        int count = 0;
        
        while(tags.next()){
            
            String tag = tags.getString( "tag" );//.replace("'", "''");

            if(!checkInGlobalTagList(tag, global_tag_list))
                global_tag_list.add(tag);

            Utils.printIf(++count, "Tags processed:", 1000);
            
        }
        
        return global_tag_list;

    }
        
    /**
     * Auxiliary method
     * @param tag 
     * @param global_tag_list
     * @return Boolean value
     */
    private static boolean checkInGlobalTagList(String tag, ArrayList<String> global_tag_list){
        
        for(String gtag: global_tag_list){
            if(Tags.compareStrings(tag, gtag) >= SIMILARITY_CRETERIA)
                return true;
        }
        
        return false;
        
    }
    
    /**
     * Given a specific tag, find its representative inside the global tags list. Used in methods that create vector spaces.
     * @param tag
     * @param global_tags
     * @return The representative tag
     */
    public static String findRepresentative(String tag, ArrayList<String> global_tags){
        
        for(String gtag: global_tags){
            
            if(Tags.compareStrings(tag, gtag) >= SIMILARITY_CRETERIA)
                return gtag;
                
        }
        
        return tag;
        
    }
    
    
}