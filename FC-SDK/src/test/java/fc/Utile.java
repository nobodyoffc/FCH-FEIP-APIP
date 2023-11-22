package fc;

import apipClass.TxInfo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Utile {
    public static void main(String[] args) {
        String classNamesStr ="SignInData,CidInfo,BlockInfo,TxInfo,WebhookInfo,Address,Block,BlockHas,BlockMark,Cash,CashMark,Nobody,OpReturn,P2SH,P2SHRaw,Tx,TxHas,TxMark,App,AppData,Box,BoxData,Cid,CidData,Code,CodeData,Contact,ContactData,FcInfo,Group,GroupData,HomepageData,Mail,MailData,MasterData,Nid,NidData,NobodyData,NoticeFeeData,Proof,ProofData,Protocol,ProtocolData,ReputationData,Secret,SecretData,Service,ServiceData,Statement,StatementData,Team,TeamData";
        String[] classNames = classNamesStr.split(",");
        for(String name:classNames){
            System.out.println("    public static "+name+" get"+name+"List(Object responseData) {\n" +
                    "        Type t = new TypeToken<"+name+">() {}.getType();\n" +
                    "        Gson gson = new Gson();\n" +
                    "        return gson.fromJson(gson.toJson(responseData), t);\n" +
                    "    }");
        }
    }
}
