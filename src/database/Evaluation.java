package database;

import evaluator.Factor;
import evaluator.RespVarEnum;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Methods to deal specifically with the Evaluation database. 
 * Evaluation is intent to store the results of the experiments
 * @author Paulo
 */
public class Evaluation extends DataBase {
    
    private static Evaluation instance = new Evaluation("Evaluation");
    
    private Evaluation(String dbName){
        super(dbName);
    }
    
    public static Evaluation getInstance(){
        
        if(instance.dbCon == null)
            instance.dbCon = instance.open();
        
        return instance;
    }
           
    public ResultSet getExperiments(int evaluation_id, int rv_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select experiment_id, rv_value, standard_deviation, confidence_interval_amp, rv_name from experiments " +
                                "join response_variables on experiments.rv_id = response_variables.rv_id " +
                                "where evaluation_id = "+evaluation_id+" and experiments.rv_id = "+rv_id);
        
    }
    
   
    public ResultSet getExperimentPair(int evaluation_id1, int evaluation_id2, int rv_id, String id_filter) throws SQLException{
        
        String where = (id_filter.equals("")) ? " " : " and experiment_id in "+id_filter;
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select evaluation_name, experiments.evaluation_id, experiment_id, rv_value, standard_deviation, confidence_interval_amp, rv_name from experiments " +
                                "join response_variables on experiments.rv_id = response_variables.rv_id " +
                                "join evaluations on evaluations.evaluation_id = experiments.evaluation_id " +
                                "where (experiments.evaluation_id = "+evaluation_id1+" or experiments.evaluation_id = "+evaluation_id2+") "
                              + "and experiments.rv_id = "+rv_id + where + " order by experiment_id"
        );
        
    }
    
    public int sizeOfResponseVariables() throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet ftSet = st.executeQuery("select count(rv_id) from response_variables");
        ftSet.next();
        return ftSet.getInt("count");
    }
    
    public void insertInResponseVariables(int rv_id, String rv_name, String y_axis) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("insert into response_variables(rv_id, rv_name, y_axis) values(" + rv_id + ",'" + rv_name + "','" + y_axis + "')");
    }
    
    public void saveExperiment(int eval_id, int exp_id, int rv_id, float mean, float std_dev, float conf_int) throws SQLException{
        
        Statement st = dbCon.createStatement();
        
        st.executeUpdate("INSERT INTO experiments VALUES ("+eval_id+","+exp_id+","+rv_id+","+mean+","+std_dev+","+conf_int+");");
        
    }
    
    public void updateResponseVariables(int rv_id, String rv_name, String y_axis) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update response_variables set rv_name = '" + rv_name + "', y_axis = '"+y_axis+"' where rv_id = " + rv_id);
    }
    
    public int sizeOfFactorTypes() throws SQLException{
        
        Statement st = dbCon.createStatement();
        ResultSet ftSet = st.executeQuery("select count(factor_id) from factors");
        ftSet.next();
        return ftSet.getInt("count");
    }
    
    public void insertInFactorTypes(int ft_id, String ft_name) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("insert into factors(factor_id, factor_name) values(" +ft_id + ",'" + ft_name + "')");
    }
    
    public void updateFactorTypes(int ft_id, String ft_name) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("update factors set factor_name = '" + ft_name + "' where factor_id = " + ft_id);
    }
    
    public ResultSet getExperiment(int eval_id, int resp_var_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select * from experiments where evaluation_id = " + eval_id + " and rv_id = " + resp_var_id 
                               + " order by experiment_id"
        );
        
    }
    
    public ResultSet getExperimentIDs(int eval_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select experiment_id, rv_value from experiments where evaluation_id = "+eval_id+""
                + " and rv_id = " + RespVarEnum.T_TIME_NO_QUEUE.value);
        
    }
    
    public ResultSet getTimeClasses(int evaluation_id, int experiment_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select etc.time_class_id, etc.time_class_value, tc.lower_bound, tc.upper_bound from experiment_x_time_classes as etc " +
                "join time_classes as tc on tc.class_id = etc.time_class_id " + 
                "where evaluation_id = "+evaluation_id + " and experiment_id = " + experiment_id);
        
    }
    
    public ResultSet getResponseVariables(int eval_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select distinct experiments.rv_id, r.rv_name, r.y_axis from experiments " +
                               "join response_variables as r on r.rv_id = experiments.rv_id " + 
                               "where evaluation_id = "+eval_id
        );
        
    }
    
    public void saveFactor(int evaluation_id, int factor_id, String level_1, String level_2) throws SQLException{
        
        Statement st = dbCon.createStatement();
        
        st.executeUpdate("insert into evaluation_x_factors (evaluation_id, factor_id, level_1, level_2) " +
            " values ( " +evaluation_id+ "," +factor_id+ ",'" +level_1+ "','" +level_2+ "')"
        );
            
    }
    
    public void saveComposedFactor(int evaluation_id, int factor_id, 
                                    String level_1, String level_2, int composed_factor) throws SQLException{
        
        Statement st = dbCon.createStatement();
        
        st.executeUpdate("insert into evaluation_x_factors (evaluation_id, factor_id, level_1, level_2, composed_factor_id) " +
                " values ( " +evaluation_id+ "," +factor_id+ ",'" +level_1+ "','" +level_2+ "'," +composed_factor+  ")"
        );
            
    }
    
    public int saveEvaluation(String ev_name, float conf_interv) throws SQLException{
        
        Statement st = dbCon.createStatement();
        
        ResultSet ins = st.executeQuery("insert into evaluations (evaluation_name, confidence_interval, multi_lvl_step) "
                    + "values ('"+ev_name+"',"+conf_interv+", "+Factor.getStep()+") returning evaluation_id;");
            
        ins.next();
        return ins.getInt("evaluation_id");
            
    }
    
    public ResultSet getFactors(int eval_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select * from evaluation_x_factors "
                + "join factors on evaluation_x_factors.factor_id = factors.factor_id "
                + "where evaluation_id = " + eval_id + " order by ins_order" );
        
    }
    
    public void saveTClasses(int evaluation_id, int experiment_id, int class_id, float class_value) throws SQLException{
        
        Statement st = dbCon.createStatement();
        st.executeUpdate("insert into experiment_x_time_classes VALUES ("+evaluation_id+", "+experiment_id+", "+class_id+", "+class_value+");");
    }
    
    public ResultSet getEvaluation(int eval_id) throws SQLException{
        
        Statement st = dbCon.createStatement();
        return st.executeQuery("select * from evaluations where evaluation_id = " + eval_id );
        
    }
    
}
