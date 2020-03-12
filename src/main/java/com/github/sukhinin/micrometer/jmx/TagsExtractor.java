package com.github.sukhinin.micrometer.jmx;

import io.micrometer.core.instrument.Tag;

import javax.management.ObjectName;

@FunctionalInterface
public interface TagsExtractor {
    Iterable<Tag> extract(ObjectName obj);
}
