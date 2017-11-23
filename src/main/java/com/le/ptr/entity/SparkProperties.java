package com.le.ptr.entity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by tanghl on 2017/9/20.
 */
public class SparkProperties {

    private static final Logger LOGGER = LogManager.getLogger(SparkProperties.class.getName());

    public SparkProperties() {
        try {
            Properties props = new Properties();
            props.load(KafkaProperties.class.getClassLoader().getResourceAsStream("spark.properties"));
            this.PTRApp = props.getProperty("spark.PTRApp");
            this.showApp = props.getProperty("spark.showApp");
            this.master = props.getProperty("spark.master");
            this.PTRDuration = Integer.parseInt(props.getProperty("spark.PTRDuration"));
            this.showDuration = Integer.parseInt(props.getProperty("spark.showDuration"));
            this.offLineAllocateDuration = Integer.parseInt(props.getProperty("spark.offLineAllocateDuration"));
            this.PTRInit = props.getProperty("spark.PTRInit");
            this.PTRA = Double.parseDouble(props.getProperty("spark.PTRA"));
            this.PTRShareSliceNode = props.getProperty("spark.PTRShareSliceNode");
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private String master;

    private String PTRApp;

    private String showApp;

    private int PTRDuration;

    private int showDuration;

    private int offLineAllocateDuration;

    private String PTRInit;

    private double PTRA;

    private String PTRShareSliceNode;

    public String getMaster() {
        return master;
    }

    public String getPTRApp() {
        return PTRApp;
    }

    public String getShowApp() {
        return showApp;
    }

    public int getPTRDuration() {
        return PTRDuration;
    }

    public int getShowDuration() {
        return showDuration;
    }

    public String getPTRInit() {
        return PTRInit;
    }

    public double getPTRA() {
        return PTRA;
    }

    public String getPTRShareSliceNode() {
        return PTRShareSliceNode;
    }

    public int getOffLineAllocateDuration() {
        return offLineAllocateDuration;
    }
}
