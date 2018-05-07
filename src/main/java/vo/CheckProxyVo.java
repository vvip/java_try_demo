package vo;

import java.sql.Timestamp;

//
public class CheckProxyVo extends ProxyVo
{
    private Timestamp checkTime;

    private String speedTargetSite;

    private float speedHttpSite;

    private float speedHttpsSite;

    public CheckProxyVo(ProxyVo proxyVo, Timestamp timeStamp)
    {
        checkTime = timeStamp;
        this.setHost(proxyVo.getHost());
        this.setPort(proxyVo.getPort());
        this.setType(proxyVo.getType());
        this.setAnonymity(proxyVo.getAnonymity());
        this.setOrigin(proxyVo.getOrigin());
        this.setSpeed(proxyVo.getSpeed());
        this.setCreateTime(proxyVo.getCreateTime());
        this.setUpdateTime(proxyVo.getUpdateTime());
        this.setDeleteTime(proxyVo.getDeleteTime());
        this.setDeleted(proxyVo.getDeleted());
    }

    public Timestamp getCheckTime()
    {
        return checkTime;
    }

    public void setCheckTime(Timestamp checkTime)
    {
        this.checkTime = checkTime;
    }

    public String getSpeedTargetSite()
    {
        return speedTargetSite;
    }

    public void setSpeedTargetSite(String speedTargetSite)
    {
        this.speedTargetSite = speedTargetSite;
    }

    public float getSpeedHttpSite()
    {
        return speedHttpSite;
    }

    public void setSpeedHttpSite(float speedHttpSite)
    {
        this.speedHttpSite = speedHttpSite;
    }

    public float getSpeedHttpsSite()
    {
        return speedHttpsSite;
    }

    public void setSpeedHttpsSite(float speedHttpsSite)
    {
        this.speedHttpsSite = speedHttpsSite;
    }

    @Override
    public String toString()
    {
        return String.format("host: %15s; port: %5d; type: %5s; speed: %s; speedHttpSite: %s; createTime: %s; checkTime: %s", this.getHost(), this.getPort(), this.getType(), this.getSpeed(), this.getSpeedHttpSite(), this.getCheckTime(), this.getCheckTime());
    }
}
