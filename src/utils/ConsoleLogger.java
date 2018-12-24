package utils;

import java.util.*;

public class ConsoleLogger implements RSELogger {
    
    public ConsoleLogger(){
        
    }

    @Override
    public synchronized void writeEntry(Collection entry) {
        
        for (Iterator line = entry.iterator(); line.hasNext();)
            System.out.println(line.next());
            System.out.println();
            
    }
    
    @Override
    public synchronized void writeEntry(String entry) {
        
            System.out.println(entry);
            
    }

}