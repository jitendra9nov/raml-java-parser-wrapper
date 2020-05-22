package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;

import static com.bhadouriya.raml.efficacies.BeanFormulator.handleTypes;
import static java.util.Objects.isNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperType {
    @JsonIgnore
    private TypeDeclaration typeDeclaration;
    private String description;
    private Property property;

    public WrapperType() {
    }

    public WrapperType(final TypeDeclaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
        this.description = isNull(typeDeclaration.description()) ? "" : typeDeclaration.description().value();
        this.property = handleTypes(typeDeclaration, typeDeclaration.name());
    }

    public TypeDeclaration getTypeDeclaration() {
        return this.typeDeclaration;
    }

    public String getDescription() {
        return this.description;
    }

    public Property getProperty() {
        return this.property;
    }
}
