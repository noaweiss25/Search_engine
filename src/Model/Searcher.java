package Model;

import com.medallia.word2vec.Word2VecModel;
import sun.reflect.generics.tree.Tree;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.*;

/**
 * handle on the results of a query
 */
public class Searcher {
    //List<Query> queries = new ArrayList<>();
    Ranker ranker;
    Tokenizer tokenizer;
    boolean isSemanic;
    boolean withInternet;
    boolean withstem;


    public Searcher(String qpath, String Spath, boolean stem, boolean isSemanic, boolean withInternet) throws IOException {
        //readQuery(qpath);
        ranker = new Ranker();
        tokenizer = new Tokenizer(Spath, stem);
        this.isSemanic = isSemanic;
        this.withInternet = withInternet;
        withstem = stem;


    }

    /**
     *
     * @param path to query text
     * @return a List of Queries
     * @throws IOException
     */

    public List readQuery(String path) throws IOException {
        List<Query> queries = new ArrayList<>();
        File qu = new File(path + "//queries.txt");
        String[] allFileDocs = (new String(Files.readAllBytes(qu.toPath()))).split("<top>");

        for (int i = 1; i < allFileDocs.length; i++) {
            //System.out.println(allFileDocs[i].split("<num>")[1].split("<title>")[0]);
            int ID = Integer.parseInt(allFileDocs[i].split("<num>")[1].split("<title>")[0].substring(9, 12));
            String title = allFileDocs[i].split("<title>")[1].split("<desc>")[0];
            String desc = allFileDocs[i].split("<desc>")[1].split("<narr>")[0];
            String narr = allFileDocs[i].split("<narr>")[1].split("</top>")[0];
            Query query = new Query(ID, title, desc, narr);
            queries.add(query);
        }
        return queries;
    }

    /**
     *
     * @param query
     * @return
     * @throws IOException
     * @throws ParseException
     */

    public HashMap<String, Double> calculate(Query query) throws IOException, ParseException {
        HashMap<String, Double> title = mostRel(query.title,false);
        HashMap<String, Double> desc = mostRel(query.decs,true);
        HashMap<String, Double> sorteddocs = new HashMap<>();
        HashSet<String> allDocs = new HashSet<>();
        allDocs.addAll(title.keySet());
        allDocs.addAll(desc.keySet());
        for (String st : allDocs) {
            double valueScore = 0;
            if (title.containsKey(st)) {
                sorteddocs.put(st, (title.get(st) * 0.45));
            }
            if (desc.containsKey(st)) {
                if (sorteddocs.containsKey(st)) {
                    sorteddocs.replace(st, sorteddocs.get(st) + (desc.get(st) * 0.55));
                } else {
                    sorteddocs.put(st, (desc.get(st) * 0.55));
                }
            }

        }
      //  System.out.println(sorteddocs);
        sorteddocs = sortDocs(sorteddocs);
    //    System.out.println(sorteddocs);
        return sorteddocs;

    }

    /**
     *
     * @param q query info
     * @return an hashmap of documens with there bm25 value according to the query info.
     * @throws ParseException
     * @throws IOException
     */

