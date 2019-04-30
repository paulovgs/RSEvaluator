package evaluator;

import utils.RSELogger;

/**
 * @author Paulo
 */
public class Factor {

    private final FactorTypesEnum factor_type;
    private final String[] values;
    private Factor factor;
    private int currentVariation;
    
    private static int level = 2; // default level
    private static int max; // default level
    private static int multi_lvl_starter;
    private static int step = 0;
    
    public Factor(FactorTypesEnum factor_type, String[] values){
        
        this.factor_type = factor_type;
        //level = f_level;
        this.values = new String[level];
        
        for(int i = 0; i < level; i++)
            this.values[i] = values[i];
        
        factor = null;
        
    }
    
    /**
     * Configures the multi level part of the experiment
     * @param min First level the factor will assume
     * @param maxv Last level the factor will assume
     * @param stepv Between min and maxv, the factor will assume multiple stepv values, starting from min
     *              For ex: setMultiLevel(3, 15, 4) leads to 4 levels: 3, 7, 11 and 15
     */
    public static void setMultiLevel(int min, int maxv, int stepv){
        
       multi_lvl_starter = min; // will be saved on db
       max = maxv;
       step = stepv;
       level = Math.floorDiv((max - multi_lvl_starter), step) + 1; // +1 to account the starter value
       
    }
    
   
    public static Factor createMultiLevelFactor(FactorTypesEnum factor_type){
        
        int qtd = Math.floorDiv((max - multi_lvl_starter), step) + 1;
        int step2 = multi_lvl_starter;
        String[] levels = new String[qtd];
        
        for(int i = 0; i < qtd; i++ ){
            levels[i] = Integer.toString(step2);
            step2 += step;
        }
        
        return new Factor(factor_type, levels);
        
    }

    // Fatores Compostos: são vistos pelo sistema como sendo um único fator. Ou seja, são fatores diferentes mas 
    // tratados como únicos na hora dos resultados da Avaliação de Desempenho
    public void compose(Factor f){
        factor = f;
    }
    
    // até o momento 1 e -1. 
    public void setCurrentVariation(int variation){
        
        currentVariation = variation;
        
        if(factor != null)
            factor.setCurrentVariation(variation);
        
    }
    
    public int getCurrentVariation(){ return currentVariation; }
    
    public String getCurrentValue(){ return (currentVariation == 1) ? values[0] : values[1]; }
    
    public String getValues(int idx){ return values[idx]; }
    
    public FactorTypesEnum getFactorType(){ return factor_type; }
    
    public Factor getComposedFactor(){ return factor; }
    
    public static void print(Factor f, RSELogger logger){
        
        logger.writeEntry("Fator: " + f.getFactorType().name());
        
        if(f.getComposedFactor() != null){
            logger.writeEntry("Composto: ");
            print(f.getComposedFactor(), logger);
        }else{
            logger.writeEntry("    Fator Composto null\n");
        }
        
    }
   
    public static void setLevel(int lvl){ level = lvl; }
    
    public static int getLevel(){ return level; }
    
    public static int getStep(){  return step;  }
    
    /**
     * Auxiliary function
     * @param val1
     * @param val2
     * @param factor_type
     * @return 
     */
    public static Factor fillFactor(String val1, String val2, FactorTypesEnum factor_type){
        
        String[] val = new String[2];
        val[0] = val1;
        val[1] = val2;
        return new Factor(factor_type, val);

    }
    
    
    
}
