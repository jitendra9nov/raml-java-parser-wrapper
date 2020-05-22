package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.raml.v2.api.model.v10.security.SecurityScheme;
import org.raml.v2.api.model.v10.security.SecuritySchemePart;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperSecurityScheme {

    @JsonIgnore
    private SecurityScheme securityScheme;
    @JsonIgnore
    private SecuritySchemePart describerBy;


    private final List<WrapperType> headers = new ArrayList<>(5);
    private final List<WrapperType> queryParams = new ArrayList<>(5);
    private final List<WrapperType> secureBy = new ArrayList<>(5);
    private List<String> protocols;

    private String description;
    private String type;

    public WrapperSecurityScheme() {
    }

    public WrapperSecurityScheme(final SecurityScheme securityScheme) {
    }
}
