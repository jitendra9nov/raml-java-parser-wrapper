package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.util.StringUtils;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RamlAttribute {
    private Input input;
    private boolean required;
    private String label;
    private String name;
    private List<Config> operators;
    private String type;

    public RamlAttribute(String name, String label, String type, boolean required) {
        this.required = required;
        this.label = StringUtils.hasText(label) ? label.replaceFirst("\\$\\.", "") : label;
        this.name = name;
        this.type = type;
    }

    public Input getInput() {
        return this.input;
    }

    public void setInput(Input input) {
        this.input = input;
    }

    public boolean isRequired() {
        return this.required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Config> getOperators() {
        return this.operators;
    }

    public void setOperators(List<Config> operators) {
        this.operators = operators;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
