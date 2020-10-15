package Model;

import org.omg.CORBA.MARSHAL;

import java.text.DecimalFormat;

/**
 * all info we save about a term in the dictinary
 */
public class TermInfo {
   private int numOfDocs;
   private int numOfCorpusApperances;
   private int lineAtPostingfile;
   private double idf;

    public TermInfo(int numOfCorpusApperances) {
        this.numOfDocs= 1;
        this.numOfCorpusApperances = numOfCorpusApperances;
    }

    public TermInfo(int numOfDocs, int numOfCorpusApperances, int lineAtPostingfile, double idf) {
        this.numOfDocs = numOfDocs;
        this.numOfCorpusApperances = numOfCorpusApperances;
        this.lineAtPostingfile = lineAtPostingfile;
        this.idf = idf;
    }

    public int getNumOfDocs() {
        return numOfDocs;
    }

    public int getNumOfCorpusApperances() {
        return numOfCorpusApperances;
    }

    public int getLineAtPostingfile() {
        return lineAtPostingfile;
    }

    public double getIdf() {
        return idf;
    }

    public void setNumOfDocs(int numOfDocs) {
        this.numOfDocs = numOfDocs;
    }

    public void setNumOfCorpusApperances(int numOfCorpusApperances) {
        this.numOfCorpusApperances = numOfCorpusApperances;
    }

    public void setLineAtPostingfile(int lineAtPostingfile) {
        this.lineAtPostingfile = lineAtPostingfile;
    }

    public void setIdf() {
        DecimalFormat d3 = new DecimalFormat("#.###");

       this.idf= Double.parseDouble(d3.format(Math.log((Document.docCollection.size()/numOfDocs))));

    }
}
