package apipRequest;

import apipClass.*;
import constants.Strings;

import java.util.ArrayList;
import java.util.List;

public class FcdslMaker {
    public static Fcdsl makeFcdslForService(String owner, String type, String size, List<String> after) {
        Fcdsl fcdsl = new Fcdsl();

        Query query = new Query();
        Terms terms = new Terms();
        String[] termsFields = new String[]{Strings.FIELDS};
        String[] termsValues = new String[]{owner};
        terms.setFields(termsFields);
        terms.setValues(termsValues);
        query.setTerms(terms);
        fcdsl.setQuery(query);

        if(type!=null) {
            Filter filter = new Filter();
            Terms terms1 = new Terms();
            String[] terms1Fields = new String[]{Strings.FIELDS};
            String[] terms1Values = new String[]{type};
            terms1.setFields(terms1Fields);
            terms1.setValues(terms1Values);
            filter.setTerms(terms1);
            fcdsl.setFilter(filter);
        }

        if(size!=null)fcdsl.setSize(size);

        if(after!=null)fcdsl.setAfter(after);

        ArrayList<Sort> sortList = Sort.makeSortList("active", false, "tRate", false, "sid", true);
        fcdsl.setSort(sortList);

        return fcdsl;
    }
}
