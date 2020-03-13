package com.github.sukhinin.micrometer.jmx;

import io.micrometer.core.instrument.*;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;

public class BindingContext {

    private final MBeanServer mBeanServer;

    private final MeterRegistry registry;

    private final ObjectName obj;

    private final Iterable<Tag> tags;

    BindingContext(MBeanServer mBeanServer, MeterRegistry registry, ObjectName obj, Iterable<Tag> tags) {
        this.mBeanServer = Objects.requireNonNull(mBeanServer);
        this.registry = Objects.requireNonNull(registry);
        this.obj = Objects.requireNonNull(obj);
        this.tags = Objects.requireNonNull(tags);
    }

    public ObjectName getObjectName() {
        return obj;
    }

    public Iterable<Tag> getTags() {
        return tags;
    }

    public void bindGauge(String attrName, String meterName, String description) {
        AtomicReference<Meter> meterRef = new AtomicReference<>();
        ToDoubleFunction<BindingContext> accessor = ignored -> getAttributeValueOrUnbindMeter(registry, meterRef, obj, attrName);
        Meter meter = Gauge.builder(meterName, this, accessor)
                .description(description).tags(tags).register(registry);
        meterRef.set(meter);
    }

    public void bindTimeGauge(String attrName, String meterName, String description, TimeUnit timeUnit) {
        AtomicReference<Meter> meterRef = new AtomicReference<>();
        ToDoubleFunction<BindingContext> accessor = ignored -> getAttributeValueOrUnbindMeter(registry, meterRef, obj, attrName);
        Meter meter = TimeGauge.builder(meterName, this, timeUnit, accessor)
                .description(description).tags(tags).register(registry);
        meterRef.set(meter);
    }

    public void bindFunctionCounter(String attrName, String meterName, String description) {
        AtomicReference<Meter> meterRef = new AtomicReference<>();
        ToDoubleFunction<BindingContext> accessor = ignored -> getAttributeValueOrUnbindMeter(registry, meterRef, obj, attrName);
        Meter meter = FunctionCounter.builder(meterName, this, accessor)
                .description(description).tags(tags).register(registry);
        meterRef.set(meter);
    }

    private Double getAttributeValueOrUnbindMeter(MeterRegistry registry, AtomicReference<? extends Meter> meterRef, ObjectName obj, String attrName) {
        try {
            return ((Number) mBeanServer.getAttribute(obj, attrName)).doubleValue();
        } catch (ClassCastException | JMException e) {
            registry.remove(meterRef.get());
            return Double.NaN;
        }
    }
}
