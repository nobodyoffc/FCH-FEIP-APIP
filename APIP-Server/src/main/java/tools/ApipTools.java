package tools;

import apipClass.*;
import constants.ApiNames;
import initial.Initiator;
import redis.clients.jedis.Jedis;

import static constants.ApiNames.apiList;
import static constants.ApiNames.freeApiList;
import static constants.Constants.FchToSatoshi;
import static constants.Strings.*;

public class ApipTools {

    public static String getApiNameFromUrl(String url) {
        int lastSlashIndex = url.lastIndexOf('/');
        if (lastSlashIndex != -1 && lastSlashIndex != url.length() - 1) {
            String name = url.substring(lastSlashIndex + 1);
            if(apiList.contains(name)||freeApiList.contains(name)) {
                return name;
            }
            return "";
        } else {
            return "";  // Return empty string if '/' is the last character or not found
        }

    }

    public static int getNPrice(String apiName, Jedis jedis){
        try {
            return Integer.parseInt(jedis.hget(N_PRICE,apiName));
        }catch (Exception e){
           return -1;
        }
    }

    public static Fcdsl addFilterTermsToFcdsl(DataRequestBody requestBody, String field, String value) {
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

    public static Fcdsl addExceptTermsToFcdsl(DataRequestBody requestBody, String field, String value) {
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
}
