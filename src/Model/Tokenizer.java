package Model;


import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;
import java.io.*;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;


public class Tokenizer {
    public HashSet<String> stopWord = new HashSet<>();
    private HashSet<String> stopWordCapital = new HashSet<>();
    private HashMap<String, Integer> toIndex = new HashMap<>();
    public HashMap<String, int[]> entities = new HashMap<>();
    private HashSet<String> dollarFormat = new HashSet<>();
    private HashSet<String> distance = new HashSet<>();
    private HashSet<String> weigh = new HashSet<>();
    private HashSet<String> Months = new HashSet<>();
    private HashSet<String> numFollowersCheck = new HashSet<>();
    private HashSet<Character> ignoreThem= new HashSet<>();
    private int docCounter = 0;
    private englishStemmer stem;
    private boolean toStem;

    public Tokenizer(String path,boolean toStem) throws IOException {
        File stop = new File(path+"//stop_words.txt");
        BufferedReader br = new BufferedReader(new FileReader(stop));
        String st;
        stem = new englishStemmer();
        this.toStem = toStem;
        while ((st = br.readLine()) != null) {
            stopWord.add(st);
            String c = st.charAt(0) + "";
            String st2 = c.toUpperCase() + st.substring(1);
            stopWordCapital.add(st2);

        }
        char add ='A';
        for (int i = 0; i < 26 ; i++) {
            stopWordCapital.add(add+"");
            add++;

        }
        stopWordCapital.remove("May");//check
        stopWord.remove("m");//check
        dollarFormat.add("million");
        dollarFormat.add("trillion");
        dollarFormat.add("billion");
        dollarFormat.add("bn");
        dollarFormat.add("m");
        dollarFormat.add("dollars");
        dollarFormat.add("u.s");


        distance.add("km");
        distance.add("kilometers");
        distance.add("meters");
        distance.add("inch");
        distance.add("mm");
        distance.add("millimeters");
        distance.add("centimeters");


        weigh.add("kilograms");
        weigh.add("kg");
        weigh.add("gr");
        weigh.add("mg");
        weigh.add("milligrams");
        weigh.add("tons");
        numFollowersCheck.addAll(weigh);
        numFollowersCheck.addAll(distance);
        numFollowersCheck.add("million");
        numFollowersCheck.add("billion");
        numFollowersCheck.add("thousand");

        Months.add("Jan");
        Months.add("JAN");
        Months.add("January");
        Months.add("JANUARY");


        Months.add("Feb");
        Months.add("FEB");
        Months.add("February");
        Months.add("FEBRUARY");


        Months.add("Mar");
        Months.add("MAR");
        Months.add("March");
        Months.add("MARCH");


        Months.add("April");
        Months.add("APRIL");
        Months.add("Apr");
        Months.add("APR");


        Months.add("May");
        Months.add("MAY");

        Months.add("June");
        Months.add("JUNE");
        Months.add("Jun");
        Months.add("JUN");


        Months.add("July");
        Months.add("JULY");
        Months.add("Jul");
        Months.add("JUL");

        Months.add("August");
        Months.add("AUGUST");
        Months.add("Aug");
        Months.add("AUG");

        Months.add("Sep");
        Months.add("September");
        Months.add("SEP");
        Months.add("SEPTEMBER");

        Months.add("OCTOBER");
        Months.add("OCT");
        Months.add("Oct");
        Months.add("October");

        Months.add("November");
        Months.add("Nov");
        Months.add("NOV");
        Months.add("NOVEMBER");

        Months.add("DECEMBER");
        Months.add("DEC");
        Months.add("Dec");
        Months.add("December");

        ignoreThem.add('/');
        ignoreThem.add('-');
        ignoreThem.add('.');

    }

