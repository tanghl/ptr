package com.le.ptr.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by tanghl on 2017/9/20.
 */
public class RedisProperties {

    public static final String CLUSTER = "cluster";

    public static final String SINGLE = "single";

    private static final Logger LOGGER = LogManager.getLogger(RedisProperties.class.getName());

    public RedisProperties() {
        try {
            Properties props = new Properties();
            props.load(RedisProperties.class.getClassLoader().getResourceAsStream("jedis.properties"));
            this.mode = props.getProperty("redis.mode");
            this.addr = props.getProperty("redis.addr_batch");
            this.password = props.getProperty("redis.password_batch");
            this.port = Integer.parseInt(props.getProperty("redis.port_batch"));
            this.total = props.getProperty("redis.total_batch");
            this.show = props.getProperty("redis.show_batch");
            this.member = props.getProperty("redis.member_batch");
            this.p = props.getProperty("redis.p_batch");
            this.m = props.getProperty("redis.m_batch");
            this.relation = props.getProperty("redis.relation_batch");
            this.hwmoffline = props.getProperty("redis.hwmoffline_batch");
            this.addrWrite = props.getProperty("redis.addrWrite");
            this.portWrite = Integer.parseInt(props.getProperty("redis.portWrite"));
            this.passwordWrite = props.getProperty("redis.passwordWrite");
            this.ptrKey = props.getProperty("redis.ptrKey");
            this.dbIndex = Integer.parseInt(props.getProperty("redis.dbIndex"));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private String mode;

    private String addr;

    private String password;

    private int port;

    private String show;

    private String total;

    private String p;

    private String member;

    private String m;

    private String relation;

    private String hwmoffline;

    private String addrWrite;

    private int portWrite;

    private String passwordWrite;

    private String ptrKey;

    private int dbIndex;

    public int getPort() {
        return port;
    }

    public String getAddr() {
        return addr;
    }

    public String getShow() {
        return show;
    }

    public String getTotal() {
        return total;
    }

    public String getP() {
        return p;
    }

    public String getMember() {
        return member;
    }

    public String getPassword() {
        return password;
    }

    public String getM() {
        return m;
    }

    public String getRelation() {
        return relation;
    }

    public String getHwmoffline() {
        return hwmoffline;
    }

    public String getMode() {
        return mode;
    }

    public String getAddrWrite() {
        return addrWrite;
    }

    public int getPortWrite() {
        return portWrite;
    }

    public String getPasswordWrite() {
        return passwordWrite;
    }

    public String getPtrKey() {
        return ptrKey;
    }

    public int getDbIndex() {
        return dbIndex;
    }
}

