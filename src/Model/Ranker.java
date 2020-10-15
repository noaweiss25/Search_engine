package Model;

/**
 * ranker of a term using bm25 technique
 */
public class Ranker {
    double avergeDocSize;
    double numOfDocs;
    double b;
    double k;

    public Ranker() {
        int sum=0;
        this.numOfDocs = Document.docCollection.size();
        for (Document d:Document.docCollection.values()) {
            sum=sum+d.getUniqueWords();

        }

        this.avergeDocSize=sum/numOfDocs;
        System.out.println(avergeDocSize);
        this.b = 0.005;
        this.k = 0.07;//maybe change
    }

    /**
     * return value of bm25 on a specific term on a specific doc
     * @param tf
     * @param idf
     * @param docUniqueSize
     * @return
     */
    public double bm25(double tf,double idf,int docUniqueSize){
        idf=idf;
        double mone= tf*(k+1);
        double relAvg=docUniqueSize/avergeDocSize;
        double mehane=tf+k*(1-b+b*relAvg);
        double answer =mone/mehane;
        return idf*answer;
    }



}
