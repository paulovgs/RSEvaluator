package app;

import utils.Config;
import static utils.Config.CONFIDENCE_INTERV;
import database.Evaluation;
import evaluator.Benchmarker;
import evaluator.Factor;
import reports.FactorInfluence;
import evaluator.FactorTypesEnum;
import evaluator.ResponseVariable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import reports.BarChart;
import reports.Histogram;
import reports.LineChart;
import utils.ConsoleLogger;
import utils.Utils;

/**
 * @author Paulo Vicente
 * @data 16/08/2017
 */
public class App {
    
    public static void main(String[] args) {
        
       // exp();
        //histo();
        graphs();
        //multiLvlExp();
        
    }
    
    public static void exp() {
        
        try{
            
            double ini = System.currentTimeMillis();
            
            FactorTypesEnum.persistFactorTypes();
            ResponseVariable.persistResponseVariables();
            
            Evaluation evaluation = Evaluation.getInstance();
            int id = evaluation.saveEvaluation("UserUser R2 17 Nov", CONFIDENCE_INTERV);
            
            Benchmarker bcmk = Benchmarker.getInstance(); 
            bcmk.settings(CONFIDENCE_INTERV, id);
            
            ConsoleLogger logger = new ConsoleLogger();
            logger.writeEntry("Evaluation id: "+id);

            bcmk.warmUp();
            
            Config.setFactors(bcmk);
            bcmk.persistFactors();
            Config.setResponseVariables(); // diz quais serão salvas

            bcmk.setNumberOfReplicas(10);
            //bcmk.pilotExperiment();
            bcmk.pilotExperiment2();
            bcmk.experiment(true);
            
            logger.writeEntry("Experimento Realizado Com Sucesso!!!!");
            logger.writeEntry("Tempo de execução: " + (System.currentTimeMillis() - ini)/1000 + " segundos");
            

        }  catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
    
    public static void histo() {
        
        int eval_id = 475;
        Histogram.generate(eval_id);
    }
    
    public static void graphs(){

        try {
            
            int id = 463, id2 = 464;
            
            Benchmarker bcmk = Benchmarker.getInstance();
            bcmk.settings(CONFIDENCE_INTERV, id);

            Utils.makeDir(id);
            String path = "Experiments/"+id; //Linux
            //String path = "Experiments\\" + id; //Windows

            Evaluation evaluation = Evaluation.getInstance();
            ResultSet rvar = evaluation.getResponseVariables(id);

            int[] filter = {0, 4, 6}; // atenção: ESSES VALORES CORRESPONDEM AOS EXPERIMENT_IDS 1,3 E 5!!!! p/todos colocar filter = new int[0]

            while(rvar.next()){

                int rv_id = rvar.getInt("rv_id");
                String rv_name = rvar.getString("rv_name");
           //     FactorInfluence.generatePieChart(path, rv_id, rv_name);
                 BarChart.generate(path, rv_name, id, rv_id, rvar.getString("y_axis"));
              //  BarChart.generate2(path, rv_name, rvar.getString("y_axis"), id, id2, rv_id, filter);
              //  LineChart.generate(path, rv_name, id, rv_id, rvar.getString("y_axis"));

            }
                                           
        } catch (SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
    
    public static void multiLvlExp() {
        
        try{
            
            double ini = System.currentTimeMillis();
            
            FactorTypesEnum.persistFactorTypes();
            ResponseVariable.persistResponseVariables();
            Factor.setMultiLevel(3, 15, 2);
            
            Evaluation evaluation = Evaluation.getInstance();
            int id = evaluation.saveEvaluation("Content-Based Multinível Teste", CONFIDENCE_INTERV);
            
            Benchmarker bcmk = Benchmarker.getInstance(); 
            bcmk.settings(CONFIDENCE_INTERV, id);
            
            ConsoleLogger logger = new ConsoleLogger();
            logger.writeEntry("Evaluation id: "+id);

            bcmk.warmUp();
            
            Config.setFactors(bcmk);
            bcmk.persistFactors();
            Config.setResponseVariables(); // diz quais serão salvas

            bcmk.setNumberOfReplicas(11);
            //bcmk.pilotExperiment();
            bcmk.oneFacMultiLvlExp(true);
            
            logger.writeEntry("Experimento Realizado Com Sucesso!!!!");
            logger.writeEntry("Tempo de execução: " + (System.currentTimeMillis() - ini)/1000 + " segundos");

        }  catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
        

}

