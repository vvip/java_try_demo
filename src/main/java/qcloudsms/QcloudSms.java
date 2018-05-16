package qcloudsms;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import org.json.JSONException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


public class QcloudSms
{
    public static void main(String args[])
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println("Start Time: " + dateFormat.format(date));

        checkUpdate("圣墟", "https://book.qidian.com/info/1004608738#Catalog");

    }

    private static List<String> readFile(String fileName)
    {
        File file = new File(fileName);
        List<String> lineList = new ArrayList<>();
        if (file.exists())
        {
            try
            {
                lineList = Files.lines(Paths.get(fileName)).collect(Collectors.toList());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            //System.out.println("File Name: " + fileName + " not exists, try to create it!");
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return lineList;
    }

    private static void writeFile(String fileName, String title)
    {
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(fileName, true);
            fileWriter.write(title + "\n");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (fileWriter != null)
                {
                    fileWriter.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static void checkUpdate(String book, String url)
    {
        String qidian = getHttpContent(url);
        Document document = Jsoup.parse(qidian);
        String title = document.select("li.update p.cf a.blue").text();
        System.out.println("Current Title: " + title);
        // 获取当前title，判断是否为最新内容
        // sendSms(book, title);
        if (readFile(book).contains(title))
        {
            System.out.println("Can't Find New Update!");
        }
        else
        {
            writeFile(book, title);
            sendSms(book, title);
            System.out.println("Find New Update! Alarmed");
        }
        System.out.println();
    }

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
                content.append(data);
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

    private static void sendSms(String book, String title)
    {
        // 短信应用SDK AppID
        int appid = 1400086086; // 1400开头

        // 短信应用SDK AppKey
        String appkey = "52bfc16a564d6c47c2b5a3052b110000";

        // 需要发送短信的手机号码
        String[] phoneNumbers = {"15100000038", "17300000013"};

        // 短信模板ID，需要在短信应用中申请
        int templateId = 112957; // NOTE: 这里的模板ID`7839`只是一个示例，真实的模板ID需要在短信控制台中申请

        // 签名
        String smsSign = "NetSec"; // NOTE: 这里的签名"腾讯云"只是一个示例，真实的签名需要在短信控制台中申请，另外签名参数使用的是`签名内容`，而不是`签名ID`

        try
        {
            SmsSingleSender ssender = new SmsSingleSender(appid, appkey);
            SmsSingleSenderResult result = ssender.send(0, "86", phoneNumbers[0],
                    "【NetSec】您关注的 《" + book + "》 已经更新，文章标题《" + title + "》。如非本人操作，请忽略本短信。", "", "");
            System.out.println(result);
        }
        catch (HTTPException e)
        {
            // HTTP响应码错误
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            // json解析错误
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // 网络IO错误
            e.printStackTrace();
        }
    }

}
