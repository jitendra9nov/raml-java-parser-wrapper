package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.File;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Artifacts {
    private String apiName;
    private WrapperApi wrapperApi;
    private String baseDirectory;
    private String basePackage;
    @JsonIgnore
    private Map<String, File> directories;
    private boolean isAnnotation;
    private String packageName;
    private String ramlPath;

    public Artifacts() {
    }

    public Artifacts(final String apiName, final WrapperApi wrapperApi, final String baseDirectory, final String basePackage) {
        this.apiName = apiName;
        this.wrapperApi = wrapperApi;
        this.baseDirectory = baseDirectory;
        this.basePackage = basePackage;
    }

    public String getApiName() {
        return this.apiName;
    }

    public void setApiName(final String apiName) {
        this.apiName = apiName;
    }

    public WrapperApi getWrapperApi() {
        return this.wrapperApi;
    }

    public void setWrapperApi(final WrapperApi wrapperApi) {
        this.wrapperApi = wrapperApi;
    }

    public String getBaseDirectory() {
        return this.baseDirectory;
    }

    public void setBaseDirectory(final String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getBasePackage() {
        return this.basePackage;
    }

    public void setBasePackage(final String basePackage) {
        this.basePackage = basePackage;
    }

    public Map<String, File> getDirectories() {
        return this.directories;
    }

    public void setDirectories(final Map<String, File> directories) {
        this.directories = directories;
    }

    public boolean isAnnotation() {
        return this.isAnnotation;
    }

    public void setAnnotation(final boolean annotation) {
        this.isAnnotation = annotation;
    }

    public String getPackageName() {
        return this.packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    public String getRamlPath() {
        return this.ramlPath;
    }

    public void setRamlPath(final String ramlPath) {
        this.ramlPath = ramlPath;
    }
}
