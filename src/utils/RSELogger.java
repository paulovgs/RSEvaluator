package utils;

import java.util.*;

public interface RSELogger {
    
    public void writeEntry(Collection entry); // Write list of lines
    public void writeEntry(String entry); // Write single lines

}