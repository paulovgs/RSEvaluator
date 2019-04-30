package utils;

import evaluator.Benchmarker;
import evaluator.Factor;
import static evaluator.FactorTypesEnum.*;
import static evaluator.RespVarEnum.*;
import evaluator.TimeDistEnum;

/**
 * Set RSE configurations. Will be used in the whole life-cycle of the performance evaluation.
 * @author Paulo
 */
public class Config {
    
    /**
     * Invalid number constant
     */
    public static final int INVALID_NUMBER = -1;

    
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
     * DB_NAME = Name of the database (e.g. MovieLens10M; R2Yahoo).
     * USER_ID = Label of user ID field in the DB_NAME database (e.g. user_id; person_id)
     * ITEM_ID = Label of item ID field in the DB_NAME database (e.g. movie_id; song_id)
     * ITEM_TABLE = Table of items name (e.g. movies; songs)
     */
    public static final String DB_NAME = "MovieLens10M";
    public static final String USER_ID = "user_id";
    public static final String ITEM_ID = "movie_id";
    public static final String ITEM_TABLE = "movies";
    
    /**
     * You may have different database classes with different behaviors (but they need to extend the skeleton ones). 
     * So, you need to state wich one will be used in the current experiment
     * GEN_CLASS General use
     * CF_CLASS Specific for collaborative filtering algorithms
     * CB_CLASS Specific for content-based algorithms
     */
    public static final String GEN_CLASS = "Generic";
    public static final String CF_CLASS = "CollabFiltDB";
    public static final String CB_CLASS = "ContentBasedDB";
    
    
    /**
     * Data processing configurations
     * STORAGED_CORRELATION Number of pearson correlations between items/users that will be stored
     * NON_PER_LIMIT Top X items retrieved sorted by its non-personalized score. X will be the NON_PER_LIMIT
     * MAX_SCALE Max value of the ratings scale.
     */
    public static final int STORAGED_CORRELATION = 2000;
    public static final int NON_PER_LIMIT = 1000;

    
    /**
     * Experiment configurations
     * RECOMMENDER Algorithm to be used in the current experiment (e.g. UserUserCF; ItemItemCF; ContentBased; Hybrid)
     * DIST_TYPE Type of time distribution used (e.g. TimeDistEnum.T_ALL_AT_ONCE; TimeDistEnum.T_NORMAL_DIST)
     * TIME_RANGE Amount of time in seconds that the normal distribution will hold in each experiment
     * ACCEPTABLE_VALUE Significant values are those which are equal to or above the desired ACCEPTABLE_VALUE. Used in precisision and recall measures
     * PREDICTION_LIMIT Recommendation list will be pruned using this value
     * CONFIDENCE_INTERV The confidence interval of an experiment
     */
    public static final String RECOMMENDER = "UserUserCF"; 
    public static final TimeDistEnum DIST_TYPE = TimeDistEnum.T_NORMAL_DIST;
    public static int TIME_RANGE = 400;
    public static final float ACCEPTABLE_VALUE = (float) 6.5;
    public static final float PREDICTION_LIMIT = (float) 3; // limita a list de recomendação para valores maiores que esse
    public static final float MAX_SCALE = 10; // valor máximo da escala, util para escalar em alguns recomendadores
    public static final float CONFIDENCE_INTERV = (float) 0.9;


        
    /**
     * Choose the factors to be used in the multifactorial experiment.
     * Current types: T_WORKLOAD; T_CANDIDATES_SIZE; T_ALTERNATIVE_RECOMMENDATION; T_RECOMMENDATION_LIST_LENGTH; T_NEIGHBOORHOOD_SIZE
     * Use fillFactor to evaluate a specific factor. Otherwise, use setDefault
     * @param bcmk Instance of the Benchmarker class
     */
    public static void setFactors(Benchmarker bcmk){
               
        Factor workload = Factor.fillFactor("30", "35", T_WORKLOAD);      
        Factor cand_alter = Factor.fillFactor("25", "40", T_CANDIDATES_SIZE);      
        Factor alternative = Factor.fillFactor("false", "true", T_ALTERNATIVE_RECOMMENDATION); 
        
        T_RECOMMENDATION_LIST_LENGTH.setDefault("10");
        T_NEIGHBOORHOOD_SIZE.setDefault("20");

        cand_alter.compose(alternative);
        bcmk.addFactor(workload);
        bcmk.addFactor(cand_alter);
        
    }
    
    /**
     * Multi-level experiment configurations
     * MULTI_LVL_MIN Initial value of the factor
     * MULTI_LVL_MAX Final value of the factor
     * MULTI_LVL_STEP Between MULTI_LVL_MIN and MULTI_LVL_MAX, the selected factor will assume multiple MULTI_LVL_STEP values,
     * starting from MULTI_LVL_MIN
     */
    public static final int MULTI_LVL_MIN = 3; 
    public static final int MULTI_LVL_MAX = 15;
    public static final int MULTI_LVL_STEP = 2;
    
    /**
     * Choose the various levels that the selected factor will have in the multi-level experiment
     * You can choose only one multi-level factor. Use setDefault for the other ones.
     * @param bcmk Instance of the Benchmarker class
     */
    public static void setMultiLevelFactor(Benchmarker bcmk){
               
        Factor list = Factor.createMultiLevelFactor(T_RECOMMENDATION_LIST_LENGTH);
        
        T_WORKLOAD.setDefault("2500");
        T_ALTERNATIVE_RECOMMENDATION.setDefault("false");
        T_NEIGHBOORHOOD_SIZE.setDefault("25");
        T_CANDIDATES_SIZE.setDefault("30");
                
        bcmk.addFactor(list);
        
    }
    
    /**
     * Choose which response variables will be used in experiment
     */
    public static void setResponseVariables(){

        T_USER_COVERAGE.awake = true;
        T_TIME_W_QUEUE.awake = true;    
        T_THROUGHPUT.awake = true;
        T_TIME_NO_QUEUE.awake = true;
        
    }
    
}