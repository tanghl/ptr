package com.le.ptr.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by tanghl on 2017/9/20.
 */
public class TimeUtil {

    public static String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
    }

    public static int getSlice(){
        LocalDateTime now=LocalDateTime.now();
        return now.getHour() * 60 + now.getMinute();
    }

    public static String renameKey(String oldKey,String mark){
        if(oldKey.lastIndexOf(".")>0){
            return oldKey.substring(0, oldKey.lastIndexOf(".") + 1).concat(mark);
        }else{
            return  oldKey;
        }
    }

    public static String renameNewKey(String oldKey,String mark){
        return mark.concat(".").concat(oldKey.substring(0, oldKey.lastIndexOf(".")));
    }

}
