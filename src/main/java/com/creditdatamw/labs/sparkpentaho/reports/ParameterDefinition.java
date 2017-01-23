package com.creditdatamw.labs.sparkpentaho.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableSet;

import java.util.List;

/**
 * Definition of a parameter that a {@link ReportDefinition} accepts
 *
 */
@JsonTypeName("parameterDefinition")
public final class ParameterDefinition {
    @JsonProperty("name")
    private final String name;

    @JsonProperty("required")
    private final boolean required;

    @JsonProperty("default")
    private final Object defaultValue;

    @JsonProperty("type")
    private final Class type;

    @JsonCreator
    public ParameterDefinition(@JsonProperty("name") String name,
                               @JsonProperty("required") boolean required,
                               @JsonProperty("type") String typeStr,
                               @JsonProperty("default") Object defaultValue) {
        this.name = name;
        this.required = required;
        this.type = typeFromStr(typeStr);
        this.defaultValue = defaultValue;
    }

    public ParameterDefinition(@JsonProperty("name") String name,
                               @JsonProperty("required") boolean required,
                               @JsonProperty("type") Class type,
                               @JsonProperty("default") Object defaultValue) {
        this.name = name;
        this.required = required;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public ParameterDefinition(String name, boolean required, String type) {
        this.name = name;
        this.required = required;
        this.type = typeFromStr(type);
        this.defaultValue = null;
    }

    public ParameterDefinition(String name, boolean required, Class type) {
        this.name = name;
        this.required = required;
        this.type = type;
        this.defaultValue = null;
    }

    public ParameterDefinition(String name, Class type) {
        this.name = name;
        this.type = type;
        this.required = false;
        this.defaultValue = null;
    }

    private static Class typeFromStr(String typeStr) {
        try {
            if (WHITELISTED_CLASSES.contains(typeStr)) {
                Class clazz = Class.forName(typeStr);
                return clazz;
            }
        } catch(Exception e) {
            // fall through
        }

        if (isOneOf(typeStr, "text", "string")) {
            return String.class;
        }

        if (isOneOf(typeStr,"int","integer")) {
            return Integer.class;
        }

        if (typeStr.equalsIgnoreCase("long")) {
            return Long.class;
        }

        if (isOneOf(typeStr,"bool","boolean")) {
            return Boolean.class;
        }

        if (isOneOf(typeStr,"array","list")) {
            return List.class;
        }

        return Object.class;
    }

    private static boolean isOneOf(String val, String alias1, String alias2) {
        if (val.equalsIgnoreCase(alias1) || val.equalsIgnoreCase(alias2)) {
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }

    @JsonIgnore
    public boolean isMandatory() {
        return required;
    }

    public Class getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    @JsonIgnore
    @Override
    public Object clone() throws CloneNotSupportedException {
        return null;
    }

    private static final ImmutableSet<String> WHITELISTED_CLASSES = ImmutableSet.of(
            "java.lang.Number",
            "java.lang.Integer",
            "java.lang.Double",
            "java.lang.String",
            "java.lang.Long",
            "java.lang.Boolean",
            "java.lang.Char",
            "java.lang.Object",
            "java.util.Date"
    );
}