    /**
     *
     * @param before
     * @param cur
     * @param follow
     * @return is term legal for our dictionary
     */
    private boolean isTermLegit(String before , String cur , String follow){
        if(stopWord.contains(cur))
            return false;
        if(stopWordCapital.contains(cur)){
            if(Character.isUpperCase(before.charAt(0))||Character.isUpperCase(follow.charAt(0))) {
                return true;
            }
            else
                return false;
        }
        if(ignoreThem.contains(cur.charAt(0)))
            return false;
        return true;
    }
    public HashMap<String, Integer> TokenizeForQ(String doc) throws ParseException {
        doc = doc.replaceAll(",|\\*|\\(|\\)|'|\"|:|;|`|\\{|}|\\?|\\[|]|\\\\|#|--|\\+|---|&|\\.\\.\\.|\\.\\.|\\||=|>|<|//|", "");
        doc=doc.replace( "!", "");
        doc=doc.replace( "^", "");
        doc=doc.replace( "#", "");
        doc=doc.replace( "@", "");
        doc=doc.replace( "~", "");
        doc=doc.replace( "_", "");
        doc=doc.replace( "�", "");
        doc=doc.replace( "°", "");


        String[] tokens = doc.trim().split("\\s+");
        ArrayList<String> ready = new ArrayList<>();
        for (int i=0;i<tokens.length ;i++) {
            String tok = tokens[i];
            String follow ="a";
            String before ="a";
            if(i<tokens.length-1)
                follow = tokens[i+1];
            if(i!=0){
                before = tokens[i-1];
            }
            if (tok.length()>0 && isTermLegit(before,tok,follow) ) {
                if (tok.charAt(tok.length() - 1) == '.') {
                    String s = tok.substring(0, tok.length() - 1);
                    if(s.length()>0)
                        ready.add(s);

                } else if (tok.charAt(0) == '-') {
                    String s = tok.substring(1);
                    if(s.length()>0)
                        ready.add(s);
                } else
                    ready.add(tok);
            }
        }
        HandleTerms(ready);
        HashMap<String, Integer> toSend = new HashMap<>();
        toSend.putAll(toIndex);
        toIndex.clear();
        return toSend;


    }

    /**
     * updates the doc info and tokenizing the terms
     * @param doc
     * @return
     * @throws ParseException
     */
    public HashMap<String, Integer> Tokenize(String doc) throws ParseException {
        docCounter++;
        String ID = doc.split("<DOCNO>")[1].split("</DOCNO")[0];
        doc = doc.split("<TEXT>")[1].split("</TEXT")[0];
        doc= doc.replaceAll("<.*>","");
        doc = doc.replaceAll(",|\\*|\\(|\\)|'|\"|:|;|`|\\{|}|\\?|\\[|]|\\\\|#|--|\\+|---|&|\\.\\.\\.|\\.\\.|\\||=|>|<|//|", "");
        doc=doc.replace( "!", "");
        doc=doc.replace( "^", "");
        doc=doc.replace( "#", "");
        doc=doc.replace( "@", "");
        doc=doc.replace( "~", "");
        doc=doc.replace( "_", "");
        doc=doc.replace( "�", "");
        doc=doc.replace( "°", "");


        String[] tokens = doc.trim().split("\\s+");
        ArrayList<String> ready = new ArrayList<>();
        for (int i=0;i<tokens.length ;i++) {
            String tok = tokens[i];
            String follow ="a";
            String before ="a";
            if(i<tokens.length-1)
                follow = tokens[i+1];
            if(i!=0){
                before = tokens[i-1];
            }
            if (tok.length()>0 && isTermLegit(before,tok,follow) ) {
                if (tok.charAt(tok.length() - 1) == '.') {
                    String s = tok.substring(0, tok.length() - 1);
                    if(s.length()>0)
                        ready.add(s);

                } else if (tok.charAt(0) == '-') {
                    String s = tok.substring(1);
                    if(s.length()>0)
                        ready.add(s);
                } else
                    ready.add(tok);
            }
        }
        int Docsize = ready.size();
        HandleTerms(ready);
        HashMap<String, Integer> toSend = new HashMap<>();
        toSend.putAll(toIndex);


        int uniqueterms = toIndex.size();
        Document document = new Document(ID,Docsize,uniqueterms);
        int mostfq = 0;
        String mostfqSt = "";
        for(String st : toIndex.keySet()){
            if(toIndex.get(st)>= mostfq){
                mostfq = toIndex.get(st);
                mostfqSt = st;
            }
        }
        document.setMax_tf(mostfq);
        document.setMost_fq_Term(mostfqSt);
        document.setUniqueWords(toIndex.size());
        Document.docCollection.put(docCounter,document);
        Document.docIdToDocNumber.put(ID,docCounter);


        toIndex.clear();
        return toSend;
    }

