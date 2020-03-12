package com.github.sukhinin.micrometer.jmx;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class TagsUtilTest {

    @Test
    void shouldTestForTagsInCollectionByKeys() {
        Iterable<Tag> tags = Tags.of("tag1", "value1", "tag2", "value2");

        assertTrue(TagsUtil.hasKeys(tags));
        assertTrue(TagsUtil.hasKeys(tags, "tag1"));
        assertTrue(TagsUtil.hasKeys(tags, "tag1", "tag2"));

        assertFalse(TagsUtil.hasKeys(tags, "tag1", "tag2", "tag3"));
    }

    @Test
    void shouldReturnTagKeys() {
        Iterable<Tag> tags = Tags.of("tag1", "value1", "tag2", "value2");
        assertIterableEquals(Arrays.asList("tag1", "tag2"), TagsUtil.getKeys(tags));
    }
}
