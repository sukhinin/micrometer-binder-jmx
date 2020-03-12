package com.github.sukhinin.micrometer.jmx;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class BindingContextTest {

    private static final String JMX_DOMAIN = "com.github.sukhinin.micrometer.binder.jmx";

    private static final String OBJECT_NAME = JMX_DOMAIN + ":type=Value";

    private static final Iterable<Tag> TAGS = Tags.of("tag", "value");

    private MeterRegistry registry;

    private BindingContext context;

    @BeforeEach
    void beforeEachTest() throws Exception {
        DoubleValue value = new DoubleValue();
        value.setValue(1.0);

        MBeanServer mBeanServer = MBeanServerFactory.newMBeanServer();
        mBeanServer.registerMBean(value, new ObjectName(OBJECT_NAME));

        registry = new SimpleMeterRegistry();
        context = new BindingContext(mBeanServer, registry, new ObjectName(OBJECT_NAME), TAGS);
    }

    @Test
    void shouldReturnObjectName() throws MalformedObjectNameException {
        ObjectName obj = context.getObjectName();
        assertEquals(new ObjectName(OBJECT_NAME), obj);
    }

    @Test
    void shouldReturnTags() {
        Iterable<Tag> tags = context.getTags();
        assertIterableEquals(TAGS, tags);
    }

    @Test
    void shouldBindGauge() {
        context.bindGauge("Value", "double.value", "description");
        Gauge gauge = registry.get("double.value").gauge();
        assertEquals("description", gauge.getId().getDescription());
        assertEquals(1.0, gauge.value());
    }

    @Test
    void shouldBindTimeGauge() {
        context.bindTimeGauge("Value", "double.value", "description", TimeUnit.MINUTES);
        TimeGauge timeGauge = registry.get("double.value").timeGauge();
        assertEquals("description", timeGauge.getId().getDescription());
        assertEquals(TimeUnit.MINUTES.toSeconds(1), timeGauge.value());
    }

    @Test
    void shouldBindFunctionCounter() {
        context.bindFunctionCounter("Value", "double.value", "description");
        FunctionCounter functionCounter = registry.get("double.value").functionCounter();
        assertEquals("description", functionCounter.getId().getDescription());
        assertEquals(1.0, functionCounter.count());
    }

    @Test
    void shouldUnbindMeterOnError() {
        context.bindGauge("NoSuchValue", "double.value", "description");
        Gauge gauge = registry.get("double.value").gauge();
        assertEquals(Double.NaN, gauge.value());
        assertNull(registry.find("double.value").gauge());
    }
}
