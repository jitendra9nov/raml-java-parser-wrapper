package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Property {

    private final List<String> dependencies = new ArrayList<>();
    private BeanObject beanObject;
    private String defaultValue;
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
        return beanObject;
    }

    public void setBeanObject(BeanObject beanObject) {
        this.beanObject = beanObject;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<?> getEnumValues() {
        return enumValues;
    }

    public void setEnumValues(List<?> enumValues) {
        this.enumValues = enumValues;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public List<String> getExamples() {
        return examples;
    }

    public void setExamples(List<String> examples) {
        this.examples = examples;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Property getItems() {
        return items;
    }

    public void setItems(Property items) {
        this.items = items;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public Double getMultipleOf() {
        return multipleOf;
    }

    public void setMultipleOf(Double multipleOf) {
        this.multipleOf = multipleOf;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getSchemeContnt() {
        return schemeContnt;
    }

    public void setSchemeContnt(String schemeContnt) {
        this.schemeContnt = schemeContnt;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isUniqueItems() {
        return uniqueItems;
    }

    public void setUniqueItems(boolean uniqueItems) {
        this.uniqueItems = uniqueItems;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setBean(final BeanObject beanObject) {
        if (null != beanObject) {
            dependencies.add(beanObject.getName());
        }
        this.beanObject = beanObject;
    }
}
