package recommender.algorithm;

import static utils.Config.MAX_SCALE;
import static utils.Config.PREDICTION_LIMIT;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import database.skeleton.CollabFiltDBSkeleton;
import database.factory.CollabFiltDBSkeletonFactory;
import database.skeleton.ContentBasedDBSkeleton;
import database.factory.ContentBasedDBSkeletonFactory;
import database.factory.GenericSkelFactory;
import database.skeleton.GenericSkeleton;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import utils.Tags;
import utils.User;

/**
 * @author Paulo
 * @date 10/04/2017
 */
public class ContentBased extends Recommender{
    
    protected static float SIMILARITY_CRETERIA = (float)0.7;
    
    public ContentBased(int candidates, int neighbors, User t_user,  int rec_list_length){
        super(candidates, neighbors, t_user, rec_list_length);
    }
        
    @Override
    public LinkedHashMap<Integer, Float> score() throws SQLException{
              
        gen.setHistoryAverageRating(user);
        
        // escolhendo os itens candidadtos a serem recomendados(item i), cada um com seu vetor de atributos
        ResultSet itemSet = cbased.getCBCandidatesWithVector(user.getID(), candidates);
        //vetor de atributos do target user
        ResultSet user_vector = cbased.getUserVector(user.getID());
        // mapeia o vetor de atributos do usuário
        Map<Integer, Float> user_attr_vector = new HashMap<>();
        
        while(user_vector.next())
            user_attr_vector.put( user_vector.getInt("tag_id") , user_vector.getFloat("relevance") );
        
        float den, den2 = 0, item_attr_rel, score;
        
        int current_item_id, get_id_change, attr_id;
        LinkedHashMap<Integer, Float> recommendation_list = new LinkedHashMap<>();
        
        for(Entry<Integer, Float> entry : user_attr_vector.entrySet())
            den2 += Math.pow(entry.getValue(), 2);
        den2 = (float) Math.sqrt(den2);
        
        Map<Integer, Float> nume = new HashMap<>();
        Map<Integer, Float> deno = new HashMap<>();
        String item_label = gen.getItemIDLabel();
        
        while (itemSet.next()){ // para cada filme candidato calcula o score e ranqueia 
               
            current_item_id = itemSet.getInt(item_label);
            attr_id = itemSet.getInt("tag_id");
            item_attr_rel = itemSet.getFloat("relevance");
            Float user_attr_rela = user_attr_vector.get(attr_id);
            
            if(!nume.containsKey(current_item_id)){
                
                nume.put(current_item_id, 0f);
                deno.put(current_item_id, 0f);
                
            }
                
            nume.put(current_item_id, nume.get(current_item_id) + item_attr_rel * ((user_attr_rela != null) ? user_attr_rela : 0f) );// Ax*Bx + Ay*By ...
            deno.put(current_item_id, deno.get(current_item_id) + (float)Math.pow(item_attr_rel, 2));
        }

                    
        for(Entry<Integer, Float> entry : nume.entrySet()){
            
            den = (float) (Math.sqrt(deno.get(entry.getKey())) * den2); // den2 ja esta com a raiz contabilizada
            score = (den == 0) ? 0 : MAX_SCALE * entry.getValue()/den;

            if(score > 0) // valores negativos indicam dessimilaridades
                recommendation_list.put(entry.getKey(), score); 
        }
        
        
        return recommendation_list;
        
    }
    
    @Override
    public void alternativeScore(int items_qtd, LinkedHashMap<Integer, Float> recommendation_list) throws SQLException{
        
      //  GenericSkeleton gen = GenericSkelFactory.getInstance();
        ResultSet items = gen.getNonPersonalizedScore(items_qtd, user.getID());
        
        while(items.next()){
            
            int item_id = items.getInt( gen.getItemIDLabel() );
            float semi = (user.getHistoryAverageRt() > 0) ? (items.getFloat("non_personalized_score") + user.getHistoryAverageRt()) / 2
                                                     :  items.getFloat("non_personalized_score");
            
            if(!recommendation_list.containsKey(item_id) && semi >= PREDICTION_LIMIT) // semi personalized prediction
                recommendation_list.put(item_id, semi); // insere no final da lista
 
        }
        
    }
    
