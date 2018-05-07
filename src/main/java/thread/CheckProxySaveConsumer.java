package thread;

import vo.CheckProxyVo;
import vo.ProxyVo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;

// 消费者：将已经检查的数据插入DB中
public class CheckProxySaveConsumer implements Runnable
{
    private BlockingQueue<ProxyVo> blockingQueueOrigin;
    private BlockingQueue<CheckProxyVo> blockingQueueCheck;
    private int count = 0;

    public CheckProxySaveConsumer(BlockingQueue<ProxyVo> blockingQueueOrigin, BlockingQueue<CheckProxyVo> blockingQueueCheck)
    {
        this.blockingQueueOrigin = blockingQueueOrigin;
        this.blockingQueueCheck = blockingQueueCheck;
    }

    @Override
    public void run()
    {
        System.out.println("Start Thread: CheckProxySaveConsumer");

        String insert = "INSERT INTO proxy_check (host, port, type, anonymity, origin, speed, speed_http_site, speed_https_site, check_time)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)  ON DUPLICATE KEY UPDATE anonymity=?, origin=?, speed=?, speed_http_site=?, speed_https_site=?, check_time=?, update_time=now()";
        //try (Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/java_spider", "root", null))
        try (Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/java_spider", "root", ""))
        {
            // create a Statement

            try (PreparedStatement preparedStatement = connection.prepareStatement(insert))
            {
                // 直到待检查的队列为空才退出
                do
                {
                    if (!blockingQueueCheck.isEmpty())
                    {
                        CheckProxyVo checkProxyVo = null;
                        try
                        {
                            checkProxyVo = blockingQueueCheck.take();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        preparedStatement.setString(1, checkProxyVo.getHost());
                        preparedStatement.setLong(2, checkProxyVo.getPort());
                        preparedStatement.setString(3, checkProxyVo.getType());
                        preparedStatement.setString(4, checkProxyVo.getAnonymity());
                        preparedStatement.setString(5, checkProxyVo.getOrigin());
                        preparedStatement.setDouble(6, checkProxyVo.getSpeed());
                        preparedStatement.setDouble(7, checkProxyVo.getSpeedHttpSite());
                        preparedStatement.setDouble(8, checkProxyVo.getSpeedHttpsSite());
                        preparedStatement.setTimestamp(9, checkProxyVo.getCheckTime());

                        preparedStatement.setString(10, checkProxyVo.getAnonymity());
                        preparedStatement.setString(11, checkProxyVo.getOrigin());
                        preparedStatement.setDouble(12, checkProxyVo.getSpeed());
                        preparedStatement.setDouble(13, checkProxyVo.getSpeedHttpSite());
                        preparedStatement.setDouble(14, checkProxyVo.getSpeedHttpsSite());
                        preparedStatement.setTimestamp(15, checkProxyVo.getCheckTime());
                        //preparedStatement.addBatch();
                        preparedStatement.execute();
                        count++;
                    }
                    //preparedStatement.executeBatch();
                    else
                    {
                        // 如果待插入检查结果的队列为空，则休眠10秒钟
                        try
                        {
                            Thread.sleep(150000);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                while  (!blockingQueueOrigin.isEmpty() || !blockingQueueCheck.isEmpty());
                System.out.println("CheckProxySaveConsumer Over! All Count: " + count);
                /*
                if (blockingQueueCheck.isEmpty())
                {
                    Date date = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    System.out.println("Check Proxy End Time - CheckProxySaveConsumer: " + dateFormat.format(date));
                    System.exit(1);
                }*/
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
