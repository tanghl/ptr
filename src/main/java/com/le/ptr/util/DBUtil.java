package com.le.ptr.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.MapType;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by tanghl on 2017/11/13.
 */
public class DBUtil {

    private static final Logger LOGGER = LogManager.getLogger(DBUtil.class.getName());

    public static DataSource getDataSource(String driver, String jdbc, String userName, String password, int initSize) {
        PoolProperties p = new PoolProperties();
        p.setUrl(jdbc);
        p.setDriverClassName(driver);
        p.setUsername(userName);
        p.setPassword(password);
        p.setJmxEnabled(true);
        p.setTestWhileIdle(false);
        p.setTestOnBorrow(true);
        p.setValidationQuery("SELECT 1");
        p.setTestOnReturn(false);
        p.setValidationInterval(30000);
        p.setTimeBetweenEvictionRunsMillis(30000);
        p.setMaxActive(10);
        p.setInitialSize(initSize);
        p.setMaxWait(100);
        p.setRemoveAbandonedTimeout(60);
        p.setMinEvictableIdleTimeMillis(10000);
        p.setMinIdle(10);
        p.setMaxIdle(10);
        p.setLogAbandoned(true);
        p.setRemoveAbandoned(true);
        p.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;" +
                "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        DataSource datasource = new DataSource();
        datasource.setPoolProperties(p);
        return datasource;
    }

    public static Map<String, Object> getConfObj(String[] args, String fileName) {
        String path = null;
        Map<String, Object> conf = null;
        if (args != null && args.length > 1) {
            for (String arg : args) {
                if (arg.lastIndexOf(fileName) > 0) {
                    path = arg;
                }
            }
        } else {
            LOGGER.debug("no read conf file, then read inner file");
            path = DBUtil.class.getClassLoader().getResource(fileName).getPath();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            MapType type = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
            conf = mapper.readValue(new File(path), type);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        return conf;
    }

    public static void readDB(Map<String, Object> conf, String mark, String sql, Map<String, String> str, Map<String, Long> num) {
        DataSource ds = getDataSource(conf.get("driver").toString(), conf.get("jdbc").toString(), conf.get("username").toString(), conf.get("password").toString(), Integer.parseInt(conf.get("initSize").toString()));
        Connection conn = null;
        PreparedStatement pstm;
        ResultSet rs;
        try {
            conn = ds.getConnection();
            pstm = conn.prepareStatement(sql);
            pstm.setString(1, TimeUtil.getCurrentTime());
            rs = pstm.executeQuery();
            if ("hive".equals(mark)) {
                while (rs.next()) {
                    str.put(rs.getString(1), rs.getString(2));
                }
            } else if ("mysql".equals(mark)) {
                while (rs.next()) {
                    num.put(rs.getString(1), rs.getLong(2)*1000);
                }
            }
            pstm.close();
            rs.close();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
    }
}