package com.github.sukhinin.micrometer.jmx;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.management.*;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JmxMeterBinderTest {

    private static final String JMX_DOMAIN = "com.github.sukhinin.micrometer.binder.jmx";

    private static final String OBJECT_TYPE_ATTRIBUTE = "Value";

    private static final String OBJECT_NAME = JMX_DOMAIN + ":type=" + OBJECT_TYPE_ATTRIBUTE;

    private static final Iterable<Tag> EXTRACTED_TAGS = Tags.of("etag", "evalue");

    private static final Iterable<Tag> PRESET_TAGS = Tags.of("ptag", "pvalue");

    private MBeanServer mBeanServer;

    private MeterRegistry registry;

    @BeforeEach
    void beforeEachTest() {
        mBeanServer = MBeanServerFactory.newMBeanServer();
        registry = new SimpleMeterRegistry();
    }

    @Test
    void shouldCallBindingCallbackForExistingMBeansWithProperContext() throws JMException {
        JmxMeterBinder binder = new JmxMeterBinder(JMX_DOMAIN, x -> EXTRACTED_TAGS, PRESET_TAGS, mBeanServer);

        mBeanServer.registerMBean(new DoubleValue(), new ObjectName(OBJECT_NAME));

        AtomicBoolean callbackHasBeenCalled = new AtomicBoolean(false);
        binder.bindMetricsForMBeanType(registry, OBJECT_TYPE_ATTRIBUTE, ctx -> {
            assertEquals(new ObjectName(OBJECT_NAME), ctx.getObjectName());
            assertIterableEquals(Tags.concat(EXTRACTED_TAGS, PRESET_TAGS), ctx.getTags());
            callbackHasBeenCalled.set(true);
        });

        assertTrue(callbackHasBeenCalled.get());
    }

    @Test
    void shouldCallBindingCallbackForFutureMBeansWithProperContext() throws JMException {
        JmxMeterBinder binder = new JmxMeterBinder(JMX_DOMAIN, x -> EXTRACTED_TAGS, PRESET_TAGS, mBeanServer);

        AtomicBoolean callbackHasBeenCalled = new AtomicBoolean(false);
        binder.bindMetricsForMBeanType(registry, OBJECT_TYPE_ATTRIBUTE, ctx -> {
            assertEquals(new ObjectName(OBJECT_NAME), ctx.getObjectName());
            assertIterableEquals(Tags.concat(EXTRACTED_TAGS, PRESET_TAGS), ctx.getTags());
            callbackHasBeenCalled.set(true);
        });

        mBeanServer.registerMBean(new DoubleValue(), new ObjectName(OBJECT_NAME));

        assertTrue(callbackHasBeenCalled.get());
    }

    @Test
    void shouldUnregisterMBeanServerNotificationListenersOnClose() throws JMException {
        MBeanServer spyMBeanServer = spy(mBeanServer);
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName(OBJECT_NAME));

        JmxMeterBinder binder = new JmxMeterBinder(JMX_DOMAIN, x -> EXTRACTED_TAGS, PRESET_TAGS, spyMBeanServer);
        binder.bindMetricsForMBeanType(registry, OBJECT_TYPE_ATTRIBUTE, ctx -> {});

        verify(spyMBeanServer, times(0)).removeNotificationListener(any(ObjectName.class), any(NotificationListener.class));
        binder.close();
        verify(spyMBeanServer, times(1)).removeNotificationListener(any(ObjectName.class), any(NotificationListener.class));
    }

}
