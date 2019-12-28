package evaluator;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import database.Evaluation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.util.HashMap;
import recommender.algorithm.*;
import recommender.factory.*;

import utils.Config;
import static utils.Config.DIST_TYPE;
import static utils.Config.RECOMMENDER;
import static utils.Config.TIME_RANGE;
import database.skeleton.CollabFiltDBSkeleton;
import database.factory.CollabFiltDBSkeletonFactory;
import database.skeleton.ContentBasedDBSkeleton;
import database.factory.ContentBasedDBSkeletonFactory;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import static evaluator.FactorTypesEnum.*;
import java.util.LinkedList;
import java.util.Queue;
import org.apache.commons.math3.distribution.TDistribution;
import static recommender.algorithm.Recommender.NO_RECOMMENDATION;
import utils.User;
import utils.Utils;

/**
 * @author Paulo
 */
public class Benchmarker {
    
    private int level;
    private float confidence_interval;
    private int number_of_replicas;
    private int number_of_experiments;
    private float max_conf_interv_amp;
    private float max_st_dev;
    private int evaluation_id;
    
    public ArrayList<Float> time_list;
    private Queue< User > arrival_queue;
    private List<Factor> factors;
    private ArrayList <Integer> test_set;
    private Map<RespVarEnum, ArrayList> response_map;
    private Map<Integer, Float> time_classes;

    private Benchmarker(){}
    
    private static Benchmarker instance = new Benchmarker();
   
    /*=================================================*
    *               Getters And Setters                *
    *==================================================*/
    
    public static Benchmarker getInstance(){ return instance; }
        
    public void settings(float confidence_interval, int evaluation_id){
        
        this.level = Factor.getLevel();
        this.confidence_interval = confidence_interval;
        max_conf_interv_amp = 1000;
        max_st_dev = 1000;
        this.evaluation_id = evaluation_id;
        
        response_map = new HashMap<>();
                
        test_set = new ArrayList<>();
        time_list = new ArrayList<>();
        time_classes = new HashMap<>();
        
        arrival_queue = new LinkedList<>();
        
        RespVarEnum[] response_var = RespVarEnum.values();
        for(RespVarEnum resp : response_var){
            response_map.put(resp, new ArrayList<>());
        }
        
        factors = new ArrayList<>();
        
    }

    public boolean addFactor(Factor f){ return factors.add(f); }
    
    public Queue<User> getArrivalQueue(){ return arrival_queue; }
    
    public Map<Integer, Float> getTimeClasses(){ return time_classes; }
    
    public int getLevel(){ return level; }
    
    public int getEvaluationID(){ return evaluation_id; }
    
    public List<Factor> getFactors(){ return factors; }
    
    public void setNumberOfReplicas(int n_of_replicas){ 
        
        number_of_replicas = n_of_replicas; 
    
    }
    
    public ArrayList<Integer> getTestSet(){ return test_set; }
    
    // returns the first n users of test_set. They represent the workload that will be used
    public List<Integer> getTestSetFromWorkload(int nof_users){ return test_set.subList(0, nof_users); }
    
    public void setTestSet(Map< Integer,  Collection<Integer> > user_map, int piece, boolean shuffle){
        
        test_set = (ArrayList <Integer>) user_map.get(piece); // k fold
        if(shuffle == true)
            Collections.shuffle(test_set); 
        
    }
    
    public static Map< Integer,  Collection<Integer> > getAllUsers(int split_rate) throws SQLException{
        
        int size = 0, counter = 0, idx = 0;
        Map< Integer,  Collection<Integer> > map = new HashMap<>();
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet rSet = gen.getAllUsersRandom();
        
        if(rSet.last()){
            size = rSet.getRow();
            rSet.beforeFirst();
        }
        
        split_rate = (int) Math.ceil (size / (float) split_rate);
        ArrayList<Integer> list = new ArrayList<>();
        
        while (rSet.next()){
            
            list.add(rSet.getInt(gen.getUserIDLabel()));
            counter++;
            
            if(counter % split_rate == 0 || rSet.isLast()){ 
                map.put(++idx, list);
                list = new ArrayList<>();
            }
        }
        
        return map;
               
    }    
        