    /**
     * handle all the terms before sending them to indexer
     * @param arr
     * @return
     * @throws ParseException
     */
    public ArrayList<String> HandleTerms(ArrayList<String> arr) throws ParseException {
        DatesConvertor d = new DatesConvertor();
        for (int i = 0; i < arr.size(); i++) {
            String term = arr.get(i);
            String followTerm="";
            if(i+1<arr.size()){
                followTerm=arr.get(i+1);
            }
            if ((Character.isLetter(term.charAt(0)))) {
                if(Months.contains(term) && (followTerm).matches("[0-9]+")){
                    if(d.MonthBeforeNumber(term,followTerm,toIndex)==1){
                        i++; continue;
                        }
                }
                String capitalStr = term.toUpperCase();
                if (Character.isUpperCase(term.charAt(0))) {
                    if (i + 1 < arr.size() && Character.isUpperCase(arr.get(i + 1).charAt(0))) {
                        i = i+ EntityLocator(arr, i)-1;
                        continue;
                    }
                    term = term.toLowerCase();
                    if(toStem){
                        stem.setCurrent(term);
                        stem.stem();
                        capitalStr = stem.getCurrent().toUpperCase();
                    }
                    if(!(stopWord.contains(term)))
                        if (toIndex.containsKey(capitalStr)) {
                            toIndex.replace(capitalStr, toIndex.get(capitalStr) + 1);
                        } else if (toIndex.containsKey(term))
                            toIndex.replace(term, toIndex.get(term) + 1);
                        else {
                            toIndex.put((capitalStr), 1);
                        }
                } else {
                    int oldVal = 0;
                    if(toStem){
                        stem.setCurrent(term);
                        stem.stem();
                        term = stem.getCurrent();
                    }
                    if(!(stopWord.contains(term.toLowerCase())))
                        if (!toIndex.containsKey(capitalStr)) {
                            addValue(term.toLowerCase());
                        } else {
                            oldVal = toIndex.remove(capitalStr);
                            toIndex.put(term.toLowerCase(), oldVal + 1);
                        }
                }


            } else if(isNumberType(term)) {
                if(Months.contains(followTerm) && (term).matches("[0-9]+")){
                    if(d.MonthAfterNumber(term,followTerm,toIndex)==1){
                        i++;continue;
                    }
                }
                int signer = SignsConvert(arr, i);

                if (signer != 0) {
                    i = i + signer - 1;
                    continue;
                } else {
                        Double count = Double.parseDouble(arr.get(i));
                        if (numFollowersCheck.contains(followTerm)) {
                            String KMB = arr.get(i + 1);
                            if (KMB.toLowerCase().equals("thousand")) {
                                count = count * 1000.0;
                                i++;
                            } else if (KMB.toLowerCase().equals("million")) {
                                count = count * 1000000.0;
                                i++;

                            } else if (KMB.toLowerCase().equals("billion")) {
                                count = count * 1000000000.0;
                                i++;
                            } else if (weigh.contains(KMB.toLowerCase())) {
                                WeigthExpreshion(count, KMB.toLowerCase());
                                i++;
                                continue;
                            } else if (distance.contains(KMB.toLowerCase())) {
                                distanceExpresion(count, KMB.toLowerCase());
                                i++;
                                continue;
                            }
                        }
                        String num = withSuffix(count);
                        addValue(num);

                }

            }
            else{
                addValue(term.toUpperCase());
            }


        }

        return null;
    }

    /**
     *
     * @param term
     * @return if term is number or money
     */
    public boolean isNumberType(String term){
        String signTerms = "";
        if(term.charAt(0)=='$') {
             signTerms = term.substring(1);
        }

        if(term.matches("-?\\d+(\\.\\d+)?")||signTerms.matches("-?\\d+(\\.\\d+)?")){

            return true;
        }
       // addValue(term.toLowerCase());
        return false;
    }

    /**
     *
     * @param count
     * @return term number value to K M B
     */
    public static String withSuffix(Double count) {
        String s;

        DecimalFormat d3 = new DecimalFormat("#.###");
        if (count < 1000) return "" + d3.format(count);
        else if (count < 1000000) {
            s = d3.format(count / 1000) + "K";
            return s;

        } else if (count < 1000000000) {
            return d3.format(count / 1000000) + "M";

        }

        return d3.format(count / 1000000000) + "B";


    }

    /**
     *
     * @param count
     * @return dollar expression
     */
    public static String MillonsSuffix(double count) {
        String s;
        long mil = 1000000;
        DecimalFormat d3 = new DecimalFormat("#.###");
        DecimalFormat d4 = new DecimalFormat("#,###,###");
        if (count >= mil) {
            if (count >= 1000000000 && count < mil * 1000000)
                return (d4.format((Double.parseDouble(d3.format(count / (mil)))))) + " M";
            else if (count >= mil * 1000000)
                return (d4.format((Double.parseDouble(d3.format(count / mil))))) + " M";
            return (d4.format(Double.parseDouble(d3.format(count / mil)))) + " M";

        }
        return d4.format(Double.parseDouble(d3.format(count)));


    }

