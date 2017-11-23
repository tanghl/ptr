package com.le.ptr.util;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by tanghl on 2017/10/24.
 */
public class RedisUtil implements Serializable {

    private static final Logger LOGGER = LogManager.getLogger(RedisUtil.class.getName());

    public static JedisCluster connectCluster(String hostName, int port, String password) {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        Set<HostAndPort> hosts = new HashSet<>();
        hosts.add(new HostAndPort(hostName, port));
        JedisCluster jsCluster = new JedisCluster(hosts, 3000, 500, 3, password, poolConfig);
        return jsCluster;
    }

    public static Jedis connectSingle(String hostName, int port, String password) {
        Jedis jedis = new Jedis(hostName, port, 5000, 1000);
        if (!"".equals(password)) {
            jedis.auth(password);
        }
        return jedis;
    }

    public static void closeCluster(JedisCluster jedisCluster) {
        try {
            jedisCluster.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void closeSingle(Jedis jedis) {
        jedis.close();
    }

}
