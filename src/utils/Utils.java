package utils;

import static utils.Config.MAX_SCALE;
import com.google.common.collect.Table;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @date 03/27/2018
 * @author Paulo
 */
public class Utils {
    
    public void Utils(){}
    
    // ordena um mapa generico por valor
    public static <K, V extends Comparable<? super V>> LinkedHashMap<K, V> sortByValue(LinkedHashMap<K, V> unsortedMap, boolean natural_order) {

        if(unsortedMap.isEmpty())
            return unsortedMap;
        
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(unsortedMap.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (natural_order == true) ? (o1.getValue()).compareTo(o2.getValue()) : (o2.getValue()).compareTo(o1.getValue());
            }
            
        });

        LinkedHashMap<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        
        return result;

    }
    
    public static <K, V> void printMap(Map<K, V> map) {

        for (Map.Entry<K, V> entry : map.entrySet()) {
            //System.out.printf("%s%-7d%s%.2f", "ID: ", entry.getKey(),"Pred.: " , entry.getValue());
            //System.out.println( "ID: "+ entry.getKey()+" Pred.: " + entry.getValue());
            System.out.println( entry.getValue());
        }
        
    }
    
    public static LinkedHashMap<Integer, Float> pruneMap(LinkedHashMap<Integer, Float> map, int quantity_limit, float value_limit){
        
        if(map.isEmpty())
                return map;
        
        int counter = 0;
       
       Iterator<Map.Entry<Integer, Float >> iter = map.entrySet().iterator();
       
        while (iter.hasNext()) {
            
            Entry<Integer, Float > entry = iter.next();
            
            counter++;
            if(counter > quantity_limit){
                
                iter.remove();
                
            }else if(entry.getValue() < value_limit){ // retira da lista valores abaixo de um determinado limiar
                iter.remove();
                counter--;
            }
            
        }
        
       return map;
       
    }
    
    public static void nonPersonalizedScore(){
        
        try {
            
            GenericSkeleton gen = GenericSkelFactory.getInstance();
            ResultSet items = gen.getAllItems();
            
            Table<Integer, Integer, Float> ratings = gen.getAllRatings();
            float alpha = MAX_SCALE;
            float overall_average = gen.getOverallAverage();
           
            while(items.next()){
                
                int item_id = items.getInt( gen.getItemIDLabel() );
                Map<Integer, Float> item_ratings = ratings.column( item_id );
                                
                float sum = 0;
                for(Entry<Integer, Float> entry : item_ratings.entrySet())
                    sum += entry.getValue();
                   
                float score = (sum + alpha * overall_average) / ( item_ratings.size() + alpha);
                gen.setNonPersonalizedScore(score, item_id);
                
                printIf(item_id, "Item:", 250);
                
            }
            
        } catch (SQLException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void printIf(int counter, String message, int condition){
        if(counter % condition == 0)
            System.out.println(message + " " + counter);
    }
    
    public static void printExperiment(int index, int var){
        
            switch(var){
                case 1:
                    System.out.println("============================================================="); 
                    System.out.println("                    Experimento " + index);
                    System.out.println("=============================================================");
                break;
                case 2:
                    System.out.println("\n====================Repetição " + index + "=================\n"); 
                break;
            }
                    
        }
    
    public static void makeDir(int id){
        
        String OpSys = System.getProperty("os.name");
        String slash = (OpSys.equals("Linux")) ? "/" : "\\";
        String path = "Experiments"+ slash + id;

        File file = new File(System.getProperty("user.dir") + slash + path); //linux
        if (!file.exists())
                    file.mkdirs();
            
        
    }
    
    public static boolean itensAverageRating() throws SQLException{
        return User.computeAverageRating(false);
    }
    
    public static void removeBottom(){
        
        try {
            
            GenericSkeleton gen = GenericSkelFactory.getInstance();
            ResultSet items = gen.getAllItems();
            
            String iid = gen.getItemIDLabel();
            
            int count = 0;
            
            while(items.next()){
                
                int item_id = items.getInt(iid);
                
                gen.executeUpdate("delete from item_similarity where item_x = "+item_id+" and " +
                    "item_y not in(select item_y from item_similarity where item_x = "+item_id+" order by similarity desc limit 1000)"
                );
                                
                Utils.printIf(++count, "Items processed:", 1000);
                
            }
            

        } catch (SQLException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void removeBottomUsers(){
        
        try {
            
            GenericSkeleton gen = GenericSkelFactory.getInstance();
            ResultSet users = gen.getAllUsers();
            
            String uid = gen.getUserIDLabel();
            
            int count = 0;
            
            while(users.next()){
                
                int user_id = users.getInt(uid);
                
                gen.executeUpdate("delete from user_similarity where user_x = "+user_id+" and " +
                    "user_y not in(select user_y from user_similarity where user_x = "+user_id+" order by similarity desc limit 1000)"
                );
                                
                Utils.printIf(++count, "Users processed:", 1000);
                
            }
            

        } catch (SQLException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void removeBelowID(int user_id){

        try {
            GenericSkeleton gen = GenericSkelFactory.getInstance();
            gen.executeUpdate("delete from user_similarity where user_x < "+user_id );
            
        } catch (SQLException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
        
}
