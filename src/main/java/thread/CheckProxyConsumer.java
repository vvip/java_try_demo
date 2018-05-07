package thread;

import proxy.CheckProxy;
import vo.CheckProxyVo;
import vo.ProxyVo;

import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;

// 作为 消费者：检查队列不断消费待检查的数据
// 作为 生产者：将检查结果存入阻塞队列
public class CheckProxyConsumer implements Runnable
{
    private BlockingQueue<ProxyVo> blockingQueueOrigin;
    private BlockingQueue<CheckProxyVo> blockingQueueCheck;
    private int threadNum;
    private String checkSite;       // 待检查连通性的站点
    private String checkContent;    // 检查连通性站点的返回比对内容
    private Timestamp timestamp;

    public CheckProxyConsumer(BlockingQueue<ProxyVo> blockingQueueOrigin, BlockingQueue<CheckProxyVo> blockingQueueCheck,
                              int threadNum, String checkSite, String checkContent, Timestamp timestamp)
    {
        this.blockingQueueOrigin = blockingQueueOrigin;
        this.blockingQueueCheck = blockingQueueCheck;
        this.threadNum = threadNum;
        this.checkSite = checkSite;
        this.checkContent = checkContent;
        this.timestamp = timestamp;
    }

    @Override
    public void run()
    {
        System.out.printf("Start Thread: CheckProxyConsumer - %3d", threadNum);
        while (!blockingQueueOrigin.isEmpty())
        {
            ProxyVo proxyVo = null;
            try
            {
                proxyVo = blockingQueueOrigin.take();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            CheckProxy checkProxy = new CheckProxy(proxyVo, this.checkSite, this.checkContent, this.timestamp);
            CheckProxyVo checkProxyVo = checkProxy.checkProxyVo();
            try
            {
                blockingQueueCheck.put(checkProxyVo);
            }
            catch (InterruptedException e)
            {
                System.out.println("PUT blockingQueueCheck Error: ");
                e.printStackTrace();
            }
            System.out.printf("Thread - %3d || blockingQueueOrigin size: %4d ||  Consumer: %s%n", threadNum, blockingQueueOrigin.size(), checkProxyVo.toString());
        }
    }
}
