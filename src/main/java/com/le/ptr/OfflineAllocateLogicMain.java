package com.le.ptr;

import com.le.ptr.util.DBUtil;
import com.le.ptr.util.PTRAlgorithm;
import com.le.ptr.util.RedisUtil;
import com.le.ptr.util.TimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Created by tanghl on 2017/11/7.
 */
public class OfflineAllocateLogicMain {

    private static final Logger LOGGER = LogManager.getLogger(OfflineAllocateLogicMain.class.getName());

    private static Map<String,Long> demand = new HashMap<>();

    private static Map<String,String> supply = new HashMap<>();

    private static Map<String,String> relation = new HashMap<>();

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
         * read supply data from hive(1 hour)
         */
        DBUtil.readDB(hive, "hive", hive.get("supply").toString(), supply, null);
        DBUtil.readDB(hive, "hive", hive.get("relation").toString(), relation, null);
        Map<String,Integer> validDemand=new HashMap<>();
        Map<String,Integer> validSupply=new HashMap<>();
        Map<String,List<String>> validRelation=new HashMap<>();
        Jedis jsCluster = RedisUtil.connectSingle(redisConf.get("addr").toString(), Integer.parseInt(redisConf.get("port").toString()), redisConf.get("password").toString());
        demand.forEach((k, v)->{
            if (v>Integer.MAX_VALUE){
                validDemand.put(k,0);
            }else{
                String show=jsCluster.get("ARK.fc.order.".concat(TimeUtil.getCurrentTime()).concat(".").concat(k));
                long showValue=Long.parseLong(show==null?"0":show);
                if((v-showValue) > 0){
                    validDemand.put(k,(int)(v-showValue));
                }else{
                    validDemand.put(k,0);
                }
            }
        });
        supply.forEach((k,v)->{
            long val=Long.parseLong(v);
            if (val>Integer.MAX_VALUE){
                validSupply.put(k,0);
            }else{
                validSupply.put(k,(int)val);
            }
        });
        relation.forEach((k,v)->{
            validRelation.put(k, Arrays.asList(v.split("\\|")));
        });
        Map<String,String> hwm=PTRAlgorithm.hwmPlan(validSupply,validDemand,validRelation);
        hwm.forEach((k,v) -> {
            jsCluster.hset("hwmoffline",k,v);
        });
        jsCluster.close();
        LOGGER.info(hwm);
        long end=System.currentTimeMillis();
        LOGGER.info("总时间:"+(end-start));
    }

}
