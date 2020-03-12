package com.github.sukhinin.micrometer.jmx;

import io.micrometer.core.instrument.Tag;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class TagsUtil {

    public static boolean hasKeys(Iterable<Tag> tags, String... keys) {
        return Arrays.stream(keys).allMatch(name ->
                StreamSupport.stream(tags.spliterator(), false).anyMatch(tag -> tag.getKey().equals(name))
        );
    }

    public static Iterable<String> getKeys(Iterable<Tag> tags) {
        return StreamSupport.stream(tags.spliterator(), false).map(Tag::getKey).collect(Collectors.toList());
    }

    private TagsUtil() {
        throw new UnsupportedOperationException();
    }
}
