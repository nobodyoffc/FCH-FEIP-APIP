package fc_dsl;

import java.util.ArrayList;

public class Sort {
    private String field;
    private String order = "desc";

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public String getOrder() {
        return order;
    }
    public void setOrder(String order) {
        this.order = order;
    }
    public static ArrayList<Sort> makeSortList(String field1, Boolean isAsc1, String field2, Boolean isAsc2, String field3, Boolean isAsc3){
        ArrayList<Sort> sortList = new ArrayList<>();
        Sort sort = new Sort();
        sort.setField(field1);
        if(isAsc1)
            sort.setOrder("asc");
        sortList.add(sort);

        if(field2!=null) {
            Sort sort1 = new Sort();
            sort1.setField(field2);
            if(isAsc2)sort1.setOrder("asc");
            sortList.add(sort1);
        }

        if(field3!=null) {
            Sort sort1 = new Sort();
            sort1.setField(field3);
            if(isAsc3)sort1.setOrder("asc");
            sortList.add(sort1);
        }
        return sortList;
    }
}
