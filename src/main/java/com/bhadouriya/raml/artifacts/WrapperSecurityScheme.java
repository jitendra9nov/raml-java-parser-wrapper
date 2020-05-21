package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.raml.v2.api.model.v10.security.SecurityScheme;
import org.raml.v2.api.model.v10.security.SecuritySchemePart;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperSecurityScheme {

    @JsonIgnore
    private SecurityScheme securityScheme;
    @JsonIgnore
    private SecuritySchemePart describerBy;


    //private List<String> protocols;
    //private List<WrapperSecurityScheme> secureBy=new ArrayList<>(5);
    //private List<WrapperSecurityScheme> secureBy=new ArrayList<>(5);
    //private List<WrapperSecurityScheme> secureBy=new ArrayList<>(5);

    private String description;
    private String type;

    public WrapperSecurityScheme() {
    }

    public WrapperSecurityScheme(final SecurityScheme securityScheme) {
    }
}
