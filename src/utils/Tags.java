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

public class Tags {
    
    protected static float SIMILARITY_CRETERIA = (float)0.7;


    /**
     * @param str1
     * @param str2
     * @return lexical similarity value in the range [0,1]
     */
    public static double compareStrings(String str1, String str2) {
        
        //modificação para corrigir um bug. Quando as duas strings tem uma letra e são iguais estava retornando NaN - Not A Number
        str1 = str1.toUpperCase().replaceAll("\\s+", " ");// normaliza os espaços
        str2 = str2.toUpperCase().replaceAll("\\s+", " ");
        if(str1.length() == 1 && str2.length() == 1)
          return (str1.equals(str2)) ? 1 : 0;
        
        ArrayList pairs1 = wordLetterPairs(str1);
        ArrayList pairs2 = wordLetterPairs(str2);

        int intersection = 0;
        int union = pairs1.size() + pairs2.size();
        for (int i=0; i<pairs1.size(); i++) {
            Object pair1=pairs1.get(i);
            for(int j=0; j<pairs2.size(); j++) {
                Object pair2=pairs2.get(j);
                if (pair1.equals(pair2)) {
                    intersection++;
                    pairs2.remove(j);
                    break;
                }   
            }
        }
    
        return (2.0*intersection)/union;
    }
    
    /** @return an ArrayList of 2-character Strings. */
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
    
    /** @return an array of adjacent letter pairs contained in the input string */
    private static String[] letterPairs(String str) {
        
        int numPairs = str.length()-1;  
        String[] pairs = new String[numPairs];
        for (int i=0; i<numPairs; i++)
            pairs[i] = str.substring(i,i+2);
       
        return pairs;
        
    }
    
    // dado uma tabela, formata corretamente todas as suas strings e atualiza o bd. 
    // isso atualmente funciona com uma table que tenha duas chaves primarias + target_column como chave primaria tambem(para entender ver a estrutura da table tags_old)
    // Pode ser melhorado depois. Pode ser melhorada a performance fazendo um bulk upate
    public static boolean formatString(String table, String pk1, String pk2, String target_column) {
        
        int counter = 0;
        
        try {
                        
            ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
            GenericSkeleton gen = GenericSkelFactory.getInstance();
            ResultSet rSet = gen.getAllFromTable(table);
            
            while(rSet.next()){
                
                String tgt = rSet.getString(target_column);
                String new_target = tgt.replaceAll("\\s+"," ");
                new_target =  new_target.replaceAll("'", "''"); // escapa aspas corretamente no postgreSQL
                tgt = tgt.replaceAll("'", "''");
                
                cbased.updateFormatedString(table, target_column, new_target, pk1, pk2, rSet.getInt(pk1), rSet.getInt(pk2), tgt);
                Utils.printIf(++counter, "Strings Formated:", 1000);
                
            }
            
        } catch (SQLException ex) {
            
            Logger.getLogger(Tags.class.getName()).log(Level.SEVERE, null, ex);
            return false;
            
        }
        
        return true;
        
    }
    
    public static Map<Integer, ArrayList<String> > reduce(ResultSet tagSet, GenericSkeleton gen, String space_vec) throws SQLException{
        
        int current_id, item_change = NO_RECOMMENDATION;
        Map< Integer, ArrayList<String>> tagMap = new HashMap<>();
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
            
            if(tagSet.isLast()) // adiciona o ultimo elemento
                tagMap.put(item_change, list);
            
        }
        
        return tagMap;
        
    }
    
    // dado todas as tags do sistema gera uma lista de tags globais que a representam
    // é usada em conjunto com createVectorSpace de ContentBased
    public static ArrayList<String> getGlobalTagList() throws SQLException{
        
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        ResultSet tags = cbased.getItemAndTags();
        ArrayList<String> global_tag_list = new ArrayList();
        int count = 0;
        
        while(tags.next()){
            
            String tag = tags.getString( "tag" );//.replace("'", "''");
            // se nao estiver na lista global, adiciona
            if(!checkInGlobalTagList(tag, global_tag_list))
                global_tag_list.add(tag);

            Utils.printIf(++count, "Tags processed:", 500);
            
        }
        
        return global_tag_list;

    }
    
    // cria a global tag list de uma lista de tags distintas
    public static ArrayList<String> getGlobalTagList2() throws SQLException{
        
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        ResultSet tags = cbased.getGlobalTagList();
        ArrayList<String> global_tag_list = new ArrayList();
        int count = 0;
        
        while(tags.next()){
            
            String tag = tags.getString( "tag" );//.replace("'", "''");
            // se nao estiver na lista global, adiciona
            if(!checkInGlobalTagList(tag, global_tag_list))
                global_tag_list.add(tag);

            Utils.printIf(++count, "Tags processed:", 1000);
            
        }
        
        return global_tag_list;

    }
        
    private static boolean checkInGlobalTagList(String tag, ArrayList<String> global_tag_list){
        
        for(String gtag: global_tag_list){
            if(Tags.compareStrings(tag, gtag) >= SIMILARITY_CRETERIA)
                return true;
        }
        
        return false;
        
    }
    
    public static String findRepresentative(String tag, ArrayList<String> global_tags){
        
        for(String gtag: global_tags){
            
            if(Tags.compareStrings(tag, gtag) >= SIMILARITY_CRETERIA)
                return gtag;
                
        }
        
        return tag;
        
    }
    
    
}