    public int getDocCounter() {
        return docCounter;
    }

    /**
     * handle dollar and precent expressions
     * @param terms
     * @param index
     * @return
     */
    public int SignsConvert(ArrayList<String> terms, int index) {
        String tok = terms.get(index);
        String tok2check = null;
        if (index + 1 < terms.size())
            tok2check = terms.get(index + 1);
        else{
            tok2check = "";
        }


        if (tok2check.equals("percent") || tok2check.equals(("percentage"))) {
            String str = tok + "%";
            addValue(str);
            return 2;

        } else if (tok.charAt(0) == '$' || dollarFormat.contains(tok2check.toLowerCase())) {


            int expSize = DollarExpressionSize(terms, index, dollarFormat);
            return DollarsHandler(terms, index, expSize);

        } else if (tok2check.contains("/") && Character.isDigit(tok2check.charAt(0))) {
            String tok3check = null;
            if (index + 2 < terms.size()) {
                tok3check = terms.get(index + 1);
                if (terms.get(index + 2).toLowerCase().equals("dollars")) {
                    String fraction = tok + " " + tok2check + " Dollars";
                    addValue(fraction);
                    return 3;
                }
            }

        }
        return 0;

    }

    /**
     * handle dollar expression
     * @param terms
     * @param index
     * @param expSize
     * @return
     */
    public int DollarsHandler(ArrayList<String> terms, int index, int expSize) {
        if (expSize == 0) {
            return 0;
        }
        String tok = terms.get(index);
        if (terms.get(index).charAt(0) == '$') {
            tok = tok.substring(1);
        }
        switch (expSize) {
            case 1:
                tok = MillonsSuffix(Double.parseDouble(tok));
                if (toIndex.containsKey( tok + " Dollars")) {
                    toIndex.replace(tok + " Dollars", toIndex.get(tok + " Dollars") + 1);
                    return expSize;
                } else
                    toIndex.put((tok) + " Dollars", 1);
                return expSize;
            case 2:
                if (terms.get(index + 1).toLowerCase().equals("million"))
                    tok = MillonsSuffix(Double.parseDouble(tok) * 1000000);
                else if (terms.get(index + 1).toLowerCase().equals("billion"))
                    tok = MillonsSuffix(Double.parseDouble(tok) * 1000000000);
                else {
                    tok = MillonsSuffix(Double.parseDouble(tok));
                }
                addValue(tok + " Dollars");
                return expSize;
            case 3:
                if (terms.get(index + 1).toLowerCase().equals("m"))
                    tok = MillonsSuffix(Double.parseDouble(tok) * 1000000);
                else if (terms.get(index + 1).toLowerCase().equals("bn"))
                    tok = MillonsSuffix(Double.parseDouble(tok) * 1000000000);

                addValue(tok + " Dollars");
                return expSize;
            case 4:
                if (terms.get(index + 1).toLowerCase().equals("million"))
                    tok = MillonsSuffix(Double.parseDouble(tok) * 1000000);
                else if (terms.get(index + 1).toLowerCase().equals("billion"))
                    tok = MillonsSuffix(Double.parseDouble(tok) * 1000000000);
                else if (terms.get(index + 1).toLowerCase().equals("trillion"))
                    tok = MillonsSuffix(Double.parseDouble(tok) * 1000 * 1000000000);

                addValue(tok + " Dollars");
                return expSize;


        }
        return 0;
    }

    /**
     * give size of the dollar expression
     * @param terms
     * @param index
     * @param dollarFormat
     * @return
     */
    public int DollarExpressionSize(ArrayList<String> terms, int index, HashSet<String> dollarFormat) {
        int expSize = 1;
        if (index + 1 < terms.size() && dollarFormat.contains(terms.get(index + 1).toLowerCase())) {
            expSize++;
            if (index + 2 < terms.size() && dollarFormat.contains(terms.get(index + 2).toLowerCase())) {
                expSize++;
                if (index + 3 < terms.size() && dollarFormat.contains(terms.get(index + 3).toLowerCase())) {
                    expSize++;
                }
            }
            if (expSize == 2 && !terms.get(index + 1).toLowerCase().equals("dollars") && !(terms.get(index).charAt(0) == '$'))
                return 0;

        }
        return expSize;
    }

