package utils;

import database.factory.ContentBasedDBFactory;
import database.skeleton.ContentBasedDBSkeleton;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Person who will request a recommendation
 * @author Paulo (2018-12-03)
 */
public class User {

    private final int user_id;
    private double arrival_time;
    private float history_average_rt;

    /**
     * Default constructor
     * @param user_id User id
     */
    public User(int user_id) {
        this.user_id = user_id;
    }

    /**
     * Alternative constructor with two parameters
     * @param user_id User id
     * @param arrival_time User arrival time in the queue
     */
    public User(int user_id, double arrival_time) {
        this.user_id = user_id;
        this.arrival_time = arrival_time;
    }

    /**
     * Returns the id of the user
     * @return user_id
     */
    public int getID() {return user_id;}

    /**
    * Returns the user arrival time in the queue
    * @return arrival_time
    */
    public double getArrivalTime(){return arrival_time;}
    
    /**
     * Returns the user's history average rating.
     * @return history_average_rt
     */
    public float getHistoryAverageRt() {return history_average_rt;}

    /**
     * Set the user arrival time
     * @param time Arrival time in queue
     */
    public void setArrivalTime(double time){arrival_time = time;}

    /**
     * Set the user's history average rating.
     * @param average_test Average of rating related to the user's history
     */
    public void setHistoryAverageRt(float average_test){this.history_average_rt = average_test;}
   
    /**
     * Calculate and store the average of ratings for each user (only the history portion(75%)is considered)
     * @return boolean
     * @throws SQLException
     */
    public static boolean computeHistoryAverageRating() throws SQLException {

        int count = 0;
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet users = gen.getAllUserIDs();

        while (users.next()) {
            int user_id = users.getInt(gen.getUserIDLabel());
            gen.updateHistoryAverage(user_id);
            Utils.printIf(++count, "Amount of users updates:", 100);
        }

        return true;

    }

    /**
     * Calculate and store the global average of ratings for each user or for each item (using 100% of ratings)
     * @param user_or_item_avg true for user, false for item
     * @return boolean
     * @throws java.sql.SQLException
     */
    public static boolean computeAverageRating(boolean user_or_item_avg) throws SQLException {

        int count = 0;
        String avg_type, table;
        GenericSkeleton gen = GenericSkelFactory.getInstance();

        avg_type = (user_or_item_avg) ? gen.getUserIDLabel() : gen.getItemIDLabel();
        table = (user_or_item_avg) ? "users" : gen.getItemTableLabel();

        ResultSet avgSet = gen.getAverageRating(avg_type);

        while (avgSet.next()) {
            gen.updateAverageRating(table, avg_type, avgSet.getFloat("avg"), avgSet.getInt(avg_type));
            Utils.printIf(++count, "Updated entries:", 1000);
        }

        return true;
        
    }

    /**
     * Used for database configuration. User_ids in the ratings table will be inserted into users table.
     * @return boolean value
     * @throws java.sql.SQLException
     */
    public static boolean insertUsers() throws SQLException {

        int count = 0;
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet userSet = gen.getUsersFromRTNotInUsers();

        while (userSet.next()) {
            gen.insertInUsers(userSet.getInt(gen.getUserIDLabel()));
            Utils.printIf(++count, "Users Inserted:", 1000);
        }

        return true;

    }

    /**
     * Used for database configuration. User_ids in the tags table will be inserted into users table.
     * @return boolean value
     * @throws SQLException 
     */
    public static boolean insertUsersFromTags() throws SQLException {

        int count = 0;
        ContentBasedDBSkeleton cbased = ContentBasedDBFactory.getInstance();
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet userSet = cbased.getUsersFromTagsNotInUsers();

        while (userSet.next()) {
            gen.insertInUsers(userSet.getInt(gen.getUserIDLabel()));
            Utils.printIf(++count, "Users inserted:", 100);
        }

        return true;

    }

    /**
     * Will divide the user's ratings. Each user will have 75% for history and 25% for test set.
     */
    public static void splitRatings75() {

        try {

            GenericSkeleton gen = GenericSkelFactory.getInstance();
            ResultSet users = gen.getAllUsers();
            gen.resetHistory();
            int counter = 0;

            while (users.next()) {
                gen.splitRatings75(users.getInt(gen.getUserIDLabel()));
                Utils.printIf(++counter, "Users processed:", 10);
            }

        } catch (SQLException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
