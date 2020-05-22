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
    private final List<WrapperSecurityScheme> wrapperSecureBy = new ArrayList<>(5);
    private final List<WrapperSecurityScheme> wrapperSecuritySchemes = new ArrayList<>(5);
    @JsonIgnore
    private Api api;
    @JsonIgnore
    private List<DocumentationItem> documentation;
    @JsonIgnore
    private List<MimeType> mediaTypes;
    private List<String> protocols;
    private String description;
    private String baseUrl;

    private String title;
    private String version;

    public WrapperApi() {
    }

    public WrapperApi(Api api) {
        this.api = api;
        this.allotApi();
    }

    private void allotApi() {
        this.baseUrl = isNull(this.api.baseUri()) ? "" : this.api.baseUri().value();
        this.mediaTypes = this.api.mediaType();
        this.protocols = this.api.protocols();
        this.title = isNull(this.api.title()) ? "" : this.api.title().value();
        this.version = isNull(this.api.version()) ? "" : this.api.version().value();
        this.documentation = this.api.documentation();
        this.description = isNull(this.api.description()) ? "" : this.api.description().value();
        this.allotSecurity();
        this.resourceIterator(this.api.resources(), null, null);

    }

    private void allotSecurity() {
        if (!isEmpty(this.api.securitySchemes())) {
            this.api.securitySchemes().forEach(ss -> {
                this.wrapperSecuritySchemes.add(new WrapperSecurityScheme(ss));
            });
        }
        if (!isEmpty(this.api.securedBy())) {
            this.api.securedBy().forEach(ssr -> {
                if (!isNull(ssr.securityScheme())) {
                    this.wrapperSecureBy.add(new WrapperSecurityScheme(ssr.securityScheme()));
                }

            });
        }
    }

    private void resourceIterator(final List<Resource> resources, final List<TypeDeclaration> uriParam, final List<WrapperResource> wrapperResources) {
        if (!isEmpty(resources)) {
            for (final Resource resource : resources) {
                List<WrapperResource> wrapperResourcesLoc = wrapperResources;
                try {
                    final WrapperResource wRes = new WrapperResource(resource, uriParam);

                    if (null == wrapperResources) {
                        this.wrapperResources.add(wRes);
                        wrapperResourcesLoc = wRes.getChildWrapperResources();
                    } else {
                        wrapperResourcesLoc.add(wRes);
                    }
                } catch (final IllegalStateException ex) {
                    this.resourceIterator(resource.resources(), resource.uriParameters(), wrapperResourcesLoc);
                    continue;
                }
            }

        }
    }

    public List<WrapperResource> getWrapperResources() {
        return this.wrapperResources;
    }

    public List<WrapperSecurityScheme> getWrapperSecureBy() {
        return this.wrapperSecureBy;
    }

    public List<WrapperSecurityScheme> getWrapperSecuritySchemes() {
        return this.wrapperSecuritySchemes;
    }

    public Api getApi() {
        return this.api;
    }

    public List<DocumentationItem> getDocumentation() {
        return this.documentation;
    }

    public List<MimeType> getMediaTypes() {
        return this.mediaTypes;
    }

    public List<String> getProtocols() {
        return this.protocols;
    }

    public String getDescription() {
        return this.description;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public String getTitle() {
        return this.title;
    }

    public String getVersion() {
        return this.version;
    }
}
