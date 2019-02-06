package reports;

import app.Test;
import database.Evaluation;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartPanel; 
import org.jfree.chart.ChartUtils;
//import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart; 
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.statistics.DefaultStatisticalCategoryDataset;
import org.jfree.data.statistics.StatisticalCategoryDataset;
import org.jfree.ui.ApplicationFrame; 
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author Paulo
 * @data 11/13/2017
 */
public class BarChart extends ApplicationFrame{
    
    public BarChart( String path, String applicationTitle , String chartTitle, int evaluation_id, int rv_id, String y_axis) throws SQLException {
        
        super( applicationTitle );  

        final StatisticalCategoryDataset dataset = createDataset(evaluation_id, rv_id);

        final CategoryAxis xAxis = new CategoryAxis("Experiments");
        xAxis.setLowerMargin(0.01d); // percentage of space before first bar
        xAxis.setUpperMargin(0.01d); // percentage of space after last bar
        xAxis.setCategoryMargin(0.2d); // percentage of space between categories
        final ValueAxis yAxis = new NumberAxis(y_axis);

        // define the plot
        final CategoryItemRenderer renderer = new StatisticalBarRenderer();
        
        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        final JFreeChart barChart = new JFreeChart(chartTitle, new Font("Helvetica", Font.BOLD, 16), plot, true);

        barChart.setBackgroundPaint( new Color(204, 204, 204) );
        // set up gradient paints for series...
        // gradiente nao esta funcionando na 1.0; legenda não ta funcionando na 1.19
        GradientPaint gp = new GradientPaint( 0.0f, 0.0f, new Color(0, 255, 255), 0.0f, 0.0f, new Color(0, 153, 153) );
        renderer.setSeriesPaint(0, gp);
        //barChart.getTitle().setPaint(Color.red);
        
        // BarRenderer rdr = (BarRenderer) plot.getRenderer();
        // rdr.setItemMargin(0.5);
        plot.setBackgroundPaint(new Color(242, 242, 242));
        plot.setRangeGridlinePaint(Color.black);
       
        /*JFreeChart barChart = ChartFactory.createBarChart3D(
        chartTitle, "Experimentos", "Valores",
        createDataset(evaluation_id, rv_id),
        PlotOrientation.VERTICAL,
        true, true, false);*/
        
        ChartPanel chartPanel = new ChartPanel( barChart );
        //chartPanel.setPreferredSize(new java.awt.Dimension( 500 , 375 ) ); 
        chartPanel.setPreferredSize(new java.awt.Dimension( 450 , 325 ) ); 
        setContentPane( chartPanel );
        
        saveAsPNG(path, chartTitle, barChart);
            
         
   }
    
    // construtor de pares
    public BarChart( String path, String applicationTitle , String chartTitle, String y_axis, int evaluation_id1, int evaluation_id2, int rv_id, int[] id_filter ) throws SQLException {
        
        super( applicationTitle );  

        final StatisticalCategoryDataset dataset = createDatasetPair(evaluation_id1, evaluation_id2, rv_id, id_filter);

        final CategoryAxis xAxis = new CategoryAxis("Experiments");
        xAxis.setLowerMargin(0.01d); // percentage of space before first bar
        xAxis.setUpperMargin(0.01d); // percentage of space after last bar
        xAxis.setCategoryMargin(0.2d); // percentage of space between categories
        final ValueAxis yAxis = new NumberAxis(y_axis);


        // define the plot
        final CategoryItemRenderer renderer = new StatisticalBarRenderer();

        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
        final JFreeChart barChart = new JFreeChart(chartTitle, new Font("Helvetica", Font.BOLD, 14), plot, true);
       // barChart.setBorderPaint(color);
        
        BarRenderer rdr = (BarRenderer) plot.getRenderer();
        rdr.setItemMargin(0.05);
        
        barChart.setBackgroundPaint( new Color(204, 204, 204) );
        // set up gradient paints for series...
        GradientPaint gp1 = new GradientPaint( 0.0f, 0.0f, new Color(0, 255, 255), 0.0f, 0.0f, new Color(0, 153, 153) );
        GradientPaint gp = new GradientPaint( 0.0f, 0.0f, new Color(255, 0, 191), 0.0f, 0.0f, new Color(51, 0, 38) );
        renderer.setSeriesPaint(0, gp1);
        renderer.setSeriesPaint(1, gp);
        
        plot.setBackgroundPaint(new Color(242, 242, 242));
        plot.setRangeGridlinePaint(Color.black);

        ChartPanel chartPanel = new ChartPanel( barChart );
        chartPanel.setPreferredSize(new java.awt.Dimension( 360 , 260 ) );
        setContentPane( chartPanel );   
        
        saveAsPNG(path, "Comp_"+chartTitle, barChart);
            
         
   }
    
