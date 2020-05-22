package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang.WordUtils;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

import static com.bhadouriya.raml.efficacies.BeanFormulator.toBean;
import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperRequest {
    private BeanObject beanObject;
    private String example;
    private List<String> examples;
    private String mediaType;
    private String requestBeanJson;
    private String requestBeanName;
    @JsonIgnore
    private List<TypeDeclaration> requests;
    private boolean required;

    public WrapperRequest() {

    }

    public WrapperRequest(final List<TypeDeclaration> requests) {
        this.requests = requests;
        this.grabRequest();
    }

    private void grabRequest() {
        this.requests.forEach(td -> {
            this.mediaType = td.name();
            this.required = Boolean.TRUE.equals(td.required());
            this.requestBeanName = WordUtils.capitalize(td.type());
            this.requestBeanJson = td.toJsonSchema();

            String exampleLoc = isNull(td.example()) ? null : td.example().value();
            if (!isNull(exampleLoc) && exampleLoc.startsWith("[") && exampleLoc.endsWith("]")) {
                exampleLoc = exampleLoc.substring(1, exampleLoc.length() - 1).replaceAll("\\s+", "");
            }
            this.example = exampleLoc;

            if (!isEmpty(td.examples())) {
                final List<String> LocExamples = new ArrayList<>(5);
                td.examples().forEach(ex -> {
                    LocExamples.add(ex.value());
                });
                this.examples = LocExamples;
            }
            this.beanObject = toBean(td);
        });
    }

    public BeanObject getBeanObject() {
        return this.beanObject;
    }

    public String getExample() {
        return this.example;
    }

    public List<String> getExamples() {
        return this.examples;
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public String getRequestBeanJson() {
        return this.requestBeanJson;
    }

    public String getRequestBeanName() {
        return this.requestBeanName;
    }

    public List<TypeDeclaration> getRequests() {
        return this.requests;
    }

    public boolean isRequired() {
        return this.required;
    }
}
