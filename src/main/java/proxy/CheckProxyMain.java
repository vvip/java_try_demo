package proxy;

import thread.CheckProxyConsumer;
import thread.CheckProxyProducer;
import thread.CheckProxySaveConsumer;
import vo.CheckProxyVo;
import vo.ProxyVo;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
            System.out.println("    java -jar xxxx.jar targetSite respContent threadNum");
            System.out.println("eg: java -jar xxxx.jar http://www.baidu.com/ baidu.min.css 100");
            System.exit(0);
        }
        System.out.println("Target Site: " + args[0]);
        System.out.println("Target Response Keyword: " + args[1]);
        System.out.println("Thread Num: " + args[2]);
        int threadNum = Integer.parseInt(args[2]);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // 生产者： 把需要检查的代理IP地址列表加入线程
        CheckProxyProducer checkProxyProducer = new CheckProxyProducer(blockingQueueOrigin);
        Thread threadProxyProducer = new Thread(checkProxyProducer);
        threadProxyProducer.start();
        Thread.sleep(2000);

        // 消费者-生产正： 把队列中的待检查IP列表消费掉，并加入待存入DB的数据库
        List<Thread> threadListCheckProxy = new ArrayList<>(threadNum);
        for (int i = 0; i < threadNum; i++)
        {
            Thread thread = new Thread(new CheckProxyConsumer(blockingQueueOrigin, blockingQueueCheck, i, args[0], args[1], timestamp));
            thread.start();
            threadListCheckProxy.add(thread);
        }

        Thread.sleep(2000);
        CheckProxySaveConsumer checkProxySaveConsumer = new CheckProxySaveConsumer(blockingQueueOrigin, blockingQueueCheck);
        Thread threadProxySaveConsumer = new Thread(checkProxySaveConsumer);
        threadProxySaveConsumer.start();

        // times 用来强制退出的超时控制
        int times = 0;
        while (true)
        {
            int aliveThreadCnt = disPlayStateAndIsAlive(threadProxyProducer, threadProxySaveConsumer, threadListCheckProxy);

            if (blockingQueueOrigin.isEmpty() && blockingQueueCheck.isEmpty())
            {
                times++;
                Thread.sleep(5000);
                if (blockingQueueCheck.isEmpty() && (aliveThreadCnt == 0 || times >= 60))
                {
                    date = new Date();
                    System.out.println("Check Proxy End Time: " + dateFormat.format(date));
                    System.exit(1);
                }
            }
            else
            {
                Thread.sleep(5000);
            }
        }
    }

    private static int disPlayStateAndIsAlive(Thread threadProxyProducer, Thread threadProxySaveConsumer, List<Thread> threadListCheckProxy)
    {//《Java并发10:线程的状态Thread.State及其线程状态之间的转换》
        int threadAlive = 0;
        int threadDeath = 0;
        int threadNew = 0;
        int threadRUNNABLE = 0;
        int threadBLOCKED = 0;
        int threadWAITING = 0;
        int threadTIMED_WAITING = 0;
        int threadTERMINATED = 0;
        if (threadProxyProducer.isAlive())
        {
            System.out.println("threadProxyProducer State: " + threadProxyProducer.getState()
                + ", Is Alive: " + threadProxyProducer.isAlive());
        }
        if (threadProxySaveConsumer.isAlive())
        {
            System.out.println("threadProxySaveConsumer State: " + threadProxySaveConsumer.getState()
                + ", Is Alive: " + threadProxySaveConsumer.isAlive());
        }

        //int count = 0;
        for (Thread thread : threadListCheckProxy)
        {
            //count++;
            //System.out.println("Thread "+count+" Status: " + thread.getState() + ", Is Alive: " + thread.isAlive());
            if (thread.isAlive())
            {
                threadAlive++;
            }
            else
            {
                threadDeath++;
            }
            Thread.State threadState = thread.getState();
            if (threadState.equals(Thread.State.NEW))
                threadNew++;
            if (threadState.equals(Thread.State.RUNNABLE))
                threadRUNNABLE++;
            if (threadState.equals(Thread.State.BLOCKED))
                threadBLOCKED++;
            if (threadState.equals(Thread.State.WAITING))
                threadWAITING++;
            if (threadState.equals(Thread.State.TIMED_WAITING))
                threadTIMED_WAITING++;
            if (threadState.equals(Thread.State.TERMINATED))
                threadTERMINATED++;
        }
        System.out.println(threadListCheckProxy.size() + " size Thread List, Alive: " + threadAlive + "; Death: " + threadDeath + ".");
        System.out.println("Thread List, NEW: " + threadNew + "; RUNNABLE: " + threadRUNNABLE + "; BLOCKED: " + threadBLOCKED
            + "; WAITING: " + threadWAITING + "; TIMED_WAITING: " + threadTIMED_WAITING + "; TERMINATED: " + threadTERMINATED);
        System.out.println("blockingQueueOrigin size: " + blockingQueueOrigin.size()
            + ";  blockingQueueCheck size: " + blockingQueueCheck.size());
        return threadAlive;
    }
}
