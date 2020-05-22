package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang.WordUtils;
import org.raml.v2.api.model.v10.bodies.Response;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

import static com.bhadouriya.raml.efficacies.BeanFormulator.toBean;
import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperResponse {
    private final List<WrapperType> wrapperHeaders = new ArrayList<>(5);
    private BeanObject beanObject;
    @JsonIgnore
    private List<TypeDeclaration> body;
    private String code;
    private String description;
    private String example;
    private List<String> examples;
    private String mediaType;
    private String responseBeanJson;
    private String responseBeanName;
    private boolean required;
    @JsonIgnore
    private Response response;

    public WrapperResponse() {

    }

    public WrapperResponse(final Response response) {
        this.response = response;
        this.grabResponse();
    }

    private void grabResponse() {
        this.body = this.response.body();
        this.code = isNull(this.response.code()) ? "" : this.response.code().value();
        this.description = isNull(this.response.description()) ? "" : this.response.description().value();

        if (!isEmpty(this.response.headers())) {
            this.response.headers().forEach(header -> {
                this.wrapperHeaders.add(new WrapperType(header));
            });
        }
        this.body.forEach(td -> {
            this.mediaType = td.name();
            this.required = Boolean.TRUE.equals(td.required());
            this.responseBeanName = WordUtils.capitalize(td.type());
            this.responseBeanJson = td.toJsonSchema();

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

    public List<TypeDeclaration> getBody() {
        return this.body;
    }

    public String getCode() {
        return this.code;
    }

    public String getDescription() {
        return this.description;
    }

    public String getExample() {
        return this.example;
    }

    public List<String> getExamples() {
        return this.examples;
    }

    public List<WrapperType> getWrapperHeaders() {
        return this.wrapperHeaders;
    }

    public String getMediaType() {
        return this.mediaType;
    }

    public String getResponseBeanJson() {
        return this.responseBeanJson;
    }

    public String getResponseBeanName() {
        return this.responseBeanName;
    }

    public boolean isRequired() {
        return this.required;
    }

    public Response getResponse() {
        return this.response;
    }
}
