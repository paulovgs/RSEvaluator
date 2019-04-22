package utils;

import java.util.Collection;

/**
 * Class designed to log data
 * @author Paulo
 */
public interface RSELogger {
    
    /** 
     * Write a list of lines
     * @param entry - Collection of entries to be written 
     */
    public void writeEntry(Collection entry);
    
    /** 
     * Write a single line
     * @param entry - Entry to be written
     */
    public void writeEntry(String entry);

}