    // cria user_vector a partir do item_vector existente
    // é feito com base no historico de itens do usuário
    public static void createUserVector()throws SQLException{
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        CollabFiltDBSkeleton collab = CollabFiltDBSkeletonFactory.getInstance();
        
        ResultSet users = gen.getAllUsers();
        int counter = 0;
        
        while(users.next()){
            
            int user_id = users.getInt( gen.getUserIDLabel() );
            
            ResultSet history = gen.getHistoryFromUser(users.getInt( gen.getUserIDLabel() ));
            Map<Integer, Float> user_vector = new HashMap<>();

            while(history.next()){

                int item_id = history.getInt( gen.getItemIDLabel() );

                ResultSet item_vector = cbased.getItemVectorSpace(item_id);

                float rating = history.getFloat( "rating" );

                while(item_vector.next()){

                    int tag_id = item_vector.getInt( "tag_id" );
                    float t_relevance = item_vector.getFloat( "relevance" );
                    Float relevance = user_vector.get(tag_id);

                    if(relevance == null)
                        user_vector.put(tag_id, t_relevance * rating);
                    else
                        user_vector.put(tag_id, relevance + (t_relevance * rating) );

                } 

            }

            // essa normalização foi mudada por um scale factor global. Acredito que faça mais sentido
            /*float scale_factor = 0; // put into a 0-1 scale
            for(Entry<Integer, Float> entry : user_vector.entrySet()){
                float val = entry.getValue();
                if(scale_factor < val)
                    scale_factor = val;
            }*/


            if(user_vector.size() > 0){ // at least one entry to insert

                String bulk_insert = "insert into user_vector (user_id, tag_id, relevance) values ";

                //normaliza e salva no user_vector
                for(Entry<Integer, Float> entry : user_vector.entrySet()){

                    //bulk_insert += " (" + user_id + "," + entry.getKey() + "," + entry.getValue()/scale_factor + "),";
                    bulk_insert += " (" + user_id + "," + entry.getKey() + "," + entry.getValue() + "),";

                }

                bulk_insert = bulk_insert.substring(0, bulk_insert.length() - 1); // retira a virgula do final
                bulk_insert += ";";
                collab.bulkInsSim(bulk_insert);

            }

           counter++;
           if(counter % 250 == 0)
                System.out.println(counter + " Vector Spaces were created.");
            
        }
    }
    
    // Cria item vector com base em tag_ids usando stemming
    // Melhoria: colocar tag id mapeado em global tags, pois está pegando tag por value, seria melhor se fosse pelo id
    // Pode ser otimizada com bulk insert
    // Só funciona com a tabela item vector totalmente vazia; caso contrário pode dar algum problema
    public static void createItemVector(String vector_space, ArrayList<String> global_tags) throws SQLException{

        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        
        ResultSet tagSet;
        Map<Integer, ArrayList<String>> documents;
        Map<String, Integer> tags = new HashMap<>();
        Table<Integer, Integer, Boolean> inserted_item_space = HashBasedTable.create(); // item_id, tag_id
        
        ResultSet tag_ids = cbased.getTagIDs();
        while(tag_ids.next()){
            tags.put(tag_ids.getString("tag"), tag_ids.getInt( "tag_id"));
        }
        
        tagSet = cbased.getItemAndTags();
        documents = Tags.reduce(tagSet, gen, vector_space);

        //Utils.printMap(documents);
        int count = 0, doc_size = documents.size();
        
        for (Map.Entry<Integer, ArrayList<String>> entry : documents.entrySet()) {
            
            ArrayList<String> document = entry.getValue();
            int id = entry.getKey();
            
            for(String tag: document){ 
                
                // pega um representante e insere a tag id correspondente
                tag = Tags.findRepresentative(tag, global_tags).replace("'", "''"); //encontra a tag representante
                
                if(tags.containsKey(tag)){

                    int tag_id = tags.get(tag);
                    
                    if(!inserted_item_space.contains(id, tag_id)){
                        
                        double tfidf = TFIDF(tag, documents, document, doc_size);
                        cbased.insertNewItemVector(id, tag_id, tfidf);
                        inserted_item_space.put(id, tag_id, true);
                        
                    }
                }
            }
            
            count++;
            if(count % 25 == 0)
                System.out.println(count + " items were processed.");
            
        }
        
    }
    
