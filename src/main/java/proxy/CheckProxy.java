package proxy;

import constants.CommonConstant;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import vo.CheckProxyVo;
import vo.ProxyVo;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.SocketException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.sql.Timestamp;

public class CheckProxy
{
    private ProxyVo proxyVo;

    private String checkSite;

    private String checkContent;

    private Timestamp timestamp;

    public CheckProxy(ProxyVo proxyVo, String checkSite, String checkContent, Timestamp timeStamp)
    {
        this.proxyVo = proxyVo;
        this.checkSite = checkSite;
        this.checkContent = checkContent;
        this.timestamp = timeStamp;
    }

    // 通过遍历不同站点列表，检测不同站点连通性和url
    public CheckProxyVo checkProxyVo()
    {
        CheckProxyVo checkProxyVo = new CheckProxyVo(this.proxyVo, this.timestamp);
        this.proxyVo.setType("http");
        float speedHttpSite = checkProxyBySite();
        //proxyVo.setType("https");
        //float speedHttpsSite = checkProxyBySite();
        checkProxyVo.setSpeedHttpSite(speedHttpSite);
        checkProxyVo.setSpeedTargetSite(this.checkSite);
        //checkProxyVo.setSpeedHttpsSite(speedHttpsSite);
        return checkProxyVo;
    }

    // 检查指定站点的网络连通性，返回连通时间，超时返回9999
    public float checkProxyBySite()
    {
        float speed = 999999000;
        long startTime = System.currentTimeMillis();
        // 访问目标地址
        HttpGet httpGet = new HttpGet(this.checkSite);
        // 设置代理IP、端口、协议（请分别替换）
        HttpHost proxy = new HttpHost(this.proxyVo.getHost(), (int) this.proxyVo.getPort(), this.proxyVo.getType());
        // 把代理设置到请求配置
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(CommonConstant.CONNECT_TIMEOUT)
            .setConnectionRequestTimeout(CommonConstant.CONNECTION_REQUEST_TIMEOUT)
            .setSocketTimeout(CommonConstant.SOCKET_TIMEOUT)
            .setProxy(proxy)
            .build();
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        configureHttpClientTwo(httpClientBuilder);
        // 实例化CloseableHttpClient对象
        CloseableHttpClient httpclient = httpClientBuilder.setDefaultRequestConfig(requestConfig).build();

        // 请求返回
        try
        {
            CloseableHttpResponse httpResp = httpclient.execute(httpGet);
            try
            {
                //System.out.println(httpResp.getStatusLine());
                String httpRespContent = EntityUtils.toString(httpResp.getEntity());
                //System.out.println(httpRespContent);
                if (httpRespContent.toString().indexOf(this.checkContent) != -1)
                {
                    //System.out.println(proxyVo.getType() + " Proxy OK! ==>> " + httpRespContent.toString());
                    long endTime = System.currentTimeMillis();
                    speed = endTime - startTime;
                }
            }
            finally
            {
                httpResp.close();
            }
        }
        catch (SocketException e)
        {
            //e.printStackTrace();
            System.out.println("Error: " + proxyVo.getHost() + ":" + proxyVo.getPort());
            speed = 999999000;
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.out.println("Error: " + proxyVo.getHost() + ":" + proxyVo.getPort());
            speed = 999999000;
        }
        return speed / 1000;
    }

    // httpClient 忽略证书校验
    public static void configureHttpClientOne(HttpClientBuilder httpClientBuilder)
    {
        try
        {
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy()
            {
                // 信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException
                {
                    return true;
                }
            }).build();
            httpClientBuilder.setSSLContext(sslContext);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void configureHttpClientTwo(HttpClientBuilder httpClientBuilder)
    {
        try
        {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager()
            {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException
                {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException
                {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers()
                {
                    //return new X509Certificate[0];
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[]{tm}, null);
            httpClientBuilder.setSSLContext(sslContext);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
