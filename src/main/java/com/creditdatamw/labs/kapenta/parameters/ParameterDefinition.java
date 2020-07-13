package com.creditdatamw.labs.kapenta.parameters;

import com.creditdatamw.labs.kapenta.reportdefinition.ReportDefinition;
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
public final class ParameterDefinition implements Cloneable{

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

    @JsonProperty("name")
    private final String name;

    @JsonProperty("required")
    private final boolean required;

    @JsonProperty("default")
    private final Object defaultValue;

    @JsonProperty("type")
    private final String typeName;

    @JsonIgnore
    private final Class type;

    @JsonCreator
    public ParameterDefinition(@JsonProperty("name") String name,
                               @JsonProperty("required") boolean required,
                               @JsonProperty("type") String typeStr,
                               @JsonProperty("default") Object defaultValue) {
        this.name = name;
        this.required = required;
        this.typeName = typeStr;
        this.type = typeFromStr(typeStr);
        this.defaultValue = defaultValue;
    }

    public ParameterDefinition(@JsonProperty("name") String name,
                               @JsonProperty("required") boolean required,
                               @JsonProperty("type") Class type,
                               @JsonProperty("default") Object defaultValue) {
        this.name = name;
        this.required = required;
        this.typeName = type.getSimpleName();
        this.type = type;
        this.defaultValue = defaultValue;
    }

    public ParameterDefinition(String name, boolean required, String typeStr) {
        this.name = name;
        this.required = required;
        this.typeName = typeStr;
        this.type = typeFromStr(typeStr);
        this.defaultValue = null;
    }

    public ParameterDefinition(String name, boolean required, Class type) {
        this.name = name;
        this.required = required;
        this.typeName = type.getSimpleName();
        this.type = type;
        this.defaultValue = null;
    }

    public ParameterDefinition(String name, Class type) {
        this.name = name;
        this.typeName = type.getSimpleName();
        this.type = type;
        this.required = false;
        this.defaultValue = null;
    }

    /**
     * Parses the type specification into a class object.
     * @param typeStr type specification, can be FQCN or simple name like String
     * @return the class for the given type if possible, defaulting to Object.class
     */
    public static Class<?> typeFromStr(String typeStr) {
        try {
            if (WHITELISTED_CLASSES.contains(typeStr)) {
                Class<?> clazz = Class.forName(typeStr);
                return clazz;
            }
        } catch(Exception e) {
            // fall through
        }

        typeStr  = typeStr.toLowerCase().trim();

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
        return new ParameterDefinition(this.name, this.required, this.type, this.defaultValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterDefinition that = (ParameterDefinition) o;

        if (required != that.required) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (defaultValue != null ? !defaultValue.equals(that.defaultValue) : that.defaultValue != null) return false;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (required ? 1 : 0);
        result = 31 * result + (defaultValue != null ? defaultValue.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }


    public static ParameterDefinition requiredParameter(String name, String type) {
        return new ParameterDefinition(name, true, type);
    }

    public static ParameterDefinition requiredParameter(String name, Class clazz) {
        return new ParameterDefinition(name, true, clazz);
    }

    public static <T> ParameterDefinition requiredParameter(String name, T defaultValue, Class clazz) {
        return new ParameterDefinition(name, true, clazz, defaultValue);
    }

    public static ParameterDefinition optionalParameter(String name, Class clazz) {
        return new ParameterDefinition(name, clazz);
    }

    public static <T> ParameterDefinition optionalParameter(String name, T defaultValue, Class clazz) {
        return new ParameterDefinition(name, false, clazz, defaultValue);
    }
}
