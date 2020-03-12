package com.github.sukhinin.micrometer.jmx;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class JmxMeterBinder implements AutoCloseable {

    private final String jmxDomain;

    private final TagsExtractor tagger;

    private final Iterable<Tag> tags;

    private final MBeanServer mBeanServer;

    private final List<Runnable> cleanupRunnables = new CopyOnWriteArrayList<>();

    public JmxMeterBinder(String jmxDomain, TagsExtractor tagger) {
        this(jmxDomain, tagger, Tags.empty());
    }

    public JmxMeterBinder(String jmxDomain, TagsExtractor tagger, Iterable<Tag> tags) {
        this(jmxDomain, tagger, tags, ManagementFactory.getPlatformMBeanServer());
    }

    public JmxMeterBinder(String jmxDomain, TagsExtractor tagger, Iterable<Tag> tags, MBeanServer mBeanServer) {
        this.jmxDomain = Objects.requireNonNull(jmxDomain);
        this.tagger = Objects.requireNonNull(tagger);
        this.tags = Objects.requireNonNull(tags);
        this.mBeanServer = Objects.requireNonNull(mBeanServer);
    }

    public void bindMetricsForMBeanType(MeterRegistry registry, String type, BindingCallback callback) {
        try {
            bindMetersForExistingMBeans(registry, type, callback);
            ensureMetersBindingForFutureMBeans(registry, type, callback);
        } catch (OperationsException e) {
            throw new RuntimeException("Error registering Kafka JMX based metrics", e);
        }
    }

    private void bindMetersForExistingMBeans(MeterRegistry registry, String type, BindingCallback callback) throws OperationsException {
        Set<ObjectName> objects = mBeanServer.queryNames(new ObjectName(jmxDomain + ":type=" + type + ",*"), null);
        for (ObjectName obj : objects) {
            bindMetersForMBean(registry, obj, callback);
        }
    }

    private void ensureMetersBindingForFutureMBeans(MeterRegistry registry, String type, BindingCallback callback) throws OperationsException {
        NotificationListener listener = (notification, handback) -> {
            ObjectName obj = ((MBeanServerNotification) notification).getMBeanName();
            bindMetersForMBean(registry, obj, callback);
        };
        NotificationFilter filter = notification -> {
            if (!MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notification.getType())) {
                return false;
            }
            ObjectName obj = ((MBeanServerNotification) notification).getMBeanName();
            return obj.getDomain().equals(jmxDomain) && obj.getKeyProperty("type").equals(type);
        };
        mBeanServer.addNotificationListener(MBeanServerDelegate.DELEGATE_NAME, listener, filter, null);
        cleanupRunnables.add(() -> {
            try {
                mBeanServer.removeNotificationListener(MBeanServerDelegate.DELEGATE_NAME, listener);
            } catch (InstanceNotFoundException | ListenerNotFoundException ignored) {
                // The listener cannot be found, so we don't have to unregister anything
            }
        });
    }

    private void bindMetersForMBean(MeterRegistry registry, ObjectName obj, BindingCallback callback) {
        try {
            Tags tags = Tags.concat(this.tags, tagger.extract(obj));
            callback.invoke(new BindingContext(mBeanServer, registry, obj, tags));
        } catch (Exception e) {
            throw new RuntimeException("Error invoking binding callback", e);
        }
    }

    @Override
    public void close() {
        for (Runnable runnable : cleanupRunnables) {
            runnable.run();
        }
    }
}
