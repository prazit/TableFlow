package com.tflow.system;

public class Properties extends java.util.Properties {

    public boolean getPropertyBoolean(String property, boolean defaultValue) {
        return Boolean.parseBoolean(getProperty(property, String.valueOf(defaultValue)));
    }

    public long getPropertyLong(String property, long defaultValue) {
        return Long.parseLong(getProperty(property, String.valueOf(defaultValue)));
    }

    public int getPropertyInt(String property, int defaultValue) {
        return Integer.parseInt(getProperty(property, String.valueOf(defaultValue)));
    }

}
