package Model;//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Model.Document;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.util.ArrayList;

public class ReadFile {
    private ArrayList<String> docsToParse = new ArrayList<>();
    //private Model.Tokenizer tokenizer = new Model.Tokenizer("C:\\Users\\sigeli\\Downloads\\google\\google\\stop.txt");
    private ArrayList<File> files = new ArrayList<>();
    private int fileNo;

    public ReadFile(String path) throws IOException, ParseException {
        File[] corpus = new File(path).listFiles((File::isDirectory));
        for (File file : corpus) {
            File[] allFiles = file.listFiles();
            for (int i = 0; i < allFiles.length; i++) {
                files.add(allFiles[i]);
            }
        }
        fileNo = 0;

    }

    /**
     * read a chunk of 8 files
     * @return docs list of those 8 files
     * @throws ParseException
     * @throws IOException
     */
    public ArrayList<String> readChunk() throws ParseException, IOException {
        for (int j = fileNo; j < files.size() && j <8 + fileNo; j++) {
            File x = files.get(j);
            String[] allFileDocs = (new String(Files.readAllBytes(x.toPath()))).split("<DOC>");

            for (int i = 0; i < allFileDocs.length; i++) {
                if (allFileDocs[i].contains("<TEXT>")) {
                    docsToParse.add(allFileDocs[i]);

                }
            }

        }
        fileNo+=8;
        return docsToParse;

        /*
        int i = 0;
        while (i < docsToParse.size()) {
            tokenizer.Tokenize(docsToParse.get(i));
            i++;
        }
         docsToParse.clear();
*/

    }

    /**
     * clean data stracture
     */
    public void clearDocsToParse(){
        docsToParse.clear();
    }

    /**
     *
     * @return if all files done checked
     */
    public boolean isAllFilesDone(){
        if(fileNo>=files.size()-1){
            return true;
        }
        return false;
    }

}


