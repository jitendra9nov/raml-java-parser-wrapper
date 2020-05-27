package com.bhadouriya.raml.service;

import com.bhadouriya.raml.artifacts.WrapperApi;

public class StubCacheEntity {
    WrapperApi wrapperApi;
    StubValidationService stubValidationService;

    public StubCacheEntity(final WrapperApi wrapperApi, final StubValidationService stubValidationService) {
        this.wrapperApi = wrapperApi;
        this.stubValidationService = stubValidationService;
    }

    public WrapperApi getWrapperApi() {
        return this.wrapperApi;
    }

    public void setWrapperApi(final WrapperApi wrapperApi) {
        this.wrapperApi = wrapperApi;
    }

    public StubValidationService getStubValidationService() {
        return this.stubValidationService;
    }

    public void setStubValidationService(final StubValidationService stubValidationService) {
        this.stubValidationService = stubValidationService;
    }
}
