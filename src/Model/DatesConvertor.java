package Model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * convert all dates expressions
 */
public class DatesConvertor {
    public SimpleDateFormat newDate = new SimpleDateFormat("MM-dd", Locale.ENGLISH);
    private SimpleDateFormat years = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
    private SimpleDateFormat dayToMonth = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
    private SimpleDateFormat monthToDay = new SimpleDateFormat("MMM dd", Locale.ENGLISH);

    public DatesConvertor() {

    }

    /**
     * handle case of month and then number term
     * @param month
     * @param num
     * @param toindex
     * @return
     * @throws ParseException
     */
    public int MonthBeforeNumber(String month, String num, HashMap<String, Integer> toindex) throws ParseException {

        if (num.length() <= 2) {
            Date d = monthToDay.parse(month + " " + num);
            newDate = new SimpleDateFormat("MM-dd", Locale.ENGLISH);
            String str = newDate.format(d);
            addValue(str,toindex);
            return 1;

        } else if (num.length() == 4) {
            Date d = years.parse(month + " " + num);
            newDate = new SimpleDateFormat("YYYY-MM", Locale.ENGLISH);
            String str = newDate.format(d);
            addValue(str,toindex);
            return 1;
        }
        //addValue(month,toindex);
        return 0;
    }

    /**
     * handle case of numner and then month terms
     * @param num
     * @param month
     * @param toindex
     * @return
     * @throws ParseException
     */
    public int MonthAfterNumber(String num, String month, HashMap<String, Integer> toindex) throws ParseException {

        if (num.length() <= 2) {
            Date d = dayToMonth.parse(num + " " + month);
            newDate = new SimpleDateFormat("MM-dd", Locale.ENGLISH);
            String str = newDate.format(d);
            addValue(str,toindex);
            return 1;
        }
        return 0;
    }
    public void addValue(String val,HashMap<String, Integer> toIndex) {

        if (toIndex.containsKey(val)) {
            toIndex.replace(val, toIndex.get(val) + 1);
        } else {
            toIndex.put(val, 1);
        }
    }

}

