package com.bhadouriya.raml.validation;

import org.springframework.http.HttpStatus;

public class RamlResourceNotFoundException extends RamlException {


    protected RamlResourceNotFoundException(ErrorMessage message) {
        super(message);
    }

    public static RamlResourceNotFoundException create(String resourcePath) {
        String errorMess = String.format(Constant.NO_RAML_RESOURCE_FOUD_FOR_RESOURCE, resourcePath);

        return new RamlResourceNotFoundException(new ErrorMessage(ErrorType.RAML_RESOURCE_NOT_FOUND.name(), errorMess));
    }

    public static RamlResourceNotFoundException create(String resourcePath, String methodName) {
        String errorMess = String.format(Constant.NO_RAML_RESOURCE_FOUD_FOR_TYPE, methodName, resourcePath);

        return new RamlResourceNotFoundException(new ErrorMessage(ErrorType.RAML_RESOURCE_NOT_FOUND.name(), errorMess));
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }
}
