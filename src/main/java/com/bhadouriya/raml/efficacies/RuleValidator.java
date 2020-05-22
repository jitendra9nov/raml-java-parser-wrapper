package com.bhadouriya.raml.efficacies;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import org.hamcrest.Matcher;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Boolean.TRUE;
import static java.util.Arrays.asList;

public class RuleValidator {
    private static final Logger LOGGER = Logger.getLogger(RuleValidator.class.getName());
    private final Object jsonObject;
    private boolean isFound;

    public RuleValidator(Object jsonObject) {
        this.jsonObject = jsonObject;
    }

    private static String convertReaderToString(final Reader reader) throws IOException {
        if (reader != null) {
            final Writer writer = new StringWriter();

            final char[] buffer = new char[1024];
            try {

                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                reader.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public static RuleValidator with(final InputStream is) throws IOException {
        final Reader reader = new InputStreamReader(is);
        return with(reader);
    }

    public static RuleValidator with(final Reader reader) throws IOException {
        return new RuleValidator(JsonPath.parse(convertReaderToString(reader)).json());
    }

    public static RuleValidator with(final String json) throws IOException {
        return new RuleValidator(JsonPath.parse(json).json());
    }

    public RuleValidator fresh() {
        return new RuleValidator(this.jsonObject);
    }


    //MATCHER

    public RuleValidator negate(final Boolean value) {
        this.found(this.found() && !TRUE.equals(value));
        return this;
    }

    public RuleValidator notDefined(final String path) {
        try {
            final Configuration c = Configuration.defaultConfiguration();
            JsonPath.using(c).parse(this.jsonObject).read(path);
            LOGGER.log(Level.INFO, String.format("Document contains the path [%s] but was expected not to", path));
        } catch (final PathNotFoundException e) {
            e.printStackTrace();
        }
        return this;
    }

    public <T> RuleValidator and(RuleValidator... more) {
        this.found(this.found() &&
                !asList(more).stream().filter(item -> item.notFound()).findAny().isPresent());
        return this;
    }

    public <T> RuleValidator and(final String path, final Matcher<T> matcher) {
        final boolean isFound = this.found();
        this.the(path, matcher);
        this.found(isFound && this.found());
        return this;
    }

    public <T> RuleValidator or(RuleValidator... more) {

        this.found(this.found() ||
                !asList(more).stream().filter(item -> item.found()).findAny().isPresent());
        return this;
    }

    public <T> RuleValidator or(final String path, final Matcher<T> matcher) {
        boolean isFound = this.found();
        this.the(path, matcher);
        this.found(isFound || this.found());
        return this;
    }

    private <T> RuleValidator the(String path, Matcher<T> matcher) {
        T obj = null;
        try {
            obj = JsonPath.read(this.jsonObject, path);
            LOGGER.log(Level.INFO, String.format("JSON path [%s], Value[%s]", path, obj));
        } catch (final Exception e) {
            LOGGER.log(Level.WARNING, String.format("Error Reading JSON path [%s], Value[%s]", path) + e.getMessage());
        }
        if (!this.found(matcher.matches(obj))) {
            LOGGER.log(Level.INFO, String.format("JSON Assert Error: \nExpected:\n%s\nActual:\n%s", matcher.toString(), obj));
        }
        return this;
    }

    public boolean found() {
        return this.isFound;
    }

    public boolean notFound() {
        return !this.isFound;
    }

    public boolean found(final boolean found) {
        return this.isFound = found;
    }
}

