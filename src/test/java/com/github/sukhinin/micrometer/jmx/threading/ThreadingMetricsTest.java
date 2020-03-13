package com.github.sukhinin.micrometer.jmx.threading;

import com.github.sukhinin.micrometer.jmx.DoubleValue;
import com.github.sukhinin.micrometer.jmx.TagsUtil;
import com.github.sukhinin.micrometer.jmx.kafka.KafkaProducerMetrics;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ThreadingMetricsTest {

    private MeterRegistry registry;

    private MBeanServer mBeanServer;

    @BeforeEach
    void beforeEachTest() throws JMException {
        registry = new SimpleMeterRegistry();
        mBeanServer = MBeanServerFactory.newMBeanServer();
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName("java.lang:type=Threading"));
    }

    @Test
    void shouldBindProducerMetricsToMeterRegistry() {
        ThreadingMetrics binder = new ThreadingMetrics(Tags.empty(), "java.lang.", mBeanServer);
        binder.bindTo(registry);

        Meter daemonThreadCountMeter = registry.find("java.lang.daemon-thread-count").meter();
        assertNotNull(daemonThreadCountMeter);
        assertIterableEquals(Collections.emptyList(), TagsUtil.getKeys(daemonThreadCountMeter.getId().getTags()));
    }
}
