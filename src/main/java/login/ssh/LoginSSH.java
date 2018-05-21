package login.ssh;


import com.jcraft.jsch.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LoginSSH
{
    private final static Logger logger = LoggerFactory.getLogger(LoginSSH.class);

    /**
     * 执行shell命令
     * @param command
     * @return
     */
    public static int execute(final String command, String ip, String username, String password, int port) {
        int returnCode = 0;
        List<String> stdout= new ArrayList<String>();
        JSch jsch = new JSch();
        SshUserInfo sshUserInfo = new SshUserInfo();
        try {
            //创建session并且打开连接，因为创建session之后要主动打开连接
            Session session = jsch.getSession(username, ip, port);
            session.setPassword(password);
            session.setUserInfo(sshUserInfo);
            session.connect(30000);   // making a connection with timeout.
            //session.connect();

            //打开通道，设置通道类型，和执行的命令
            Channel channel = session.openChannel("exec");
            ChannelExec channelExec = (ChannelExec)channel;
            channelExec.setCommand(command);

            channelExec.setInputStream(null);
            BufferedReader input = new BufferedReader(new InputStreamReader
                    (channelExec.getInputStream()));

            channelExec.connect();
            System.out.println("The remote command is :" + command);

            //接收远程服务器执行命令的结果
            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
                System.out.println(line);
            }
            input.close();

            // 得到returnCode
            if (channelExec.isClosed()) {
                returnCode = channelExec.getExitStatus();
            }

            // 关闭通道
            channelExec.disconnect();
            //关闭session
            session.disconnect();

        } catch (JSchException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return returnCode;
    }

    public static void main(String[] args)
    {
        logger.info("SSH login Start!");
        logger.debug("HOST: " + args[0] + " ;  USERNAME: " + args[1] + " ;  PASSWORD: " + args[2] + " ; PORT: " + args[3]);
        execute("ls -alh /", args[0], args[1], args[2], Integer.parseInt(args[3]));
    }

}
