package database.factory;

import static utils.Config.CB_CLASS;
import static utils.Config.DB_NAME;
import database.skeleton.ContentBasedDBSkeleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo
 * @date 06/02/2018
 */
public abstract class ContentBasedDBSkeletonFactory {

    public abstract ContentBasedDBSkeleton getDataBaseInst(String dbName);
    
    public static ContentBasedDBSkeleton getInstance(){
        
        try {
           
            ContentBasedDBSkeletonFactory cb_factory = (ContentBasedDBSkeletonFactory) Class.forName("database.factory."+CB_CLASS+"Factory").newInstance();
            ContentBasedDBSkeleton cbased = cb_factory.getDataBaseInst(DB_NAME);
            return cbased;
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
           
            Logger.getLogger(ContentBasedDBSkeletonFactory.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Can't get ContentBasedDBSkeletonFactory class");
            
        }   
         
    }
    
}



