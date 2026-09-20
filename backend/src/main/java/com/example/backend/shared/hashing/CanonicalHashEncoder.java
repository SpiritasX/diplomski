package com.example.backend.shared.hashing;

import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class CanonicalHashEncoder {

    private final StringBuilder builder = new StringBuilder();

    public CanonicalHashEncoder append(String name, String value) {
        String normalizedValue = Objects.toString(value, "");

        builder.append(name.length())
                .append(':')
                .append(name)
                .append('=')
                .append(normalizedValue.length())
                .append(':')
                .append(normalizedValue)
                .append('\n');
        return this;
    }

    public String build() {
        return builder.toString();
    }
}
