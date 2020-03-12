package com.github.sukhinin.micrometer.jmx.kafka;

import com.github.sukhinin.micrometer.jmx.DoubleValue;
import com.github.sukhinin.micrometer.jmx.TagsUtil;
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

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class KafkaConsumerMetricsTest {

    private MeterRegistry registry;

    private MBeanServer mBeanServer;

    @BeforeEach
    void beforeEachTest() throws JMException {
        registry = new SimpleMeterRegistry();
        mBeanServer = MBeanServerFactory.newMBeanServer();
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName("kafka.consumer:type=consumer-metrics,client-id=client1"));
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName("kafka.consumer:type=consumer-coordinator-metrics,client-id=client1"));
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName("kafka.consumer:type=consumer-fetch-manager-metrics,client-id=client1"));
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName("kafka.consumer:type=consumer-fetch-manager-metrics,client-id=client1,topic=topic1"));
        mBeanServer.registerMBean(new DoubleValue(), new ObjectName("kafka.consumer:type=consumer-fetch-manager-metrics,client-id=client1,topic=topic1,partition=partition1"));
    }

    @Test
    void shouldBindProducerMetricsToMeterRegistry() {
        KafkaConsumerMetrics binder = new KafkaConsumerMetrics(Tags.empty(), "kafka.consumer.", mBeanServer);
        binder.bindTo(registry);

        Meter connectionCloseRateMeter = registry.find("kafka.consumer.connection-close-rate").meter();
        assertNotNull(connectionCloseRateMeter);
        assertIterableEquals(Arrays.asList("client.id"), TagsUtil.getKeys(connectionCloseRateMeter.getId().getTags()));

        Meter timeBetweenPollAvgMeter = registry.find("kafka.consumer.time-between-poll-avg").meter();
        assertNotNull(timeBetweenPollAvgMeter);
        assertIterableEquals(Arrays.asList("client.id"), TagsUtil.getKeys(timeBetweenPollAvgMeter.getId().getTags()));

        Meter commitLatencyAvgMeter = registry.find("kafka.consumer.commit-latency-avg").meter();
        assertNotNull(commitLatencyAvgMeter);
        assertIterableEquals(Arrays.asList("client.id"), TagsUtil.getKeys(commitLatencyAvgMeter.getId().getTags()));

        Meter preferredReadReplicaMeter = registry.find("kafka.consumer.preferred-read-replica").meter();
        assertNotNull(preferredReadReplicaMeter);
        assertIterableEquals(Arrays.asList("client.id", "partition", "topic"), TagsUtil.getKeys(preferredReadReplicaMeter.getId().getTags()));

        Meter bytesConsumedRateMeter = registry.find("kafka.consumer.bytes-consumed-rate").meter();
        assertNotNull(bytesConsumedRateMeter);
        assertIterableEquals(Arrays.asList("client.id", "topic"), TagsUtil.getKeys(bytesConsumedRateMeter.getId().getTags()));

        Meter fetchLatencyAvgMeter = registry.find("kafka.consumer.fetch-latency-avg").meter();
        assertNotNull(fetchLatencyAvgMeter);
        assertIterableEquals(Arrays.asList("client.id"), TagsUtil.getKeys(fetchLatencyAvgMeter.getId().getTags()));
    }
}
