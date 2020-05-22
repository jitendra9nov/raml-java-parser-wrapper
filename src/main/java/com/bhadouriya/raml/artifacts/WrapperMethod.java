package com.bhadouriya.raml.artifacts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.raml.v2.api.model.v10.methods.Method;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WrapperMethod {

    private final List<WrapperType> wrapperHeaders = new ArrayList<>(5);
    private final List<WrapperType> wrapperQueryParams = new ArrayList<>(5);
    private final List<WrapperResponse> wrapperResponses = new ArrayList<>(5);
    private final List<RuleSet> ruleSet = new ArrayList<>(5);
    private WrapperRequest wrapperRequest;
    private String description;
    @JsonIgnore
    private Method method;
    private Map<String, RamlAttribute> requestJsonPath = new LinkedHashMap<>(5);
    private String requestSample;
    private Map<Integer, String> responseCodes = new LinkedHashMap<>(5);
    private Map<Integer, String> responseSamples = new LinkedHashMap<>(5);
    private Integer successCode;
    private String type;

    public WrapperMethod() {
    }

    public WrapperMethod(final Method method) {
        this.method = method;
        this.allotMethod();
    }

    private void allotMethod() {
        this.type = this.method.method();
        this.description = isNull(this.method.description()) ? "" : this.method.description().value();

        if (!isEmpty(this.method.body())) {
            this.wrapperRequest = new WrapperRequest(this.method.body());
        }
        if (!isEmpty(this.method.responses())) {
            this.method.responses().forEach(resp -> {
                this.wrapperResponses.add(new WrapperResponse(resp));
            });
        }
        if (!isEmpty(this.method.headers())) {
            this.method.headers().forEach(header -> {
                this.wrapperHeaders.add(new WrapperType(header));
            });
        }
        if (!isEmpty(this.method.queryParameters())) {
            this.method.queryParameters().forEach(parm -> {
                this.wrapperQueryParams.add(new WrapperType(parm));
            });
        }
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

    public List<RuleSet> getRuleSet() {
        return this.ruleSet;
    }

    public WrapperRequest getWrapperRequest() {
        return this.wrapperRequest;
    }

    public String getDescription() {
        return this.description;
    }

    public Method getMethod() {
        return this.method;
    }

    public Map<String, RamlAttribute> getRequestJsonPath() {
        return this.requestJsonPath;
    }

    public void setRequestJsonPath(final Map<String, RamlAttribute> requestJsonPath) {
        this.requestJsonPath = requestJsonPath;
    }

    public String getRequestSample() {
        return this.requestSample;
    }

    public void setRequestSample(final String requestSample) {
        this.requestSample = requestSample;
    }

    public Map<Integer, String> getResponseCodes() {
        return this.responseCodes;
    }

    public void setResponseCodes(final Map<Integer, String> responseCodes) {
        this.responseCodes = responseCodes;
    }

    public Map<Integer, String> getResponseSamples() {
        return this.responseSamples;
    }

    public void setResponseSamples(final Map<Integer, String> responseSamples) {
        this.responseSamples = responseSamples;
    }

    public Integer getSuccessCode() {
        return this.successCode;
    }

    public void setSuccessCode(final Integer successCode) {
        this.successCode = successCode;
    }

    public String getType() {
        return this.type;
    }
}
