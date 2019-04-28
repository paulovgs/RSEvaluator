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
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return "Error when running multi factorial experiment.";
        }

        
    }
    
    public String histo(int evaluation_id) {        
        return Histogram.generate(evaluation_id);
    }
    
    public String graphs(int id, int id_comp, int opt, String op_sys){
        
        String result = "";

        try {
            
            Benchmarker bcmk = Benchmarker.getInstance();
            bcmk.settings(CONFIDENCE_INTERV, id);

            Utils.makeDir(Integer.toString(id));
            String path = (App.isWindows(op_sys)) ? "Experiments\\" + id : "Experiments/"+id;

            Evaluation evaluation = Evaluation.getInstance();
            ResultSet rvar = evaluation.getResponseVariables(id);
            
            if(rvar.next() == false)
                return "Tere is no data to present";


            int[] filter = {0, 4, 6}; // atenção: ESSES VALORES CORRESPONDEM AOS EXPERIMENT_IDS 1,3 E 5!!!! p/todos colocar filter = new int[0]

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
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            result = "Error when creating graphs.";
        }
        
        return result;


    }
    
    public String multiLvlExp(String exp_name, boolean warmup, int number_of_replicas) {
        
        try{
            
            double ini = System.currentTimeMillis();
            
            FactorTypesEnum.persistFactorTypes();
            ResponseVariable.persistResponseVariables();
            Factor.setMultiLevel(3, 15, 2);
            
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

            bcmk.oneFacMultiLvlExp(true);
            
            logger.writeEntry("Experimento Realizado Com Sucesso!!!!");
            logger.writeEntry("Tempo de execução: " + (System.currentTimeMillis() - ini)/1000 + " segundos");
            return "Experimento Realizado Com Sucesso!!!! Evaluation ID: "+id;

        }  catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
            return "Error when running multi level experiment.";
        }

        
    }

    
}
