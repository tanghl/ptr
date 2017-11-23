package com.le.ptr.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by tanghl on 2017/9/20.
 */
public class KafkaProperties {

    private static final Logger LOGGER = LogManager.getLogger(KafkaProperties.class.getName());

    public KafkaProperties() {
        try {
            Properties props = new Properties();
            props.load(KafkaProperties.class.getClassLoader().getResourceAsStream("kafka.properties"));
            this.topic = props.getProperty("kafka.topic");
            this.broker = props.getProperty("kafka.broker");
            this.port = Integer.parseInt(props.getProperty("kafka.port"));
            this.group = props.getProperty("kafka.group");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private String topic;

    private String broker;

    private int port;

    private String group;

    public String getTopic() {
        return topic;
    }

    public String getBroker() {
        return broker;
    }

    public String getGroup() {
        return group;
    }

    public int getPort() {
        return port;
    }
}
