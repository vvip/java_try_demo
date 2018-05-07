package vo;

import java.sql.Timestamp;

public class ProxyVo
{
    private String host;

    private long port;

    private String type;

    private String anonymity;

    private String origin;

    private double speed;

    private Timestamp createTime;

    private Timestamp updateTime;

    private Timestamp deleteTime;

    private boolean deleted;

    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public long getPort()
    {
        return port;
    }

    public void setPort(long port)
    {
        this.port = port;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getAnonymity()
    {
        return anonymity;
    }

    public void setAnonymity(String anonymity)
    {
        this.anonymity = anonymity;
    }

    public String getOrigin()
    {
        return origin;
    }

    public void setOrigin(String origin)
    {
        this.origin = origin;
    }

    public double getSpeed()
    {
        return speed;
    }

    public void setSpeed(double speed)
    {
        this.speed = speed;
    }

    public Timestamp getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime)
    {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime)
    {
        this.updateTime = updateTime;
    }

    public Timestamp getDeleteTime()
    {
        return deleteTime;
    }

    public void setDeleteTime(Timestamp deleteTime)
    {
        this.deleteTime = deleteTime;
    }

    public boolean getDeleted()
    {
        return deleted;
    }
}
