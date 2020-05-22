package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rule {
    private String combinator;
    private String field;
    private String id;
    private Boolean not;
    private String operator;
    private List<Rule> rules;
    private String value;

    public String getCombinator() {
        return this.combinator;
    }

    public void setCombinator(final String combinator) {
        this.combinator = combinator;
    }

    public String getField() {
        return this.field;
    }

    public void setField(final String field) {
        this.field = field;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Boolean getNot() {
        return this.not;
    }

    public void setNot(final Boolean not) {
        this.not = not;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(final String operator) {
        this.operator = operator;
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public void setRules(final List<Rule> rules) {
        this.rules = rules;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
