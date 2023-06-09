package temp;

import com.google.gson.Gson;

public class ParseTools {

    public static void gsonPrint(Object ob) {
        Gson gson = new Gson();
        System.out.println("***********\n" + ob.getClass().toString() + ": " + gson.toJson(ob) + "\n***********");
        return;
    }
}
