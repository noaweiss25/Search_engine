package Model;

import java.awt.image.BufferedImageOp;
import java.io.*;
import java.util.*;

/**
 * document represntaion classs
 */
public class Document {
    public static HashMap<Integer, Document> docCollection = new HashMap<>(); // all docs by given count
    public static HashMap<String, Integer> docIdToDocNumber = new HashMap<>();//all  docs id counts
    private String name;
    private int uniqueWords;
    private int max_tf;
    private int doc_Size;
    private String most_fq_Term;

    public Document(String name, int doc_Size, int uniqueWords) {
        this.name = name;
        this.doc_Size = doc_Size;
        this.uniqueWords = uniqueWords;
    }

    public Document(String name, int uniqueWords, int max_tf, int doc_Size, String most_fq_Term) {
        this.name = name;
        this.uniqueWords = uniqueWords;
        this.max_tf = max_tf;
        this.doc_Size = doc_Size;
        this.most_fq_Term = most_fq_Term;
    }

    public Set<String> returnTop5() throws IOException {
        HashMap<String, Double> top5 = new HashMap<>();
        BufferedReader readEnt= new BufferedReader(new FileReader(new File(Indexer.postPath+"\\topEnt.txt")));
        String st="";
        String theChooce="";
        while (!(st.equals(this.getName().trim()))&&theChooce!=null){
            theChooce =readEnt.readLine();
            try {
                st = theChooce.split("&")[0].trim();
            }catch (Exception e){
                System.out.println(theChooce);
            }

        }
        if(theChooce==null){
            return null;
        }
        System.out.println(theChooce);
        String[] enti= theChooce.split("&")[1].substring(1,theChooce.length()-2-theChooce.split("&")[0].length()).split(",");
        for (String s:enti) {
            System.out.println(s);
            top5.put(s.split("#")[0],Double.parseDouble(s.split("#")[1]));
        }
        return sortDocs(top5).keySet();
    }

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
        for (int i = 0; i < 5; i++) {
            if (i < list.size())
                temp.put(list.get(i).getKey(), list.get(i).getValue());
        }
        return temp;


    }


    public String getName() {
        return name;
    }

    public void setUniqueWords(int uniqueWords) {
        this.uniqueWords = uniqueWords;
    }

    public void setMax_tf(int max_tf) {
        this.max_tf = max_tf;
    }

    public void setMost_fq_Term(String most_fq_Term) {
        this.most_fq_Term = most_fq_Term;
    }

    public static HashMap<Integer, Document> getDocCollection() {
        return docCollection;
    }

    public static HashMap<String, Integer> getDocIdToDocNumber() {
        return docIdToDocNumber;
    }

    public int getUniqueWords() {
        return uniqueWords;
    }

    public int getMax_tf() {
        return max_tf;
    }

    public int getDoc_Size() {
        return doc_Size;
    }

    public String getMost_fq_Term() {
        return most_fq_Term;
    }

    @Override
    public String toString() {
        return name + " " + uniqueWords + " " + max_tf + " " + doc_Size + "_" + most_fq_Term;
    }
}
