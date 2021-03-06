package database.factory;

import static utils.Config.DB_NAME;
import static utils.Config.GEN_CLASS;
import database.skeleton.GenericSkeleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Paulo
 */
public abstract class GenericSkelFactory {

    public abstract GenericSkeleton getDataBaseInst(String dbName);
    
    public static GenericSkeleton getInstance(){
        
        try {
           
            GenericSkelFactory gen_factory = (GenericSkelFactory) Class.forName("database.factory."+GEN_CLASS+"Factory").newInstance();
            GenericSkeleton gen = gen_factory.getDataBaseInst(DB_NAME);
            return gen;
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
           
            Logger.getLogger(GenericSkelFactory.class.getName()).log(Level.SEVERE, null, ex);
            throw new RuntimeException("Can't get GenericSkeleton class");
            
        }   
         
    }
    
}