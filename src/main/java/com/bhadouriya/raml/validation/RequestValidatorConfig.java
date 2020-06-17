package com.bhadouriya.raml.validation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class RequestValidatorConfig {
    @Value("${ramlRequestValidation.isRamlOnClasspath:true}")
    private boolean isRamlOnClasspath;

    @Value("${ramlRequestValidation.ramlPath:/raml/api.raml}")
    private String ramlPath;

    @Bean
    public ResourceService resourceService() {
        return new ResourceService(isRamlOnClasspath, ramlPath);
    }

    @Bean
    public ValidationService validationService(final ResourceService resourceService) {
        return new ValidationService(resourceService);
    }

    @Bean
    public RequestValidationFilter requestValidationFilter(final ValidationService validationService) {
        return new RequestValidationFilter(validationService);
    }
}
