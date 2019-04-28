package app;

import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static utils.Config.INVALID_NUMBER;

/**
 * Console user interface
 * @author Paulo Vicente
 */
public class App {
    
    private boolean exit;
    private String callbackMessage;
    private String op_sys;
    
    public App(){
        exit = false;
        callbackMessage = "";
        op_sys = System.getProperty("os.name").toLowerCase();
    }
    
    public static void main(String[] args) {
        
        App app = new App();
        app.clear();
        
        while(!app.exit){
            
            app.printHeader();
            app.printMenu();
            System.out.println("\n" + app.callbackMessage);
            int option = app.getInput();
            app.performAction(option);
            
            if(!app.exit) // exit can be changed into performAction method
                app.clear();
            
        }

    }
    
    private void printHeader(){
            System.out.println("|===========================================================================|");
            System.out.println("|                   Recommender Systems Evaluator                           |");
            System.out.println("|===========================================================================|");
        
    }
    
    private void printMenu(){
        System.out.println("[1] Multi Factorial Experiment");
        System.out.println("[2] Multi Level Experiment");
        System.out.println("[3] Bar Charts");
        System.out.println("[4] Factor Influence");
        System.out.println("[5] Line Charts");
        System.out.println("[6] Comparative Charts");
        System.out.println("[7] Histograms");
        System.out.println("[8] Quit");
    }
    
    private int getInput(){
        int option = -1;
        Scanner scan = new Scanner(System.in);
        
        while(option < 0 || option > 8){
            try{
                System.out.print("\nEnter your selection:");
                option = Integer.parseInt(scan.nextLine());
            }catch(NumberFormatException ex){
                System.err.println("Invalid option. Please try again");
            }
        }
        
        return option;
    }
    
    private int getIntegerInput(String input_message){

        Scanner scan = new Scanner(System.in);
        int input = INVALID_NUMBER;
                
        input_message = (input_message.isEmpty()) ? "Enter with the evaluation ID:" : input_message;
        System.out.print("\n"+input_message);
        
        try{
            input = Integer.parseInt(scan.nextLine());
        }catch(NumberFormatException ex){
            registerCallbackMessage("Error: Evaluation ID must be a valid integer.");
        }
        
        return input;
    }
    
    private String getInput(String input_message){
        Scanner scan = new Scanner(System.in);
        System.out.print("\n"+input_message);
        String input = scan.nextLine();        
        return input;
    }
    
    private String getBooleanInput(String input_message){
        Scanner scan = new Scanner(System.in);
        System.out.print("\n"+input_message);
        String input = scan.nextLine();  
        
        if(input.length() != 1)
            registerCallbackMessage("Please choose a valid option.");
        
        return input;
    }
        
    public void registerCallbackMessage(String message){
        callbackMessage = message;
    }
    
    private void performAction(int option){
        RSE rse = new RSE();
        int evaluation_id = INVALID_NUMBER;
        String callbackMsg = "";
        
        if(option >= 3 && option <= 7){ // options that need an evaluation id
            evaluation_id = getIntegerInput("");
            if(evaluation_id == INVALID_NUMBER)
                return;
        }
        
        switch(option){
            case 1:
                
                String exp_name = getInput("Experiment name:");
                String warmup = getBooleanInput("Warmup (Y/N):");
                
                if(warmup.charAt(0) == 'Y' || warmup.charAt(0) == 'N'){
                    
                    boolean warm_up = (warmup.charAt(0) == 'Y');
                    int replicas = getIntegerInput("Enter the number of replicas [0 for automatic search]:");
                    callbackMsg = rse.exp(exp_name, warm_up, replicas);
                    
                }else{
                    callbackMsg = "Please choose a valid option.";
                }

                break;
            case 2:

                String multi_exp_name = getInput("Experiment name:");
                String warmup_multi = getBooleanInput("Warmup (Y/N):");
                
                if(warmup_multi.charAt(0) == 'Y' || warmup_multi.charAt(0) == 'N'){
                    
                    boolean warm_up = (warmup_multi.charAt(0) == 'Y');
                    int replicas = getIntegerInput("Enter the number of replicas [0 for automatic search]:");
                    callbackMsg = rse.multiLvlExp(multi_exp_name, warm_up, replicas);
                    
                }else{
                    callbackMsg = "Please choose a valid option.";
                }
                break;
            case 3:
                callbackMsg = rse.graphs(evaluation_id, INVALID_NUMBER, 1, op_sys);
                break;
            case 4:
                callbackMsg = rse.graphs(evaluation_id, INVALID_NUMBER, 2, op_sys);
                break;
            case 5:
                callbackMsg = rse.graphs(evaluation_id, INVALID_NUMBER, 3, op_sys);
                break;
            case 6:
               
                int id_comp = getIntegerInput("Second evaluation ID:");
                if(id_comp == INVALID_NUMBER)
                    return;
                
                callbackMsg = rse.graphs(evaluation_id, id_comp, 4, op_sys);
                
                break;
            case 7:
                callbackMsg = rse.histo(evaluation_id);
                break;
            case 8:
                exit = true;
                break;
            default:
                System.err.println("An unkwnow error has occurred");
                
        }

        registerCallbackMessage(callbackMsg);

    }
    
    /**
     * Must be tested in other OS rather than windows
     */
    public void clear(){
        try{
            
            if(isWindows(op_sys)){
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }else{
                Runtime.getRuntime().exec("clear");
            }
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean isWindows(String os){
        return (os.contains("win")); 
    }
  
   
}

