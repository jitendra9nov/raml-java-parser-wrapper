package com.bhadouriya.raml.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
public class ErrorResponse {
    private List<ErrorMessage> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(List<ErrorMessage> errors) {
        this.errors = errors;
    }
}
