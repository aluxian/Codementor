package com.aluxian.codementor.data.converters;

import com.bluelinelabs.logansquare.typeconverters.StringBasedTypeConverter;

import java.util.UUID;

public class StringIdTypeConverter extends StringBasedTypeConverter<Long> {

    @Override
    public Long getFromString(String value) {
        return UUID.fromString(value).getMostSignificantBits();
    }

    @Override
    public String convertToString(Long value) {
        return Long.toString(value);
    }

}
