package utils;

import java.util.*;

/**
 * Log data into console
 * @author Paulo
 */
public class ConsoleLogger implements RSELogger {
    
    public ConsoleLogger(){}

    /**
     * Write a collection of entries into console
     * @param entry - collection to be written
     */
    @Override
    public synchronized void writeEntry(Collection entry) {
        
        for (Iterator line = entry.iterator(); line.hasNext();){
            System.out.println(line.next());
        }
            
    }
    
    /**
     * Write a single line into console
     * @param entry - entry to be written
     */
    @Override
    public synchronized void writeEntry(String entry) {
        System.out.println(entry);
    }

}