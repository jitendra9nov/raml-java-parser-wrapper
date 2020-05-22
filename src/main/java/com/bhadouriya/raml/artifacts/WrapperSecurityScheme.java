package com.bhadouriya.raml.artifacts;

import com.bhadouriya.raml.efficacies.BeanFormulator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.raml.v2.api.model.v10.security.SecurityScheme;
import org.raml.v2.api.model.v10.security.SecuritySchemePart;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperSecurityScheme {

    private final List<WrapperType> wrapperHeaders = new ArrayList<>(5);
    private final List<WrapperType> wrapperQueryParams = new ArrayList<>(5);
    private final List<WrapperResponse> wrapperResponses = new ArrayList<>(5);
    @JsonIgnore
    private SecurityScheme securityScheme;
    @JsonIgnore
    private SecuritySchemePart describerBy;
    private String description;
    private String displayName;
    private String name;
    private String type;
    private Property queryString;

    public WrapperSecurityScheme() {
    }

    public WrapperSecurityScheme(final SecurityScheme securityScheme) {
        this.securityScheme = securityScheme;
        this.allotSecurity();
    }

    private void allotSecurity() {
        this.name = this.securityScheme.name();
        this.description = isNull(this.securityScheme.description()) ? "" : this.securityScheme.description().value();
        this.type = this.securityScheme.type();
        this.displayName = isNull(this.securityScheme.displayName()) ? "" : this.securityScheme.displayName().value();
        this.describerBy = this.securityScheme.describedBy();
        this.describerBy();
    }

    private void describerBy() {
        if (!isNull(this.describerBy)) {

            if (null != this.describerBy.queryString()) {
                this.queryString = BeanFormulator.handleTypes(this.describerBy.queryString(), this.describerBy.queryString().name());
            }
            if (!isEmpty(this.describerBy.responses())) {
                this.describerBy.responses().forEach(resp -> {
                    this.wrapperResponses.add(new WrapperResponse(resp));
                });
            }
            if (!isEmpty(this.describerBy.headers())) {
                this.describerBy.headers().forEach(header -> {
                    this.wrapperHeaders.add(new WrapperType(header));
                });
            }
            if (!isEmpty(this.describerBy.queryParameters())) {
                this.describerBy.queryParameters().forEach(parm -> {
                    this.wrapperQueryParams.add(new WrapperType(parm));
                });
            }

        }
    }

    public SecurityScheme getSecurityScheme() {
        return this.securityScheme;
    }

    public SecuritySchemePart getDescriberBy() {
        return this.describerBy;
    }

    public List<WrapperType> getWrapperHeaders() {
        return this.wrapperHeaders;
    }

    public List<WrapperType> getWrapperQueryParams() {
        return this.wrapperQueryParams;
    }

    public List<WrapperResponse> getWrapperResponses() {
        return this.wrapperResponses;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getName() {
        return this.name;
    }

    public String getType() {
        return this.type;
    }

    public Property getQueryString() {
        return this.queryString;
    }
}

