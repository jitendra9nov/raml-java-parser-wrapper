package com.bhadouriya.raml.validation;

import org.raml.v2.api.model.common.ValidationResult;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public class RamlValidationException extends RamlException {


    protected RamlValidationException(List<ErrorMessage> messages) {
        super(messages);
    }

    public static RamlValidationException create(List<ValidationResult> validationResults) {
        List<ErrorMessage> messages = new ArrayList<>();
        for (ValidationResult validationRes : validationResults
        ) {
            messages.add(new ErrorMessage(ErrorType.BAD_REQUEST.name(), validationRes.getMessage()));
        }
        return new RamlValidationException(messages);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.BAD_REQUEST;
    }
}
