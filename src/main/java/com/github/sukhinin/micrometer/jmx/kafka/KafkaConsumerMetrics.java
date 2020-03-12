package com.github.sukhinin.micrometer.jmx.kafka;

import com.github.sukhinin.micrometer.jmx.BindingContext;
import com.github.sukhinin.micrometer.jmx.TagsUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class KafkaConsumerMetrics extends AbstractKafkaMetrics {

    private static final String JMX_DOMAIN = "kafka.consumer";

    private static final String DEFAULT_METER_NAME_PREFIX = "kafka.consumer.";

    public KafkaConsumerMetrics() {
        this(Tags.empty());
    }

    public KafkaConsumerMetrics(Iterable<Tag> tags) {
        this(tags, DEFAULT_METER_NAME_PREFIX);
    }

    public KafkaConsumerMetrics(Iterable<Tag> tags, String meterNamePrefix) {
        this(tags, meterNamePrefix, ManagementFactory.getPlatformMBeanServer());
    }

    public KafkaConsumerMetrics(Iterable<Tag> tags, String meterNamePrefix, MBeanServer mBeanServer) {
        super(JMX_DOMAIN, tags, meterNamePrefix, mBeanServer);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        binder.bindMetricsForMBeanType(registry, "consumer-metrics", this::bindCommonMetrics);
        binder.bindMetricsForMBeanType(registry, "consumer-metrics", this::bindConsumerMetrics);
        binder.bindMetricsForMBeanType(registry, "consumer-coordinator-metrics", this::bindCoordinatorMetrics);
        binder.bindMetricsForMBeanType(registry, "consumer-fetch-manager-metrics", this::bindFetchManagerMetrics);
    }

    /**
     * Binds consumer-specific metrics.
     *
     * @param ctx binding context
     * @see <a href="https://kafka.apache.org/documentation/#consumer_monitoring">https://kafka.apache.org/documentation/#consumer_monitoring</a>.
     */
    private void bindConsumerMetrics(BindingContext ctx) {
        ctx.bindTimeGauge("time-between-poll-avg", meterNamePrefix + "time-between-poll-avg", "The average delay between invocations of poll().", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("time-between-poll-max", meterNamePrefix + "time-between-poll-max", "The max delay between invocations of poll().", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("last-poll-seconds-ago", meterNamePrefix + "last-poll-ago", "Time since the last poll() invocation.", TimeUnit.SECONDS);
        ctx.bindGauge("poll-idle-ratio-avg", meterNamePrefix + "poll-idle-ratio-avg", "The average fraction of time the consumer's poll() is idle as opposed to waiting for the user code to process records.");
    }

    /**
     * Binds consumer group coordinator metrics.
     *
     * @param ctx binding context
     * @see <a href="https://kafka.apache.org/documentation/#consumer_group_monitoring">https://kafka.apache.org/documentation/#consumer_group_monitoring</a>.
     */
    private void bindCoordinatorMetrics(BindingContext ctx) {
        ctx.bindTimeGauge("commit-latency-avg", meterNamePrefix + "commit-latency-avg", "The average time taken for a commit request.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("commit-latency-max", meterNamePrefix + "commit-latency-max", "The max time taken for a commit request.", TimeUnit.MILLISECONDS);
        ctx.bindGauge("commit-rate", meterNamePrefix + "commit-rate", "The number of commit calls per second.");
        ctx.bindFunctionCounter("commit-total", meterNamePrefix + "commit-total", "The total number of commit calls.");
        ctx.bindGauge("assigned-partitions", meterNamePrefix + "assigned-partitions", "The number of partitions currently assigned to this consumer.");
        ctx.bindTimeGauge("heartbeat-response-time-max", meterNamePrefix + "heartbeat-response-time-max", "The max time taken to receive a response to a heartbeat request.", TimeUnit.MILLISECONDS);
        ctx.bindGauge("heartbeat-rate", meterNamePrefix + "heartbeat-rate", "The average number of heartbeats per second.");
        ctx.bindFunctionCounter("heartbeat-total", meterNamePrefix + "heartbeat-total", "The total number of heartbeats.");
        ctx.bindTimeGauge("join-time-avg", meterNamePrefix + "join-time-avg", "The average time taken for a group rejoin.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("join-time-max", meterNamePrefix + "join-time-max", "The max time taken for a group rejoin.", TimeUnit.MILLISECONDS);
        ctx.bindGauge("join-rate", meterNamePrefix + "join-rate", "The number of group joins per second.");
        ctx.bindFunctionCounter("join-total", meterNamePrefix + "join-total", "The total number of group joins.");
        ctx.bindTimeGauge("sync-time-avg", meterNamePrefix + "sync-time-avg", "The average time taken for a group sync.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("sync-time-max", meterNamePrefix + "sync-time-max", "The max time taken for a group sync.", TimeUnit.MILLISECONDS);
        ctx.bindGauge("sync-rate", meterNamePrefix + "sync-rate", "The number of group syncs per second.");
        ctx.bindFunctionCounter("sync-total", meterNamePrefix + "sync-total", "The total number of group syncs.");
        ctx.bindTimeGauge("rebalance-latency-avg", meterNamePrefix + "rebalance-latency-avg", "The average time taken for a group rebalance.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("rebalance-latency-max", meterNamePrefix + "rebalance-latency-max", "The max time taken for a group rebalance.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("rebalance-latency-total", meterNamePrefix + "rebalance-latency-total", "The total time taken for group rebalances so far.", TimeUnit.MILLISECONDS);
        ctx.bindFunctionCounter("rebalance-total", meterNamePrefix + "rebalance-total", "The total number of group rebalances participated.");
        ctx.bindGauge("rebalance-rate-per-hour", meterNamePrefix + "rebalance-rate-per-hour", "The number of group rebalance participated per hour.");
        ctx.bindFunctionCounter("failed-rebalance-total", meterNamePrefix + "failed-rebalance-total", "The total number of failed group rebalances.");
        ctx.bindGauge("failed-rebalance-rate-per-hour", meterNamePrefix + "failed-rebalance-rate-per-hour", "The number of failed group rebalance event per hour.");
        ctx.bindTimeGauge("last-rebalance-seconds-ago", meterNamePrefix + "last-rebalance-ago", "The number of seconds since the last rebalance event.", TimeUnit.SECONDS);
        ctx.bindTimeGauge("last-heartbeat-seconds-ago", meterNamePrefix + "last-heartbeat-ago", "The number of seconds since the last controller heartbeat.", TimeUnit.SECONDS);
        ctx.bindTimeGauge("partitions-revoked-latency-avg", meterNamePrefix + "partitions-revoked-latency-avg", "The average time taken by the on-partitions-revoked rebalance listener callback.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("partitions-revoked-latency-max", meterNamePrefix + "partitions-revoked-latency-max", "The max time taken by the on-partitions-revoked rebalance listener callback.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("partitions-assigned-latency-avg", meterNamePrefix + "partitions-assigned-latency-avg", "The average time taken by the on-partitions-assigned rebalance listener callback.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("partitions-assigned-latency-max", meterNamePrefix + "partitions-assigned-latency-max", "The max time taken by the on-partitions-assigned rebalance listener callback.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("partitions-lost-latency-avg", meterNamePrefix + "partitions-lost-latency-avg", "The average time taken by the on-partitions-lost rebalance listener callback.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("partitions-lost-latency-max", meterNamePrefix + "partitions-lost-latency-max", "The max time taken by the on-partitions-lost rebalance listener callback.", TimeUnit.MILLISECONDS);
    }

    /**
     * Binds consumer fetch manager metrics.
     *
     * @param ctx binding context
     * @see <a href="https://kafka.apache.org/documentation/#consumer_fetch_monitoring">https://kafka.apache.org/documentation/#consumer_fetch_monitoring</a>.
     */
    private void bindFetchManagerMetrics(BindingContext ctx) {
        // Metrics reported per consumer, topic and partition
        if (TagsUtil.hasKeys(ctx.getTags(), "topic", "partition")) {
            ctx.bindGauge("preferred-read-replica", meterNamePrefix + "preferred-read-replica", "The current read replica for the partition, or -1 if reading from leader.");
            ctx.bindGauge("records-lag", meterNamePrefix + "records-lag", "The latest lag of the partition.");
            ctx.bindGauge("records-lag-avg", meterNamePrefix + "records-lag-avg", "The average lag of the partition.");
            ctx.bindGauge("records-lag-max", meterNamePrefix + "records-lag-max", "The max lag of the partition.");
            ctx.bindGauge("records-lead", meterNamePrefix + "records-lead", "The latest lead of the partition.");
            ctx.bindGauge("records-lead-avg", meterNamePrefix + "records-lead-avg", "The average lead of the partition.");
            ctx.bindGauge("records-lead-min", meterNamePrefix + "records-lead-min", "The min lead of the partition.");
            // Metrics reported per consumer and topic
        } else if (TagsUtil.hasKeys(ctx.getTags(), "topic")) {
            ctx.bindGauge("bytes-consumed-rate", meterNamePrefix + "bytes-consumed-rate", "The average number of bytes consumed per second for a topic.");
            ctx.bindFunctionCounter("bytes-consumed-total", meterNamePrefix + "bytes-consumed-total", "The total number of bytes consumed for a topic.");
            ctx.bindGauge("fetch-size-avg", meterNamePrefix + "fetch-size-avg", "The average number of bytes fetched per request for a topic.");
            ctx.bindGauge("fetch-size-max", meterNamePrefix + "fetch-size-max", "The maximum number of bytes fetched per request for a topic.");
            ctx.bindGauge("records-consumed-rate", meterNamePrefix + "records-consumed-rate", "The average number of records consumed per second for a topic.");
            ctx.bindFunctionCounter("records-consumed-total", meterNamePrefix + "records-consumed-total", "The total number of records consumed for a topic.");
            ctx.bindGauge("records-per-request-avg", meterNamePrefix + "records-per-request-avg", "The average number of records in each request for a topic.");
            // Metrics reported just per consumer
        } else {
            ctx.bindTimeGauge("fetch-latency-avg", meterNamePrefix + "fetch-latency-avg", "The average time taken for a fetch request.", TimeUnit.MILLISECONDS);
            ctx.bindTimeGauge("fetch-latency-max", meterNamePrefix + "fetch-latency-max", "The max time taken for any fetch request.", TimeUnit.MILLISECONDS);
            ctx.bindGauge("fetch-rate", meterNamePrefix + "fetch-rate", "The number of fetch requests per second.");
            ctx.bindTimeGauge("fetch-throttle-time-avg", meterNamePrefix + "fetch-throttle-time-avg", "The average throttle time.", TimeUnit.MILLISECONDS);
            ctx.bindTimeGauge("fetch-throttle-time-max", meterNamePrefix + "fetch-throttle-time-max", "The maximum throttle time.", TimeUnit.MILLISECONDS);
            ctx.bindGauge("fetch-total", meterNamePrefix + "fetch-total", "The total number of fetch requests.");
        }
    }
}
