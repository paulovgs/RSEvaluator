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
 * Encapsulates auxiliaries methods.
 * @author Paulo
 */
public class Utils {
    
    public void Utils(){}
    
    /**
     * Sorts a map by value
     * @param unsortedMap Map to be sorted
     * @param natural_order Choose between natural and reverse order (true or false, respectively)
     * @return The sorted map
     */
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
    
    /**
     * Prints a map. Just for test purposes
     * @param map Map to be printed
     */
    public static <K, V> void printMap(Map<K, V> map) {

        for (Map.Entry<K, V> entry : map.entrySet()) {
            //System.out.printf("%s%-7d%s%.2f", "ID: ", entry.getKey(),"Pred.: " , entry.getValue());
            //System.out.println( "ID: "+ entry.getKey()+" Pred.: " + entry.getValue());
            System.out.println( entry.getValue());
        }
        
    }
    
    /**
     * Given a map, will remove all entries below the threshold. Also, its length will be at most the ammount specified in the second parameter.
     * @param map The input map
     * @param max_length The biggest ammount of items the map can have
     * @param threshold The lowest value that an item can have inside the input map
     * @return The pruned map
     */
    public static LinkedHashMap<Integer, Float> pruneMap(LinkedHashMap<Integer, Float> map, int max_length, float threshold){
        
        if(map.isEmpty())
            return map;
        
        int counter = 0;
       
        Iterator<Map.Entry<Integer, Float >> iter = map.entrySet().iterator();
       
        while (iter.hasNext()) {
            
            Entry<Integer, Float > entry = iter.next();
            counter++;
            if(counter > max_length){
                
                iter.remove();
                
            }else if(entry.getValue() < threshold){ // retira da lista valores abaixo de um determinado limiar
                iter.remove();
                counter--;
            }
            
        }
        
       return map;
       
    }
    
    /**
     * Computes and stores the non-personalized score of all items
     */
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

    /**
     * Prints the specified message on console when a certain condition is satisfied. Must be used inside a loop.
     * For example, would print a "Items processed: x" every x items processed
     * @param counter The current counter of the loop
     * @param message The message to be printed
     * @param condition Will print the message every when counter were a multiple of condition
     */
    public static void printIf(int counter, String message, int condition){
        if(counter % condition == 0)
            System.out.println(message + " " + counter);
    }
    
    /**
     * Prints a label at the beginning of each experiment/repetition
     * @param index The current experiment
     * @param var 1 for experiment; 2 for repetition
     */
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
    
    /**
     * Creates a folder inside the one called "Experiments". Its name is usually the id of the current experiment.
     * @param dir_name The name of the folder
     */
    public static void makeDir(String dir_name){
        
        String OpSys = System.getProperty("os.name");
        String slash = (OpSys.equals("Linux")) ? "/" : "\\";
        String path = "Experiments"+ slash + dir_name;

        File file = new File(System.getProperty("user.dir") + slash + path); //linux
        if (!file.exists())
                    file.mkdirs();
        
    }
    
    /**
     * Computes the average rating of each item
     * @return 
     * @throws SQLException 
     */
    public static boolean itensAverageRating() throws SQLException{
        return User.computeAverageRating(false);
    }
        
}
