package com.aluxian.codementor.utils;

import android.text.TextUtils;

import com.google.gson.FieldNamingStrategy;

import java.lang.reflect.Field;

public class CamelCaseNamingStrategy implements FieldNamingStrategy {

    @Override
    public String translateName(Field f) {
        String[] parts = f.getName().split("_");

        for (int i = 1; i < parts.length; i++) {
            parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
        }

        return TextUtils.join("", parts);
    }

}