    public void NumberExpression(String exp) {
        String[] occr = exp.split("-");
        String toAdd = "";
        for (int i = 0; i < occr.length; i++) {
            Double count = Double.parseDouble(occr[i]);
            String num = withSuffix(count);
            toAdd = toAdd + "-" + num;

        }
        if (toIndex.containsKey(toAdd)) {
            toIndex.replace(toAdd, toIndex.get(toAdd) + 1);
        } else
            toIndex.put((toAdd), 1);
    }

    /**
     * handle entity expressions
     * @param arr
     * @param index
     * @return
     */
    public int EntityLocator(ArrayList<String> arr, int index) {
        String str = arr.get(index);
        int counter = 1;
        for (int i = index + 1; i < arr.size() && i < index + 4; i++) {
            if (Character.isUpperCase(arr.get(i).charAt(0))) {
                str = str + " " + arr.get(i);
                counter++;

            } else {
                break;
            }
        }
        //System.out.println("jump is "+counter);
        String[] entityapart = str.split(" ");
        if(Indexer.approvedEntities.contains(str.toUpperCase())){
            addValue(str.toUpperCase());//check!
            return counter;
        }
        if (entities.containsKey(str.toUpperCase())) {
            int[] adder = entities.get(str.toUpperCase());
            if (adder[0] == docCounter) {
                if (adder[2] == 0) {
                    adder[1]++;

                    for (int i = 0; i < entityapart.length; i++) {
                        if(!(stopWord.contains(entityapart[i].toLowerCase()))) {
                            if (toIndex.containsKey(entityapart[i].toLowerCase()))
                                addValue(entityapart[i].toLowerCase());

                            else {
                                addValue(entityapart[i].toUpperCase());
                            }
                        }
                    }
                    return entityapart.length;
                } else {
                        addValue(str.toUpperCase());//check
                    return counter;
                }
            } else {
                if(adder[2]!=2)
                    adder[2] = 1;
                addValue(str.toUpperCase());//check!
                return counter;
            }

        } else {
            entities.put(str.toUpperCase(), new int[]{docCounter, 1, 0});//check
            for (int i = 0; i < entityapart.length; i++) {
                if(toIndex.containsKey(entityapart[i].toLowerCase()))
                    addValue(entityapart[i].toLowerCase());

                else{
                    addValue(entityapart[i].toUpperCase());
                }
            }
            return entityapart.length;
        }


    }

    /**
     * handle weight exression
     * @param num
     * @param weigth
     */
    public void WeigthExpreshion(Double num, String weigth) {
        String gr = "";
        String s = " grams";
        double newnum = 0.0;
        if (weigth.equals("kilograms") || weigth.equals("kg")) {
            gr = withSuffix((num * 1000.0)) + s;
            addValue(gr);
        } else if (weigth.equals("gr")) {
            gr = withSuffix((num)) + s;
            addValue(gr);
        } else if (weigth.equals("mg") || weigth.equals("milligrams")) {
            gr = withSuffix(num / 1000.0) + s;
            addValue(gr);

        } else if (weigth.equals("tons")) {
            gr = withSuffix(num * 1000000) + s;
            addValue(gr);


        }
    }

    /**
     * handle distance expression
     * @param num
     * @param weigth
     */
    public void distanceExpresion(Double num, String weigth) {
        String cen = "";
        String s = " cm";
        double newnum = 0.0;
        if (weigth.equals("km") || weigth.equals("kilometers")) {
            cen = withSuffix((num * 1000000.0)) + s;
            addValue(cen);
        } else if (weigth.equals("centimeters")) {
            cen = withSuffix((num)) + s;
            addValue(cen);
        } else if (weigth.equals("mm") || weigth.equals("millimeters")) {
            cen = withSuffix(num / 10.0) + s;
            addValue(cen);

        } else if (weigth.equals("meters")) {
            cen = withSuffix(num * 100) + s;
            addValue(cen);
        } else if (weigth.equals("inch")) {
            cen = withSuffix(num * 2.54) + s;
            addValue(cen);
        }

    }

    /**
     * determine how to add value to doc dictionary
     * @param val
     */
    public void addValue(String val) {
        if(toStem){
            stem.setCurrent(val.toLowerCase());//////////maybe change
            stem.stem();
            if(Character.isUpperCase(val.charAt(0)))
                val = stem.getCurrent().toUpperCase();
            else
                val = stem.getCurrent();
        }
        if(!(stopWord.contains(val.toLowerCase()))) {
            if (toIndex.containsKey(val)) {
                toIndex.replace(val, toIndex.get(val) + 1);
            } else {
                toIndex.put(val, 1);
            }
        }
    }


}


