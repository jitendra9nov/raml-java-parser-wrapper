package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleSet {
    private String code;
    private String message;
    private String name;
    private Integer order;
    private String query;
    private Rule ruleQuery;

    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return this.order;
    }

    public void setOrder(final Integer order) {
        this.order = order;
    }

    public String getQuery() {
        return this.query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    public Rule getRuleQuery() {
        return this.ruleQuery;
    }

    public void setRuleQuery(final Rule ruleQuery) {
        this.ruleQuery = ruleQuery;
    }
}
