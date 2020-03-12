package com.github.sukhinin.micrometer.jmx.kafka;

import com.github.sukhinin.micrometer.jmx.DoubleValue;
import com.github.sukhinin.micrometer.jmx.TagsUtil;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class KafkaProducerMetricsTest {

    private MeterRegistry registry;

    private MBeanServer mBeanServer;

    @BeforeEach
    void beforeEachTest() throws JMException {
        registry = new SimpleMeterRegistry();
        mBeanServer = MBeanServerFactory.newMBeanServer();
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName("kafka.producer:type=producer-metrics,client-id=client1"));
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName("kafka.producer:type=producer-topic-metrics,client-id=client1,topic=topic1"));
    }

    @Test
    void shouldBindProducerMetricsToMeterRegistry() {
        KafkaProducerMetrics binder = new KafkaProducerMetrics(Tags.empty(), "kafka.producer.", mBeanServer);
        binder.bindTo(registry);

        Meter connectionCloseRateMeter = registry.find("kafka.producer.connection-close-rate").meter();
        assertNotNull(connectionCloseRateMeter);
        assertIterableEquals(Arrays.asList("client.id"), TagsUtil.getKeys(connectionCloseRateMeter.getId().getTags()));

        Meter waitingThreadsMeter = registry.find("kafka.producer.waiting-threads").meter();
        assertNotNull(waitingThreadsMeter);
        assertIterableEquals(Arrays.asList("client.id"), TagsUtil.getKeys(waitingThreadsMeter.getId().getTags()));

        Meter batchSizeAvgMeter = registry.find("kafka.producer.batch-size-avg").meter();
        assertNotNull(batchSizeAvgMeter);
        assertIterableEquals(Arrays.asList("client.id"), TagsUtil.getKeys(batchSizeAvgMeter.getId().getTags()));

        Meter kafkaProducerByteRateMeter = registry.find("kafka.producer.byte-rate").meter();
        assertNotNull(kafkaProducerByteRateMeter);
        assertIterableEquals(Arrays.asList("client.id", "topic"), TagsUtil.getKeys(kafkaProducerByteRateMeter.getId().getTags()));
    }
}
