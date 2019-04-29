package database;

import evaluator.FactorTypesEnum;
import evaluator.ResponseVariable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

/**
 * @author Paulo 
 */
public class Groundwork extends DataBase {
    
    public Groundwork(String dbName) {
        super(dbName);
    }
    
    public boolean createDatabase(String dbName){
        
        try {
            
            Connection con = DriverManager.getConnection(url, username, password);
            Statement st = con.createStatement();
            st.executeUpdate("create database \"" + dbName + "\"");
            return true;
            
        } catch (SQLException ex) {
           // Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
    }
    
    public boolean createEvalDatabase(){
        return createDatabase("Evaluation");
    }
    
    public void createEvalStructure(){
        
        try {
           
            Statement st = dbCon.createStatement();
            
            st.executeUpdate(
                    "CREATE SEQUENCE public.evaluations_evaluation_id_seq;" +
                    "ALTER SEQUENCE public.evaluations_evaluation_id_seq OWNER TO postgres;"
            );
            
            st.executeUpdate(
                    "CREATE SEQUENCE public.factors_ins_order_seq;" +
                    "ALTER SEQUENCE public.factors_ins_order_seq OWNER TO postgres;"
            );
            
            st.executeUpdate(
                "CREATE TABLE public.evaluations" +
                "(" +
                "    evaluation_id integer NOT NULL DEFAULT nextval('evaluations_evaluation_id_seq'::regclass)," +
                "    evaluation_name character varying COLLATE pg_catalog.\"default\" NOT NULL," +
                "    confidence_interval real NOT NULL," +
                "    description character varying COLLATE pg_catalog.\"default\"," +
                "    multi_lvl_step integer," +
                "    multi_lvl_starter integer," + 
                "    CONSTRAINT evaluations_pkey PRIMARY KEY (evaluation_id)" +
                ")" +
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.evaluations OWNER to postgres;" +
                        
                "CREATE TABLE public.response_variables" +
                "(" +
                "    rv_id integer NOT NULL," +
                "    rv_name character varying COLLATE pg_catalog.\"default\" NOT NULL," +
                "    y_axis character varying COLLATE pg_catalog.\"default\"," +
                "    CONSTRAINT response_variables_pkey PRIMARY KEY (rv_id)" +
                ")" +
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.response_variables OWNER to postgres;" +
                                  
                "CREATE TABLE public.experiments("+
                "   evaluation_id integer NOT NULL,"+
                "   experiment_id integer NOT NULL,"+
                "   rv_id integer NOT NULL,"+
                "   rv_value real NOT NULL,"+
                "   standard_deviation real NOT NULL,"+
                "   confidence_interval_amp real NOT NULL,"+
                "   CONSTRAINT experiments_pkey PRIMARY KEY (experiment_id, rv_id, evaluation_id),"+
                "   CONSTRAINT evaluation_id_fkey FOREIGN KEY (evaluation_id)"+
                "       REFERENCES public.evaluations (evaluation_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION,"+
                "   CONSTRAINT rv_id_fkey FOREIGN KEY (rv_id)"+
                "   REFERENCES public.response_variables (rv_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION )"+
                    "WITH ( OIDS = FALSE )" +
                    "TABLESPACE pg_default;" +
                    "ALTER TABLE public.experiments OWNER to postgres;" +
                        
                        
                "CREATE TABLE public.factors" +
                "(" +
                "    factor_id integer NOT NULL," +
                "    factor_name character varying COLLATE pg_catalog.\"default\" NOT NULL," +
                "    CONSTRAINT factors_pkey PRIMARY KEY (factor_id)" +
                ")" +
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.factors OWNER to postgres;" +
                        
                "CREATE TABLE public.evaluation_x_factors("+
                "   evaluation_id integer NOT NULL,"+
                "   factor_id integer NOT NULL,"+
                "   level_1 character varying COLLATE pg_catalog.\"default\" NOT NULL,"+
                "   level_2 character varying COLLATE pg_catalog.\"default\","+
                "   composed_factor_id integer,"+
                "   ins_order integer NOT NULL DEFAULT nextval('factors_ins_order_seq'::regclass),"+
                "   CONSTRAINT evalfac_pkey PRIMARY KEY (evaluation_id, factor_id),"+
                "   CONSTRAINT composed_factor_type_fkey FOREIGN KEY (composed_factor_id)"+
                "   REFERENCES public.factors (factor_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION,"+
                "   CONSTRAINT evaluation_id_fkey FOREIGN KEY (evaluation_id)"+
                "   REFERENCES public.evaluations (evaluation_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION,"+
                "   CONSTRAINT factor_type_fkey FOREIGN KEY (factor_id)"+
                "   REFERENCES public.factors (factor_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION)"+
                    "WITH ( OIDS = FALSE )" +
                    "TABLESPACE pg_default;" +
                    "ALTER TABLE public.evaluation_x_factors OWNER to postgres;" +
                        
                "CREATE TABLE public.time_classes" +
                "(" +
                "    class_id smallint NOT NULL," +
                "    class_name character varying COLLATE pg_catalog.\"default\" NOT NULL," +
                "    class_description character varying COLLATE pg_catalog.\"default\" NOT NULL," +
                "    lower_bound real,"+
                "    upper_bound real,"+
                "    "+
                "    "+
                "   CONSTRAINT time_classes_pkey PRIMARY KEY (class_id))"+
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.time_classes OWNER to postgres;" +
                        
                        
                "CREATE TABLE public.experiment_x_time_classes" +
                "(" +
                "    evaluation_id integer NOT NULL," +
                "    experiment_id integer NOT NULL," +
                "    time_class_id smallint NOT NULL," +
                "    time_class_value real NOT NULL," +
                "    CONSTRAINT experiment_x_time_classes_pkey PRIMARY KEY (time_class_id, experiment_id, evaluation_id)," +
                "    CONSTRAINT evaluation_fk FOREIGN KEY (evaluation_id)" +
                "    REFERENCES public.evaluations (evaluation_id) MATCH SIMPLE" +
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION,"+
                "   CONSTRAINT time_class_fk FOREIGN KEY (time_class_id)"+
                    "   REFERENCES public.time_classes (class_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION"+
                ")" +
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.experiment_x_time_classes OWNER to postgres;"
                        
            );
                                    
                        
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        try{
            
            insertTimeClasses();
            FactorTypesEnum.persistFactorTypes();
            ResponseVariable.persistResponseVariables();
            
        } catch (SQLException ex) {
            Logger.getLogger(Groundwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void insertTimeClasses(){
        
        try {
            
            Statement st = dbCon.createStatement();
            st.executeUpdate(
                "INSERT INTO public.time_classes(class_id, class_name, class_description, lower_bound, upper_bound)" +
                "VALUES (1, 'c1', '0-0.5Trec', 0, 0.5)," +
                "(2, 'c2', '0.5 - 1 Trec', 0.5, 1)," +
                "(3, 'c3', '1 - 2 Trec', 1, 2)," +
                "(4, 'c4', '2 - 3 Trec', 2, 3)," +
                "(5, 'c5', '3 or bigger Trec', 3, 4);"
            );
            
        } catch (SQLException ex) {
            Logger.getLogger(Groundwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void setDb(String dbName){
        url += dbName;
    }
    
    public void createStructure(){
               
        try {
           
            Statement st = dbCon.createStatement();
            st.executeUpdate(
                "CREATE TABLE public.users" +
                "(" +
                "    user_id integer NOT NULL," +
                "    global_avg_rt real," +
                "    history_avg_rt real," +
                "    CONSTRAINT users_pkey PRIMARY KEY (user_id)" +
                ")" +
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.users OWNER to postgres;" +
                
                "CREATE TABLE public.movies("+
                "   movie_id integer NOT NULL,"+
                "   title character varying COLLATE pg_catalog.\"default\" NOT NULL,"+
                "   genres character varying COLLATE pg_catalog.\"default\","+
                "   global_avg_rt real,"+
                "   popularity integer,"+
                "   non_personalized_score real,"+
                "   CONSTRAINT movies_pkey PRIMARY KEY (movie_id))"+
                    "WITH ( OIDS = FALSE )" +
                    "TABLESPACE pg_default;" +
                    "ALTER TABLE public.movies OWNER to postgres;" +
                    
                "CREATE TABLE public.ratings("+
                "   user_id integer NOT NULL,"+
                "   movie_id integer NOT NULL,"+
                "   rating real NOT NULL,"+
                "   \"timestamp\" integer NOT NULL,"+
                "   is_history boolean,"+
                "   CONSTRAINT ratings_pkey PRIMARY KEY (user_id, movie_id),"+
                "   CONSTRAINT movie_id_fkey FOREIGN KEY (movie_id)"+
                "   REFERENCES public.movies (movie_id) MATCH SIMPLE"+
                "   ON UPDATE NO ACTION"+
                "   ON DELETE NO ACTION,"+
                "   CONSTRAINT user_id_fkey FOREIGN KEY (user_id)"+
                    "   REFERENCES public.users (user_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION )"+
                    "WITH ( OIDS = FALSE )" +
                    "TABLESPACE pg_default;" +
                    "ALTER TABLE public.ratings OWNER to postgres;" +
                        
                        
                "CREATE TABLE public.item_similarity("+
                "   item_x integer NOT NULL,"+
                "   item_y integer NOT NULL,"+
                "   similarity real NOT NULL,"+
                "   CONSTRAINT item_similarity_pkey PRIMARY KEY (item_x, item_y),"+
                "   CONSTRAINT \"iFkey\" FOREIGN KEY (item_x)"+
                    "   REFERENCES public.movies (movie_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION,"+
                "   CONSTRAINT \"jFkey\" FOREIGN KEY (item_y)"+
                    "   REFERENCES public.movies (movie_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION )"+
                    "WITH ( OIDS = FALSE )" +
                    "TABLESPACE pg_default;" +
                    "ALTER TABLE public.item_similarity OWNER to postgres;" +
                        
                        
                "CREATE TABLE public.user_similarity("+
                "   user_x integer NOT NULL,"+
                "   user_y integer NOT NULL,"+
                "   similarity real NOT NULL,"+
                "   CONSTRAINT similarity_weigth_pkey PRIMARY KEY (user_x, user_y),"+
                "   CONSTRAINT \"user_I_fkey\" FOREIGN KEY (user_x)"+
                    "   REFERENCES public.users (user_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION,"+
                "   CONSTRAINT \"user_J_fkey\" FOREIGN KEY (user_y)"+
                "   REFERENCES public.users (user_id) MATCH SIMPLE"+
                "   ON UPDATE NO ACTION"+
                "   ON DELETE NO ACTION)"+
                    "WITH ( OIDS = FALSE )" +
                    "TABLESPACE pg_default;" +
                    "ALTER TABLE public.user_similarity OWNER to postgres;" +
                        
                        
               "CREATE TABLE public.tags" +
                "(" +
                "    tag_id integer NOT NULL," +
                "    tag character varying COLLATE pg_catalog.\"default\" NOT NULL," +
                "    tag_popularity integer," +
                "    CONSTRAINT tags_genome_pkey PRIMARY KEY (tag_id)" +
                ")" +
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.tags OWNER to postgres;" +
                        
                "CREATE TABLE public.item_vector" +
                "(" +
                "    movie_id integer NOT NULL," +
                "    tag_id integer NOT NULL," +
                "    relevance real NOT NULL," +
                "   CONSTRAINT tag_relevance_pkey PRIMARY KEY (movie_id, tag_id),"+
                "   CONSTRAINT movie_id_fk FOREIGN KEY (movie_id)"+
                    "   REFERENCES public.movies (movie_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION,"+
                "   CONSTRAINT tag_id_fk FOREIGN KEY (tag_id)"+
                    "   REFERENCES public.tags (tag_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION"+
                ")" +
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.item_vector OWNER to postgres;" +
                        
                        
                "CREATE TABLE public.user_vector" +
                "(" +
                "    user_id integer NOT NULL," +
                "    tag_id integer NOT NULL," +
                "    relevance real NOT NULL," +
                "   CONSTRAINT user_vector_space_pkey PRIMARY KEY (user_id, tag_id),"+
                "   CONSTRAINT tag_id_fkey FOREIGN KEY (tag_id)"+
                    "   REFERENCES public.tags (tag_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION,"+
                "   CONSTRAINT user_id_fkey FOREIGN KEY (user_id)"+
                    "   REFERENCES public.users (user_id) MATCH SIMPLE"+
                    "   ON UPDATE NO ACTION"+
                    "   ON DELETE NO ACTION"+
                ")" +
                "WITH ( OIDS = FALSE )" +
                "TABLESPACE pg_default;" +
                "ALTER TABLE public.user_vector OWNER to postgres;"
                        
            );
                        
                        
        } catch (SQLException ex) {
            Logger.getLogger(DataBase.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    public int importData(String table, String folder, String file_name, char delimiter, String columns){
        
                
        String OpSys = System.getProperty("os.name");
        String slash = (OpSys.equals("Linux")) ? "/" : "\\";
        
        if(!columns.equals("")) columns = "("+columns+")";
        
        String sql = "COPY " + table + columns + " FROM stdin DELIMITER '" + delimiter +"'";

        //st.executeUpdate("copy movies(movie_id, title, genres) from '" + folder + slash + file_name +"' delimiter '" + delimiter +"';");

        try {

            BaseConnection pgcon = (BaseConnection)dbCon;
            CopyManager mgr = new CopyManager(pgcon);
            Reader in = new BufferedReader( new FileReader(new File(folder + slash + file_name)) );
            long rows  = mgr.copyIn(sql, in);
            return (int)rows;
            
        } catch (SQLException | IOException ex) {
            Logger.getLogger(Groundwork.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return -1;
            
    }
    
    public void dropConstraint(String table, String key){
        
        try {
            Statement st = dbCon.createStatement();
            st.executeUpdate("ALTER TABLE " + table + " DROP CONSTRAINT " + key + ";");
        } catch (SQLException ex) {
            Logger.getLogger(Groundwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addForeignKey(String table, String fk_name, String column_reference, String table_reference, String column_tab_reference){
        
         try {
            Statement st = dbCon.createStatement();
            st.executeUpdate("ALTER TABLE "+table+" ADD CONSTRAINT "+fk_name+" FOREIGN KEY ("+column_reference+") "
                    + "REFERENCES "+table_reference+" ("+column_tab_reference+") MATCH SIMPLE;");
        } catch (SQLException ex) {
            Logger.getLogger(Groundwork.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
     
    
}
