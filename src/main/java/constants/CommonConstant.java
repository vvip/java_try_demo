package constants;

import java.util.Arrays;
import java.util.List;

public class CommonConstant
{
    // 数据库连接配置
    public static final String MYSQL_HOST = "localhost";
    public static final String MYSQL_PORT = "3306";
    public static final String MYSQL_DATABASE = "java_spider";
    public static final String MYSQL_USER = "root";
    //public static final String MYSQL_PASS = "";

    // ssh2hw
    public static final String MYSQL_PASS = "3389!@Asdb";

    // 连接超时时间，单位毫秒
    public static final int CONNECT_TIMEOUT = 10000;

    // 设置从connect Manager获取connection超时时间，单位毫秒。httpclient新加属性
    public static final int CONNECTION_REQUEST_TIMEOUT = 10000;

    // 请求获取数据的超时时间，单位毫秒。
    public static final int SOCKET_TIMEOUT = 30000;

    public static final List<String> CHECK_SITE_LIST = Arrays.asList(
        "http://www.baidu.com/",
        "https://www.baidu.com/",
        "https://www.bing.com/",
        "https://www.google.com/");
}
