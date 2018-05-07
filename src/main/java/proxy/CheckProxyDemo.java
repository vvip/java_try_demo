package proxy;

import vo.ProxyVo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CheckProxyDemo
{
    final BlockingQueue<ProxyVo> blockingQueue = new ArrayBlockingQueue<>(1000);

    public static void main(String args[])
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println("Check Proxy Start Time: " + dateFormat.format(date));

        CheckProxyDemo checkProxy = new CheckProxyDemo();
        // 获取待检查的IP代理列表
        new Thread(checkProxy.new Producer()).start();
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        new Thread(checkProxy.new Consumer()).start();
        new Thread(checkProxy.new Consumer()).start();
        new Thread(checkProxy.new Consumer()).start();
        new Thread(checkProxy.new Consumer()).start();


        // 生产者：保存已经检查完毕的数据

        // 消费者：将已经检查的数据插入DB中

        date = new Date();
        System.out.println("Check Proxy End Time: " + dateFormat.format(date));
    }

    // 生产者：队列不断插入需要检查的数据
    class Producer implements Runnable
    {
        @Override
        public void run()
        {
            String query = "select host, port, type, anonymity, origin, speed, create_time from proxy_origin where deleted = false";
            try (Connection connection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/java_spider", "root", ""))
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
                            blockingQueue.put(proxyVo);
                            System.out.println("blockingQueueOrigin size: " + blockingQueue.size());
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
        }
    }

    // 消费者：检查队列不断消费待检查的数据
    class Consumer implements Runnable
    {
        @Override
        public void run()
        {
            while (!blockingQueue.isEmpty())
            {
                try
                {
                    Thread.sleep(100);
                    ProxyVo proxyVo = blockingQueue.take();
                    System.out.println("Consumer: " + proxyVo.getHost());
                    System.out.println("blockingQueueOrigin size: " + blockingQueue.size());
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
