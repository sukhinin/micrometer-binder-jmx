package com.github.sukhinin.micrometer.jmx.kafka;

import com.github.sukhinin.micrometer.jmx.BindingContext;
import com.github.sukhinin.micrometer.jmx.TagsUtil;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

public class KafkaProducerMetrics extends AbstractKafkaMetrics {

    private static final String JMX_DOMAIN = "kafka.producer";

    private static final String DEFAULT_METER_NAME_PREFIX = "kafka.producer.";

    public KafkaProducerMetrics() {
        this(Tags.empty());
    }

    public KafkaProducerMetrics(Iterable<Tag> tags) {
        this(tags, DEFAULT_METER_NAME_PREFIX);
    }

    public KafkaProducerMetrics(Iterable<Tag> tags, String meterNamePrefix) {
        this(tags, meterNamePrefix, ManagementFactory.getPlatformMBeanServer());
    }

    public KafkaProducerMetrics(Iterable<Tag> tags, String meterNamePrefix, MBeanServer mBeanServer) {
        super(JMX_DOMAIN, tags, meterNamePrefix, mBeanServer);
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        binder.bindMetricsForMBeanType(registry, "producer-metrics", this::bindCommonMetrics);
        binder.bindMetricsForMBeanType(registry, "producer-metrics", this::bindProducerMetrics);
        binder.bindMetricsForMBeanType(registry, "producer-metrics", this::bindSenderMetrics);
        binder.bindMetricsForMBeanType(registry, "producer-topic-metrics", this::bindPerTopicSenderMetrics);
    }

    /**
     * Binds producer-specific metrics.
     *
     * @param ctx binding context
     * @see <a href="https://kafka.apache.org/documentation/#producer_monitoring">https://kafka.apache.org/documentation/#producer_monitoring</a>.
     */
    private void bindProducerMetrics(BindingContext ctx) {
        ctx.bindGauge("waiting-threads", meterNamePrefix + "waiting-threads", "The number of user threads blocked waiting for buffer memory to enqueue their records.");
        ctx.bindGauge("buffer-total-bytes", meterNamePrefix + "buffer-total-bytes", "The maximum amount of buffer memory the client can use (whether or not it is currently used).");
        ctx.bindGauge("buffer-available-bytes", meterNamePrefix + "buffer-available-bytes", "The total amount of buffer memory that is not being used (either unallocated or in the free list).");
        ctx.bindGauge("bufferpool-wait-time", meterNamePrefix + "bufferpool-wait-time", "The fraction of time an appender waits for space allocation.");
    }

    /**
     * Binds per-client producer sender metrics.
     *
     * @param ctx binding context
     * @see <a href="https://kafka.apache.org/documentation/#producer_sender_monitoring">https://kafka.apache.org/documentation/#producer_sender_monitoring</a>.
     */
    private void bindSenderMetrics(BindingContext ctx) {
        ctx.bindGauge("batch-size-avg", meterNamePrefix + "batch-size-avg", "The average number of bytes sent per partition per-request.");
        ctx.bindGauge("batch-size-max", meterNamePrefix + "batch-size-max", "The max number of bytes sent per partition per-request.");
        ctx.bindGauge("batch-split-rate", meterNamePrefix + "batch-split-rate", "The average number of batch splits per second.");
        ctx.bindFunctionCounter("batch-split-total", meterNamePrefix + "batch-split-total", "The total number of batch splits.");
        ctx.bindGauge("compression-rate-avg", meterNamePrefix + "compression-rate-avg", "The average compression rate of record batches.");
        ctx.bindTimeGauge("metadata-age", meterNamePrefix + "metadata-age", "The age of the current producer metadata being used.", TimeUnit.SECONDS);
        ctx.bindTimeGauge("produce-throttle-time-avg", meterNamePrefix + "produce-throttle-time-avg", "The average time a request was throttled by a broker.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("produce-throttle-time-max", meterNamePrefix + "produce-throttle-time-max", "The maximum time a request was throttled by a broker.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("record-queue-time-avg", meterNamePrefix + "record-queue-time-avg", "The average time record batches spent in the send buffer.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("record-queue-time-max", meterNamePrefix + "record-queue-time-max", "The maximum time record batches spent in the send buffer.", TimeUnit.MILLISECONDS);
        ctx.bindGauge("record-size-avg", meterNamePrefix + "record-size-avg", "The average record size.");
        ctx.bindGauge("record-size-max", meterNamePrefix + "record-size-max", "The maximum record size.");
        ctx.bindGauge("records-per-request-avg", meterNamePrefix + "records-per-request-avg", "The average number of records per request.");
        ctx.bindTimeGauge("request-latency-avg", meterNamePrefix + "request-latency-avg", "The average request latency.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("request-latency-max", meterNamePrefix + "request-latency-max", "The maximum request latency.", TimeUnit.MILLISECONDS);
        ctx.bindGauge("requests-in-flight", meterNamePrefix + "requests-in-flight", "The current number of in-flight requests awaiting a response.");
    }

    /**
     * Binds per-topic producer sender metrics.
     *
     * @param ctx binding context
     * @see <a href="https://kafka.apache.org/documentation/#producer_sender_monitoring">https://kafka.apache.org/documentation/#producer_sender_monitoring</a>.
     */
    private void bindPerTopicSenderMetrics(BindingContext ctx) {
        ctx.bindGauge("byte-rate", meterNamePrefix + "byte-rate", "The average number of bytes sent per second for a topic.");
        ctx.bindFunctionCounter("byte-total", meterNamePrefix + "byte-total", "The total number of bytes sent for a topic.");
        ctx.bindGauge("compression-rate", meterNamePrefix + "compression-rate", "The average compression rate of record batches for a topic.");
        ctx.bindGauge("record-error-rate", meterNamePrefix + "record-error-rate", "The average per-second number of record sends that resulted in errors for a topic.");
        ctx.bindFunctionCounter("record-error-total", meterNamePrefix + "record-error-total", "The total number of record sends that resulted in errors for a topic.");
        ctx.bindGauge("record-retry-rate", meterNamePrefix + "record-retry-rate", "The average per-second number of retried record sends for a topic.");
        ctx.bindFunctionCounter("record-retry-total", meterNamePrefix + "record-retry-total", "The total number of retried record sends for a topic.");
        ctx.bindGauge("record-send-rate", meterNamePrefix + "record-send-rate", "The average number of records sent per second for a topic.");
        ctx.bindFunctionCounter("record-send-total", meterNamePrefix + "record-send-total", "The total number of records sent for a topic.");
    }
}
