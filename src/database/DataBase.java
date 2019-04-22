package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.Config;

/**
 * @author Paulo 
 */
public abstract class DataBase {
    
    //Connection dbCon; 
    public Connection dbCon; 
    protected static String url = Config.DB_URL;
    protected static String username = Config.DB_USERNAME;
    protected static String password = Config.DB_PASS;
    
    public DataBase(String dbName){
    
        this.dbCon = null;
        url = Config.DB_URL + dbName;
        dbCon = open();
        
    }
    
    public final Connection open(){
    
        try {
            
            Class.forName("org.postgresql.Driver");  
            dbCon = DriverManager.getConnection(url, username, password);
            return dbCon;
            
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    
    }
    
    public void setURL(String url){
        this.url = url;
    }
    
    public void setUsername(String username){
        this.username = username;
    }
    
    public void setPassword(String password){
        this.password = password;
    }
    
    public void close(){
        
        try {
            
            this.dbCon.close();
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    

    
}