    public HashMap<String, Double> mostRel(String q,boolean description) throws ParseException, IOException {
        ArrayList<String> titleTerms = new ArrayList<>();
        titleTerms.addAll(tokenizer.TokenizeForQ(q).keySet());
        for (int i = 0; i < titleTerms.size(); i++) {

            if (Indexer.dictionary.containsKey(titleTerms.get(i).toLowerCase())) {
                titleTerms.set(i, titleTerms.get(i).toLowerCase());
            } else if (Indexer.dictionary.containsKey(titleTerms.get(i).toUpperCase())) {
                titleTerms.set(i, titleTerms.get(i).toUpperCase());
            }
        }
        HashMap<String, Double> semantic = new HashMap<>();
        if (isSemanic && !withInternet) {
            try {
                Word2VecModel model = Word2VecModel.fromTextFile(new File("resources\\word2vec.c.output.model.txt"));
                com.medallia.word2vec.Searcher s = model.forSearch();
                int numResult = 3;
                int size = titleTerms.size();
                //System.out.println(size);

                for (int i = 0; i < size; i++) {
                    if (titleTerms.get(i).contains("-") || titleTerms.get(i).contains(" ") || titleTerms.get(i).contains("/")) {
                        continue;
                    }
                    List<com.medallia.word2vec.Searcher.Match> matches = s.getMatches(titleTerms.get(i), numResult);
                    for (com.medallia.word2vec.Searcher.Match match : matches) {
                        if (!(titleTerms.contains(match.match()))) {
                            titleTerms.add(match.match());
                            semantic.put(match.match(), 0.05);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (com.medallia.word2vec.Searcher.UnknownWordException e) {
            }

        } else if (isSemanic && withInternet) {

            JSONParse high = new JSONParse();
            DatamuseQuery datamuseQuery = new DatamuseQuery();
            int size = titleTerms.size();
            //System.out.println(size);

            for (int i = 0; i < size; i++) {
                // System.out.println(size);
                if (titleTerms.get(i).contains("-") || titleTerms.get(i).contains(" ") || titleTerms.get(i).contains("/")) {
                    continue;
                }
                String[] st = high.parseWords(datamuseQuery.findSimilar(titleTerms.get(i)));
                double score = 0.07;
                for (int j = 0; j < 2; j++) {
                    titleTerms.add(st[j]);
                    score = score - 0.01;
                    semantic.put(st[j], score);
                }
            }

        }

        HashSet<String> releventDocs = new HashSet<>();
        ArrayList<HashMap<String, Double>> scorePerTerm = new ArrayList<>();
        for (String term : titleTerms) {
            HashMap<String, Double> termScores = new HashMap<>();
            double idf = 0;
            if (Indexer.dictionary.containsKey(term)) {
                HashMap<String, Double> terms = getTfs(term.toLowerCase().charAt(0), Indexer.dictionary.get(term).getLineAtPostingfile());
                // System.out.println(terms);
                idf = Indexer.dictionary.get(term).getIdf();
                for (String st : terms.keySet()) {
                    int docid = Document.docCollection.get(Document.docIdToDocNumber.get(st)).getUniqueWords();
                    double tf = terms.get(st);
                    if (isSemanic) {
                        if (semantic.containsKey(st)) {
                            tf = tf * semantic.get(st);
                        }
                    }
                    double add = ranker.bm25(tf, idf, docid);
                    releventDocs.add(st);
                    termScores.put(st, add);

                }

            }
            scorePerTerm.add(termScores);
        }
        HashMap<String, Double> sorteddocs = new HashMap<>();
        for (String st : releventDocs) {

            double valueScore = 0;
            double i=1;
            for (HashMap<String, Double> map : scorePerTerm) {
                if (map.containsKey(st)) {
                    valueScore += (map.get(st))*i;
                    if(!withstem) {
                        if (!description)
                            i = i * 0.9;
                        else
                            i = i * 0.8;
                    }else{
                        if (!description)
                            i = i * 1.05;
                    }
                }
            }
            sorteddocs.put(st, valueScore);
        }
        //System.out.println(sorteddocs);
        // sorteddocs = sortDocs(sorteddocs);
        //System.out.println(sorteddocs);
        return sorteddocs;
    }

    /**
     *
     * @param hash of docs
     * @return the top 50 values hashmap
     */

    public static HashMap<String, Double> sortDocs(HashMap<String, Double> hash) {
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(hash.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                if (o1.getValue() > o2.getValue())
                    return -1;           // Neither val is NaN, thisVal is smaller
                if (o1.getValue() < o2.getValue())
                    return 1;
                return 0;
            }
        });

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (int i = 0; i < 50; i++) {
            if (i < list.size())
                temp.put(list.get(i).getKey(), list.get(i).getValue());

        }

        return temp;


    }

    /**
     *
     * @param firstLetter
     * @param line_pointer
     * @return hash map of docs with there tf's according to term
     * @throws IOException
     */
    public HashMap<String, Double> getTfs(char firstLetter, int line_pointer) throws IOException {
        File file = new File(Indexer.postPath + "//" + firstLetter + ".txt");
        HashMap<String, Double> terms = new HashMap<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        int i = 1;
        while (i < line_pointer) {
            bufferedReader.readLine();
            i++;
        }
        String line = bufferedReader.readLine().split("&")[1];
        //System.out.println(line);
        String[] docs = line.split(" ");
        int sum = 0;
        for (int j = 0; j < docs.length; j++) {


            String ID = Document.docCollection.get(Integer.parseInt((docs[j].split("#")[0])) + sum).getName();

            double tf = (Integer.parseInt(docs[j].split("#")[1]));
            double tf2 = tf / (Document.docCollection.get(Document.docIdToDocNumber.get(ID)).getMax_tf());
            //tf = tf / (Document.docCollection.get(Document.docIdToDocNumber.get(ID)).getUniqueWords());

            terms.put(ID, (tf2));
            sum = sum + Integer.parseInt(docs[j].split("#")[0]);


        }
        return terms;
    }


}