    private StatisticalCategoryDataset createDatasetPair(int evaluation_id1, int evaluation_id2, int rv_id, int[] filter ) throws SQLException {
       
            Evaluation evl = Evaluation.getInstance();
            String id_filter = "";
            
            if(filter.length != 0){ // filtro para mostrar só alguns ids
                id_filter = Arrays.toString(filter).replace('[', '(').replace(']', ')');
            }
                       
            ResultSet rs = evl.getExperimentPair(evaluation_id1, evaluation_id2, rv_id, id_filter);
            
            final DefaultStatisticalCategoryDataset result = new DefaultStatisticalCategoryDataset();
            
            while(rs.next()){ 
                
                float value = rs.getFloat("rv_value");
                float IC =  ((rs.getFloat("confidence_interval_amp")) * value)/ 2;
                result.add(value, IC, rs.getInt("evaluation_id")+": "+rs.getString("evaluation_name"), ""+ (rs.getInt("experiment_id")+1));
                
            }
            
            //result.add(50, 2, "Grupo 1", "1");
            //result.add(49, 1.5, "Grupo 2", "1");
            //result.add(53, 1.25, "Grupo 3", "1");
                        
            return result;
            
   }
    
    private StatisticalCategoryDataset createDataset(int evaluation_id, int rv_id ) throws SQLException {
       
        Evaluation evl = Evaluation.getInstance();
        ResultSet rs = evl.getExperiments(evaluation_id, rv_id);

        final DefaultStatisticalCategoryDataset result = new DefaultStatisticalCategoryDataset();

        while(rs.next()){ 

            float value = rs.getFloat("rv_value");
            float IC =  ((rs.getFloat("confidence_interval_amp")) * value)/ 2;
            //float st_dev = rs.getFloat("standard_deviation");
            result.add(value, IC, rs.getString("rv_name"), ""+ (rs.getInt("experiment_id")+1));
           // result.add(value, st_dev, rs.getString("rv_name"), ""+ (rs.getInt("experiment_id")+1));
        }

        return result;
            
   }
   
    private void saveAsPNG(String path, String chartTitle, JFreeChart barChart){
        
        try {
            
            String OpSys = System.getProperty("os.name");
            String slash = (OpSys.equals("Linux")) ? "/" : "\\";

            File bchart = new File( path + slash + "bar_Chart_"+chartTitle+".png" );
            ChartUtils.saveChartAsPNG( bchart , barChart , 450 , 325 );
        } catch (IOException ex) {
            Logger.getLogger(BarChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void generate(String path, String title, int evaluation_id, int rv_id, String y){
        
        try{
                
            BarChart chart = new BarChart(path, title, title, evaluation_id, rv_id, y);

            chart.pack( );        
            RefineryUtilities.centerFrameOnScreen( chart );        
            chart.setVisible( true );
            
                                
        }catch (SQLException ex) {
            System.err.println("ERROR: Can't generate bar chart of "+title);
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void generate2(String path, String title, String y_axis, int evaluation_id1, int evaluation_id2, int rv_id, int[] filter){
        
        try{
                
                //BarChart chart = new BarChart(path, title, title, y_axis, evaluation_id1, evaluation_id2, rv_id, filter);
                BarChart chart = new BarChart(path, title, title, y_axis, evaluation_id1, evaluation_id2, rv_id, filter);
                chart.pack( );        
                RefineryUtilities.centerFrameOnScreen( chart );        
                chart.setVisible( true );
                
        }catch (SQLException ex) {
            System.err.println("ERROR: Can't generate bar chart of "+title);
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
}