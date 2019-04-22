package reports;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;

/**
 * @author Paulo (18/09/05)
 */
public class Results extends ApplicationFrame{
    
    String applicationTitle;
    String chartTitle;
    String path;
    int evaluation_id;
    int rv_id;
    
    public Results(String path, String applicationTitle , String chartTitle, int evaluation_id, int rv_id){
        
        super( applicationTitle );
        
        this.path = path;
        this.applicationTitle = applicationTitle;
        this.chartTitle = chartTitle;
        this.evaluation_id = evaluation_id;
        this.rv_id = rv_id;
        
        
    }
    
    private void saveAsPNG(String path, String chartTitle, JFreeChart barChart){
        
        try {
            
            String OpSys = System.getProperty("os.name");
            String slash = (OpSys.equals("Linux")) ? "/" : "\\";

            File bchart = new File( path + slash + "bar_Chart_"+chartTitle+".png" );
            ChartUtils.saveChartAsPNG( bchart , barChart , 708 , 530 );
        } catch (IOException ex) {
            Logger.getLogger(BarChart.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
}
