package proxy;

import org.json.JSONObject;
import vo.ProxyVo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SaveProxy
{
    public static void main(String args[])
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println("Get Proxy Start Time: " + dateFormat.format(date));
        List<ProxyVo> proxyVoList = getProxyList();
        System.out.println("Proxy List Count: " + proxyVoList.size());

        saveProxyVoList(proxyVoList);

        date = new Date();
        System.out.println("Check Proxy End Time: " + dateFormat.format(date));
    }

    // 获取代理IP列表
    private static List<ProxyVo> getProxyList()
    {
        List<ProxyVo> proxyVoList = new ArrayList<>();
        String proxyListUrl = "https://raw.githubusercontent.com/fate0/proxylist/master/proxy.list";
        String proxyPageContent = getHttpContent(proxyListUrl);
        for (String proxyLine : proxyPageContent.split("\n"))
        {
            //System.out.println(proxyLine);
            JSONObject jsonObject = new JSONObject(proxyLine);
            ProxyVo proxyVo = new ProxyVo();
            proxyVo.setHost(jsonObject.getString("host"));
            proxyVo.setPort(jsonObject.getLong("port"));
            proxyVo.setType(jsonObject.getString("type"));
            proxyVo.setAnonymity(jsonObject.getString("anonymity"));
            proxyVo.setOrigin(jsonObject.getString("from"));
            proxyVo.setSpeed(jsonObject.getDouble("response_time"));
            proxyVoList.add(proxyVo);
        }
        return proxyVoList;
    }

    // proxyVoList 存入数据库中
    private static void saveProxyVoList(List<ProxyVo> proxyVoList)
    {
        String insert = "INSERT INTO proxy_origin (host, port, type, anonymity, origin, speed) VALUES (?, ?, ?, ?, ?, ?)  ON DUPLICATE KEY UPDATE anonymity=?, origin=?, speed=?, update_time=now()";
        try (Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/java_spider", "root", null))
        // 密码是没发连接上的，因为网络不通
        //try (Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/java_spider", "root", "3389!@Asdb"))
        {
            // create a Statement

            try (PreparedStatement preparedStatement = connection.prepareStatement(insert))
            {
                for (ProxyVo proxyVo : proxyVoList)
                {
                    preparedStatement.setString(1, proxyVo.getHost());
                    preparedStatement.setLong(2, proxyVo.getPort());
                    preparedStatement.setString(3, proxyVo.getType());
                    preparedStatement.setString(4, proxyVo.getAnonymity());
                    preparedStatement.setString(5, proxyVo.getOrigin());
                    preparedStatement.setDouble(6, proxyVo.getSpeed());

                    preparedStatement.setString(7, proxyVo.getAnonymity());
                    preparedStatement.setString(8, proxyVo.getOrigin());
                    preparedStatement.setDouble(9, proxyVo.getSpeed());
                    preparedStatement.addBatch();
                    //preparedStatement.execute();
                }
                preparedStatement.executeBatch();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    // 根据 URL 获取页面内容
    private static String getHttpContent(String urlStr)
    {
        StringBuilder content = new StringBuilder();
        try
        {
            //建立连接
            URL url = new URL(urlStr);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoInput(true);
            httpUrlConn.setRequestMethod("GET");
            httpUrlConn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
            //获取输入流
            InputStream input = httpUrlConn.getInputStream();
            //将字节输入流转换为字符输入流
            InputStreamReader read = new InputStreamReader(input, "utf-8");
            //为字符输入流添加缓冲
            BufferedReader br = new BufferedReader(read);
            // 读取返回结果
            String data = br.readLine();
            while (data != null)
            {
                //System.out.println(data);
                content.append(data + "\n");
                data = br.readLine();
            }
            // 释放资源
            br.close();
            read.close();
            input.close();
            httpUrlConn.disconnect();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return content.toString();
    }
}
