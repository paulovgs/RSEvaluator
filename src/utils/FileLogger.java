package utils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Log data into file
 * @author Paulo
 */
public class FileLogger implements RSELogger {

    PrintWriter log_file;

    /**
     * Create a new file, which will contains the log data
     * @param file_name - the name of the file to be created
     */
    public FileLogger(String file_name) {

        try {

            File file = new File(file_name);
            log_file = new PrintWriter(new FileWriter(file, true));

        } catch (IOException ex) {
            Logger.getLogger(FileLogger.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Write a collection of entries into file
     * @param entry - collection to be written
     */
    @Override
    public synchronized void writeEntry(Collection entry) {

        for (Iterator line = entry.iterator(); line.hasNext();) {
            log_file.println(line.next());
        }

        log_file.println();

    }
    
    /**
     * Write a single line into file
     * @param entry - entry to be written
     */
    @Override
    public synchronized void writeEntry(String entry) {

        log_file.println(entry);

    }

}
