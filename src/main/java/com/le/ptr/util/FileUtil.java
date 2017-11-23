package com.le.ptr.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by tanghl on 2017/11/13.
 */
public class FileUtil {

    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class.getName());

    public static Properties readFile(String path) {
        Properties prop = new Properties();
        existsFile(path);
        InputStream input = null;
        try {
            input = new FileInputStream(path);
            prop.load(input);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }
        }
        return prop;
    }

    public static void writeFile(String path,Map<String,String> data) {
        Properties prop = new Properties();
        data.forEach(prop::setProperty);
        OutputStream output = null;
        try {
            output = new FileOutputStream(path);
            prop.store(output, null);
        } catch (IOException io) {
            LOGGER.error(io.getMessage());
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage());
                }
            }

        }
    }

    public static void existsFile(String path) {
        File file = new File(path);
        if (!file.isDirectory() && !file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    public static Map<String,String> prop2Map(Properties prop){
        Stream<Map.Entry<Object, Object>> stream = prop.entrySet().stream();
        Map<String, String> map = stream.collect(Collectors.toMap(
                e -> String.valueOf(e.getKey()),
                e -> String.valueOf(e.getValue())));
        return map;
    }
}