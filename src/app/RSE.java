package app;

import database.Evaluation;
import evaluator.Benchmarker;
import evaluator.Factor;
import evaluator.FactorTypesEnum;
import evaluator.ResponseVariable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import reports.BarChart;
import reports.FactorInfluence;
import reports.Histogram;
import reports.LineChart;
import utils.Config;
import static utils.Config.CONFIDENCE_INTERV;
import utils.ConsoleLogger;
import utils.Utils;

/**
 * Contains the main functionalities of RSE. Will be accessed from a user interface.
 * @author Paulo
 */
public class RSE {
    
    /**
     * Multi factorial experiment with two levels of variation each.
     * @param exp_name The name of the experiment
     * @param warmup Should the warmup method be used? True for use it, false otherwise
     * @param number_of_replicas The desired number of replicas. If 0, the framwork will try to find the ideal number of replicas. This operation may take a long time though.
     * @return Success/fail message
     */
    public String exp(String exp_name, boolean warmup, int number_of_replicas) {
        
        try{
            
            double ini = System.currentTimeMillis();
            
            FactorTypesEnum.persistFactorTypes();
            ResponseVariable.persistResponseVariables();
            
            Evaluation evaluation = Evaluation.getInstance();
            int id = evaluation.saveEvaluation(exp_name, CONFIDENCE_INTERV);
            
            Benchmarker bcmk = Benchmarker.getInstance(); 
            bcmk.settings(CONFIDENCE_INTERV, id);
            
            ConsoleLogger logger = new ConsoleLogger();
            logger.writeEntry("Evaluation id: "+id);

            if(warmup)
                bcmk.warmUp();
            
            Config.setFactors(bcmk);
            bcmk.persistFactors();
            Config.setResponseVariables(); // diz quais serão salvas

            if(number_of_replicas > 0){
                bcmk.setNumberOfReplicas(number_of_replicas);
            }else{ //bcmk.pilotExperiment();
                bcmk.setNumberOfReplicas(3); // initial replicas
                bcmk.pilotExperiment2();
            }
            
            bcmk.experiment(true);
            
            logger.writeEntry("Experimento Realizado Com Sucesso!!!!");
            logger.writeEntry("Tempo de execução: " + (System.currentTimeMillis() - ini)/1000 + " segundos");
            
            return "Experimento Realizado Com Sucesso!!!! Evaluation ID: "+id;

        }  catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            return "Error when running multi factorial experiment.";
        }
        
    }
    
    /**
     * Create and save histograms into the project folder.
     * @param evaluation_id The ID of the desired experiment.
     * @return Success/fail message
     */
    public String histo(int evaluation_id) {        
        return Histogram.generate(evaluation_id);
    }
    
    /**
     * Present results in a graphical way.
     * ToDo: The filter for comparative graphs must be dynamically choosed 
     * @param id The evaluation ID
     * @param id_comp The second evaluation ID. It is only used in comparative graphs.
     * @param opt 1 = Show results in bar graphs. 2 = Show factor influence in pie graphs. 
     *            3 = Line graphs for the multilevel experiments. 4 = Comparative bar graphs between two evaluations.
     * @param op_sys The property "name" of the current operating system
     * @return Success/fail/info message
     */
    public String graphs(int id, int id_comp, int opt, String op_sys){
        
        String result = "";

        try {
            
            Benchmarker bcmk = Benchmarker.getInstance();
            bcmk.settings(CONFIDENCE_INTERV, id);

            Utils.makeDir(Integer.toString(id));
            String path = (Menu.isWindows(op_sys)) ? "Experiments\\" + id : "Experiments/"+id;

            Evaluation evaluation = Evaluation.getInstance();
            ResultSet rvar = evaluation.getResponseVariables(id);
            
            if(rvar.next() == false)
                return "Tere is no data to present";

            // filter exp 1,3 and 5. For all exp, put new int[0]
            int[] filter = {0, 4, 6}; 

            do{
                
                int rv_id = rvar.getInt("rv_id");
                String rv_name = rvar.getString("rv_name");
                
                switch(opt){
                    case 1:
                        result = BarChart.generate(path, rv_name, id, rv_id, rvar.getString("y_axis"));
                        break;
                    case 2:
                        result = FactorInfluence.generatePieChart(path, rv_id, rv_name);
                        break;
                    case 3:
                        result = LineChart.generate(path, rv_name, id, rv_id, rvar.getString("y_axis"));
                        break;
                    case 4:
                        result = BarChart.generate2(path, rv_name, rvar.getString("y_axis"), id, id_comp, rv_id, filter);
                        break;
                    default:
                        break;
                }
                
            }while(rvar.next());
                                           
        } catch (SQLException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            result = "Error when creating graphs.";
        }
        
        return result;


    }
    
         
    
    /**
     * Executes the one-factor-multi-level experiment
     * @param exp_name The name of the experiment
     * @param warmup Should the warmup method be used? True for use it, false otherwise
     * @param number_of_replicas The desired number of replicas. If 0, the framwork will try to find the ideal number of replicas. This operation may take a long time though.
     * @return Success/fail message
     */
    public String multiLvlExp(String exp_name, boolean warmup, int number_of_replicas) {
        
        try{
            
            double ini = System.currentTimeMillis();
            
            FactorTypesEnum.persistFactorTypes();
            ResponseVariable.persistResponseVariables();
            Factor.setMultiLevel(Config.MULTI_LVL_MIN, Config.MULTI_LVL_MAX, Config.MULTI_LVL_STEP);
            
            Evaluation evaluation = Evaluation.getInstance();
            int id = evaluation.saveEvaluation(exp_name, CONFIDENCE_INTERV);
            
            Benchmarker bcmk = Benchmarker.getInstance(); 
            bcmk.settings(CONFIDENCE_INTERV, id);
            
            ConsoleLogger logger = new ConsoleLogger();
            logger.writeEntry("Evaluation id: "+id);

            if(warmup)
                bcmk.multiLvlWarmup();
            
            Config.setFactors(bcmk);
            bcmk.persistFactors();
            Config.setResponseVariables(); // diz quais serão salvas
            
            if(number_of_replicas > 0){
                bcmk.setNumberOfReplicas(number_of_replicas);
            }else{ //bcmk.pilotExperiment();
                bcmk.setNumberOfReplicas(3); // initial replicas
                bcmk.pilotExperiment2();
            }

            bcmk.oneFacMultiLvlExp(true);
            
            logger.writeEntry("Experimento Realizado Com Sucesso!!!!");
            logger.writeEntry("Tempo de execução: " + (System.currentTimeMillis() - ini)/1000 + " segundos");
            return "Experimento Realizado Com Sucesso!!!! Evaluation ID: "+id;

        }  catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
            return "Error when running multi level experiment.";
        }

        
    }
    
    /**
     * Checks if the evaluation database exists. Create one if do not exist yet. 
     */
    public void checkEvaluationDatabase(){
        
    }

    
}
