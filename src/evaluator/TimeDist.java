package evaluator;

import database.Evaluation;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import utils.User;

/**
 * @date 03/14/2018
 * @author aluno
 * simula diferentes distribuições de tempo de chegada dos usuarios
 */
public class TimeDist implements Runnable{

    private int number_of_requests;
    private float time_range;
    private ArrayList <Integer> sliced_list;
    private List<User> pre_queue; // deve ser uma lista ordenada pelo menor tempo
    private Queue<User> arrival_queue;
    
    private TimeDistEnum distribution_type;
    
    
    public TimeDist(int number_of_requests, ArrayList <Integer> sliced_list, TimeDistEnum distribution_type, float time_range){
        
        this.time_range = time_range * 1000; // para que o valor informado seja equivalente a segundos;
        
        this.number_of_requests = number_of_requests;
        this.sliced_list = sliced_list;
        this.pre_queue = new ArrayList();
        this.distribution_type = distribution_type;
        
        Benchmarker bcmk = Benchmarker.getInstance();
        arrival_queue = bcmk.getArrivalQueue();

        switch(this.distribution_type){
            
            case T_NORMAL_DIST:
                normalDist();
            break;
            
            case T_ALL_AT_ONCE:
                allAtOnce();
            break;
                
        }
        
    }

    @Override
    public void run() {
        
        try {
            
            //preenche fila, de acordo com start time e com os tempos da distribuição
            double start_time = System.currentTimeMillis(); // pega a hora do inicio da execução dos testes

            while(!pre_queue.isEmpty()){

                double time = start_time + pre_queue.get(0).getArrivalTime();
                double now = System.currentTimeMillis();

                if( time <= now ){ // preenche a fila simulando chegada do usuário
                    User user = pre_queue.remove(0);
                    user.setArrivalTime( now );
                    arrival_queue.offer( user ); // transfere da pré fila para a fila
                }

                TimeUnit.MILLISECONDS.sleep((long) 0.5); // delay

            }

        } catch (InterruptedException ex) {
            Logger.getLogger(TimeDist.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        //if(pre_queue.isEmpty()) System.out.println("Pré Fila vazia!!!!!!!!!!!");
        
        
    }
    
    private void allAtOnce(){
        
        long arrival_time = 0; // todos chegam no tempo 0

        for(int i = 0; i < number_of_requests; i++){
            
            User u = new User(sliced_list.get(i), arrival_time);
            pre_queue.add(u);
            
        }
        
    }
    
    private void normalDist(){
           
        float mean = time_range/2; 
        float dp = mean/3;
        List< Double > vaux = new ArrayList<>();
        int i;
        
        
        for (i = 0; i < number_of_requests; i++){
            vaux.add( getGaussian(mean, dp) );
        }
        
        Collections.sort(vaux); // ordena por menor tempo
         
        for(i = 0; i < number_of_requests; i++){
            
            User u = new User(sliced_list.get(i), vaux.get(i));
            pre_queue.add(u);
            
        }
        
       // int c = 0, d = 0, e = 0;
        
        /*for(i = 0; i < pre_queue.size(); i++){
            
            User u = pre_queue.get(i);
            double n = u.getArrivalTime();
            System.out.println("user: " + u.getID() + " time: " + u.getArrivalTime());
           if(n >= (mean-dp) && n <= (mean+dp)) c++;
            if(n >= mean-2*dp && n <= mean+2*dp) d++;
            if(n >= mean-3*dp && n <= mean+3*dp) e++;
          
        }*/
        
        
        /*System.out.println("+- 1 Dp: " + (float)(c/(float)number_of_requests)*100);
        System.out.println("+- 2 Dp: " + (float)(d/(float)number_of_requests)*100);
        System.out.println("+- 3 Dp: " + (float)(e/(float)number_of_requests)*100);*/
    
    }
    
    private double getGaussian(float mean, float stdDev){
      
      Random random = new Random();    
      return (random.nextGaussian() * stdDev) + mean;
      
    }
    
    public static void createTimeClasses(ArrayList<Float> response_time, float recommendation_time, Map<Integer, Float> t_class){
        
        //Map<Integer, Float> time_classes = new HashMap<>();
        
        if(t_class.isEmpty()){
            
            for(int i = 1; i <= 5; i++)
                t_class.put(i, 0f);
            
        }
        
        response_time.forEach((time) -> {
            
            if(time <= 0.5 * recommendation_time)
                t_class.put(1, t_class.get(1) + 1);
            else if(time <= recommendation_time)
                t_class.put(2, t_class.get(2) + 1);
            else if(time <= 2 * recommendation_time)
                t_class.put(3, t_class.get(3) + 1);
            else if(time <= 3 * recommendation_time)
                t_class.put(4, t_class.get(4) + 1);
            else
                t_class.put(5, t_class.get(5) + 1);
            
        });
        
        
    }
    
    public static void saveTimeClasses(int evaluation_id, int experiment_id, Map<Integer, Float> time_classes, Evaluation eval, int div_factor) throws SQLException{
        
        for(Integer key : time_classes.keySet())
            eval.saveTClasses(evaluation_id, experiment_id, key, time_classes.get(key)/div_factor);
        
    }
       

        
}


