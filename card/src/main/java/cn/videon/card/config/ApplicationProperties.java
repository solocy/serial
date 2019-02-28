package cn.videon.card.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.Serializable;

@ConfigurationProperties("application")
public class ApplicationProperties implements Serializable {

    private String channel ;
    private String ipcip;
    private String serial;


    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getIpcip() {
        return ipcip;
    }

    public void setIpcip(String ipcip) {
        this.ipcip = ipcip;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }
}