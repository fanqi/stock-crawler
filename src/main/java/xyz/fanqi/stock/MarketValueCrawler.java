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

    public static final int Aggregate_Market_Value_Num = 4;
    public static final int Circulated_Market_Value_Num = 5;

    public static void main(String[] args) throws IOException {
        //获取token
        String token = getToken();
        //抓取总市值数据
        JSONObject aggregateMarketValue = getMarketValueData(token, Aggregate_Market_Value_Num);

        //解析总市值数据
        System.out.println("总市值数据:");
        parseMarketValueData(aggregateMarketValue, Aggregate_Market_Value_Num);


        //抓取流通市值数据
        JSONObject circulatedMarketValue = getMarketValueData(token, Circulated_Market_Value_Num);
        System.out.println("流通市值数据:");
        parseMarketValueData(circulatedMarketValue, Circulated_Market_Value_Num);
        //解析流通市值数据

    }

    public static JSONObject getMarketValueData(String token, int marketValueType) {
        try {
            Map<String, String> queryParamMap = new HashMap<String, String>();
            queryParamMap.put("token", token);
            queryParamMap.put("p", "1");
            queryParamMap.put("perpage", "25");
            queryParamMap.put("sort", "{\"column\":"+marketValueType+",\"order\":\"ASC\"}");
            if (marketValueType == Aggregate_Market_Value_Num) {
                queryParamMap.put("showType", "[\"\",\"\",\"onList\",\"onList\",\"onTable\",\"onList\",\"onList\",\"onList\"]");
            } else if (marketValueType == Circulated_Market_Value_Num) {
                queryParamMap.put("showType", "[\"\",\"\",\"onList\",\"onList\",\"onList\",\"onTable\",\"onList\",\"onList\"]");
            }
            return JSON.parseObject(Jsoup.connect("http://www.iwencai.com/stockpick/cache").data(queryParamMap).ignoreContentType(true).execute().body());
        } catch (IOException e) {
            System.out.println("获取" + marketValueType + "数据失败");
            return null;
        }
    }

    public static void parseMarketValueData(JSONObject marketValueData, int marketValueType) {
        JSONArray titleJSONArray = marketValueData.getJSONArray("title");
        String updateDate = titleJSONArray.getString(marketValueType).replace("\r", "")
                .substring(10 + (marketValueType == Circulated_Market_Value_Num ? 3 : 0));
        String titleStr = String.format("日期\t排名\t%s\t%s\t%s\t%s"
                , titleJSONArray.getString(0)
                , titleJSONArray.getString(1)
                , titleJSONArray.getString(2)
                , titleJSONArray.getString(marketValueType).replace("\r", "")
                        .substring(0, 6 + (marketValueType == Circulated_Market_Value_Num ? 3 : 0))
        );
        System.out.println(titleStr);
        JSONArray resultJSONArray = marketValueData.getJSONArray("result");
        for (int i = 0; i < resultJSONArray.size(); i++) {
            JSONArray marketValue = resultJSONArray.getJSONArray(i);
            String marketValueStr = String.format("%s\t%d\t%s\t%s\t%s\t%s"
                    , updateDate
                    , i + 1
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
