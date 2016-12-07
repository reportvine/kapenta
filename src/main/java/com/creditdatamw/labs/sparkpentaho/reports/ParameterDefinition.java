package com.creditdatamw.labs.sparkpentaho.reports;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

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
                               @JsonProperty("type") Class type,
                               @JsonProperty("default") Object defaultValue) {
        this.name = name;
        this.required = required;
        this.type = type;
        this.defaultValue = defaultValue;
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

    public String getName() {
        return name;
    }

    public boolean isMandatory() {
        return required;
    }

    public Class getValueType() {
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
}
