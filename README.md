# Micrometer JMX bindings

[![Build Status](https://travis-ci.com/sukhinin/micrometer-binder-jmx.svg?branch=master)](https://travis-ci.com/sukhinin/micrometer-binder-jmx)
[![codebeat badge](https://codebeat.co/badges/268929b8-c3c0-4cfb-8bb5-ad7946ce4a66)](https://codebeat.co/projects/github-com-sukhinin-micrometer-binder-jmx-master)
[![Download](https://api.bintray.com/packages/sukhinin/maven/micrometer-binder-jmx/images/download.svg)](https://bintray.com/sukhinin/maven/micrometer-binder-jmx/_latestVersion)

Micrometer is a great library provides a simple facade over the instrumentation clients 
for the most popular monitoring systems.

However the current Micrometer release (1.3.1 at the time of writing) provides only 
limited support for Kafka consumer metrics and completely lacks binder implementations
for producer metrics. This is to be addressed in [#1173](https://github.com/micrometer-metrics/micrometer/pull/1173)
and [#1722](https://github.com/micrometer-metrics/micrometer/issues/1722) but support
not there yet.

Also Micrometer has decided to move away from monitoring Kafka via JMX to using to native
Kafka metrics API. This design has a number of advantages as well as some drawbacks, 
such as requiring to pass a Kafka consumer / producer instance to the binder.
 
Micrometer JMX bindings library was started as an attempt to implement better Kafka support
for Micrometer. A side product of such attempt are helper classes that simplify binding
Micrometer to JMX. It is expected that the library will not be limited to Kafka and will 
support other metrics exposed via JMX hence no Kafka reference in library name.

## Collecting Kafka consumer and producer metrics
Setting up consumer and producer metrics collection is as simple as calling `bindTo()` method:
```java
// Bind consumer metrics to global meter registry
KafkaConsumerMetrics binder = new KafkaConsumerMetrics();
binder.bindTo(Metrics.globalRegistry);
```
```java
// Bind producer metrics to global meter registry
KafkaProducerMetrics binder = new KafkaProducerMetrics();
binder.bindTo(Metrics.globalRegistry);
```

It does not matter if you call the `bindTo()` method before or after creating consumers
and producers: the binding implementation will register existing MBeans and subscribe to new 
MBean change notifications. However, calling `bindTo()` while creating consumers or producers 
on the other thread can lead to a race condition and should be avoided.

Do not forget to `close()` the binder when it is no longer needed.

## Implementing custom JMX meter binders
To implement a custom Micrometer binder you should create `JmxMeterBinder` instance 
per JMX domain and call `bindMetricsForMBeanType()` method for each MBean type providing
a callback to be invoked for every existing and future MBean matching given domain and type.
Inside a callback use `bindXxx()` methods of the supplied `BindingContext` instance 
to expose MBean attributes as gauges, time gauges, or functional counters.

A meter will be unregistered automatically if reading its value results in an error. 
This is to prevent outdated or unreadable meters from polluting the registry.

You should also call `close()` on each `JmxMeterBinder` instance when it is no longer needed.
The current implementation removes notification subscriptions and prevents registration 
of future meters but does not unregister existing meters.

For an example implementation please refer to the [ThreadingMetrics](https://github.com/sukhinin/micrometer-binder-jmx/blob/master/src/main/java/com/github/sukhinin/micrometer/jmx/threading/ThreadingMetrics.java)
class.
