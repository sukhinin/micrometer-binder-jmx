package com.github.sukhinin.micrometer.jmx.threading;

import com.github.sukhinin.micrometer.jmx.JmxMeterBinder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.MeterBinder;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.Collections;

public class ThreadingMetrics implements MeterBinder, AutoCloseable {

    private static final String JMX_DOMAIN = "java.lang";

    private static final String DEFAULT_METER_NAME_PREFIX = "java.lang.";

    private final JmxMeterBinder binder;

    private String meterNamePrefix;

    public ThreadingMetrics() {
        this(Tags.empty());
    }

    public ThreadingMetrics(Iterable<Tag> tags) {
        this(tags, DEFAULT_METER_NAME_PREFIX);
    }

    public ThreadingMetrics(Iterable<Tag> tags, String meterNamePrefix) {
        this(tags, meterNamePrefix, ManagementFactory.getPlatformMBeanServer());
    }

    public ThreadingMetrics(Iterable<Tag> tags, String meterNamePrefix, MBeanServer mBeanServer) {
        this.binder = new JmxMeterBinder(JMX_DOMAIN, obj -> Collections.emptyList(), tags, mBeanServer);
        this.meterNamePrefix = meterNamePrefix;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        binder.bindMetricsForMBeanType(registry, "Threading", ctx -> {
            ctx.bindGauge("DaemonThreadCount", meterNamePrefix + "daemon-thread-count", "DaemonThreadCount");
            ctx.bindGauge("PeakThreadCount", meterNamePrefix + "peak-thread-count", "PeakThreadCount");
            ctx.bindGauge("ThreadCount", meterNamePrefix + "thread-count", "ThreadCount");
            ctx.bindFunctionCounter("TotalStartedThreadCount", meterNamePrefix + "total-started-thread-count", "TotalStartedThreadCount");
        });
    }

    @Override
    public void close() {
        binder.close();
    }
}
