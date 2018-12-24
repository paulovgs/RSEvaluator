package database.factory;

import database.skeleton.CollabFiltDBSkeleton;
import database.CollaborativeFiltDB;

/**
 *
 * @author Paulo
 * @date 06/02/2018
 */
public class CollabFiltDBFactory extends CollabFiltDBSkeletonFactory{
    
    @Override
    public CollabFiltDBSkeleton getDataBaseInst(String dbName){
        return CollaborativeFiltDB.getInstance(dbName);
    }


}
