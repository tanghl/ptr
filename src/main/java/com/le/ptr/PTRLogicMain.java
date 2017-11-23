package com.le.ptr;

import com.le.ptr.entity.RedisProperties;
import com.le.ptr.entity.SparkProperties;
import com.le.ptr.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.util.*;

/**
 * Created by tanghl on 2017/11/13.
 */
public class PTRLogicMain {

    private static final Logger LOGGER = LogManager.getLogger(PTRLogicMain.class.getName());

    private static Map<String, Long> demand = new HashMap<>();

    private static Map<String, String> member = new HashMap<>();

    private static SparkProperties sparkProperties = new SparkProperties();

    private static RedisProperties redisProperties = new RedisProperties();

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Map<String, Object> dbConf = DBUtil.getConfObj(args, "db.json");
        Map<String, Object> redisConf = DBUtil.getConfObj(args, "redis.json");
        Map<String, Object> mysql = (LinkedHashMap) dbConf.get("mysql");
        Map<String, Object> hive = (LinkedHashMap) dbConf.get("hive");
        /**
         * read demand data from db(1 minute)
         */
        DBUtil.readDB(mysql, "mysql", mysql.get("demand").toString(), null, demand);
        /**
         * read member trend line(1 day)
         */
        DBUtil.readDB(hive, "hive", hive.get("member").toString(), member, null);
        /**
         * read fc(1 minute)
         */
        String classPath=PTRLogicMain.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String filePath = classPath.substring(0,classPath.lastIndexOf(File.separator)).concat(File.separator).concat(TimeUtil.getCurrentTime().concat(".properties"));
        LOGGER.info("cache file dir:"+filePath);
        Properties prop = FileUtil.readFile(filePath);
        Map<String, String> p = FileUtil.prop2Map(prop);
        Jedis jsCluster = RedisUtil.connectSingle(redisConf.get("addr").toString(), Integer.parseInt(redisConf.get("port").toString()), redisConf.get("password").toString());
        demand.forEach((k, v) -> {
            String[] memberSet = PTRAlgorithm.dealMember(member.get(k));
            List<String> pSet = new ArrayList<>();
            if (p.get(k) != null) {
                pSet = Arrays.asList(p.get(k).trim().split("\\|"));
            }
            double allocateCpm = 0d;
            if (v > Integer.MAX_VALUE) {
                allocateCpm = PTRAlgorithm.comAllocation(memberSet, 0, sparkProperties.getPTRShareSliceNode());
            } else {
                allocateCpm = PTRAlgorithm.comAllocation(memberSet, v.intValue(), sparkProperties.getPTRShareSliceNode());
            }
            String possibility = sparkProperties.getPTRInit() + "";
            if (!pSet.isEmpty() && "".equals(pSet.get(0))) {
                p.put(k, possibility.concat(p.get(k)));
            }
            String[] ret = PTRMain.fillSet(pSet, possibility);
            if (ret != null && ret.length > 1) {
                StringBuffer sb = new StringBuffer(p.get(k) == null ? "" : p.get(k));
                for (String s : ret) {
                    sb.append("|").append(s);
                }
                p.put(k, sb.toString());
            }
            String showKey = redisProperties.getShow().concat(".").concat(TimeUtil.getCurrentTime()).concat(".").concat(k);
            if (pSet != null && pSet.size() > 0) {
                possibility = PTRAlgorithm.comProbability(sparkProperties.getPTRA(), Double.parseDouble(pSet.get(pSet.size() - 1)), jsCluster.get(showKey) == null ? 0 : Integer.parseInt(jsCluster.get(showKey)), allocateCpm);
            } else {
                possibility = PTRAlgorithm.comProbability(sparkProperties.getPTRA(), Double.parseDouble(sparkProperties.getPTRInit()), jsCluster.get(showKey) == null ? 0 : Integer.parseInt(jsCluster.get(showKey)), allocateCpm);
            }

            if (pSet != null && pSet.size() < TimeUtil.getSlice()) {
                p.put(k, p.get(k) == null?possibility:p.get(k).concat("|").concat(possibility));
                jsCluster.set(redisProperties.getPtrKey().concat(".").concat(k), possibility);
            }
        });
        FileUtil.writeFile(filePath, p);
        RedisUtil.closeSingle(jsCluster);
        long end = System.currentTimeMillis();
        LOGGER.info("总时间:" + (end - start));
    }

}