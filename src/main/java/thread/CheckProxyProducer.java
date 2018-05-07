package thread;

import constants.CommonConstant;
import vo.ProxyVo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.BlockingQueue;

// 生产者：队列不断插入需要检查的数据
public class CheckProxyProducer implements Runnable
{
    private BlockingQueue<ProxyVo> blockingQueueOrigin;

    public CheckProxyProducer(BlockingQueue<ProxyVo> blockingQueueOrigin)
    {
        this.blockingQueueOrigin = blockingQueueOrigin;
    }

    @Override
    public void run()
    {
        System.out.println("Start Thread: CheckProxyProducer");
        String query = "select host, port, type, anonymity, origin, speed, create_time from proxy_origin where deleted = false";
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
                    ProxyVo proxyVo = new ProxyVo();
                    proxyVo.setHost(resultSet.getString("host"));
                    proxyVo.setPort(resultSet.getLong("port"));
                    proxyVo.setType(resultSet.getString("type"));
                    proxyVo.setAnonymity(resultSet.getString("anonymity"));
                    proxyVo.setOrigin(resultSet.getString("origin"));
                    proxyVo.setSpeed(resultSet.getDouble("speed"));
                    proxyVo.setCreateTime(resultSet.getTimestamp("create_time"));
                    try
                    {
                        blockingQueueOrigin.put(proxyVo);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        System.out.println("End Thread: CheckProxyProducer");
    }
}
