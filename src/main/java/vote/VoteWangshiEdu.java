package vote;

import constants.CommonConstant;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

// 切换代理IP进行POST投票
public class VoteWangshiEdu
{
    public static void main(String[] args)
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println("Check Proxy Start Time: " + dateFormat.format(date));

        JSONObject jsonObject = new JSONObject();
        //jsonObject.append("id", 12703058);
        jsonObject.put("id", 12703058);
        //jsonObject.append("token", "");
        jsonObject.put("token", "");
        System.out.println(jsonObject);

        String query = "select host, port from proxy_check where speed_http_site != 999999 and target_site=\"https://www.6tiantian.com/\" order by speed_http_site";
        try (Connection connection = DriverManager.getConnection("jdbc:mariadb://" + CommonConstant.MYSQL_HOST
                + ":" + CommonConstant.MYSQL_PORT + "/" + CommonConstant.MYSQL_DATABASE,
            CommonConstant.MYSQL_USER, CommonConstant.MYSQL_PASS))
        {
            try (Statement statement = connection.createStatement())
            {
                ResultSet resultSet = statement.executeQuery(query);
                resultSet.last();   // 将游标移动到最后一行上
                System.out.println("Select All Count: " + resultSet.getRow()); // 打印当前行数
                resultSet.beforeFirst();    // 回复结果集到初始状态，即游标从第一行开始
                while (resultSet.next())
                {
                    // 设置代理IP、端口、协议（请分别替换）
                    // HttpHost proxy = new HttpHost("61.4.184.180", (int) 3128, "http");
                    System.out.println("host: "+ resultSet.getString("host")+ ", port: " + resultSet.getInt("port"));
                    HttpHost proxy = new HttpHost(resultSet.getString("host"), resultSet.getInt("port"), "http");
                    RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(CommonConstant.CONNECT_TIMEOUT)
                        .setConnectionRequestTimeout(CommonConstant.CONNECTION_REQUEST_TIMEOUT)
                        .setSocketTimeout(CommonConstant.SOCKET_TIMEOUT)
                        .setProxy(proxy)
                        .build();

                    vote(requestConfig, jsonObject);
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        System.out.println("End Thread: VoteWangshiEdu");
    }

    private static void vote(RequestConfig requestConfig, JSONObject jsonObject)
    {
        //CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        CloseableHttpClient closeableHttpClient = httpClientBuilder.setDefaultRequestConfig(requestConfig).build();

        try
        {
            HttpPost httpPost = new HttpPost("https://www.6tiantian.com/share/student/hw/like");
            httpPost.addHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.addHeader("Referer", "https://www.6tiantian.com/web/student/hw_share.html?id=12703058&key=f1ad68df40ccaf8e7d3a6623ed3a0f57&isShare=true&ref=REF_SHARE&customConfigId=1");
            httpPost.addHeader("Cookie", "SESSION=3bae1bd7-0b50-4887-909a-825746feab2");
            // 解决中文乱码问题
            //StringEntity stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
            StringEntity stringEntity = new StringEntity(jsonObject.toString(), "UTF-8");
            stringEntity.setContentEncoding("UTF-8");

            httpPost.setEntity(stringEntity);

            try (CloseableHttpResponse closeableHttpResponse = closeableHttpClient.execute(httpPost))
            {
                System.out.println(closeableHttpResponse.getStatusLine());
                String resp = EntityUtils.toString(closeableHttpResponse.getEntity());
                if (resp.equals("{}"))
                {
                    System.out.println("Vote Success!");
                }
                else
                {
                    System.out.println(resp);
                }
            }
            finally
            {
                closeableHttpClient.close();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
