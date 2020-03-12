package com.github.sukhinin.micrometer.jmx;

import javax.management.MalformedObjectNameException;

@FunctionalInterface
public interface BindingCallback {
    void invoke(BindingContext context) throws MalformedObjectNameException;
}
