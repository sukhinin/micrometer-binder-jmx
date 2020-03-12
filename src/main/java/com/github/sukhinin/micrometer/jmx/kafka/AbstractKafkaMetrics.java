package com.github.sukhinin.micrometer.jmx.kafka;

import com.github.sukhinin.micrometer.jmx.BindingContext;
import com.github.sukhinin.micrometer.jmx.JmxMeterBinder;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.MeterBinder;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractKafkaMetrics implements MeterBinder, AutoCloseable {

    protected final JmxMeterBinder binder;

    protected final String meterNamePrefix;

    public AbstractKafkaMetrics(String jmxDomain, Iterable<Tag> tags, String meterNamePrefix, MBeanServer mBeanServer) {
        this.binder = new JmxMeterBinder(jmxDomain, this::extractTagsFromObjectName, tags, mBeanServer);
        this.meterNamePrefix = meterNamePrefix;
    }

    /**
     * Binds common producer/consumer/connect/streams metrics.
     *
     * @param ctx    binding context
     * @see <a href="https://kafka.apache.org/documentation/#selector_monitoring">https://kafka.apache.org/documentation/#selector_monitoring</a>.
     */
    protected void bindCommonMetrics(BindingContext ctx) {
        ctx.bindGauge("connection-close-rate", meterNamePrefix + "connection-close-rate", "Connections closed per second in the window.");
        ctx.bindFunctionCounter("connection-close-total", meterNamePrefix + "connection-close-total", "Total connections closed in the window.");
        ctx.bindGauge("connection-creation-rate", meterNamePrefix + "connection-creation-rate", "New connections established per second in the window.");
        ctx.bindFunctionCounter("connection-creation-total", meterNamePrefix + "connection-creation-total", "Total new connections established in the window.");
        ctx.bindGauge("network-io-rate", meterNamePrefix + "network-io-rate", "The average number of network operations (reads or writes) on all connections per second.");
        ctx.bindFunctionCounter("network-io-total", meterNamePrefix + "network-io-total", "The total number of network operations (reads or writes) on all connections.");
        ctx.bindGauge("outgoing-byte-rate", meterNamePrefix + "outgoing-byte-rate", "The average number of outgoing bytes sent per second to all servers.");
        ctx.bindFunctionCounter("outgoing-byte-total", meterNamePrefix + "outgoing-byte-total", "The total number of outgoing bytes sent to all servers.");
        ctx.bindGauge("request-rate", meterNamePrefix + "request-rate", "The average number of requests sent per second.");
        ctx.bindFunctionCounter("request-total", meterNamePrefix + "request-total", "The total number of requests sent.");
        ctx.bindGauge("request-size-avg", meterNamePrefix + "request-size-avg", "The average size of all requests in the window.");
        ctx.bindGauge("request-size-max", meterNamePrefix + "request-size-max", "The maximum size of any request sent in the window.");
        ctx.bindGauge("incoming-byte-rate", meterNamePrefix + "incoming-byte-rate", "Bytes/second read off all sockets.");
        ctx.bindFunctionCounter("incoming-byte-total", meterNamePrefix + "incoming-byte-total", "Total bytes read off all sockets.");
        ctx.bindGauge("response-rate", meterNamePrefix + "response-rate", "Responses received per second.");
        ctx.bindFunctionCounter("response-total", meterNamePrefix + "response-total", "Total responses received.");
        ctx.bindGauge("select-rate", meterNamePrefix + "select-rate", "Number of times the I/O layer checked for new I/O to perform per second.");
        ctx.bindFunctionCounter("select-total", meterNamePrefix + "select-total", "Total number of times the I/O layer checked for new I/O to perform.");
        ctx.bindTimeGauge("io-wait-time-ns-avg", meterNamePrefix + "io-wait-time-avg", "The average length of time the I/O thread spent waiting for a socket ready for reads or writes.", TimeUnit.NANOSECONDS);
        ctx.bindGauge("io-wait-ratio", meterNamePrefix + "io-wait-ratio", "The fraction of time the I/O thread spent waiting.");
        ctx.bindTimeGauge("io-time-ns-avg", meterNamePrefix + "io-time-avg", "The average length of time for I/O per select call.", TimeUnit.NANOSECONDS);
        ctx.bindGauge("io-ratio", meterNamePrefix + "io-ratio", "The fraction of time the I/O thread spent doing I/O.");
        ctx.bindGauge("connection-count", meterNamePrefix + "connection-count", "The current number of active connections.");
        ctx.bindGauge("successful-authentication-rate", meterNamePrefix + "successful-authentication-rate", "Connections per second that were successfully authenticated using SASL or SSL.");
        ctx.bindFunctionCounter("successful-authentication-total", meterNamePrefix + "successful-authentication-total", "Total connections that were successfully authenticated using SASL or SSL.");
        ctx.bindGauge("failed-authentication-rate", meterNamePrefix + "failed-authentication-rate", "Connections per second that failed authentication.");
        ctx.bindFunctionCounter("failed-authentication-total", meterNamePrefix + "failed-authentication-total", "Total connections that failed authentication.");
        ctx.bindGauge("successful-reauthentication-rate", meterNamePrefix + "successful-reauthentication-rate", "Connections per second that were successfully re-authenticated using SASL.");
        ctx.bindFunctionCounter("successful-reauthentication-total", meterNamePrefix + "successful-reauthentication-total", "Total connections that were successfully re-authenticated using SASL.");
        ctx.bindTimeGauge("reauthentication-latency-max", meterNamePrefix + "reauthentication-latency-max", "The maximum latency observed due to re-authentication.", TimeUnit.MILLISECONDS);
        ctx.bindTimeGauge("reauthentication-latency-avg", meterNamePrefix + "reauthentication-latency-avg", "The average latency observed due to re-authentication.", TimeUnit.MILLISECONDS);
        ctx.bindGauge("failed-reauthentication-rate", meterNamePrefix + "failed-reauthentication-rate", "Connections per second that failed re-authentication.");
        ctx.bindFunctionCounter("failed-reauthentication-total", meterNamePrefix + "failed-reauthentication-total", "Total connections that failed re-authentication.");
        ctx.bindFunctionCounter("successful-authentication-no-reauth-total", meterNamePrefix + "successful-authentication-no-reauth-total", "Total connections that were successfully authenticated by older, pre-2.2.0 SASL clients that do not support re-authentication.");
    }

    @Override
    public void close() {
        binder.close();
    }

    private Iterable<Tag> extractTagsFromObjectName(ObjectName obj) {
        List<Tag> tags = new ArrayList<>();

        String clientId = obj.getKeyProperty("client-id");
        if (clientId != null) {
            tags.add(Tag.of("client.id", clientId));
        }

        String topic = obj.getKeyProperty("topic");
        if (topic != null) {
            tags.add(Tag.of("topic", topic));
        }

        String partition = obj.getKeyProperty("partition");
        if (partition != null) {
            tags.add(Tag.of("partition", partition));
        }

        String node = obj.getKeyProperty("node");
        if (node != null) {
            tags.add(Tag.of("node", node));
        }

        return tags;
    }
}
