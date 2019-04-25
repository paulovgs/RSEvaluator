package reports;

import database.Evaluation;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RefineryUtilities;

/**
 * Create a line chart representation of the results
 * @author Paulo
 */
public class LineChart extends Results{
    
    public LineChart( String path, String applicationTitle , String chartTitle, int evaluation_id, int rv_id, String x_axis ) throws SQLException {
        
        super(path, applicationTitle, chartTitle, evaluation_id, rv_id);
        
        Evaluation evl = Evaluation.getInstance();
        ResultSet rs = evl.getFactors(evaluation_id);
        rs.next(); // first and only one factor in this mode
        
        String factor_name = rs.getString("factor_name");
        
        JFreeChart lineChart = ChartFactory.createLineChart(
           chartTitle,
           factor_name, x_axis,
           createDataset(),
           PlotOrientation.VERTICAL,
           true,true,false);

        ChartPanel chartPanel = new ChartPanel( lineChart );
        chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
        setContentPane( chartPanel );
        
    }
    
    public static void generate(String path, String title, int evaluation_id, int rv_id, String y){
        
        try{
                
            LineChart chart = new LineChart(path, title, title, evaluation_id, rv_id, y);

            chart.pack( );
            RefineryUtilities.centerFrameOnScreen( chart );
            chart.setVisible( true );                   
                                
        }catch (SQLException ex) {
            System.err.println("ERROR: Can't generate line chart of " + title);
            Logger.getLogger(LineChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

   private DefaultCategoryDataset createDataset( ) throws SQLException {
       
        DefaultCategoryDataset result = new DefaultCategoryDataset( );
        
        Evaluation evl = Evaluation.getInstance();
        ResultSet rs = evl.getExperiments(evaluation_id, rv_id);
      
        ResultSet ev = evl.getEvaluation(evaluation_id);
        ev.next();
        int multi_lvl_step = ev.getInt("multi_lvl_step");
        int multi_lvl_starter = ev.getInt("multi_lvl_starter");

        while(rs.next()){ 
            
            float value = rs.getFloat("rv_value");
            int val = multi_lvl_starter + (multi_lvl_step * (rs.getInt("experiment_id")));
            result.addValue(value, rs.getString("rv_name"), "" + val);
            
        }

        return result;

   }
   
    
    
}
