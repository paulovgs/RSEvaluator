package database.factory;

import database.ContentBasedDB;
import database.skeleton.ContentBasedDBSkeleton;

/**
 *
 * @author Paulo
 * @date 06/02/2018
 */
public class ContentBasedDBFactory extends ContentBasedDBSkeletonFactory{
    
    @Override
    public ContentBasedDBSkeleton getDataBaseInst(String dbName){
        return ContentBasedDB.getInstance(dbName);
    }


}