    /*
    * Forma alternativa de criar item e user vector
    * A estrutura das tabelas precisam ser (int item_id, String tag, float relevance)
    * Embora antigo, esse método funciona bem. 
    * Para funcionar é preciso recriar as tabelas com a estrutura certa no banco
    */
    public static void createVectorSpace(String vector_space, ArrayList<String> global_tags) throws SQLException{
        
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        
        ResultSet tagSet;
        Map<Integer, ArrayList<String>> documents;
        
        if(vector_space.equals("item")){
            tagSet = cbased.getItemAndTags();
            documents = Tags.reduce(tagSet, gen, vector_space);
            
        }else{
            tagSet = cbased.getUserAndTags();
            documents = Tags.reduce(tagSet, gen, vector_space);
            
        }
 
        //Utils.printMap(documents);
        int count = 0;
        
        for (Map.Entry<Integer, ArrayList<String>> entry : documents.entrySet()) {
            
            ArrayList<String> document = entry.getValue();
            
            for(String tag: document){ 
                
                tag = Tags.findRepresentative(tag, global_tags).replace("'", "''"); //encontra a tag representante
                double tfidf = TFIDF(tag, documents, document, documents.size());
                // verifica qual vector space é e se já não foi inserido
                int id = entry.getKey();
                if(vector_space.equals("item") && !cbased.hasItemAxis(id, tag)){ 
                    cbased.insertItemVector(id, tag, tfidf);
                }else if(vector_space.equals("user") && !cbased.hasUserAxis(id, tag)){
                    cbased.insertUserVector(id, tag, tfidf);
                }
                    
            }
            
            count++;
            if(count % 250 == 0)
                System.out.println(count + vector_space + " processed.");
            
        }
        
        
    }
    
    
    // procura gênero nos filmes separados por |, cria novas tags e insere em item_vector
    public static void createTagsFromGenres() throws SQLException{
        
        Map<String, Integer> tag_genres = new HashMap<>();
        Map<String, Integer> tag_genres_ids = new HashMap<>();
        
        GenericSkeleton gen = GenericSkelFactory.getInstance();
        ContentBasedDBSkeleton cbased = ContentBasedDBSkeletonFactory.getInstance();
        
        ResultSet it = gen.getAllItems();
        
        while(it.next()){
            
            // faz o split de cada genero e adiciona no mapa
            String[] genres = it.getString("genres").split("\\|");

            for(String genre : genres){
           
                Integer count = tag_genres.get(genre);
                count = (count == null) ? 1 : ++count;
                tag_genres.put(genre, count);
           
            }
            
        }
        
        for(Entry<String, Integer> entry : tag_genres.entrySet()){
            
            String genre = entry.getKey();
            int tag_id;
            
            if( (tag_id = cbased.hasTag(genre)) == NO_RECOMMENDATION){ // se a tag ja estiver inserida retorna seu id
                tag_id = cbased.insertTag(genre, entry.getValue());    
            }
            tag_genres_ids.put(genre, tag_id);
            
        }
        
        
        it.beforeFirst();
        
        while(it.next()){
            
            String[] genres = it.getString("genres").split("\\|");
            int movie_id = it.getInt( gen.getItemIDLabel() );

            for(String genre : genres){
           
                int t_id = tag_genres_ids.get(genre);
                
                if(t_id != NO_RECOMMENDATION) // no genre listed
                    cbased.insertTagRelevance(movie_id, t_id);
            }
            
        }
        
        
    }
    
    //dado uma lista, verifica se ela possui ao menos uma chave parecida de acordo com o criterio
    public static boolean hasSimilarKey(ArrayList<String> list, String tag){
        
        for(String element : list){    
           if(Tags.compareStrings(tag, element) >= SIMILARITY_CRETERIA)
               return true;
        }
        
        return false;
    }
    
    public static double TFIDF(String tag, Map<Integer, ArrayList<String>> documents, ArrayList<String> current_document, int N) throws SQLException{
        
        double fkj = 0, nk = 0, maxfzj;
        
        for(String element : current_document){
            if(Tags.compareStrings(tag, element) >= SIMILARITY_CRETERIA)
                fkj++;
        }
        
        maxfzj = current_document.size();
        Set<Integer> keys = documents.keySet();
        
        for(Integer key : keys){
            if(hasSimilarKey(documents.get(key), tag))
               nk++;
        }
        
        //System.out.println("fkj = "+fkj+", maxfzj = "+maxfzj+", total = "+N+", nk = "+nk);
        return (maxfzj == 0 || nk== 0) ? 0 :((fkj/maxfzj) * Math.log10(N/nk));
        
    }
    
    
}
