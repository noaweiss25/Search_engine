package Model;

/**
 * instance of query type
 */
public class Query {
    int ID;
    String title;
    String decs;
    String narr;

    public Query(int ID, String title, String decs, String narr) {
        this.ID = ID;
        this.title = title;
        this.decs = decs;
        this.narr = narr;
    }

    public int getID() {
        return ID;
    }

    public String getTitle() {
        return title;
    }

    public String getDecs() {
        return decs;
    }

    public String getNarr() {
        return narr;
    }
}
