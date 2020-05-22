package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {

    private BeanObject beanObject;
    private String defaultValue;
    private final List<String> dependencies = new ArrayList<>();
    private String description;
    private List<?> enumValues;
    private String example;
    private List<String> examples;
    private String format;
    private Property items;
    private Number max;
    private Number min;
    private Double multipleOf;
    private String name;
    private boolean required;
    private String schemeContnt;
    private String type;
    private boolean uniqueItems;
    private String value;

    public Property() {
    }

    public Property(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public BeanObject getBeanObject() {
        return this.beanObject;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public List<String> getDependencies() {
        return this.dependencies;
    }

    public String getDescription() {
        return this.description;
    }

    public List<?> getEnumValues() {
        return this.enumValues;
    }

    public String getExample() {
        return this.example;
    }

    public List<String> getExamples() {
        return this.examples;
    }

    public String getFormat() {
        return this.format;
    }

    public Property getItems() {
        return this.items;
    }

    public Number getMax() {
        return this.max;
    }

    public Number getMin() {
        return this.min;
    }

    public Double getMultipleOf() {
        return this.multipleOf;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRequired() {
        return this.required;
    }

    public String getSchemeContnt() {
        return this.schemeContnt;
    }

    public String getType() {
        return this.type;
    }

    public boolean isUniqueItems() {
        return this.uniqueItems;
    }

    public String getValue() {
        return this.value;
    }

    public void setBeanObject(BeanObject beanObject) {
        this.beanObject = beanObject;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setEnumValues(List<?> enumValues) {
        this.enumValues = enumValues;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setItems(Property items) {
        this.items = items;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public void setMultipleOf(Double multipleOf) {
        this.multipleOf = multipleOf;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setSchemeContnt(String schemeContnt) {
        this.schemeContnt = schemeContnt;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setBean(final BeanObject beanObject) {
        if (null != beanObject) {
            this.dependencies.add(beanObject.getName());
        }
        this.beanObject = beanObject;
    }
}
