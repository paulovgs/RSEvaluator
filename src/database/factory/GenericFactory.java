package database.factory;

import database.Generic;
import database.skeleton.GenericSkeleton;

/**
 *
 * @author Paulo
 * @date 06/02/2018
 */
public class GenericFactory extends GenericSkelFactory{
    
    @Override
    public GenericSkeleton getDataBaseInst(String dbName){
        return Generic.getInstance(dbName);
    }


}
