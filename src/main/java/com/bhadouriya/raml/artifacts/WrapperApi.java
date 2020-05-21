package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.raml.v2.api.model.v10.api.Api;
import org.raml.v2.api.model.v10.api.DocumentationItem;
import org.raml.v2.api.model.v10.bodies.MimeType;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.resources.Resource;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperApi {
    private final List<WrapperResource> wrapperResources = new ArrayList<>(5);
    @JsonIgnore
    private Api api;
    @JsonIgnore
    private List<DocumentationItem> documentation;
    @JsonIgnore
    private List<MimeType> mediaTypes;
    private List<String> protocols;
    private final List<WrapperSecurityScheme> secureBy = new ArrayList<>(5);
    private final List<WrapperSecurityScheme> securitySchemes = new ArrayList<>(5);
    private String description;
    private String baseUrl;

    private String title;
    private String version;

    public WrapperApi() {
    }

    public WrapperApi(Api api) {
        this.api = api;
        allotApi();
    }

    private void allotApi() {
        baseUrl = isNull(api.baseUri()) ? "" : api.baseUri().value();
        mediaTypes = api.mediaType();
        protocols = api.protocols();
        title = isNull(api.title()) ? "" : api.title().value();
        version = isNull(api.version()) ? "" : api.version().value();
        documentation = api.documentation();
        description = isNull(api.description()) ? "" : api.description().value();
        allotSecurity();
        resourceIterator(api.resources(), null, null);

    }

    private void allotSecurity() {
        if (!isEmpty(api.securitySchemes())) {
            api.securitySchemes().forEach(ss -> {
                securitySchemes.add(new WrapperSecurityScheme(ss));
            });
        }
        if (!isEmpty(api.securedBy())) {
            api.securedBy().forEach(ssr -> {
                if (!isNull(ssr.securityScheme())) {
                    securitySchemes.add(new WrapperSecurityScheme(ssr.securityScheme()));
                }

            });
        }
    }

    private void resourceIterator(List<Resource> resources, List<TypeDeclaration> uriParam, List<WrapperResource> wrapperResources) {
        if (!isEmpty(resources)) {
            resources.forEach(resource -> {
                //List<WrapperResource> wrapperResourcesLoc =
            });
        }
    }
}
