package apipRequest;

import apipClass.*;
import constants.Strings;

import java.util.ArrayList;
import java.util.List;

public class FcdslMaker {
    public static Fcdsl makeFcdslForService(String owner, String type, String size, List<String> after,boolean onlyActive, boolean ignoreClosed) {
        Fcdsl fcdsl = new Fcdsl();

        Query query = new Query();
        Terms terms = new Terms();
        String[] termsFields = new String[]{Strings.OWNER};
        String[] termsValues = new String[]{owner};
        terms.setFields(termsFields);
        terms.setValues(termsValues);
        query.setTerms(terms);
        fcdsl.setQuery(query);

        if(type!=null) {
            Filter filter = new Filter();
            Terms terms1 = new Terms();
            String[] terms1Fields = new String[]{Strings.TYPES};
            String[] terms1Values = new String[]{type};
            terms1.setFields(terms1Fields);
            terms1.setValues(terms1Values);
            filter.setTerms(terms1);
            fcdsl.setFilter(filter);
        }
        if(onlyActive){
            Except except = new Except();
            Terms terms2 = new Terms();
            String[] terms2Fields = new String[]{Strings.ACTIVE};
            String[] terms2Values = new String[]{Strings.FALSE};
            terms2.setFields(terms2Fields);
            terms2.setValues(terms2Values);
            except.setTerms(terms2);
            fcdsl.setExcept(except);
        }else if(ignoreClosed){
            Except except = new Except();
            Terms terms2 = new Terms();
            String[] terms2Fields = new String[]{Strings.CLOSED};
            String[] terms2Values = new String[]{Strings.TRUE};
            terms2.setFields(terms2Fields);
            terms2.setValues(terms2Values);
            except.setTerms(terms2);
            fcdsl.setExcept(except);
        }

        if(size!=null)fcdsl.setSize(size);

        if(after!=null)fcdsl.setAfter(after);

        ArrayList<Sort> sortList = Sort.makeSortList("active", false, "lastTime", false, "sid", true);
        fcdsl.setSort(sortList);

        return fcdsl;
    }
}
