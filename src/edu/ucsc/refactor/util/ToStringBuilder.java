package edu.ucsc.refactor.util;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Helps with {@code toString()} methods.
 *
 * @author hsanchez@cs.ucsc.edu (Huascar A. Sanchez)
 */
public class ToStringBuilder {
    // We use a Linked Hash Map to ensure ordering of elements.
    private final Map<String, Object> map = new LinkedHashMap<String, Object>();
    private final String name;

    public ToStringBuilder(String name) {
        this.name = name;
    }

    public ToStringBuilder add(String name, Object value) {
        if (map.put(name, value) != null) {
            throw new RuntimeException("Duplicate names: " + name);
        }
        return this;
    }

    @Override public String toString() {
        return name + map.toString().replace('{', '[').replace('}', ']');
    }
}