package reports;

import utils.Config;
import database.Evaluation;
import evaluator.Benchmarker;
import evaluator.Factor;
import evaluator.FactorTypesEnum;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import utils.ConsoleLogger;
import utils.RSELogger;
import utils.Utils;

/**
 * Calculate the factor influence and create a pie chart representation of it.
 * @author Paulo
 */
public class FactorInfluence {
    
    private final Benchmarker benchmarker;
    private final int resp_var_id; // Cada análise de influencia de fatores se refere a uma variavel de resposta
    private final String chart_name;
    Map< String, Double > factors_inf = new HashMap<>();
    Map< String, Double > sum_of_squares = new HashMap<>();
    double q0,SS0;
    // for factor blend
    int[] variation, result;
    int pos;
    
    String[] label;
    //to compose the factors influence names
    String alfa[] = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", 
                     "N", "O", "P", "Q", "R", "S", "T", "U", "V", "X", "Z", "Y", "W" };
    
    private final String path;
        
    public FactorInfluence(int rv_id, String name, String path){
        
        this.benchmarker = Benchmarker.getInstance();
        this.resp_var_id = rv_id;
        q0 = 0;
        chart_name = name;
        this.path = path;
        
    }
    
    public void retrieveFromDB() throws SQLException{
        
        Evaluation evaluation = Evaluation.getInstance();
        ResultSet factors = evaluation.getFactors(benchmarker.getEvaluationID());
        Map<Integer, Factor> fmap = new LinkedHashMap<>(); // a ordem aqui é muito importante para produzir os fatores corretamente
        Map<Integer, Integer> composed_map = new HashMap<>(); // guarda a informação de composed_factor_id
        
        while(factors.next()){
            
          Factor factor = Factor.fillFactor(factors.getString("level_1"), factors.getString("level_2"),
                                        FactorTypesEnum.valueOf( factors.getString("factor_name") ));
          
          int factor_id = factors.getInt("factor_id");
          fmap.put(factor_id, factor);
          
          int composed_id = factors.getInt("composed_factor_id");
          if( composed_id != 0) // 0 means SQL null
              composed_map.put(factor_id, composed_id);
             
        }
                
        
        for(Entry<Integer, Factor> entry : fmap.entrySet()){
            
            Factor f = entry.getValue();
            int key = entry.getKey();
            
            if(composed_map.containsKey( key )){
                fmap.get( composed_map.get(key) ).compose(f);
            }else{
                benchmarker.addFactor(f);
            }
        }
        
        
    }
    
    public void factorsWeight() throws SQLException{
        
        int i = 0, j;
        Evaluation evaluation = Evaluation.getInstance();
        retrieveFromDB();
        List<Factor> factors = benchmarker.getFactors();
                
        // a ordem dos fatores pega no banco de dados deve ser a mesma ordem feita nos experimentos,
        // para a multiplicação dos 1s e -1s ser feita de forma correta e os labels (A,B,C...) corresponderem aos
        // fatores corretos. Isso está acontecendo como deveria
        
        // invertendo a ordem para ver se as letras correspondem
        /*List<Factor> factors2 = benchmarker.getFactors();
        List<Factor> factors = new ArrayList<>();
        factors.add(factors2.get(2));
        factors.add(factors2.get(1));
        factors.add(factors2.get(0));
        
        for (j = 0; j < factors.size(); j++){
            System.out.println(factors.get(j).getFactorType().name());
        }*/
        
        // obs: os labels da influencia dos fatores são preenchidos de acordo com a ordem da lista factors,
        // que por sua vez, vem da ordem do bd
        
       // RSELogger logger = new FileLogger(path + "\\Legenda_"+ chart_name+".txt");
        RSELogger logger = new ConsoleLogger();
        
        for (j = 0; j < factors.size(); j++){
            Factor.print(factors.get(j), logger);
        }
        
        ResultSet exp = evaluation.getExperiment(benchmarker.getEvaluationID(), resp_var_id);
        
        while(exp.next()){
            
            benchmarker.blend(i);
            double response = exp.getFloat("rv_value");
            variation = new int[factors.size()];
            
            for (j = 0; j < factors.size(); j++) {
                variation[j] = factors.get(j).getCurrentVariation();
            }
            
            for(j = 1; j <= factors.size(); j++){
                pos = j;
                result = new int[pos];
                label = new String[pos];
                factor_blend(0, variation.length - pos, 0, response);
            }

            q0 += response; 
            i++;
                                
        }
                        
        int level = benchmarker.getLevel();
        int cte = (int) Math.pow(level, benchmarker.getFactors().size());
        double SST = 0, aux;
        
        q0 /= cte;
        SS0 = (cte * Math.pow(q0, level));        
        
        Set<String> keys = factors_inf.keySet();
        for (String key : keys){
            if(key != null){
                factors_inf.put(key, factors_inf.get(key) / cte );
                aux = (cte * Math.pow(factors_inf.get(key), level));
                SST += aux;
                sum_of_squares.put("SS"+key, aux);
            }
        }
        
        int index = 0;
        for (String key : keys){
            
            if(key != null){
                // a ordem dos fatores corresponde a ordem preenchida da lista de factors gerada pelo banco de dados
                factors_inf.put(key, 100 * (sum_of_squares.get("SS"+key))/SST );
                //System.out.printf(key + ": %.3f %n", factors_inf.get(key));
                
                if(key.length() == 1){
                    logger.writeEntry(key + ": " +factors.get(index).getFactorType().name() );
                    //System.out.print(" (" +factors.get(index).getFactorType().name() +")\n");
                    index++;
                }
               
            }
        }
        
        Utils.printMap(factors_inf);
        System.out.println("");
        
    }
    
    private void factor_blend(int start, int end, int dept, double response) {
        
        if ((dept + 1) >= pos) {
            
            for (int x = start; x <= end; x++) {
                
                double var = -1000;
                String lbl = "";
                result[dept] = variation[x];
                label[dept] = alfa[x];
                
                //System.out.print("(");
                for (int i = 0; i < pos; i++) {
                    var = (var == -1000) ? result[i] : var * result[i]; 
                    lbl += label[i];  
                    //System.out.printf("%d%s", result[i], (i == pos - 1) ? "" : " ");
                }
                //System.out.print("); ");
                
                Double auxi;
                auxi = factors_inf.get(lbl); // é o que já tem lá
                factors_inf.put(lbl, (auxi == null) ?  var * response : auxi + (var * response) );
                
            }
            
        } else {
            
            for (int x = start; x <= end; x++) {
                result[dept] = variation[x];
                label[dept] = alfa[x];
                factor_blend(x + 1, end + 1, dept + 1, response);
            }
        }
                
    }
    
    public void pieChart3D() throws IOException{
        
        DefaultPieDataset dataset = new DefaultPieDataset( ); 

        Set<String> keys = factors_inf.keySet();
        for (String key : keys){
          if(key != null)
              dataset.setValue( key , factors_inf.get(key) );
        }
        
        
        // clear the benchmarker factors list
        benchmarker.getFactors().clear();
        
        JFreeChart chart = ChartFactory.createPieChart3D( 
           "Influência dos Fatores - " + chart_name ,  // chart title                   
           dataset ,         // data 
           true ,            // include legend                   
           false, 
           false
        );
        
        final PiePlot3D plot = ( PiePlot3D ) chart.getPlot( );             
        //plot.setStartAngle( 270 );             
        plot.setForegroundAlpha( 0.6f );     
        plot.setInteriorGap( 0.02f );             
        plot.setNoDataMessage("No data to display");
        
        // antigo formato da label, antes de custom label generator
//        plot.setLabelGenerator(new StandardPieSectionLabelGenerator( "{0} = {2}", new DecimalFormat("0.00"), new DecimalFormat("0.00%")) );
        plot.setLegendLabelGenerator(new StandardPieSectionLabelGenerator( "{0} = {2}", new DecimalFormat("0.00"), new DecimalFormat("0.00%")) );

        plot.setSimpleLabels(true);
        
        //plot.setLabelBackgroundPaint(null);
        //plot.setLabelOutlinePaint(null);
        //plot.setLabelShadowPaint(null);
        
        plot.setLabelGenerator(new CustomLabelGenerator());
        
        
        
        /*chart.removeLegend();  // legend example
	final LegendTitle legend = new LegendTitle(() -> {
            
            final LegendItemCollection collection = new LegendItemCollection();
                    
            collection.add(new LegendItem("A = 2", null, null, null, new Rectangle(30, 20), Color.red));
            collection.add(new LegendItem("TestIng"));
            
            return collection;
                
	});
        
	legend.setPosition(RectangleEdge.BOTTOM);
	chart.addLegend(legend);*/

        
        
        int width = 708;   /* Width of the image */             
        int height = 530;  /* Height of the image */
        
        String OpSys = System.getProperty("os.name");
        String slash = (OpSys.equals("Linux")) ? "/" : "\\";
        
        File pieChart3D = new File( path + slash + "Pie_Chart3D_"+chart_name+".png" );                           
        ChartUtils.saveChartAsPNG( pieChart3D , chart , width , height ); 
        
        ChartPanel chartPanel = new ChartPanel( chart );
        chartPanel.setPreferredSize(new java.awt.Dimension( 900 , 550 ) );

        ChartFrame frame = new ChartFrame("Test", chart);
        frame.setPreferredSize(new java.awt.Dimension( 900 , 550 ));
        frame.pack();
        frame.setVisible(true);
      
    }
    
    protected static class CustomLabelGenerator implements PieSectionLabelGenerator {
        
        public  DecimalFormat df = new DecimalFormat("0.00%");  

        @Override
        public String generateSectionLabel(final PieDataset dataset, final Comparable key) {
            String result = null;    
            if (dataset != null) {
                double value = (double) dataset.getValue(key);
                if ( value >= 2) {
                    result = key.toString() + " = " + String.format("%.2f", dataset.getValue(key)) + "%";
                }/*else{
                    result = key.toString();
                }*/
            }
            return result;
        }

        @Override
        public AttributedString generateAttributedSectionLabel(PieDataset pd, Comparable cmprbl) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }


   
    }
    
    public static String generatePieChart(String path, int rv_id, String name){
        
        try{
                
                FactorInfluence fi = new FactorInfluence(rv_id, name, path);                
                fi.factorsWeight();
                fi.pieChart3D();
                return "Factor influences were successfully generated.";
            
        }catch (IOException | SQLException ex) {
            //System.err.println("ERROR: Can't generate the factor influence chart for "+name);
            Logger.getLogger(FactorInfluence.class.getName()).log(Level.SEVERE, null, ex);
            return "ERROR: Can't generate the factor influence chart for "+name;
        }
        
        
    }
    
    
}
