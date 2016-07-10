package xyz.fanqi.stock;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by fanqi on 16/7/10.
 */
public class MarketValueCrawler {

    public static final int AGGREGATE_MARKET_VALUE_NUM = 4;
    public static final int CIRCULATED_MARKET_VALUE_NUM = 5;
    public static final String AGGREGATE_MARKET_VALUE_SHOWTYPE = "[\"\",\"\",\"onList\",\"onList\",\"onTable\",\"onList\",\"onList\",\"onList\"]";
    public static final String CIRCULATED_MARKET_VALUE_SHOWTYPE = "[\"\",\"\",\"onList\",\"onList\",\"onList\",\"onTable\",\"onList\",\"onList\"]";


    public static void main(String[] args) throws IOException {
        String token = getToken();

        int p = 1;
        int perpage = 70;
        JSONObject marketValueJSONObject;
        do {
            System.out.println("总市值数据(第" + p + "页):");
            marketValueJSONObject = getMarketValueData(token, AGGREGATE_MARKET_VALUE_NUM, p, perpage);
            parseMarketValueData(marketValueJSONObject, AGGREGATE_MARKET_VALUE_NUM, p, perpage);

            System.out.println("流通市值数据(第" + p + "页):");
            marketValueJSONObject = getMarketValueData(token, CIRCULATED_MARKET_VALUE_NUM, p, perpage);
            parseMarketValueData(marketValueJSONObject, CIRCULATED_MARKET_VALUE_NUM, p, perpage);
            p++;
        } while (marketValueJSONObject.getInteger("total") > (p - 1) * perpage);

    }

    public static JSONObject getMarketValueData(String token, int marketValueType, int p, int perpage) {
        try {
            Map<String, String> queryParamMap = new HashMap<String, String>();
            queryParamMap.put("token", token);
            queryParamMap.put("p", p + "");
            queryParamMap.put("perpage", perpage + "");
            queryParamMap.put("sort", "{\"column\":" + marketValueType + ",\"order\":\"ASC\"}");
            if (marketValueType == AGGREGATE_MARKET_VALUE_NUM) {
                queryParamMap.put("showType", AGGREGATE_MARKET_VALUE_SHOWTYPE);
            } else if (marketValueType == CIRCULATED_MARKET_VALUE_NUM) {
                queryParamMap.put("showType", CIRCULATED_MARKET_VALUE_SHOWTYPE);
            }
            return JSON.parseObject(Jsoup.connect("http://www.iwencai.com/stockpick/cache").data(queryParamMap).ignoreContentType(true).execute().body());
        } catch (IOException e) {
            System.out.println("获取" + marketValueType + "数据失败,异常信息:" + e.getMessage());
            return null;
        }
    }

    public static void parseMarketValueData(JSONObject marketValueData, int marketValueType, int p, int perpage) {
        JSONArray titleJSONArray = marketValueData.getJSONArray("title");
        String tradeDate = titleJSONArray.getString(marketValueType).replace("\r", "")
                .substring(10 + (marketValueType == CIRCULATED_MARKET_VALUE_NUM ? 3 : 0));
        String titleStr = String.format("交易日期\t排名\t%s\t%s\t%s\t%s"
                , titleJSONArray.getString(0)
                , titleJSONArray.getString(1)
                , titleJSONArray.getString(2)
                , titleJSONArray.getString(marketValueType).replace("\r", "")
                        .substring(0, 6 + (marketValueType == CIRCULATED_MARKET_VALUE_NUM ? 3 : 0))
        );
        System.out.println(titleStr);
        JSONArray resultJSONArray = marketValueData.getJSONArray("result");
        for (int i = 0; i < resultJSONArray.size(); i++) {
            JSONArray marketValue = resultJSONArray.getJSONArray(i);
            String marketValueStr = String.format("%s\t%d\t%s\t%s\t%s\t%s"
                    , tradeDate
                    , perpage * (p - 1) + i + 1
                    , marketValue.getString(0)
                    , marketValue.getString(1)
                    , marketValue.getString(2)
                    , marketValue.getString(marketValueType));
            System.out.println(marketValueStr);
        }

    }

    public static String getToken() {
        //"token":"577519776fa2ec3ee7cca5a4800a637e"
        String token = "";
        Document doc;
        try {
            doc = Jsoup.connect("http://www.iwencai.com/stockpick/search?typed=0&preParams=&ts=1&f=1&qs=result_original&selfsectsn=&querytype=&searchfilter=&tid=stockpick&w=%E5%B8%82%E5%80%BC").get();
        } catch (IOException e) {
            return token;
        }
        String pattern = "\"token\":\"(.*?)\"";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(doc.body().toString());
        if (m.find()) {
            return m.group(1);
        }
        return token;
    }

}
