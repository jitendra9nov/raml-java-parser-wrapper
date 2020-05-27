package com.bhadouriya.raml.efficacies.demo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Response {

    private int code;
    private String description;
    private List<Attribute> attributes;

    public Response(final int code, final String description, final Attribute... attributes) {
        this.code = code;
        this.description = description;
        this.attributes = (attributes == null) ? Collections.emptyList() : Arrays.asList(attributes);
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(final int code) {
        this.code = code;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public List<Attribute> getAttributes() {
        return this.attributes;
    }

    public void setAttributes(final List<Attribute> attributes) {
        this.attributes = attributes;
    }

    protected static class Attribute {
        public static final Attribute FATAL = new Attribute("fatal");
        public static final Attribute WARNING = new Attribute("warnings");
        public static final Attribute ERROR = new Attribute("errors");
        public static final Attribute INPUT = new Attribute("input");
        public static final Attribute OUTPUT = new Attribute("output");
        private final String description;

        public Attribute(String description) {
            this.description = description;
        }
    }
}
