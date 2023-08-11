package APIP1V1_FCDSL;

import esTools.Sort;

import java.util.ArrayList;
import java.util.List;

public class Fcdsl{

    private String index;
    private String[] ids;
    private Query query;
    private Filter filter;
    private Except except;
    private String size;
    private ArrayList<Sort> sort;
    private List<String> after;

    private Object other;


    public void setFilterTerms(String field, String value) {
        Filter filter = new Filter();
        Terms terms;
        if(filter.getTerms()!=null) {
            terms = filter.getTerms();
        }else terms=new Terms();

        terms.setFields(new String[]{field});
        terms.setValues(new String[]{value});
        filter.setTerms(terms);
        this.filter=filter;
    }

    public void setExceptTerms(String field, String value) {
        Except except1 = new Except();
        Terms terms;
        if(except1.getTerms()!=null) {
            terms = except1.getTerms();
        }else terms=new Terms();

        terms.setFields(new String[]{field});
        terms.setValues(new String[]{value});
        except1.setTerms(terms);
        this.except=except1;
    }

    public Object getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String[] getIds() {
        return ids;
    }

    public void setIds(String[] ids) {
        this.ids = ids;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public ArrayList<Sort> getSort() {
        return sort;
    }

    public void setSort(ArrayList<Sort> sort) {
        this.sort = sort;
    }

    public List<String> getAfter() {
        return after;
    }

    public void setAfter(List<String> after) {
        this.after = after;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public Except getExcept() {
        return except;
    }

    public void setExcept(Except except) {
        this.except = except;
    }
}
