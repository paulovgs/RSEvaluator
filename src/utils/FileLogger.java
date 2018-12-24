package utils;

import java.io.*; 
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileLogger implements RSELogger {
    
    PrintWriter out; // Log file
    
    public FileLogger(String file_name) {
        
        try {
            
            File file = new File(file_name);
            out = new PrintWriter( new FileWriter(file, true) );
            
        } catch (IOException ex) {
            Logger.getLogger(FileLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    @Override
    public synchronized void writeEntry(Collection entry) {
        
        for (Iterator line = entry.iterator(); line.hasNext();)
            out.println(line.next());
        
        out.println();
        
        
    }
    
    @Override
    public synchronized void writeEntry(String entry) {
        
      out.println(entry);
     
    }
    
}