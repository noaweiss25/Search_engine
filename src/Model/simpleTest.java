package Model;

import com.medallia.word2vec.Word2VecModel;

import java.io.*;
import java.sql.Array;
import java.sql.Time;
import java.text.Collator;
import java.text.ParseException;
import java.time.Clock;
import java.util.*;

public class simpleTest {
    public simpleTest() throws IOException, ParseException {
    }

    public static void main(String[] args) throws Exception {
        //  String s = "C:\\Users\\aa\\corpus";
        //  String a="D:\\documents\\users\\weissf\\Downloads\\corpus\\corpus";
        //Model.ReadFile x = new Model.ReadFile(a);
/*
        String b="D:\\documents\\users\\weissf\\Downloads\\stop_words.txt";
        long startTime = System.nanoTime();
        Model.Tokenizer token= new Model.Tokenizer(b);
        System.out.println(token.isNumberType("a3-13"));
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        File stop = new File("D:\\documents\\users\\weissf\\Downloads\\doc1.txt");
        File stop2 = new File("D:\\documents\\users\\weissf\\Downloads\\doc2.txt");
        BufferedReader br = new BufferedReader(new FileReader(stop));
        String st;
        while ((st = br.readLine()) != null ) {
            sb.append(st).append("\n");

        }
        BufferedReader br2 = new BufferedReader(new FileReader(stop2));
        String st2;
        while ((st2 = br2.readLine()) != null ) {
            sb2.append(st2).append("\n");

        }


       HashMap<String,Integer> toIndex = new HashMap<>();

       //toIndex = token.Tokenize(sb.toString());
       // System.out.println("Doc1\n"+toIndex);
       toIndex = token.Tokenize(sb2.toString());
        System.out.println("DOC2 terms :"+toIndex);
        System.out.println("DOC2 size :"+toIndex.size());
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;
        System.out.println(totalTime/(1000000));
*/
        String path = "D:\\documents\\users\\sigeli\\Downloads\\corpus";
        String posting = "D:\\documents\\users\\sigeli\\Downloads\\NewGoogleFX\\posting";
        // Indexer indexer = new Indexer(path,posting,true);

        //  Model.Tokenizer tokenizer = new Model.Tokenizer("C:\\Users\\sigeli\\Downloads\\google\\stop.txt");
    /*
        BufferedReader br2 = new BufferedReader(new FileReader("D:\\documents\\users\\sigeli\\Downloads\\party.txt"));
        String st2;
        StringBuilder sb2 = new StringBuilder();
        while ((st2 = br2.readLine()) != null ) {
            sb2.append(st2).append("\n");

        }
       String[] a = sb2.toString().split(" ");
        ArrayList<String> b = new ArrayList<>();
        for (int i = 0; i <a.length ; i++) {
            b.add(a[i]);
        }
    //tokenizer.HandleTerms(b);
    */
        //indexer.mostFruc(); C:\Users\Public\noa\NewGoogleFX

        HashMap<String, Double> save = new HashMap<>();
        Query quaaery = new Query(1,"world wars","","");
        Indexer indexer = new Indexer("d:\\documents\\users\\sigeli\\Downloads\\corpus", "d:\\documents\\users\\sigeli\\Downloads\\posting", false);
        System.out.println("uploading");
        indexer.upload(false);
        System.out.println("done");
        //Document d = Document.docCollection.get(Document.docIdToDocNumber.get("FT923-10"));
        // System.out.println(d.returnTop5());
        String resultPath = "d:\\documents\\users\\sigeli\\Downloads\\New folder\\NewGoogleFX_1451\\NewGoogleFX_1933\\NewGoogleFX";

        Searcher searcher = new Searcher(resultPath, "d:\\documents\\users\\sigeli\\Downloads\\corpus", false, true, true);
        List<Query> Q = new LinkedList<>();
        Q.add(quaaery);
        int i = 1;
        for (Query qu : Q) {
            long time = System.currentTimeMillis();
            Set<String> keyset = searcher.calculate(qu).keySet();
            System.out.println(i + " time " + (System.currentTimeMillis() - time));
        }


    }
}