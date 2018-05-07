package proxy;

import thread.CheckProxyConsumer;
import thread.CheckProxyProducer;
import thread.CheckProxySaveConsumer;
import vo.CheckProxyVo;
import vo.ProxyVo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CheckProxyMain
{
    private final static BlockingQueue<ProxyVo> blockingQueueOrigin = new ArrayBlockingQueue<>(1000);
    private final static BlockingQueue<CheckProxyVo> blockingQueueCheck = new ArrayBlockingQueue<>(1000);

    public static void main(String args[]) throws InterruptedException
    {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        System.out.println("Check Proxy Start Time: " + dateFormat.format(date));
        if (args.length < 3)
        {
            System.out.println("Args Error!");
            System.out.println("java -jar xxxx.jar http://www.baidu.com/ baidu.min.css threadNum");
            System.out.println("java -jar xxxx.jar http://www.baidu.com/ baidu.min.css 100");
            System.exit(0);
        }
        System.out.println("Target Site: " + args[0]);
        System.out.println("Target Response Keyword: " + args[1]);
        System.out.println("Thread Num: " + args[2]);
        int threadNum = Integer.parseInt(args[2]);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        CheckProxyProducer checkProxyProducer = new CheckProxyProducer(blockingQueueOrigin);
        Thread threadProxyProducer = new Thread(checkProxyProducer);
        threadProxyProducer.start();
        Thread.sleep(2000);

        for (int i = 0; i < threadNum; i++)
        {
            new Thread(new CheckProxyConsumer(blockingQueueOrigin, blockingQueueCheck, i, args[0], args[1], timestamp)).start();
        }

        Thread.sleep(2000);
        CheckProxySaveConsumer checkProxySaveConsumer = new CheckProxySaveConsumer(blockingQueueOrigin, blockingQueueCheck);
        new Thread(checkProxySaveConsumer).start();

        // 生产者：保存已经检查完毕的数据

        while (true)
        {
            if (blockingQueueOrigin.isEmpty() && blockingQueueCheck.isEmpty())
            {
                Thread.sleep(50000);
                if (blockingQueueCheck.isEmpty())
                {
                    date = new Date();
                    System.out.println("Check Proxy End Time: " + dateFormat.format(date));
                    System.exit(1);
                }
            }
            else
            {
                Thread.sleep(1000);
            }
        }

    }
}
