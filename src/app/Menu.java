package app;

import database.Groundwork;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static utils.Config.DB_NAME;
import static utils.Config.INVALID_NUMBER;

/**
 * Console user interface. Here one can access the main features of RSE. Will be replaced by a GUI
 * @author Paulo Vicente
 */
public class Menu {
    
    private boolean exit_program;
    private String callbackMessage;
    private final String op_sys;
    
    /**
     * Menu constructor
     */
    public Menu(){
        exit_program = false;
        callbackMessage = "";
        op_sys = System.getProperty("os.name").toLowerCase();
    }
    
    /**
     * Main method. Starts the console interface
     */
    public static void main(String[] args) {
        
        Menu menu = new Menu();
        menu.clear(); 
        
        while(!menu.exit_program){
            
            menu.printHeader();
            menu.printMenu();
            System.out.println("\n" + menu.callbackMessage);
            
            int option = menu.getInput();
            menu.performAction(option);
            
            if(!menu.exit_program) // exit_program can be changed into performAction method
                menu.clear();
            
        }

    }
    
    /**
     * Prints a header on the console
     */
    private void printHeader(){
            System.out.println("|===========================================================================|");
            System.out.println("|                   Recommender Systems Evaluator                           |");
            System.out.println("|===========================================================================|");
        
    }
    
    /**
     * Prints the main menu
     */
    private void printMenu(){
        System.out.println("[1] Create Evaluation Database");
        System.out.println("[2] Create Specific Database");
        System.out.println("[3] Multi Factorial Experiment");
        System.out.println("[4] Multi Level Experiment");
        System.out.println("[5] Bar Charts");
        System.out.println("[6] Factor Influence");
        System.out.println("[7] Line Charts");
        System.out.println("[8] Comparative Charts");
        System.out.println("[9] Histograms");
        System.out.println("[10] Quit");
    }
        
    /**
     * This message will be displayed after some execution happens. Should be a success/fail/info message to the user 
     * @param message The message to be displayed
     */
    private void registerCallbackMessage(String message){
        callbackMessage = message;
    }
    
    /**
     * Given a menu choice, performs an appropriated action.
     * @param option The selected chosen
     */
    private void performAction(int option){
        
        RSE rse = new RSE();
        int evaluation_id = INVALID_NUMBER;
        String callbackMsg = "";
        
        if(option >= 5 && option <= 9){ // options that need an evaluation id
            evaluation_id = getInputInteger("");
            if(evaluation_id == INVALID_NUMBER)
                return;
        }
        
        switch(option){
            case 1:
                
                Groundwork struct = new Groundwork ("");
                if( struct.createEvalDatabase()){
                    
                    System.out.println("Creating...");
                    struct.setDb("Evaluation");
                    struct.open();
                    struct.createEvalStructure();
                    struct.insertTimeClasses();
                    callbackMsg = "Evaluation database was successfully created.";
                    
                }else{
                    callbackMsg = "Error: check if Evaluation database already exist.";
                }
                
                break;
            case 2:
                
                Groundwork structure = new Groundwork ("");
                if( structure.createDatabase(DB_NAME)){
                    
                    System.out.println("Creating...");
                    structure.setDb(DB_NAME);
                    structure.open(); 
                    structure.createStructure();
                    callbackMsg = DB_NAME + " database was successfully created.";
                    
                }else{
                    callbackMsg = "Error: check if "+DB_NAME+" database already exist.";
                }
                
                break;
            case 3:
                
                String exp_name = getInputString("Experiment name:");
                String warmup = getInputBoolean("Warmup (Y/N):");
                
                if(warmup.charAt(0) == 'Y' || warmup.charAt(0) == 'N'){
                    
                    boolean warm_up = (warmup.charAt(0) == 'Y');
                    int replicas = getInputInteger("Enter the number of replicas [0 for automatic search]:");
                    callbackMsg = rse.exp(exp_name, warm_up, replicas);
                    
                }else{
                    callbackMsg = "Please choose a valid option.";
                }

                break;
            case 4:

                String multi_exp_name = getInputString("Experiment name:");
                String warmup_multi = getInputBoolean("Warmup (Y/N):");
                
                if(warmup_multi.charAt(0) == 'Y' || warmup_multi.charAt(0) == 'N'){
                    
                    boolean warm_up = (warmup_multi.charAt(0) == 'Y');
                    int replicas = getInputInteger("Enter the number of replicas [0 for automatic search]:");
                    callbackMsg = rse.multiLvlExp(multi_exp_name, warm_up, replicas);
                    
                }else{
                    callbackMsg = "Please choose a valid option.";
                }
                break;
            case 5:
                callbackMsg = rse.graphs(evaluation_id, INVALID_NUMBER, 1, op_sys);
                break;
            case 6:
                callbackMsg = rse.graphs(evaluation_id, INVALID_NUMBER, 2, op_sys);
                break;
            case 7:
                callbackMsg = rse.graphs(evaluation_id, INVALID_NUMBER, 3, op_sys);
                break;
            case 8:
               
                int id_comp = getInputInteger("Second evaluation ID:");
                if(id_comp == INVALID_NUMBER)
                    return;
                
                callbackMsg = rse.graphs(evaluation_id, id_comp, 4, op_sys);
                
                break;
            case 9:
                callbackMsg = rse.histo(evaluation_id);
                break;
            case 10:
                exit_program = true;
                break;
            default:
                System.err.println("An unkwnow error has occurred");
                
        }

        registerCallbackMessage(callbackMsg);

    }
    
    /**
     * Cleans the console. Must be tested in other OS rather than windows
     */
    private void clear(){
        try{
            
            if(isWindows(op_sys)){
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }else{
                Runtime.getRuntime().exec("clear");
            }
            
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(Menu.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Checks if the OS is Windows
     * @param os the property "name" of the current operating system
     * @return true for windows, false otherwise
     */
    public static boolean isWindows(String os){
        return (os.contains("win")); 
    }
    
    /**
     * Scans the initial input. Should be a valid integer between a range of possible actions.
     * @return The scanned option if valid. Otherwise, returns the constant INVALID_NUMBER.
     */
    private int getInput(){
        
        int option = INVALID_NUMBER;
        Scanner scan = new Scanner(System.in);
        
        while(option < 0 || option > 10){
            try{
                System.out.print("\nEnter with your selection:");
                option = Integer.parseInt(scan.nextLine());
            }catch(NumberFormatException ex){
                System.err.println("Invalid option. Please try again");
            }
        }
        
        return option;
    }
    
    /**
     * Scans an integer input.
     * @param input_message A personalized message to be displayed on console. If empty, will display the standard message
     * @return The scanned and integer option if valid. Otherwise, returns the constant INVALID_NUMBER.
     */
    private int getInputInteger(String input_message){

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
    
    /**
     * Scans an input in string format
     * @param input_message The message to be displayed on console.
     * @return The scanned string.
     */
    private String getInputString(String input_message){
        
        Scanner scan = new Scanner(System.in);
        System.out.print("\n"+input_message);
        String input = scan.nextLine();        
        return input;
    }
    
    /**
     * Scans an input in the format of Y/N questions.
     * @param input_message The message to be displayed on console.
     * @return A single character string
     */
    private String getInputBoolean(String input_message){
        
        Scanner scan = new Scanner(System.in);
        System.out.print("\n"+input_message);
        String input = scan.nextLine();  
        
        if(input.length() != 1)
            registerCallbackMessage("Please choose a valid option.");
        
        return input;
    }
   
}