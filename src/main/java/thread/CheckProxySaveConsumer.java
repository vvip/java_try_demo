package thread;

import constants.CommonConstant;
import vo.CheckProxyVo;
import vo.ProxyVo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;

// 消费者：将已经检查的数据插入DB中，不断执行，随主线程退出
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

        String insert = "INSERT INTO proxy_check (host, port, type, anonymity, origin, speed, target_site, speed_http_site, speed_https_site, check_time)" +
            " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)  ON DUPLICATE KEY UPDATE anonymity=?, origin=?, speed=?, speed_http_site=?, speed_https_site=?, check_time=?, update_time=now()";
        //try (Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/java_spider", "root", null))
        try (Connection connection = DriverManager.getConnection("jdbc:mariadb://" + CommonConstant.MYSQL_HOST
                + ":" + CommonConstant.MYSQL_PORT + "/" + CommonConstant.MYSQL_DATABASE,
            CommonConstant.MYSQL_USER, CommonConstant.MYSQL_PASS))
        {
            // create a Statement

            try (PreparedStatement preparedStatement = connection.prepareStatement(insert))
            {
                // 直到待检查的队列为空才退出
                int times = 0; // count计数为10，然后退出
                do
                {
                    if (!blockingQueueCheck.isEmpty())
                    {
                        times = 0;
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
                        preparedStatement.setString(7, checkProxyVo.getTargetSite());
                        preparedStatement.setDouble(8, checkProxyVo.getSpeedHttpSite());
                        preparedStatement.setDouble(9, checkProxyVo.getSpeedHttpsSite());
                        preparedStatement.setTimestamp(10, checkProxyVo.getCheckTime());

                        preparedStatement.setString(11, checkProxyVo.getAnonymity());
                        preparedStatement.setString(12, checkProxyVo.getOrigin());
                        preparedStatement.setDouble(13, checkProxyVo.getSpeed());
                        preparedStatement.setDouble(14, checkProxyVo.getSpeedHttpSite());
                        preparedStatement.setDouble(15, checkProxyVo.getSpeedHttpsSite());
                        preparedStatement.setTimestamp(16, checkProxyVo.getCheckTime());
                        //preparedStatement.addBatch();
                        preparedStatement.execute();
                        count++;
                    }
                    //preparedStatement.executeBatch();
                    else
                    {
                        times++;
                        // 如果待插入检查结果的队列为空，则休眠10秒钟
                        try
                        {
                            Thread.sleep(10000);
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                while (!blockingQueueOrigin.isEmpty() || !blockingQueueCheck.isEmpty() || (times < 1000));
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
