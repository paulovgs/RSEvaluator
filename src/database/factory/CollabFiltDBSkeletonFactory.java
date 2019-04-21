package database.factory;

import static utils.Config.CF_CLASS;
import static utils.Config.DB_NAME;
import database.skeleton.CollabFiltDBSkeleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo
 * @date 06/02/2018
 */
public abstract class CollabFiltDBSkeletonFactory {

    public abstract CollabFiltDBSkeleton getDataBaseInst(String dbName);
    
    public static CollabFiltDBSkeleton getInstance(){
        
        try {
           
            CollabFiltDBSkeletonFactory col_factory = (CollabFiltDBSkeletonFactory) Class.forName("database.factory."+CF_CLASS+"Factory").newInstance();
            CollabFiltDBSkeleton collab = col_factory.getDataBaseInst(DB_NAME);
            return collab;
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
           
            Logger.getLogger(CollabFiltDBSkeletonFactory.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Can't get CollabFiltDBSkeletonFactory class");
            
        }   
         
    }
    
}
