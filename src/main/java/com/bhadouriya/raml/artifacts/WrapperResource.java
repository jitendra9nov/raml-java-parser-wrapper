package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.resources.Resource;

import java.util.ArrayList;
import java.util.List;

import static com.bhadouriya.raml.efficacies.Efficacy.capatalizeAndAppend;
import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperResource {
    private final List<WrapperMethod> wrapperMethods = new ArrayList<>(5);
    private final List<WrapperType> wrapperUriParams = new ArrayList<>(5);
    private final List<WrapperResource> childWrapperResources = new ArrayList<>(5);
    @JsonIgnore
    private Resource resource;
    private String description;
    private String name;
    private String path;

    public WrapperResource() {
    }

    public WrapperResource(final Resource resource, final List<TypeDeclaration> uriParams) {
        this.resource = resource;
        if (!isEmpty(uriParams)) {
            uriParams.forEach(uriParam -> {
                this.wrapperUriParams.add(new WrapperType(uriParam));
            });
        }
        this.allotResourc();
    }

    private void allotResourc() {
        this.name = capatalizeAndAppend(isNull(this.resource.displayName()) ? "" : this.resource.displayName().value());
        this.description = capatalizeAndAppend(isNull(this.resource.description()) ? "" : this.resource.description().value());
        this.path = this.resource.resourcePath();
        if (!isEmpty(this.resource.uriParameters())) {
            this.resource.uriParameters().forEach(param -> {
                this.wrapperUriParams.add(new WrapperType(param));
            });
        }
        if (!isEmpty(this.resource.methods())) {
            this.resource.methods().forEach(method -> {
                this.wrapperMethods.add(new WrapperMethod(method));
            });
        } else {
            throw new IllegalStateException("Skipping Empty resources --" + this.toString());
        }
    }

    public Resource getResource() {
        return this.resource;
    }

    public List<WrapperMethod> getWrapperMethods() {
        return this.wrapperMethods;
    }

    public List<WrapperType> getWrapperUriParams() {
        return this.wrapperUriParams;
    }

    public List<WrapperResource> getChildWrapperResources() {
        return this.childWrapperResources;
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public String getPath() {
        return this.path;
    }
}
