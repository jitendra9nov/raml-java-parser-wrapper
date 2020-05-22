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

    private final List<WrapperType> headers = new ArrayList<>(5);
    private final List<WrapperType> queryParams = new ArrayList<>(5);
    private final List<WrapperResponse> responses = new ArrayList<>(5);
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
                this.responses.add(new WrapperResponse(resp));
            });
        }
        if (!isEmpty(this.method.headers())) {
            this.method.headers().forEach(header -> {
                this.headers.add(new WrapperType(header));
            });
        }
        if (!isEmpty(this.method.queryParameters())) {
            this.method.queryParameters().forEach(parm -> {
                this.queryParams.add(new WrapperType(parm));
            });
        }
    }

    public List<WrapperType> getHeaders() {
        return this.headers;
    }

    public List<WrapperType> getQueryParams() {
        return this.queryParams;
    }

    public WrapperRequest getWrapperRequest() {
        return this.wrapperRequest;
    }

    public List<WrapperResponse> getResponses() {
        return this.responses;
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

    public void setRequestJsonPath(Map<String, RamlAttribute> requestJsonPath) {
        this.requestJsonPath = requestJsonPath;
    }

    public String getRequestSample() {
        return this.requestSample;
    }

    public void setRequestSample(String requestSample) {
        this.requestSample = requestSample;
    }

    public Map<Integer, String> getResponseCodes() {
        return this.responseCodes;
    }

    public void setResponseCodes(Map<Integer, String> responseCodes) {
        this.responseCodes = responseCodes;
    }

    public Map<Integer, String> getResponseSamples() {
        return this.responseSamples;
    }

    public void setResponseSamples(Map<Integer, String> responseSamples) {
        this.responseSamples = responseSamples;
    }

    public List<RuleSet> getRuleSet() {
        return this.ruleSet;
    }

    public Integer getSuccessCode() {
        return this.successCode;
    }

    public void setSuccessCode(Integer successCode) {
        this.successCode = successCode;
    }

    public String getType() {
        return this.type;
    }
}
