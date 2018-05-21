#  Java try repositorie! - Java练手项目

##  一、将网上公布的代理IP列表存入 Mariadb
*  _src/main/java/constants/CommonConstant.java_
    *  保存数据库连接的变量
*  _src/main/java/proxy/CheckProxy.java_
    *  获取 https://raw.githubusercontent.com/fate0/proxylist/master/proxy.list 页面的代理地址。
    *  代理地址按行解析 json 转化为 vo 对象，存入 Mariadb。
    *  针对重复的代理(host, port, type)内容，使用 Mariadb `ON DUPLICATE KEY UPDATE` 方法，仅更新部分字段。
    *  可以编译为独立运行的 jar 程序。


##  二、检查代理IP列表针对特定网站的有效性（多线程）
*  _src/main/java/proxy/CheckProxy.java_
    *  传入代理IP的 vo 对象、目标站点、站点返回关键字、检测时间，发起 http(s) 请求。
    *  返回代理的连通时间，如果返回999999则表示代理不可用。
*  _src/main/java/thread/CheckProxyProducer.java_
    *  多线程任务的 生产者：队列不断插入需要检查的代理IP vo 对象到 BlockingQueue
    *  1 个线程，BlockingQueue 队列大小 1000
*  _src/main/java/thread/CheckProxyConsumer.java_
    *  作为 消费者：不断消费待检查的数据
    *  作为 生产者：将检查结果存入阻塞队列
    *  线程数量可指定
*  _src/main/java/thread/CheckProxySaveConsumer.java_
    *  消费者：将已经检查的数据插入 DB 中，不断执行，随主线程退出
    *  1 个线程，BlockingQueue 队列大小 1000
*  _src/main/java/proxy/CheckProxyMain.java_
    *  检查代理列表针对某一站点有效性的 Main 程序，控制调度作用


##  三、使用代理IP列表对限制IP投票次数的网页进行匿名投票（单线程）
*  _src/main/java/vote/VoteWangshiEdu.java_
    *  读取 DB 中可用的代理列表，遍历使用 POST 方式提交投票


##  四、检查关注的小说是否在网站上更新，更新调用腾讯的 SMS 接口，短信提醒
*  _src/main/java/qcloudsms/qcloudsms.QcloudSms.java_
    *  获取 https://book.qidian.com/info/1004608738#Catalog 页面中最新的章节内容，存入本地文件
    *  定时再次访问页面获取最新章节，最新章节发生变化调用 qcloud 的短信接口，进行短信通知小说更新


##  五、SSH登陆服务器 并 使用 sle4f-log4j 进行日志记录
* _src/main/java/login/ssh/LoginSSH.java_
    *  通过 args 参数，分别配置 服务器IP 用户名 密码 端口
    *  登录服务器执行 ls -alh / ，然后打印返回结果

##  六、Java 与 kafka 使用


