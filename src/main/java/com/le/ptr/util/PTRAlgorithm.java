package com.le.ptr.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import scala.Tuple2;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by tanghl on 2017/9/21.
 */
public class PTRAlgorithm {

    public static DecimalFormat df = new DecimalFormat("0.00000");

    private static final Logger LOGGER = LogManager.getLogger(PTRAlgorithm.class.getName());

    public static double comAllocation(String[] totalNode, int cpmDaily, String defaultValue) {
        double totalShareNode = 0;
        double currShareNode = 0;
        try {
            totalNode = preProcessShareNode(totalNode, defaultValue);
            for (int i = 0; i < totalNode.length; i++) {
                totalShareNode = totalShareNode + Double.parseDouble(totalNode[i]);
                int indexCurrShareNode = TimeUtil.getSlice();
                if (i <= indexCurrShareNode) {
                    currShareNode = currShareNode + Double.parseDouble(totalNode[i]);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return (currShareNode / totalShareNode) * cpmDaily;
    }


    public static String comProbability(double adjustmentRate, double beforeSlotProbability, int currShow, double currAllocation) {
        double p = 0.0;
        if (currShow <= currAllocation) {
            p = beforeSlotProbability * (1 + adjustmentRate);
            if (p > 1)
                p = 1;
        } else if (currShow > currAllocation) {
            p = beforeSlotProbability * (1 - adjustmentRate);
//            p = 0.00018d;
            if (p < 0)
                p = 0;
        }
        return df.format(p);
    }

    public static String[] preProcessShareNode(String[] totalNode, String defaultValue) {
        String[] totalNodeExpand = new String[1440];
        if (totalNode.length < 1440) {
            for (int i = 0; i < totalNodeExpand.length; i++) {
                if (i < totalNode.length) {
                    totalNodeExpand[i] = totalNode[i];
                } else {
                    totalNodeExpand[i] = defaultValue;
                }
            }
        } else {
            return totalNode;
        }
        return totalNodeExpand;
    }

    public static Map<String, String> hwmPlan(Map<String, Integer> supply, Map<String, Integer> demand, Map<String, List<String>> relation) {
        /**
         * every demand total supply
         */
        Map<String, String> rankOrders = new LinkedHashMap<>();
        try{
            Map<String, Long> everyDemandTotalSupply = calEveryDemandSupply(supply, demand, relation);
            /**
             * demand node hunger rate
             */
            Map<String, Double> hunger = new HashMap<>(demand.size());
            demand.forEach((k, v) -> {
                String hungerRateString = df.format(Math.random());
                if(everyDemandTotalSupply.get(k) !=0 ){
                    hungerRateString = df.format((double) v / everyDemandTotalSupply.get(k));
                }
                double hungerRate = Double.parseDouble(hungerRateString);
                hunger.put(k, hungerRate);
            });
            Map<String, Double> hungerSorted = sortByValue(hunger);
            /**
             * location supply
             */
            hungerSorted.forEach((k, v) -> {
                try {
                    long total_remain = calEveryDemandSupply(supply, demand, relation).get(k);
                    int everyTotalDemand = demand.get(k);
                    if (total_remain != 0){
                        if (total_remain < everyTotalDemand) {
                            rankOrders.put(k, (rankOrders.size() + 1) + "".concat("_") + 1);
                        } else {
                            String rateString = df.format((double) everyTotalDemand / total_remain);
                            rankOrders.put(k, (rankOrders.size() + 1) + "".concat("_").concat(rateString));
                        }
                        String newKey = TimeUtil.renameKey(k, "relation");
                        if (relation.get(newKey) != null && relation.get(newKey).size() > 0) {
                            relation.get(newKey).forEach(e -> {
                                supply.put(e, (int) ((supply.get(e) == null ? 0 : supply.get(e)) * (1 - Double.parseDouble(rankOrders.get(k).split("_")[1]))));
                            });
                        }
                    }else{
                        rankOrders.put(k, (rankOrders.size() + 1) + "".concat("_").concat(df.format(Math.random())));
                    }
                }catch (NumberFormatException e){
                    LOGGER.error("total key:"+k+"hunger rate:"+v);
                }
            });
        }catch(Exception e){
            LOGGER.error(e.getMessage());
        }
        return rankOrders;
    }

    public static Map<String, Long> calEveryDemandSupply(Map<String, Integer> supply, Map<String, Integer> demand, Map<String, List<String>> relation) {
        Map<String, Long> everyDemandTotalSupply = new HashMap<>(demand.size());
        demand.forEach((k, v) -> {
            long[] totalSupply = {0};
            String newKey=TimeUtil.renameKey(k,"relation");
            if (relation != null && relation.size() > 0 && relation.get(newKey)!=null && relation.get(newKey).size() > 0) {
                relation.get(newKey).forEach(e -> {
                    totalSupply[0] = totalSupply[0] + (supply.get(e) == null?0:supply.get(e));
                });
            }
            everyDemandTotalSupply.put(k, totalSupply[0]);
        });
        return everyDemandTotalSupply;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> unsortMap) {

        List<Map.Entry<K, V>> list =
                new LinkedList<>(unsortMap.entrySet());

        Collections.sort(list, (o1, o2) -> (o2.getValue()).compareTo(o1.getValue()));
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static Map<String,Integer> list2Map(List<Tuple2<String,String>> list){
        Map<String,Integer> map=new HashMap<>();
        list.forEach( e->{
            try{
                map.put(e._1(),Integer.parseInt(e._2()));
            }catch (Exception exception){
                LOGGER.error(exception.getMessage());
            }
        });
        return map;
    }


    public static void validateData(Map<String, Integer> demandData, Map<String, Integer> supplyData, Map<String, List<String>> relation) {
        Set<String> demandKeys=new HashSet<>();
        Set<String> relationKeys=new HashSet<>();
        demandData.forEach( (k,v) -> {
            if(v == 0){
                demandKeys.add(k);
            }
            String newKey=TimeUtil.renameKey(k,"relation");
            if(relation.get(newKey)==null){
                relationKeys.add(k);
            }
        });
        LOGGER.debug("需求量大于整数最大值的异常订单IDS集合大小:"+demandKeys.size()+"\n\t"+demandKeys.toString());
        LOGGER.debug("需求量没有供需关系的IDS集合大小:"+relationKeys.size()+"\n\t"+relationKeys.toString());
        demandKeys.removeAll(relationKeys);
        LOGGER.debug("需求量大于整数最大值的异常订单IDS集合与需求量没有供需关系的IDS集合的差集:"+demandKeys.size()+"\n\t"+demandKeys.toString());
        Set<String> supplyKeys=new HashSet<>();
        supplyData.forEach((k,v) -> {
            if(v == 0){
                supplyKeys.add(k);
            }
        });
        LOGGER.debug("供给量大于整数最大值的异常IDS集合大小:"+supplyKeys.size()+"\n\t"+supplyKeys.toString());
    }

    public static String[] dealMember(String member) {
        String[] memberArrays = new String[]{};
        if (member != null) {
            memberArrays = member.split("\\|");
        }
        for (int i = 0; i < memberArrays.length; i++) {
            memberArrays[i] = memberArrays[i].split(":")[1];
        }
        return memberArrays;
    }
}
