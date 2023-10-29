package apipTools;

import apipClass.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import constants.ApiNames;
import cryptoTools.SHA;
import menu.Inputer;
import menu.Menu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestTools {
    private static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    private static String urlHead =null;
    private static String via =null;
    private static String result;

    public static void main(String[] args) {

        inputUrlHead();
        inputVia();
        String symKey = Inputer.inputString(br);

        while(true) {
            Menu menu = new Menu();

            ArrayList<String> menuItemList = new ArrayList<>();

            menuItemList.add("MatchAll");
            menuItemList.add("ByIds");
            menuItemList.add("Query Terms");
            menuItemList.add("Query Match");
            menuItemList.add("Query part");
            menuItemList.add("Query range");
            menuItemList.add("Query Exists");
            menuItemList.add("Query Unexists");
            menuItemList.add("Query Equals");
            menuItemList.add("All manual");


            menu.add(menuItemList);
            System.out.println(" << Apip request maker>> \n");
            menu.show();
            int choice = menu.choose(br);
            switch (choice) {
                case 1 -> makeMatchAll();
                case 2 -> makeByIds();
                case 3 -> makeTerms();
                case 4 -> makeMatch();
                case 5 -> makePart();
                case 6 -> makeRange();
                case 7 -> makeExists();
                case 8 -> makeUnexists();
                case 9 -> makeEquals();
                case 10 -> allManual();
                case 0 -> {
                    try {
                        br.close();
                        return;
                    } catch (IOException e) {
                        System.out.println("Close BufferReader wrong.");
                        return;
                    }
                }
                default -> {
                }
            }
            String sign = SHA.getSign(symKey,result);
            System.out.println("-----\n Sign: \n"+sign+"\n----");
        }
    }

    private static String inputSymKey() {
        String ask = "Input the sessionKey:";
        return Arrays.toString(Inputer.input32BytesKey(br, ask));
    }

    private static void makeEquals() {
        DataRequestBody dataRequestBody = new DataRequestBody();
        Query query = makeEmptyQury(dataRequestBody);
        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;
        query.setEquals(inputEquals());
        askSizeSortAfterThenPrint(dataRequestBody, query);
    }
    private static void makeUnexists() {
        DataRequestBody dataRequestBody = new DataRequestBody();
        Query query = makeEmptyQury(dataRequestBody);
        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;
        query.setUnexists(inputUnexists());
        askSizeSortAfterThenPrint(dataRequestBody, query);
    }

    private static Query makeEmptyQury(DataRequestBody dataRequestBody) {
        Fcdsl fcdsl = new Fcdsl();
        Query query = new Query();
        fcdsl.setQuery(query);
        dataRequestBody.setFcdsl(fcdsl);
        return query;
    }

    private static void askSizeSortAfterThenPrint(DataRequestBody dataRequestBody, Query query) {
        dataRequestBody.getFcdsl().setQuery(query);

        askSize(dataRequestBody);
        askSort(dataRequestBody);
        askAfter(dataRequestBody);

        GsonBuilder gb = new GsonBuilder();
        Gson gson = gb.setPrettyPrinting().create();
        result = gson.toJson(dataRequestBody);
        System.out.println(result);
    }

    private static void makeExists() {
        DataRequestBody dataRequestBody = new DataRequestBody();
        Query query = makeEmptyQury(dataRequestBody);

        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;

        query.setExists(inputExists());
        askSizeSortAfterThenPrint(dataRequestBody, query);
    }

    private static void makePart() {
        DataRequestBody dataRequestBody = new DataRequestBody();
        Query query = makeEmptyQury(dataRequestBody);

        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;

        query.setPart(inputPart());
        askSizeSortAfterThenPrint(dataRequestBody, query);
    }

    private static void makeRange() {
        DataRequestBody dataRequestBody = new DataRequestBody();
        Query query = makeEmptyQury(dataRequestBody);

        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;

        query.setRange(inputRange());
        askSizeSortAfterThenPrint(dataRequestBody, query);
    }

    private static void makeMatch() {
        DataRequestBody dataRequestBody = new DataRequestBody();
        Query query = makeEmptyQury(dataRequestBody);

        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;

        query.setMatch(inputMatch());
        askSizeSortAfterThenPrint(dataRequestBody, query);
    }

    private static void makeTerms() {

        DataRequestBody dataRequestBody = new DataRequestBody();
        Query query = makeEmptyQury(dataRequestBody);

        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;
        
        query.setTerms(inputTerms());
        askSizeSortAfterThenPrint(dataRequestBody, query);
    }

    private static void makeByIds() {
        DataRequestBody dataRequestBody = new DataRequestBody();

        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;
        Fcdsl fcdsl = new Fcdsl();
        dataRequestBody.setFcdsl(fcdsl);

        String[] ids = Inputer.inputStringArray(br,"Input the IDs.",0);
        dataRequestBody.getFcdsl().setIds(ids);
        
        GsonBuilder gb = new GsonBuilder();
        Gson gson = gb.setPrettyPrinting().create();
        System.out.println(gson.toJson(dataRequestBody));
    }

    private static void makeMatchAll() {
        DataRequestBody dataRequestBody = new DataRequestBody();
        Fcdsl fcdsl = new Fcdsl();
        dataRequestBody.setFcdsl(fcdsl);

        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;
        
        askSize(dataRequestBody);
        askSort(dataRequestBody);
        askAfter(dataRequestBody);

        GsonBuilder gb = new GsonBuilder();
        Gson gson = gb.setPrettyPrinting().create();
        result = gson.toJson(dataRequestBody);
        System.out.println(result);
    }

    private static String setUrlAndVia(DataRequestBody dataRequestBody) {
        String urlTail = inputUrlTail();
        if (urlTail == null) return null;
        dataRequestBody.makeRequestBody(urlHead+urlTail,via);
        return urlTail;
    }

    private static void inputVia() {
        String input;

        System.out.println("Input via:");
        input = Inputer.inputString(br);
        if(!"".equals(input))via = input;
    }

    private static void inputUrlHead() {
        String input = null;

        System.out.println("Input urlHead:");
        input = Inputer.inputString(br);
        if(!"".equals(input))urlHead = input;
    }

    private static void allManual() {
        DataRequestBody dataRequestBody = new DataRequestBody();

        String urlTail = setUrlAndVia(dataRequestBody);
        if (urlTail == null) return;
        Fcdsl fcdsl = new Fcdsl();
        dataRequestBody.setFcdsl(fcdsl);
        if(urlTail.equals(ApiNames.APIP1V1Path + ApiNames.GeneralAPI))askIndex(dataRequestBody);
        if(askByIds(dataRequestBody))return;
        if(!askMatchAll()){
            askQuery(dataRequestBody);
            askFilter(dataRequestBody);
            askExcept(dataRequestBody);
        }
        askSize(dataRequestBody);
        askSort(dataRequestBody);
        askAfter(dataRequestBody);
        
        GsonBuilder gb = new GsonBuilder();
        Gson gson = gb.setPrettyPrinting().create();
        System.out.println(gson.toJson(dataRequestBody));
    }

    private static boolean askAfter(DataRequestBody dataRequestBody) {
        while(true) {
            System.out.println("Add After? 'y' or 'n':");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                inputAfter(dataRequestBody);
                return true;
            } else if ("n".equals(input)) {
                return false;
            }
        }
    }

    private static void inputAfter(DataRequestBody dataRequestBody) {
        String[] after;
        Gson gson = new Gson();
        try {
            while(true) {
                System.out.println("Input the json string of the last:");
                String input = br.readLine();
                try{
                    after = gson.fromJson(input,String[].class);
                    dataRequestBody.getFcdsl().setAfter(Arrays.stream(after).toList());
                    return;
                }catch (Exception e){
                    System.out.println("Wrong input. A string array json is needed. Try again.");
                }
            }
        } catch (IOException e) {
            System.out.println("BufferReader wrong.");
        }
    }

    private static boolean askSort(DataRequestBody dataRequestBody) {
        while(true) {
            System.out.println("Add Sort? 'y' or 'n':");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                inputSort(dataRequestBody);
                return true;
            } else if ("n".equals(input)) {
                return false;
            }
        }
    }

    private static void inputSort(DataRequestBody dataRequestBody) {
        ArrayList<Sort> sortList = Sort.inputSortList(br);
        dataRequestBody.getFcdsl().setSort(sortList);
    }

    private static boolean askSize(DataRequestBody dataRequestBody) {
        while(true) {
            System.out.println("Add Size? 'y' or 'n':");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                inputSize(dataRequestBody);
                return true;
            } else if ("n".equals(input)) {
                return false;
            }
        }
    }

    private static void inputSize(DataRequestBody dataRequestBody) {
        String ask = "Input the size you want in one request:";
        String size = Inputer.inputIntegerStr(br, ask);
        dataRequestBody.getFcdsl().setSize(size);
    }

    private static boolean askMatchAll() {
        while(true) {
            System.out.println("Is MatchAll? 'y' or 'n':");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                return true;
            } else if ("n".equals(input)) {
                return false;
            }
        }
    }

    private static boolean askByIds(DataRequestBody dataRequestBody) {
        while(true) {
            System.out.println("Is ByIds? 'y' or 'n':");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                String[] ids = Inputer.inputStringArray(br,"Input the IDs.",0);
                dataRequestBody.getFcdsl().setIds(ids);
                return true;
            } else if ("n".equals(input)) {
                return false;
            }
        }
    }

    private static void askIndex(DataRequestBody dataRequestBody) {
        System.out.println("General request need the name of index. Input it:");
        String index = Inputer.inputString(br);
        dataRequestBody.getFcdsl().setIndex(index);
    }

    private static void askQuery(DataRequestBody dataRequestBody) {
        while(true) {
            System.out.println("Is ByIds? 'y' or 'n':");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                setQuery(dataRequestBody);
                return;
            } else if ("n".equals(input)) {
                return;
            }
        }
    }

    private static void askFilter(DataRequestBody dataRequestBody) {
        while(true) {
            System.out.println("Is Filter? 'y' or 'n':");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                setFilter(dataRequestBody);
                return;
            } else if ("n".equals(input)) {
                return;
            }
        }
    }

    private static void askExcept(DataRequestBody dataRequestBody) {
        while(true) {
            System.out.println("Is Except? 'y' or 'n':");
            String input = Inputer.inputString(br);
            if ("y".equals(input)) {
                setExcept(dataRequestBody);
                return;
            } else if ("n".equals(input)) {
                return;
            }
        }
    }


    private static void setExcept(DataRequestBody dataRequestBody) {
        Except filter = new Except();
        dataRequestBody.getFcdsl().setQuery(filter);

        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();
        menuItemList.add("Terms");
        menuItemList.add("Match");
        menuItemList.add("Part");
        menuItemList.add("Range");
        menuItemList.add("Exists");
        menuItemList.add("Unexists");
        menuItemList.add("Equals");

        menu.add(menuItemList);
        System.out.println("Choice method in your Except:");
        menu.show();
        int choice = menu.choose(br);
        switch (choice) {
            case 1 -> dataRequestBody.getFcdsl().getExcept().setTerms(inputTerms());
            case 2 -> dataRequestBody.getFcdsl().getExcept().setMatch(inputMatch());
            case 3 -> dataRequestBody.getFcdsl().getExcept().setPart(inputPart());
            case 4 -> dataRequestBody.getFcdsl().getExcept().setRange(inputRange());
            case 5 -> dataRequestBody.getFcdsl().getExcept().setExists(inputExists());
            case 6 -> dataRequestBody.getFcdsl().getExcept().setUnexists(inputUnexists());
            case 7 -> dataRequestBody.getFcdsl().getExcept().setEquals(inputEquals());
            default -> {
            }
        }
    }

    private static void setFilter(DataRequestBody dataRequestBody) {
        Filter filter = new Filter();
        dataRequestBody.getFcdsl().setFilter(filter);

        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();

        menuItemList.add("Terms");
        menuItemList.add("Match");
        menuItemList.add("Part");
        menuItemList.add("Range");
        menuItemList.add("Exists");
        menuItemList.add("Unexists");
        menuItemList.add("Equals");

        menu.add(menuItemList);
        System.out.println("Choice method in your Filter:");
        menu.show();
        int choice = menu.choose(br);
        switch (choice) {
            case 1 -> dataRequestBody.getFcdsl().getFilter().setTerms(inputTerms());
            case 2 -> dataRequestBody.getFcdsl().getFilter().setMatch(inputMatch());
            case 3 -> dataRequestBody.getFcdsl().getFilter().setPart(inputPart());
            case 4 -> dataRequestBody.getFcdsl().getFilter().setRange(inputRange());
            case 5 -> dataRequestBody.getFcdsl().getFilter().setExists(inputExists());
            case 6 -> dataRequestBody.getFcdsl().getFilter().setUnexists(inputUnexists());
            case 7 -> dataRequestBody.getFcdsl().getFilter().setEquals(inputEquals());
            default -> {
            }
        }
    }
    private static void setQuery(DataRequestBody dataRequestBody) {
        Query query = new Query();
        dataRequestBody.getFcdsl().setQuery(query);

        Menu menu = new Menu();

        ArrayList<String> menuItemList = new ArrayList<>();

        menuItemList.add("Terms");
        menuItemList.add("Match");
        menuItemList.add("Part");
        menuItemList.add("Range");
        menuItemList.add("Exists");
        menuItemList.add("Unexists");
        menuItemList.add("Equals");

        menu.add(menuItemList);
        System.out.println("Choice method in your Query:");
        menu.show();
        int choice = menu.choose(br);
        switch (choice) {
            case 1 -> dataRequestBody.getFcdsl().getQuery().setTerms(inputTerms());
            case 2 -> dataRequestBody.getFcdsl().getQuery().setMatch(inputMatch());
            case 3 -> dataRequestBody.getFcdsl().getQuery().setPart(inputPart());
            case 4 -> dataRequestBody.getFcdsl().getQuery().setRange(inputRange());
            case 5 -> dataRequestBody.getFcdsl().getQuery().setExists(inputExists());
            case 6 -> dataRequestBody.getFcdsl().getQuery().setUnexists(inputUnexists());
            case 7 -> dataRequestBody.getFcdsl().getQuery().setEquals(inputEquals());
            default -> {
            }
        }
    }

    private static Equals inputEquals() {
        Equals equals = new Equals();
        String name = "equals";
        equals.setFields(inputFeilds(name));
        equals.setValues(inputValues(name));
        return equals;
    }
    private static String[] inputUnexists() {
        String ask="Input the field:";
        return Inputer.inputStringArray(br,ask,0);
    }
    private static String[] inputExists() {
        String ask="Input the field:";
        return Inputer.inputStringArray(br,ask,0);
    }

    private static Match inputMatch() {
        Match match = new Match();
        String name = "match";
        match.setFields(inputFeilds(name));
        match.setValue(inputValue(name));
        return match;
    }
    private static Part inputPart() {
        Part part = new Part();
        String name = "part";
        part.setFields(inputFeilds(name));
        part.setValue(inputValue(name));
        return part;
    }
    private static Range inputRange() {
        Range range = new Range();
        List<String> con = new ArrayList<>();
        con.add("lt");
        con.add("lte");
        con.add("gt");
        con.add("gte");
        String name = "range";
        range.setFields(inputFeilds(name));
        while(true){
            System.out.println("Input the condition (lt, lte, gt, or gte), enter to finish:");
            String input = Inputer.inputString(br);
            if("".equals(input)){
                return range;
            }
            String ask = "Input the value:";
            String num = Inputer.inputDoubleAsString(br,ask);
            switch (input){
                case "lt" ->range.setLt(num);
                case "lte" -> range.setLte(num);
                case "gt" ->range.setGt(num);
                case "Gte" -> range.setGte(num);
                default -> {
                    return range;
                }
            }
        }
    }

    private static Terms inputTerms() {
        Terms terms = new Terms();
        String name = "terms";
        terms.setFields(inputFeilds(name));
        terms.setValues(inputValues(name));
        return terms;
    }

    private static String[] inputValues(String name) {
        String ask = "Input the values of "+name+": ";
        return Inputer.inputStringArray(br,ask,0);
    }
    private static String inputValue(String name) {
        System.out.println("Input the values of "+name+": ");
        return Inputer.inputString(br);
    }

    private static String[] inputFeilds(String name) {
        String ask = "Input the fields of "+name+": ";
        return Inputer.inputStringArray(br,ask,0);
    }

    private static String inputUrlTail() {
        String urlTail = null;
        while(urlTail==null||"".equals(urlTail)){
            System.out.println("Input urlTail:");
            try {
                urlTail = br.readLine();
            } catch (IOException e) {
                System.out.println("BufferReader wrong.");
                return null;
            }
        }
        return urlTail;
    }
}
