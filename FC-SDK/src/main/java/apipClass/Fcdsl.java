package apipClass;

import fcTools.ParseTools;
import menu.Inputer;
import menu.Menu;
import org.junit.Test;

import java.io.BufferedReader;
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


    public static final String MATCH_ALL = "matchAll";
    public static final String IDS = "ids";
    public static final String QUERY = "query";
    public static final String FILTER = "filter";
    public static final String EXCEPT = "except";
    public static final String SIZE = "size";
    public static final String SORT = "sort";
    public static final String AFTER = "after";
    public static final String OTHER = "other";
    public static final String[] FCDSL_FIELDS = new String[]{MATCH_ALL, IDS, QUERY, FILTER, EXCEPT, SIZE, SORT, AFTER, OTHER};

    public static boolean confirmAdd(String fieldName, BufferedReader br) {
        while(true) {
            System.out.println("Add "+fieldName+" ? y/n:");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                return true;
            } else if ("n".equals(input)) {
                return false;
            }
        }
    }

    public  void promoteSearch(int defaultSize, String defaultSort, BufferedReader br) {
        if(confirmAdd(QUERY, br)) inputQuery(br);
        if(confirmAdd(FILTER, br)) inputFilter(br);
        if(confirmAdd(EXCEPT, br)) inputExcept(br);
        System.out.println("The default size is "+ defaultSize +".");
        if(confirmAdd(SIZE, br)) inputSize(br);
        System.out.println("The default sort is "+ defaultSort +".");
        if(confirmAdd(SORT, br)) inputSort(br);
        if(confirmAdd(AFTER, br)) inputAfter(br);
    }

    public boolean checkFcdsl() {
        //1. ids 不可有query，filter，except，matchAll
        if(ids!=null){
            if(query!=null){
                System.out.println("With Ids search, there can't be a query.");
                return false;
            }
            if(filter!=null){
                System.out.println("With Ids search, there can't be a filter.");
                return false;
            }
            if(except!=null){
                System.out.println("With Ids search, there can't be an except.");
                return false;
            }
            if(after!=null){
                System.out.println("With Ids search, there can't be an after.");
                return false;
            }
            if(size!=null){
                System.out.println("With Ids search, there can't be a size.");
                return false;
            }
            if(sort!=null){
                System.out.println("With Ids search, there can't be a sort.");
                return false;
            }
        }

        //2. 没有query就不能有filter，except
        if(filter!=null || except!=null){
            if(query==null){
                System.out.println("Filter and except have to be used with a query.");
                return false;
            }
        }

        return true;
    }

    public void addIndex(String index){
        this.index=index;
    }

    public void addIds(String... ids){
        this.ids=ids;
        return;
    }

    public Query addNewQuery() {
        Query query = new Query();
        this.setQuery(query);
        return query;
    }

    public Filter addNewFilter() {
        Filter filter = new Filter();
        this.setFilter(filter);
        return filter;
    }

    public Except addNewExcept() {
        Except except = new Except();
        this.setExcept(except);
        return except;
    }

    public Fcdsl addNewSort(String field, String order) {
        ArrayList<Sort> sort = new ArrayList<>();
        Sort s = new Sort(field,order);
        sort.add(s);
        this.setSort(sort);
        return this;
    }

    public void appendSort(String field,String order) {
        Sort newSort = new Sort(field,order);
        this.sort.add(newSort);
    }
    public Fcdsl addSize(int size) {
        this.size = String.valueOf(size);
        return this;
    }
    public Fcdsl addNewAfter(String... values) {
        this.after= new ArrayList<>(List.of(values));
        return this;
    }
    public Fcdsl appendAfter(String value) {
        this.after.add(value);
        return this;
    }

    public static Fcdsl addFilterTermsToFcdsl(RequestBody requestBody, String field, String value) {
        Fcdsl fcdsl;
        if(requestBody.getFcdsl()!=null) {
            fcdsl = requestBody.getFcdsl();
        }else fcdsl= new Fcdsl();

        Filter filter;
        if(fcdsl.getFilter()!=null) {
            filter = fcdsl.getFilter();
        }else filter=new Filter();

        Terms terms;
        if(filter.getTerms()!=null) {
            terms = filter.getTerms();
        }else terms=new Terms();

        terms.setFields(new String[]{field});
        terms.setValues(new String[]{value});
        filter.setTerms(terms);
        fcdsl.setFilter(filter);
        return fcdsl;
    }

    public static Fcdsl addExceptTermsToFcdsl(RequestBody requestBody, String field, String value) {
        Fcdsl fcdsl;
        if(requestBody.getFcdsl()!=null) {
            fcdsl = requestBody.getFcdsl();
        }else fcdsl= new Fcdsl();

        Except except;
        if(fcdsl.getExcept()!=null) {
            except = fcdsl.getExcept();
        }else except=new Except();

        Terms terms;
        if(except.getTerms()!=null) {
            terms = except.getTerms();
        }else terms=new Terms();

        terms.setFields(new String[]{field});
        terms.setValues(new String[]{value});
        except.setTerms(terms);
        fcdsl.setExcept(except);
        return fcdsl;
    }


    public void setQueryTerms(String field, String value) {
        Query query = new Query();
        Terms terms;
        if(query.getTerms()!=null) {
            terms = query.getTerms();
        }else terms=new Terms();

        terms.setFields(new String[]{field});
        terms.setValues(new String[]{value});
        query.setTerms(terms);
        this.query=query;
    }

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

    public void setOther(Object other) {
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

    @Test
    public void test(){
        Fcdsl fcdsl = new Fcdsl();
        fcdsl.setIndex("cid");
        fcdsl.setSize("2");
        ParseTools.gsonPrint(fcdsl);
    }


    public void promoteInput(BufferedReader br) {
        while(true){
            Menu menu = new Menu();
            menu.add(FCDSL_FIELDS);
            menu.show();
            int choice = menu.choose(br);

            switch (choice){
                case 1 -> inputMatchAll(br);
                case 2 -> inputIds(br);
                case 3 -> inputQuery(br);
                case 4 -> inputFilter(br);
                case 5 -> inputExcept(br);
                case 6 -> inputSize(br);
                case 7 -> inputSort(br);
                case 8 -> inputAfter(br);
                case 9 -> inputOther(br);
                case 0 -> {
                    return;
                }
            }
        }
    }

    private void inputOther(BufferedReader br) {
        System.out.println("Input a string or a json. Enter to exit:");
        other = new Object();
        other = Inputer.inputString(br);
    }

    public void inputMatchAll(BufferedReader br) {
        while(true) {
            Menu menu = new Menu();
            menu.add(SIZE, SORT, AFTER);
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> inputSize(br);
                case 2 -> inputSort(br);
                case 3 -> inputAfter(br);
                case 0 -> {
                    return;
                }
            }
        }
    }

    public void inputAfter(BufferedReader br) {
        String[] inputs = Inputer.inputStringArray(br, "Input strings of after. Enter to end:", 0);
        if(inputs.length>0)after=List.of(inputs);
    }

    public void inputSort(BufferedReader br) {
        ArrayList<Sort> sortList = Sort.inputSortList(br);
        if(sortList!=null&&sortList.size()>0) sort = sortList;

    }

    public void inputSize(BufferedReader br) {
        String numStr = Inputer.inputIntegerStr(br,"Input size. Enter to skip:");
        if("".equals(numStr))return;
        size = numStr;
    }


    public void inputIds(BufferedReader br) {
        String[] inputs = Inputer.inputStringArray(br,"Input the ID. Enter to end:",0 );
        if(inputs.length>0)ids=inputs;
    }

    public void inputQuery(BufferedReader br) {
        query = new Query();
        query.promoteInput(QUERY,br);
    }

    public void inputFilter(BufferedReader br) {
        filter = new Filter();
        filter.promoteInput(FILTER,br);
    }

    public void inputExcept(BufferedReader br) {
        except = new Except();
        except.promoteInput(EXCEPT,br);
    }
}
