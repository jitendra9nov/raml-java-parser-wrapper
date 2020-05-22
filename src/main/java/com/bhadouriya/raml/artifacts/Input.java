package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Input {

    private String dataType;
    private String childType;
    private String type;
    private String errorText;
    private String pattern;
    private List<Config> options;

    public Input() {
    }

    public Input(String dataType, String type, String errorText, String pattern, String childType) {
        this.dataType = dataType;
        this.childType = childType;
        this.type = type;
        this.errorText = errorText;
        this.pattern = pattern;
    }

    public String getDataType() {
        return this.dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getChildType() {
        return this.childType;
    }

    public void setChildType(String childType) {
        this.childType = childType;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getErrorText() {
        return this.errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getPattern() {
        return this.pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public List<Config> getOptions() {
        return this.options;
    }

    public void setOptions(List<Config> options) {
        this.options = options;
    }
}
