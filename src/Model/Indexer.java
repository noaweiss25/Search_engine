package Model;

import javax.sound.midi.Soundbank;
import java.io.*;
import java.lang.reflect.Array;
import java.text.Collator;
import java.text.ParseException;
import java.util.*;

public class Indexer {
    public static HashMap<String, TermInfo> dictionary = new HashMap<>();
    private HashSet<String> docIdReduction = new HashSet<>();
    private HashMap<String, Integer> idSaver = new HashMap<>();
    private TreeMap<String, String> termList;
    private ReadFile readFile;
    private Tokenizer parser;
    public static String postPath;
    private Comparator<String> comp;
    private boolean newRound = false;
    private int chunkIndex;
    public static HashSet<String> approvedEntities = new HashSet<>();
    public static HashMap<String, LinkedList<String>> topEnti = new HashMap<>();
    public boolean ifstemmer;

    public Indexer(String path, String postPath, boolean ifStemmer) throws IOException, ParseException {
        ifstemmer=ifStemmer;
        termList = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
        readFile = new ReadFile(path + "\\corpus");
        parser = new Tokenizer(path, ifStemmer);
        this.postPath = postPath;
        comp = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return 0;
            }
        };
    }

    /**
     * this function fills the dictionary and responsible of creating the posting files
     *
     * @throws IOException
     * @throws ParseException
     */
    public void fillDictionary() throws IOException, ParseException {
        chunkIndex = 1;
        while (!readFile.isAllFilesDone()) {
            ArrayList<String> docs = readFile.readChunk();
            int i = 0;
            HashMap<String, Integer> tmpDoc = new HashMap<>();
            while (i < docs.size()) {
                tmpDoc = parser.Tokenize(docs.get(i));
                for (String term : tmpDoc.keySet()) {

                    int values = tmpDoc.get(term);
                    String postinglist = parser.getDocCounter() + "#" + values;//maybe doc counter + doc counter before
                    addValueToPost(term, postinglist, values);
                    addValueToDic(term, values);
                }
                tmpDoc.clear();
                i++;
            }
            newRound = true;
            System.out.println("i finished my :" + chunkIndex + "chunks");
            WriteToPostingFile(chunkIndex);
            termList.clear();
            docIdReduction.clear();
            readFile.clearDocsToParse();
            chunkIndex++;

        }
        idSaver.clear();
        merge();
        CreatePostingFiles(postPath);
        writeDocInfo();
        parser.entities.clear();
    }

    /**
     * add string value to posting file
     *
     * @param term
     * @param add
     * @param value
     */
    public void addValueToPost(String term, String add, int value) {

        int reduct = 0;
        boolean firstTimeEntity = false;
        if (parser.entities.containsKey(term) && parser.entities.get(term)[2] != 2) {
            termList.put(term, term + "&" + parser.entities.get(term)[0] + "#" + parser.entities.get(term)[1]);
            idSaver.put(term, parser.entities.get(term)[0]);
            int[] addFirstEnt = new int[]{parser.entities.get(term)[0], parser.entities.get(term)[1], 2};
            parser.entities.replace(term, addFirstEnt);
            TermInfo toadd = new TermInfo(value);
            dictionary.put(term, toadd);
            firstTimeEntity = true;
            int numDoc = parser.entities.get(term)[0];
            LinkedList entiForFirstTime = new LinkedList();
            entiForFirstTime.add(term+"#"+parser.entities.get(term)[1]);

            LinkedList enti = new LinkedList();
            enti.add(term+"#"+value);

            if (topEnti.containsKey(Document.docCollection.get(numDoc).getName())) {
                LinkedList e = topEnti.get(Document.docCollection.get(numDoc).getName());
                e.add(term+"#"+parser.entities.get(term)[1]);
                topEnti.replace(Document.docCollection.get(numDoc).getName(), topEnti.get(Document.docCollection.get(numDoc).getName()), e);
            } else {
                topEnti.put(Document.docCollection.get(numDoc).getName(), entiForFirstTime);
            }
            if (topEnti.containsKey(Document.docCollection.get(parser.getDocCounter()).getName())) {
                LinkedList e = topEnti.get(Document.docCollection.get(parser.getDocCounter()).getName());
                e.add(term+"#"+value);

                topEnti.replace(Document.docCollection.get(parser.getDocCounter()).getName(), topEnti.get(Document.docCollection.get(parser.getDocCounter()).getName()), e);
            } else {
                topEnti.put(Document.docCollection.get(parser.getDocCounter()).getName(), enti);
            }
        } else if (parser.entities.containsKey(term) && parser.entities.get(term)[2] == 2) {
            LinkedList enti = new LinkedList();
            enti.add(term+"#"+value);
            if (topEnti.containsKey(Document.docCollection.get(parser.getDocCounter()).getName())) {
                LinkedList e = topEnti.get(Document.docCollection.get(parser.getDocCounter()).getName());
                e.add(term+"#"+value);
                topEnti.replace(Document.docCollection.get(parser.getDocCounter()).getName(), topEnti.get(Document.docCollection.get(parser.getDocCounter()).getName()), e);
            } else
                topEnti.put(Document.docCollection.get(parser.getDocCounter()).getName(), enti);
        }

        if (docIdReduction.contains(term.toLowerCase())) {
            reduct = parser.getDocCounter() - idSaver.get(term.toLowerCase());

            termList.replace(term.toLowerCase(), termList.get(term.toLowerCase()) + " " + reduct + "#" + value);
            idSaver.replace(term.toLowerCase(), parser.getDocCounter());
        } else if (docIdReduction.contains(term.toUpperCase())) {
            if (Character.isUpperCase(term.charAt(0))) {
                if (!idSaver.containsKey(term.toUpperCase()) && idSaver.containsKey(term.toLowerCase()))
                    reduct = parser.getDocCounter() - idSaver.get(term.toLowerCase());

                else
                    reduct = parser.getDocCounter() - idSaver.get(term.toUpperCase());

                termList.replace(term, termList.get(term) + " " + reduct + "#" + value);
                idSaver.replace(term, parser.getDocCounter());
            } else {
                reduct = 0;
                if (!idSaver.containsKey(term.toUpperCase()) && idSaver.containsKey(term.toLowerCase())) {
                    reduct = parser.getDocCounter() - idSaver.get(term.toLowerCase());
                } else
                    reduct = parser.getDocCounter() - idSaver.get(term.toUpperCase());
                String x = termList.get(term.toUpperCase()) + " " + reduct + "#" + value;

                termList.remove(term.toUpperCase());
                termList.put(term, x);
                idSaver.remove(term.toUpperCase());
                idSaver.put(term, parser.getDocCounter());
                docIdReduction.remove(term.toUpperCase());
                docIdReduction.add(term);
            }
        } else if (docIdReduction.contains(term)) {
            try {
                reduct = parser.getDocCounter() - idSaver.get(term);
            } catch (NullPointerException e) {
                System.out.println(term);
            }

            termList.replace(term, termList.get(term) + " " + reduct + "#" + value);
            idSaver.replace(term, parser.getDocCounter());
        } else {
            docIdReduction.add(term);

            if (idSaver.containsKey(term.toLowerCase())) {
                reduct = parser.getDocCounter() - idSaver.get(term.toLowerCase());

                termList.put(term, term + "&" + reduct + "#" + value);
            } else if (idSaver.containsKey(term.toUpperCase())) {
                reduct = parser.getDocCounter() - idSaver.get(term.toUpperCase());
                if (firstTimeEntity) {
                    termList.replace(term, termList.get(term.toUpperCase()) + " " + reduct + "#" + value);
                    firstTimeEntity = false;
                } else
                    termList.put(term, term + "&" + reduct + "#" + value);

            } else {
                termList.put(term, term + "&" + add);
            }
        }

    }

    /**
     * add value to he dictionary
     *
     * @param term
     * @param values
     */
    ////////entites check for different enternce
    public void addValueToDic(String term, int values) {
        if (dictionary.containsKey(term.toLowerCase())) {
            TermInfo toadd = dictionary.get(term.toLowerCase());
            toadd.setNumOfDocs(toadd.getNumOfDocs() + 1);
            toadd.setNumOfCorpusApperances(toadd.getNumOfCorpusApperances() + values);
            dictionary.replace(term.toLowerCase(), toadd);
            idSaver.replace(term.toLowerCase(), parser.getDocCounter());
        } else if (dictionary.containsKey(term.toUpperCase())) {
            if (Character.isUpperCase(term.charAt(0))) {
                TermInfo toadd = dictionary.get(term.toUpperCase());
                toadd.setNumOfDocs(toadd.getNumOfDocs() + 1);
                toadd.setNumOfCorpusApperances(toadd.getNumOfCorpusApperances() + values);
                dictionary.replace(term, toadd);
                idSaver.replace(term, parser.getDocCounter());
            } else {
                TermInfo toadd = dictionary.get(term.toUpperCase());
                toadd.setNumOfDocs(toadd.getNumOfDocs() + 1);
                toadd.setNumOfCorpusApperances(toadd.getNumOfCorpusApperances() + values);
                dictionary.remove(term.toUpperCase());
                dictionary.put(term, toadd);
                idSaver.remove(term.toUpperCase());
                idSaver.put(term, parser.getDocCounter());

            }
        } else {
            TermInfo toadd = new TermInfo(values);
            dictionary.put(term, toadd);
            idSaver.put(term, parser.getDocCounter());
        }
    }

    /**
     * create a dedicated posting file
     *
     * @param chunkIndex file posting name
     */
    public void WriteToPostingFile(int chunkIndex) {
        File postingfile = new File(postPath + "\\" + chunkIndex + ".txt");
        BufferedWriter bW = null;
        try {
            bW = new BufferedWriter(new FileWriter(postingfile));
            for (String post : termList.values()) {
                bW.write(post);
                bW.newLine();
            }
            bW.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    /**
     * create a-Z posting files with dictionary and docs info , also create 2 files for doc display(2 kinds of sort)
     *
     * @param path
     * @throws IOException
     */
    public void CreatePostingFiles(String path) throws IOException {
        File ent = new File(postPath + "//topEnt.txt");
        BufferedWriter writeTopEnt = new BufferedWriter(new FileWriter(ent));
        for(String s:topEnti.keySet()){
            writeTopEnt.write(s+"&"+topEnti.get(s).toString());
            writeTopEnt.newLine();
        }
        writeTopEnt.flush();
        writeTopEnt.close();

        char fileName = 'a';
        int counter = 1;
        File posting = new File(postPath).listFiles()[0];
        BufferedReader fullPost = new BufferedReader(new FileReader(posting));
        System.gc();
        String stringDic="";
        if(ifstemmer){
            stringDic=postPath + "//stemDictionary.txt";
        }
        else{
            stringDic=postPath + "//dictionary.txt";
        }
        File dic = new File(stringDic);
        File dicToShow = new File(postPath + "//dicToShow.txt");
        BufferedWriter writeForDocToShow = new BufferedWriter(new FileWriter(dicToShow));
        BufferedWriter writeForDoc = new BufferedWriter(new FileWriter(dic));
        for (int i = 0; i < 27; i++) {
            if (i == 0) {
                File file = new File(path + "//numbers+signs.txt");
                BufferedWriter bW = new BufferedWriter(new FileWriter(file));
                String st = "";
                while ((st = fullPost.readLine()) != null && st.charAt(0) != 'a' && st.charAt(0) != 'A') {
                    String split = st.split("&")[0];
                    if (dictionary.containsKey(split.toLowerCase())) {
                        TermInfo ti = dictionary.get(split.toLowerCase());
                        dictionary.get(split.toLowerCase()).setLineAtPostingfile(counter);
                        dictionary.get(split.toLowerCase()).setIdf();
                        writeForDoc.write(split.toLowerCase() + "_" + ti.getNumOfDocs() + " " + ti.getNumOfCorpusApperances() + " " + ti.getLineAtPostingfile() + " " + ti.getIdf());
                        writeForDoc.newLine();
                        writeForDocToShow.write(split.toLowerCase() + " -Total appearances: " + ti.getNumOfCorpusApperances());
                        writeForDocToShow.newLine();
                    } else if (dictionary.containsKey(split.toUpperCase())) {
                        TermInfo ti = dictionary.get(split.toUpperCase());
                        dictionary.get(split.toUpperCase()).setLineAtPostingfile(counter);
                        dictionary.get(split.toUpperCase()).setIdf();
                        writeForDoc.write(split.toUpperCase() + "_" + ti.getNumOfDocs() + " " + ti.getNumOfCorpusApperances() + " " + ti.getLineAtPostingfile() + " " + ti.getIdf());
                        writeForDoc.newLine();
                        writeForDocToShow.write(split.toUpperCase() + " -Total appearances: " + ti.getNumOfCorpusApperances());
                        writeForDocToShow.newLine();
                    } else if (dictionary.containsKey(split)) {
                        TermInfo ti = dictionary.get(split);
                        dictionary.get(split).setLineAtPostingfile(counter);
                        dictionary.get(split).setIdf();
                        writeForDoc.write(split + "_" + ti.getNumOfDocs() + " " + ti.getNumOfCorpusApperances() + " " + ti.getLineAtPostingfile() + " " + ti.getIdf());
                        writeForDoc.newLine();
                        writeForDocToShow.write(split + " -Total appearances: " + ti.getNumOfCorpusApperances());
                        writeForDocToShow.newLine();
                    }
                    counter++;
                    bW.write(st);
                    bW.newLine();
                }
                bW.flush();
                bW.close();
                counter = 1;

            } else {
                File file = new File(path + "//" + fileName + ".txt");
                BufferedWriter bW = new BufferedWriter(new FileWriter(file));
                String st = "";
                while ((st = fullPost.readLine()) != null && st.charAt(0) != (fileName + 1) && st.charAt(0) != Character.toUpperCase(fileName + 1)) {
                    String split = st.split("&")[0];
                    if (dictionary.containsKey(split.toLowerCase())) {
                        TermInfo ti = dictionary.get(split.toLowerCase());
                        dictionary.get(split.toLowerCase()).setLineAtPostingfile(counter);
                        dictionary.get(split.toLowerCase()).setIdf();
                        writeForDoc.write(split.toLowerCase() + "_" + ti.getNumOfDocs() + " " + ti.getNumOfCorpusApperances() + " " + ti.getLineAtPostingfile() + " " + ti.getIdf());
                        writeForDoc.newLine();
                        writeForDocToShow.write(split.toLowerCase() + " -Total appearances: " + ti.getNumOfCorpusApperances());
                        writeForDocToShow.newLine();
                    } else if (dictionary.containsKey(split.toUpperCase())) {
                        TermInfo ti = dictionary.get(split.toUpperCase());
                        dictionary.get(split.toUpperCase()).setLineAtPostingfile(counter);
                        dictionary.get(split.toUpperCase()).setIdf();
                        writeForDoc.write(split.toUpperCase() + "_" + ti.getNumOfDocs() + " " + ti.getNumOfCorpusApperances() + " " + ti.getLineAtPostingfile() + " " + ti.getIdf());
                        writeForDoc.newLine();
                        writeForDocToShow.write(split.toUpperCase() + " -Total appearances: " + ti.getNumOfCorpusApperances());
                        writeForDocToShow.newLine();
                    }
                    bW.write(st);
                    bW.newLine();
                    counter++;
                }
                bW.flush();
                bW.close();
                fileName += 1;
                counter = 1;
            }
        }
        writeForDoc.flush();
        writeForDoc.close();
        writeForDocToShow.flush();
        writeForDocToShow.close();
        fullPost.close();
        posting.delete();

        sortidioticpeopele();


    }

    /**
     * sort by type not by alphabet  the dictionary to show
     *
     * @throws IOException
     */
    public void sortidioticpeopele() throws IOException {
        String[] terms = new String[dictionary.size()];
        File dicToShow = new File(postPath + "//dicToShow2.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(postPath + "//dicToShow2.txt"));
        Set<String> x = dictionary.keySet();
        terms = x.toArray(terms);
        System.gc();
        Arrays.sort(terms);
        for (int i = 0; i < terms.length; i++) {
            terms[i] = terms[i] + " -Total appearances: " + dictionary.get(terms[i]).getNumOfCorpusApperances();
            bw.write(terms[i]);
            bw.newLine();
        }
        bw.flush();
        bw.close();


    }

    /**
     * merge all the posting files
     *
     * @throws IOException
     */
    public void merge() throws IOException {
        File[] posting = new File(postPath).listFiles();
        Arrays.sort(posting, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (Integer.parseInt(o1.getName().substring(0, o1.getName().length() - 4)) > Integer.parseInt(o2.getName().substring(0, o2.getName().length() - 4))) {
                    return 1;
                }
                return -1;
            }
        });
        System.gc();
        boolean winnerfileisFirst = false;

        for (int i = 0; i < posting.length; i++) {
            File sortedpost = new File(postPath + "//full" + (i) + ".txt");
            BufferedWriter bW = new BufferedWriter(new FileWriter(sortedpost));
            if (i == 0) {
                merge2files(bW, new FileReader(posting[0]), new FileReader(posting[1]));
                posting[0].delete();
                posting[1].delete();
                i++;

            } else {
                File x = null;
                if (i == 2) {
                    x = new File(postPath + "//full" + (i - 2) + ".txt");
                } else
                    x = new File(postPath + "//full" + (i - 1) + ".txt");
                FileReader fullPost = new FileReader(x);
                merge2files(bW, fullPost, new FileReader(posting[i]));
                posting[i].delete();
                x.delete();
            }
            bW.flush();
            bW.close();


        }
    }

    private void merge2files(BufferedWriter bW, FileReader full, FileReader next) throws IOException {
        boolean winnerfileisFirst = false;
        BufferedReader first = new BufferedReader(full);
        BufferedReader second = new BufferedReader(next);
        String firstSeq = first.readLine();
        String secondSeq = second.readLine();
        String firstTerm = firstSeq.split("&")[0];
        String secondTerm = secondSeq.split("&")[0];
        while ((firstSeq) != null && (secondSeq) != null) {

            if (firstTerm.compareToIgnoreCase(secondTerm) == 0) {
                firstSeq = firstSeq + " " + secondSeq.split("&")[1];
                bW.write(firstSeq);
                bW.newLine();
                firstSeq = first.readLine();
                if (firstSeq != null) {
                    firstTerm = firstSeq.split("&")[0];
                } else {
                    winnerfileisFirst = true;
                }
                secondSeq = second.readLine();
                if (secondSeq != null)
                    secondTerm = secondSeq.split("&")[0];
            } else if (firstTerm.compareToIgnoreCase(secondTerm) > 0) {
                bW.write(secondSeq);
                bW.newLine();
                secondSeq = second.readLine();
                if (secondSeq != null)
                    secondTerm = secondSeq.split("&")[0];
            } else if (firstTerm.compareToIgnoreCase(secondTerm) < 0) {
                bW.write(firstSeq);
                bW.newLine();
                firstSeq = first.readLine();
                if (firstSeq != null) {
                    firstTerm = firstSeq.split("&")[0];
                } else {
                    winnerfileisFirst = true;
                }
            }
        }
        if (!winnerfileisFirst) {
            while (firstSeq != null) {
                bW.write(firstSeq);
                bW.newLine();
                firstSeq = first.readLine();
            }
        } else {
            while (secondSeq != null) {
                bW.write(secondSeq);
                bW.newLine();
                secondSeq = second.readLine();
            }
        }
        full.close();
        next.close();
    }

    /**
     * delete al data stractures
     */
    public void clearRam() {
        dictionary.clear();
        Document.docCollection.clear();
        Document.docIdToDocNumber.clear();
        topEnti.clear();

    }

    /**
     * upload infromation of a specific indexed corpus
     *
     * @throws IOException
     */
    public void upload(boolean isStem) throws IOException {
        File dic;
        if(isStem){
            dic = new File(postPath + "//stemDictionary.txt");
        }
        else{
            dic = new File(postPath + "//dictionary.txt");
        }
        BufferedReader fullPost = new BufferedReader(new FileReader(dic));
        File info = new File(postPath + "//DocInfo.txt");
        BufferedReader infoDoc = new BufferedReader(new FileReader(info));
        System.gc();
        String st = "";
        String st2 = "";
        while ((st = fullPost.readLine()) != null) {
            String[] termsplitter = st.split("_");
            String[] terminfo = termsplitter[1].split(" ");
            TermInfo ti = new TermInfo(Integer.parseInt(terminfo[0]), Integer.parseInt(terminfo[1]), Integer.parseInt(terminfo[2]), Double.parseDouble(terminfo[3]));
            dictionary.put(termsplitter[0], ti);
        }
        st2 = infoDoc.readLine();

        st2 = st2.substring(1, st2.length() - 1);
        String[] docInfo = st2.split(", ");
        for (int i = 0; i < docInfo.length; i++) {
            String[] doc = docInfo[i].split("=");
            String[] docDitForName = null;
            docDitForName = doc[1].split("_");
            if (docDitForName.length == 1) {
                String save = docDitForName[0];
                docDitForName = new String[]{save, "No Words"};
            }
            String[] docDit = docDitForName[0].split(" ");
            Document document;
            if (docDit[0].length() != 0) {
                document = new Document(docDit[0], Integer.parseInt(docDit[1]), Integer.parseInt(docDit[2]), Integer.parseInt(docDit[3]), docDitForName[1]);
                Document.docCollection.put(Integer.parseInt(doc[0].substring(0)), document);
                Document.docIdToDocNumber.put(docDit[0], Integer.parseInt(doc[0]));
            } else {
                document = new Document(docDit[1], Integer.parseInt(docDit[3]), Integer.parseInt(docDit[4]), Integer.parseInt(docDit[5]), docDitForName[1]);
                Document.docCollection.put(Integer.parseInt(doc[0].substring(0)), document);
                Document.docIdToDocNumber.put(docDit[1], Integer.parseInt(doc[0]));
            }


        }
        File ent = new File(postPath + "//Entitties.txt");
        BufferedReader infoent = new BufferedReader(new FileReader(ent));
        String ls;
        while ((ls = infoent.readLine()) != null) {
            approvedEntities.add(ls);
        }

    }

    private void writeDocInfo() throws IOException {
        System.gc();
        File doc = new File(postPath + "//DocInfo.txt");
        File ent = new File(postPath + "//Entitties.txt");
        BufferedWriter fullPostent = new BufferedWriter(new FileWriter(ent));
        BufferedWriter fullPost = new BufferedWriter(new FileWriter(doc));
        fullPost.write(Document.docCollection.toString());
        fullPost.flush();
        fullPost.close();
        String[] entnames = new String[parser.entities.size()];
        entnames = parser.entities.keySet().toArray(entnames);
        //ArrayList<String> onlyProved = new ArrayList<>();
        for (String enti : parser.entities.keySet()) {
            if ((parser.entities.get(enti)[2]) == 2) {
                fullPostent.write(enti);
                fullPostent.newLine();
            }
        }

        fullPostent.flush();
        fullPostent.close();

    }

}