    /*=================================================*
    *                    Experiments                   *
    *==================================================*/
    
    /**
     * Find the ideal number of replicas. This operation may take a lot of time
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException 
     */
    public void pilotExperiment() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
        
        number_of_replicas -= 4; // descontar a primeira
        
        do{
            
            number_of_replicas += 4;
            experiment(false);
            
            System.out.println("Exp feito com " + number_of_replicas + " repetições");
            System.out.println("Intervalo alcançado: " + max_conf_interv_amp );
            
            
        }while( (max_conf_interv_amp > 0.16) && (number_of_replicas < 25) );
        
        System.out.println("Repetições ideal = " + number_of_replicas);
        System.out.println("Intervalo Atual = " + max_conf_interv_amp);
        System.out.println("=================================================================================================");
        
    }
    

    /**
     * Calculation of the number of replicas version 2
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws SQLException 
     */
    public void pilotExperiment2() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException{
        
        float max_error = 0.14f;
        float limit = max_error * 0.02f; // 2% security limit
        int step = 3;
        
        experiment(false);
        
        while(max_conf_interv_amp > (max_error - limit) ){
            
            System.out.println("\n\nExp. feito com "+ number_of_replicas +" réplicas");
            System.out.println("Intervalo alcançado: " + max_conf_interv_amp );
            System.out.println("Desvio Padrão Alcansado: " + max_st_dev );
            
            TDistribution t = new TDistribution(number_of_replicas - 1);
            float student_t = (float)t.inverseCumulativeProbability (1 - (1 - confidence_interval)/2 );
            int new_replicas = (int) Math.ceil( Math.pow((student_t * max_st_dev/(max_error - limit)), 2) );
            System.out.println("Número calculado de New Réplicas: "+new_replicas);
            
            number_of_replicas = (new_replicas < number_of_replicas) ? number_of_replicas + step : new_replicas;
            
            System.out.println("\n\n\n============================================================");
            System.out.println("Novo experimento com " + number_of_replicas +" réplicas");
            
            experiment(false);
            
        }
        
        System.out.println("Número de réplicas alcansado!!!!!: " + number_of_replicas);
        System.out.println("Exp feito com " + number_of_replicas + " repetições");
        System.out.println("Intervalo alcançado: " + max_conf_interv_amp );
        System.out.println("=================================================================================================");
        
    }
    
    public void warmUp() throws SQLException{

        try {

            System.out.println("Starting Warm Up...");

            int range_backup = Config.TIME_RANGE;
            Config.TIME_RANGE = 7;

            Factor carga = Factor.fillFactor("50", "50", T_WORKLOAD);
            this.addFactor(carga);
           // Factor list_size = Config.fillFactor("6", "18", T_RECOMMENDATION_LIST_LENGTH);
            //this.addFactor(list_size);  
            T_ALTERNATIVE_RECOMMENDATION.setDefault("false");
            Factor qtd_items = Factor.fillFactor("15", "30", T_CANDIDATES_SIZE);
            //Factor qtd_viz = Config.fillFactor("6", "18", T_NEIGHBOORHOOD_SIZE);
           // qtd_viz.compose(qtd_items);

            this.addFactor(qtd_items);
            T_NEIGHBOORHOOD_SIZE.setDefault("15");
            T_RECOMMENDATION_LIST_LENGTH.setDefault("10");

            Config.setResponseVariables();
            this.setNumberOfReplicas(4);
            this.experiment(false);

            //this.getFactors().clear(); 
            this.settings(confidence_interval, evaluation_id); // to clear structures
            Config.TIME_RANGE = range_backup;

            System.out.println("Warm Up Finished.\n\n\n\n");

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Benchmarker.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    public void multiLvlWarmup() throws SQLException{
        
        try {

            System.out.println("Starting Warm Up...");

            int range_backup = Config.TIME_RANGE;
            Config.TIME_RANGE = 7;

            Factor lista = Factor.createMultiLevelFactor(T_RECOMMENDATION_LIST_LENGTH);
            T_WORKLOAD.setDefault("30");
            T_ALTERNATIVE_RECOMMENDATION.setDefault("false");
            T_NEIGHBOORHOOD_SIZE.setDefault("25");
            T_CANDIDATES_SIZE.setDefault("30");
            this.addFactor(lista);
        
            Config.setResponseVariables();
            this.setNumberOfReplicas(4);
            this.oneFacMultiLvlExp(false);

            //this.getFactors().clear(); 
            this.settings(confidence_interval, evaluation_id); // to clear structures
            Config.TIME_RANGE = range_backup;

            System.out.println("Warm Up Finished.\n\n\n\n");

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            Logger.getLogger(Benchmarker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
        
    public void experiment(boolean official_exp) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        
        if(number_of_replicas < 2)
            throw new RuntimeException("Number of Replicas  must be at least 2.");
        
        number_of_experiments = (int) Math.pow(level, factors.size());
        ArrayList<Float> ciamp = new ArrayList<>();
        ArrayList<Float> st_dev = new ArrayList<>();
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        int total_users = gen.getTotalNOfUsers();
        ResultSet pop = gen.getItemPopularity(); // for novelty
        Map<Integer, Integer> item_popularity = new HashMap<>();
        while(pop.next())
            item_popularity.put( pop.getInt( gen.getItemIDLabel() ), pop.getInt("count") );
        
        RespVarEnum[] response_var = RespVarEnum.values();
        
        CollabFiltDBSkeleton collab = CollabFiltDBSkeletonFactory.getInstance();
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        
        Recommender.setGenDB(gen);
        Recommender.setCollabDB(collab);
        Recommender.setContentBasedDB(cbased);
        
        for(Integer k = 0; k < number_of_experiments; k++){ // each iteraction denotes an experiment
            
            //if(official_exp)
                Utils.printExperiment(k+1, 1);
            
            blend(k); // determine the actual combination of factors

            String alternative_rec = !getCurrent(T_ALTERNATIVE_RECOMMENDATION, true).equals("-10000") ? getCurrent(T_ALTERNATIVE_RECOMMENDATION, true) : T_ALTERNATIVE_RECOMMENDATION.default_value;
            int workload = Integer.parseInt( !getCurrent(T_WORKLOAD, true).equals("-10000") ? getCurrent(T_WORKLOAD, true) : T_WORKLOAD.default_value);
            int neighbors = Integer.parseInt( !getCurrent(T_NEIGHBOORHOOD_SIZE, true).equals("-10000") ? getCurrent(T_NEIGHBOORHOOD_SIZE, true) : T_NEIGHBOORHOOD_SIZE.default_value);
            int candidates = Integer.parseInt( !getCurrent(T_CANDIDATES_SIZE, true).equals("-10000") ? getCurrent(T_CANDIDATES_SIZE, true) : T_CANDIDATES_SIZE.default_value);
            int rec_list_length = Integer.parseInt( !getCurrent(T_RECOMMENDATION_LIST_LENGTH, true).equals("-10000") ? getCurrent(T_RECOMMENDATION_LIST_LENGTH, true) : T_RECOMMENDATION_LIST_LENGTH.default_value);
            
            RecommenderFactory recommender_factory = (RecommenderFactory) Class.forName("recommender.factory." + RECOMMENDER + "Factory").newInstance();
            Recommender recommender = recommender_factory.makeRecommender(candidates, neighbors, null, rec_list_length);
            recommender.setAlternativeScore(alternative_rec);
                            
            try {
                
                int k_fold = 5;
                Map< Integer,  Collection<Integer> > user_map = getAllUsers(k_fold);
                int max_size = user_map.get(k_fold).size();
               
                if(workload > max_size) 
                    throw new RuntimeException("Max size of workload is " + max_size);
               
                // replicates each one of the k number_of_experiments
                for(int j = 0; j < number_of_replicas; j++){
                    
                    int max_q = 0;
                    
                    if(official_exp) Utils.printExperiment(j+1, 2);

                    setTestSet(user_map, (j % k_fold)+1 , true); 
                    Table<Integer, Integer, Float> recommendation_table = HashBasedTable.create(); // keys: user_id, movie_id; values: predictions
                    
                    long log_t_start = System.currentTimeMillis();
                    // simulates users arriving in different times
                    TimeDist t_dist = new TimeDist(workload, test_set, DIST_TYPE, TIME_RANGE);
                    Thread dist = new Thread( t_dist );
                    ResponseVariable rv = new ResponseVariable();

                    boolean not_empty;
                    dist.start();

                    while( (not_empty = !arrival_queue.isEmpty()) || dist.isAlive()){

                        if(not_empty){
                            
                            User usr = arrival_queue.peek(); 
                            rv.addTimeInQueue((float) (System.currentTimeMillis() - usr.getArrivalTime()));
                            
                            double time = System.currentTimeMillis();
                            Map<Integer, Float> recommendation_list;
                            
                            recommender.setUser(usr); 
                          
                           if( ( recommendation_list = recommender.recommend() ) != null){
                                
                                double final_time = System.currentTimeMillis();
                                time_list.add((float) (final_time - time));
                                rv.addResponseTime((float) (final_time - usr.getArrivalTime())); 
                                
                                recommendation_list.forEach((item_id, prediction) -> { // filling the tables user, item and prediction
                                    recommendation_table.put(usr.getID(), item_id, prediction);
                                });
                            }
                           
                            arrival_queue.poll();
                            int size = arrival_queue.size();
                            max_q = (size > max_q) ? size : max_q; 
                            
                        }

                    }
                    
                    
                    /*========= Cálculo das Variáveis de Resposta ======== */
                    List<Integer> test_set_fwkl = getTestSetFromWorkload(workload);
                    //System.out.println("Usuários: " + test_set_fwkl);
                    rv.createUserRatingsTable( test_set_fwkl ); 
                                       
                    float var_resp = NO_RECOMMENDATION;
                    
                    for(RespVarEnum resp : response_var){
                     
                        if(resp.awake){
                            
                            var_resp = resp.measure(rv, recommendation_table, test_set_fwkl, workload, rec_list_length, total_users, item_popularity, time_list);
                            if( var_resp != NO_RECOMMENDATION)
                                   response_map.get(resp).add(var_resp); 
                            
                        }
                    }
                                        
                    long fin = (System.currentTimeMillis() - log_t_start);
                    System.out.println("Tempo de execução dessa repetição: " + (fin/1000f) + " segundos");

                    time_list.clear();

                  //  System.out.println(rv.getTimeInQueue());
                  //  System.out.println(rv.getResponseTime());
                  //  System.out.println(time_classes+"\n");
                  //  System.out.println("Tamanho maximo da fila: " + max_q);

                }
                
                for(RespVarEnum resp : response_var){
                    
                    if(resp.awake){
                        
                        // System.out.println(resp.value);   System.out.println(resp);
                        
                        System.out.println(resp + "(IC = "+ConfIntervAmp(response_map.get(resp))+"): "+response_map.get(resp));
                        
                        if(resp.value < 3 || resp.value > 6 )
                        ciamp.add(ConfIntervAmp(response_map.get(resp))); 
                        
                        Float dev = StandardDeviation( Variance(response_map.get(resp)) ) / Mean(response_map.get(resp));
                        
                        if(resp.value < 3 || resp.value > 6 ){
                        if(!dev.isNaN())
                            st_dev.add(dev); 
                        }
                            
                        if(official_exp){
                            
                            Evaluation evaluation = Evaluation.getInstance();
                            persistResponseVariable(k, response_map.get(resp), resp.value, evaluation);

                            if(resp == RespVarEnum.T_TIME_W_QUEUE)
                                TimeDist.saveTimeClasses(evaluation_id, k, time_classes, evaluation, number_of_replicas);
                                
                        }

                        response_map.get(resp).clear();
                    }
                    
                }
                
                arrival_queue.clear();
                time_classes.clear();
                
            
            }catch(SQLException ex){
                Logger.getLogger(Benchmarker.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
        
        max_conf_interv_amp = Collections.max(ciamp);
        max_st_dev = Collections.max(st_dev);
        
    }
    
    public void oneFacMultiLvlExp(boolean official_exp) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        
        if(number_of_replicas < 2)
            throw new RuntimeException("Number of Replicas  must be at least 2.");
        
        number_of_experiments = (int) Math.pow(level, factors.size());
        ArrayList<Float> ciamp = new ArrayList<>();
        ArrayList<Float> st_dev = new ArrayList<>(); 
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        int total_users = gen.getTotalNOfUsers();
        
        
        ResultSet pop = gen.getItemPopularity(); // for novelty
        Map<Integer, Integer> item_popularity = new HashMap<>();
        while(pop.next())
            item_popularity.put( pop.getInt( gen.getItemIDLabel() ), pop.getInt("count") );
        
        
        RespVarEnum[] response_var = RespVarEnum.values();
        
        CollabFiltDBSkeleton collab = CollabFiltDBSkeletonFactory.getInstance();
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        
        Recommender.setGenDB(gen);
        Recommender.setCollabDB(collab);
        Recommender.setContentBasedDB(cbased);
        
        for(Integer k = 0; k < number_of_experiments; k++){ // each iteraction denotes an experiment
            
            //if(official_exp)
                Utils.printExperiment(k+1, 1);
            
            String alternative_rec = !getCurrentOneFac(T_ALTERNATIVE_RECOMMENDATION, k).equals("-10000") ? getCurrentOneFac(T_ALTERNATIVE_RECOMMENDATION, k) : T_ALTERNATIVE_RECOMMENDATION.default_value;
            int workload = Integer.parseInt( !getCurrentOneFac(T_WORKLOAD, k).equals("-10000") ? getCurrentOneFac(T_WORKLOAD, k) : T_WORKLOAD.default_value);
            int neighbors = Integer.parseInt( !getCurrentOneFac(T_NEIGHBOORHOOD_SIZE, k).equals("-10000") ? getCurrentOneFac(T_NEIGHBOORHOOD_SIZE, k) : T_NEIGHBOORHOOD_SIZE.default_value);
            int candidates = Integer.parseInt( !getCurrentOneFac(T_CANDIDATES_SIZE, k).equals("-10000") ? getCurrentOneFac(T_CANDIDATES_SIZE, k) : T_CANDIDATES_SIZE.default_value);
            int rec_list_length = Integer.parseInt( !getCurrentOneFac(T_RECOMMENDATION_LIST_LENGTH, k).equals("-10000") ? getCurrentOneFac(T_RECOMMENDATION_LIST_LENGTH, k) : T_RECOMMENDATION_LIST_LENGTH.default_value);
            
            RecommenderFactory recommender_factory = (RecommenderFactory) Class.forName("recommender.factory." + RECOMMENDER + "Factory").newInstance();
            Recommender recommender = recommender_factory.makeRecommender(candidates, neighbors, null, rec_list_length);
            recommender.setAlternativeScore(alternative_rec);
            
            System.out.println("Lista = "+ rec_list_length);
            
            // ******** NÃO MUDEI NADA DAQUI PRA FRENTE ************* só tirei prints
                            
            try {
                
                int k_fold = 5;
                Map< Integer,  Collection<Integer> > user_map = getAllUsers(k_fold);
                int max_size = user_map.get(k_fold).size();
               
                if(workload > max_size) 
                    throw new RuntimeException("Max size of workload is " + max_size);
                
                for(int j = 0; j < number_of_replicas; j++){
                    
                    int max_q = 0;
                    
                    if(official_exp) Utils.printExperiment(j+1, 2);

                    setTestSet(user_map, (j % k_fold)+1 , true); 
                    Table<Integer, Integer, Float> recommendation_table = HashBasedTable.create(); // keys: user_id, movie_id; values: predictions
                    
                    long log_t_start = System.currentTimeMillis();
                    // simula usuarios chegando em tempos diferentes
                    TimeDist t_dist = new TimeDist(workload, test_set, DIST_TYPE, TIME_RANGE);
                    Thread dist = new Thread( t_dist );
                    ResponseVariable rv = new ResponseVariable();

                    boolean not_empty;
                    dist.start();

                    while( (not_empty = !arrival_queue.isEmpty()) || dist.isAlive()){

                        if(not_empty){ 
                            
                            User usr = arrival_queue.peek(); 
                            rv.addTimeInQueue((float) (System.currentTimeMillis() - usr.getArrivalTime()));
                            
                            double time = System.currentTimeMillis();
                            Map<Integer, Float> recommendation_list;
                            
                            recommender.setUser(usr); 
                            
                           if( ( recommendation_list = recommender.recommend() ) != null){
                                
                                double final_time = System.currentTimeMillis();
                                time_list.add((float) (final_time - time));
                                rv.addResponseTime((float) (final_time - usr.getArrivalTime())); 
                                
                                recommendation_list.forEach((item_id, prediction) -> {
                                    recommendation_table.put(usr.getID(), item_id, prediction);
                                });
                            }
                           
                            arrival_queue.poll();
                            int size = arrival_queue.size();
                            max_q = (size > max_q) ? size : max_q; 
                            
                        }

                    }
                    
                    
                    /*========= Cálculo das Variáveis de Resposta ========*/
                    List<Integer> test_set_fwkl = getTestSetFromWorkload(workload);
                    rv.createUserRatingsTable( test_set_fwkl ); 
                                       
                    float var_resp = NO_RECOMMENDATION;
                    
                    for(RespVarEnum resp : response_var){
                     
                        if(resp.awake){
                            
                            var_resp = resp.measure(rv, recommendation_table, test_set_fwkl, workload, rec_list_length, total_users, item_popularity, time_list);
                            if( var_resp != NO_RECOMMENDATION)
                                   response_map.get(resp).add(var_resp); 
                            
                        }
                    }
                                        
                    long fin = (System.currentTimeMillis() - log_t_start);
                    System.out.println("Tempo de execução dessa repetição: " + (fin/1000f) + " segundos");

                    time_list.clear();

                   // System.out.println(rv.getTimeInQueue());
                  //  System.out.println(time_classes+"\n");
                  //  System.out.println("Tamanho maximo da fila: " + max_q);

                }
                
                for(RespVarEnum resp : response_var){
                    
                    if(resp.awake){
                        
                        System.out.println(resp + "(IC = "+ConfIntervAmp(response_map.get(resp))+"): "+response_map.get(resp));
                        ciamp.add(ConfIntervAmp(response_map.get(resp)));

                        Float dev = StandardDeviation( Variance(response_map.get(resp)) ) / Mean(response_map.get(resp));
                        if(!dev.isNaN())
                            st_dev.add(dev); 
                        
                        if(official_exp){
                            
                            Evaluation evaluation = Evaluation.getInstance();
                            persistResponseVariable(k, response_map.get(resp), resp.value, evaluation);

                            if(resp == RespVarEnum.T_TIME_W_QUEUE)
                                TimeDist.saveTimeClasses(evaluation_id, k, time_classes, evaluation, number_of_replicas);
                                
                        }

                        response_map.get(resp).clear();
                    }
                    
                }
                
                arrival_queue.clear();
                time_classes.clear();
                
            
            }catch(SQLException ex){
                Logger.getLogger(Benchmarker.class.getName()).log(Level.SEVERE, null, ex);
            }
        
        }
        
        max_conf_interv_amp = Collections.max(ciamp);  
        max_st_dev = Collections.max(st_dev);  
        System.out.println("maximo: " + max_conf_interv_amp);
        System.out.println("maximo: " + max_st_dev);
        
    }
    
    
    public void persistResponseVariable(int exp_id, ArrayList <Float> values, int rv_id, Evaluation evaluation){
        

        try{
            
            float std_dev = StandardDeviation( Variance(values) );
            float conf_int = ConfIntervAmp(values);
            float mean = Mean(values);  
            evaluation.saveExperiment(evaluation_id, exp_id, rv_id, mean, std_dev, conf_int);
            
        } catch (SQLException ex) {
            Logger.getLogger(Benchmarker.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    
    /*=================================================*
    *                    Statistics                    *
    *==================================================*/
    
    // If the confidence interval includes or crosses (1), 
    // then there is insufficient evidence to conclude that the groups are statistically significantly different
    public float ConfIntervAmp(ArrayList<Float> values){
        
        float std_dev = StandardDeviation( Variance(values) );
        
        TDistribution t = new TDistribution(number_of_replicas - 1);
        float student_t = (float)t.inverseCumulativeProbability (1 - (1 - confidence_interval)/2 );
        
        float H = (float) (student_t * std_dev/Math.sqrt(values.size()));
        float mean = Mean(values);
        
        return (mean == 0) ? 0 : (2 * H/mean);
        
    }
    
    public float StandardDeviation(float variance){
        return (float) Math.sqrt(variance);
    }
    
    public float Mean(ArrayList<Float> values ){
        
        int n = values.size();
        float sum = 0;
        
        for(float v : values)
            sum += v;
        
        return (n == 0) ? 0 : sum / n;
    }
    
    public float Variance(ArrayList<Float> values){
        
        int n = values.size();
        float sum = 0, avg;
        avg = Mean(values);
        
        for(float v : values)
            sum += Math.pow( v - avg, 2 );
        
        return (n-1 == 0) ? 0 : sum /(n-1);
                
    }
    

    /*=================================================*
    *               Factors Manipulation               *
    *==================================================*/
   
    // combination for the different possiblities of factors
    public void blend(Integer m){
        
        Byte b = m.byteValue();
        int i, top = factors.size() - 1;
        
        for(i = top; i >= 0 ; i-- ){
            
            Factor f = factors.get(top - i); // order A,B,C. get(i) does inverse order 
            
            if( ((b >> i) & 1) == 0 )
                f.setCurrentVariation(1);
            else
                f.setCurrentVariation(-1);
                
            //System.out.print((b >> i) & 1 );
        }

    }
       
    public String getCurrent(FactorTypesEnum factor_type, boolean valueOrVariation){
       
        int size = factors.size(), i;
        String returned;
        
        for(i = 0; i < size; i++ ){
            
            Factor f = factors.get(i);
            returned = getCurrentAux(f, factor_type, valueOrVariation);
            
            if(!returned.equals("-10000"))
                return returned;
            
        }
        
        return "-10000";
        
    }
    
    public String getCurrentOneFac(FactorTypesEnum factor_type, int it){
        
        int size = factors.size(), i;
       
        for(i = 0; i < size; i++ ){
            
            Factor f = factors.get(i);
            if(f.getFactorType() == factor_type)
                return f.getValues(it);
            
        }
        
        return "-10000";
        
    }
    
    private String getCurrentAux(Factor f, FactorTypesEnum factor_type, boolean valueOrVariation){
        
        if(f.getFactorType() == factor_type)
           return (valueOrVariation) ? f.getCurrentValue() : String.valueOf( f.getCurrentVariation() );
            
        if(f.getComposedFactor() != null)
            return getCurrentAux(f.getComposedFactor(), factor_type, valueOrVariation);
        
        return "-10000";
                
    }
    
    public void persistFactors(){

        try{

           for(Factor f : factors)
                        saveFactor(f);

        }catch (SQLException ex) {
             Logger.getLogger(Benchmarker.class.getName()).log(Level.SEVERE, null, ex);
        }
       
       
    }
    
    private boolean saveFactor(Factor f) throws SQLException{
        
        Evaluation evaluation = Evaluation.getInstance();
        
        if(f.getComposedFactor() == null){
            
            evaluation.saveFactor(evaluation_id, f.getFactorType().value, f.getValues(0), f.getValues(1));
            
        }else{
            
            evaluation.saveComposedFactor(evaluation_id, f.getFactorType().value, 
                                            f.getValues(0), f.getValues(1), f.getComposedFactor().getFactorType().value);
            
            saveFactor(f.getComposedFactor());
            
        }
        
        return true;
        
        
        
    }
    
}
