package database.factory;

import database.Generic;
import database.skeleton.GenericSkeleton;

/**
 *
 * @author Paulo
 */
public class GenericFactory extends GenericSkelFactory{
    
    @Override
    public GenericSkeleton getDataBaseInst(String dbName){
        return Generic.getInstance(dbName);
    }


}
