package com.github.sukhinin.micrometer.jmx;

public class DoubleValue implements DoubleValueMBean {

    private double value;

    public DoubleValue() {
        this(0.0);
    }

    public DoubleValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
