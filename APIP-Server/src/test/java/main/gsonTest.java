package main;


import APIP0V1_OpenAPI.DataRequestBody;
import com.google.gson.Gson;
import fcTools.ParseTools;

public class gsonTest {
    public static void main(String[] args) {
        DataRequestBody dataRequestBody = new DataRequestBody();
//
//        Fcdsl fcdsl = new Fcdsl();
//
//        List<String> afterlist = new ArrayList<String>();
//        afterlist.add("10");
//        fcdsl.setAfter(afterlist);
//        fcdsl.setIds(new String[]{"id1","id2"});
//        fcdsl.setSize("20");
//
//        Map<String, String> sortM = new HashMap<String, String>();
//        sortM.put("cid","asc");
//        fcdsl.setSort(sortM);
//        Query query = new Query();
//        Fcdsl.Equals equals = new Equals();
//        equals.setFields(new String[]{"cid","addr"});
//        query.setEquals(equals);
//        query.setEquals(new String[]{"cid","addr"});
//        fcdsl.setQuery(query);
//
//        Filter filter = new Filter();
//        Part part = new Part();
//        part.setFields(new String[]{"cid","addr"});
//        query.setEquals(equals);
//        fcdsl.setQuery(filter);
//
//        dataRequestBody.setFcdsl(fcdsl);
        String str = "{\"time\":0,\"nonce\":0,\"fcdsl\":{\"ids\":[\"id1\",\"id2\"],\"query\":{\"part\":{\"fields\":[\"cid\",\"addr\"],\"value\":\"arm\"}},\"filter\":{\"part\":{\"fields\":[\"cid\",\"addr\"],\"value\":\"arm\"}},\"size\":\"20\",\"sort\":{\"cid\":\"asc\"},\"after\":[\"10\"]}}";
        Gson gson = new Gson();
        //String str = gson.toJson(dataRequestBody);
        ///System.out.println(str);
        gson.fromJson(str,DataRequestBody.class);

        ParseTools.gsonPrint(dataRequestBody);


    }
}
