package reports;

import database.Evaluation;
import java.io.*;
import static java.lang.Math.round;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.*;
import org.jfree.data.statistics.*;
import org.jfree.chart.plot.PlotOrientation;

/**
 * Create a histogram that represents a temporal distribution of users for a specific eperiment
 * @author Paulo
 */
public class Histogram {
    
    private final int experiment_id;
    private final int evaluation_id;
    private final float avg_time_rec;
    
    public Histogram(int experiment_id, int evaluation_id, float avg_time_rec){

        this.experiment_id = experiment_id;
        this.evaluation_id = evaluation_id;
        this.avg_time_rec = avg_time_rec;
        
    }
    
    public void create(){
        
        try {
             
            Evaluation ev = Evaluation.getInstance();
            ResultSet tclasses = ev.getTimeClasses(evaluation_id, experiment_id );
            HistogramDataset dataset = new HistogramDataset();
            
            while(tclasses.next()){
                
                double[] value = new double[ round(tclasses.getFloat("time_class_value")) ];
                float lower = avg_time_rec * tclasses.getFloat("lower_bound");
                float upper = avg_time_rec * tclasses.getFloat("upper_bound");
                dataset.addSeries("c" + tclasses.getInt("time_class_id"), value, 1, lower, upper);
                
            }
            
            PlotOrientation orientation = PlotOrientation.VERTICAL;
            JFreeChart chart = ChartFactory.createHistogram("", "Recommendation Time", "User Frequency", dataset, orientation, true, false, false);

            ChartUtils.saveChartAsPNG(new File("histo_exp_id_"+experiment_id+".PNG"), chart, 450, 250);
            
        } catch (SQLException | IOException ex) {
             Logger.getLogger(Histogram.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    

    public static String generate(int evaluation_id){
        
        try {
            
            Evaluation eval = Evaluation.getInstance();
            ResultSet ev = eval.getExperimentIDs(evaluation_id);
            
            if(ev.next() == false)
               return "No histograms were generated for evaluation_id "+evaluation_id;
            
            do{
                Histogram histogram = new Histogram(ev.getInt("experiment_id"), evaluation_id, ev.getFloat("rv_value"));
                histogram.create();
            }while(ev.next());
                
            return "Histograms were successfully saved!";
            
        } catch (SQLException ex) {
            Logger.getLogger(Histogram.class.getName()).log(Level.SEVERE, null, ex);
            return "Error when creating histograms.";
        }
        
    }
    
}
