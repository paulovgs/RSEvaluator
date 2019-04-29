package utils;

import evaluator.Benchmarker;
import evaluator.Factor;
import evaluator.FactorTypesEnum;
import static evaluator.FactorTypesEnum.*;
import static evaluator.RespVarEnum.*;
import evaluator.TimeDistEnum;

/**
 * Set RSE configurations. Will be used in the whole lifecicle of the performance evaluation.
 * @author Paulo
 */
public class Config {
    
    /**
     * Generic database access configuration
     * DB_PASS = Database password
     * DB_URL = Database url
     * DB_USERNAME = Database username
     */
    public static final String DB_PASS = "pgstudent";
    public static final String DB_URL = "jdbc:postgresql://localhost:5432/";
    public static final String DB_USERNAME = "postgres";
    
    /**
     * Related to the database that will be evaluated
     * DB_NAME = Name of the database (i.e. MovieLens10M; R2Yahoo).
     * USER_ID = Label of user ID field in the DB_NAME database (i.e. user_id; person_id)
     * ITEM_ID = Label of item ID field in the DB_NAME database (i.e. movie_id; song_id)
     * ITEM_TABLE = Table of items name (i.e. movies; songs)
     */
    public static final String DB_NAME = "MovieLens10M";
    public static final String USER_ID = "user_id";
    public static final String ITEM_ID = "movie_id";
    public static final String ITEM_TABLE = "movies";
    
    /**
     * 
     */
    public static final String GEN_CLASS = "Generic";
    public static final String CF_CLASS = "CollabFiltDB";
    public static final String CB_CLASS = "ContentBasedDB";
    
    public static final int STORAGED_CORRELATION = 2000;

    public static final String RECOMMENDER = "UserUserCF"; 

   // public static final TimeDistEnum DIST_TYPE = TimeDistEnum.T_ALL_AT_ONCE;
    public static final TimeDistEnum DIST_TYPE = TimeDistEnum.T_NORMAL_DIST;
    public static int TIME_RANGE = 400;

    public static final float ACCEPTABLE_VALUE = (float) 6.5; // valor aceito como relevante
    public static final float PREDICTION_LIMIT = (float) 3; // limita a lista de recomendação para valores maiores que esse
    public static final float MAX_SCALE = 10; // valor máximo da escala, util para escalar em alguns recomendadores
    public static final float CONFIDENCE_INTERV = (float) 0.9;


// public static final String RECOMMENDER = "ItemItemCF"; 
  //  public static final String RECOMMENDER = "ContentBased"; 
    //public static final String RECOMMENDER = "Hybrid"; 
    //    public static final TimeDistEnum DIST_TYPE = TimeDistEnum.T_NORMAL_DIST;
   
    public static final int NON_PER_LIMIT = 1000;
    
    // invalid number constant
    public static final int INVALID_NUMBER = -1;
    
    
    public static final int MULTI_LVL_MIN = 3; 
    public static final int MULTI_LVL_MAX = 15;
    public static final int MULTI_LVL_STEP = 2;
    
    public static void setFactors(Benchmarker bcmk){
               
        /*
        Factor lista = Factor.createMultiLevelFactor(T_RECOMMENDATION_LIST_LENGTH);
        T_WORKLOAD.setDefault("2500");
        T_ALTERNATIVE_RECOMMENDATION.setDefault("false");
        T_NEIGHBOORHOOD_SIZE.setDefault("25");
        T_CANDIDATES_SIZE.setDefault("30");
        
        
        bcmk.addFactor(lista);
        */
        
       // /*
       
        //Factor workload = fillFactor("300", "750", T_WORKLOAD);      
        Factor workload = fillFactor("30", "35", T_WORKLOAD);      
        Factor cand_alter = fillFactor("25", "40", T_CANDIDATES_SIZE);      
        Factor alternative = fillFactor("false", "true", T_ALTERNATIVE_RECOMMENDATION); 
        
        T_RECOMMENDATION_LIST_LENGTH.setDefault("10");
        T_NEIGHBOORHOOD_SIZE.setDefault("20");

        cand_alter.compose(alternative);
        
        bcmk.addFactor(workload);
        bcmk.addFactor(cand_alter);
        
       // */
        
    }
    
    // diz quais variáveis de resposta serão salvas
    public static void setResponseVariables(){

    //    T_RMSE_ERROR.awake = true;
        T_USER_COVERAGE.awake = true;
        T_TIME_W_QUEUE.awake = true;
    
        T_THROUGHPUT.awake = true;
      //  T_NORM_DCG.awake = true;
      //  T_ITEM_COVERAGE.awake = true;
      //  T_NOVELTY.awake = true;
        
        T_TIME_NO_QUEUE.awake = true;
        
     //   T_PRECISION_OVER_N.awake = true;
     //   T_RECALL_OVER_N.awake = true;
        
        
    }
    
    public static Factor fillFactor(String val1, String val2, FactorTypesEnum factor_type){
        
        String[] val = new String[2];
        val[0] = val1;
        val[1] = val2;
        return new Factor(factor_type, val);

    }
    
    
}
