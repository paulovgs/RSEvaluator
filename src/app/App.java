package app;

import utils.Config;
import static utils.Config.CONFIDENCE_INTERV;
import database.Evaluation;
import evaluator.Benchmarker;
import evaluator.Factor;
import evaluator.FactorTypesEnum;
import evaluator.ResponseVariable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.ResultSet;
import java.util.Scanner;
import reports.BarChart;
import reports.Histogram;
import utils.ConsoleLogger;
import utils.Utils;

/**
 * @author Paulo Vicente
 */
public class App {
    
    private boolean exit;
    
    public App(){
        exit = false;
    }
    
    public static void main(String[] args) {
        
            App app = new App();
            app.printHeader();
            while(!app.exit){
                app.printMenu();
                int option = app.getInput();
                app.performAction(option);
            }
            

    }
    
    private void printHeader(){
            System.out.println("|===========================================================================|");
            System.out.println("|                   Recommender Systems Evaluator                           |");
            System.out.println("|===========================================================================|");
        
    }
    
    private void printMenu(){
        System.out.println("\n[1] Multi Factorial Experiment");
        System.out.println("[2] Multi Level Experiment");
        System.out.println("[3] Show Graphs");
        System.out.println("[4] Show Histogram");
        System.out.println("[5] Quit");
    }
    
    private int getInput(){
        int option = -1;
        Scanner scan = new Scanner(System.in);
        
        while(option < 0 || option > 5){
            try{
                System.out.print("\nEnter your selection:");
                option = Integer.parseInt(scan.nextLine());
            }catch(NumberFormatException ex){
                System.err.println("Invalid option. Please try again");
            }
        }
        
        return option;
    }
    
    private void performAction(int option){
        switch(option){
            case 1:
                exp();
                break;
            case 2:
                multiLvlExp();
                break;
            case 3:
                graphs();
                break;
            case 4:
                histo();
                break;
            case 5:
                exit = true;
                break;
            default:
                System.err.println("An unkwnow error has occurred");
                
        }
    }
    
    private void exp() {
        
        try{
            
            double ini = System.currentTimeMillis();
            
            FactorTypesEnum.persistFactorTypes();
            ResponseVariable.persistResponseVariables();
            
            Evaluation evaluation = Evaluation.getInstance();
            int id = evaluation.saveEvaluation("Testing", CONFIDENCE_INTERV);
            
            Benchmarker bcmk = Benchmarker.getInstance(); 
            bcmk.settings(CONFIDENCE_INTERV, id);
            
            ConsoleLogger logger = new ConsoleLogger();
            logger.writeEntry("Evaluation id: "+id);

          //  bcmk.warmUp();
            
            Config.setFactors(bcmk);
            bcmk.persistFactors();
            Config.setResponseVariables(); // diz quais serão salvas

            bcmk.setNumberOfReplicas(3);
            //bcmk.pilotExperiment();
          //  bcmk.pilotExperiment2();
            bcmk.experiment(true);
            
            logger.writeEntry("Experimento Realizado Com Sucesso!!!!");
            logger.writeEntry("Tempo de execução: " + (System.currentTimeMillis() - ini)/1000 + " segundos");
            

        }  catch (ClassNotFoundException | InstantiationException | IllegalAccessException | SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
    
    private void histo() {
        
        int eval_id = 475;
        Histogram.generate(eval_id);
    }
    
    private void graphs(){

        try {
            
            int id = 463, id2 = 464;
            
            Benchmarker bcmk = Benchmarker.getInstance();
            bcmk.settings(CONFIDENCE_INTERV, id);

            Utils.makeDir(Integer.toString(id));
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
           //     LineChart.generate(path, rv_name, id, rv_id, rvar.getString("y_axis"));

            }
                                           
        } catch (SQLException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }


    }
    
    private void multiLvlExp() {
        
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

