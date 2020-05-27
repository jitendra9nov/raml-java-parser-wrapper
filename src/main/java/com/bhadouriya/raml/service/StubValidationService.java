package com.bhadouriya.raml.service;

import org.raml.v2.api.model.common.ValidationResult;
import org.raml.v2.api.model.v10.datamodel.TypeDeclaration;
import org.raml.v2.api.model.v10.methods.Method;
import org.raml.v2.api.model.v10.resources.Resource;
import org.raml.v2.internal.impl.commons.model.RamlValidationResult;
import org.springframework.util.Assert;

import java.util.*;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

public class StubValidationService {
    private static final Logger LOGGER = Logger.getLogger(StubValidationService.class.getName());
    private final StubResourceService stubResourceService;

    public StubValidationService(StubResourceService stubResourceService) {
        this.stubResourceService = stubResourceService;
    }

    private static boolean expectsUriParameters(String resourcePath) {
        if (isBlank(resourcePath)) {
            return false;
        }
        return resourcePath.contains("{") || resourcePath.contains("}");
    }

    private static List<String> extraxtPathComponents(String resourcePath) {
        return Arrays.asList(resourcePath.split("/"));
    }

    private static String extraxtUriParameterkey(String ramlResourcePath) {
        return ramlResourcePath.substring(1, ramlResourcePath.length() - 1);
    }

    private static Map<String, String> extractUriParameters(String resourcePath, String ramlResourcePath) {
        if (isBlank(resourcePath) || isBlank(ramlResourcePath) || !expectsUriParameters(ramlResourcePath)) {
            return new HashMap<>();
        } else {
            List<String> pathComponents = extraxtPathComponents(resourcePath);
            List<String> ramlPathComponents = extraxtPathComponents(ramlResourcePath);
            Map<String, String> uriParameters = new HashMap<>();
            int count = 0;
            for (Iterator<String> var6 = pathComponents.iterator(); var6.hasNext(); ++count) {
                String component = var6.next();
                String ramlComponent = ramlPathComponents.get(count);
                if (ramlComponent.matches("[{][\\S]+[}]")) {
                    String key = extraxtUriParameterkey(ramlComponent);
                    uriParameters.put(key, component);
                }

            }
            return uriParameters;
        }
    }

    private List<TypeDeclaration> extractAllUriParams(Resource resource) {
        List<TypeDeclaration> resourceUriParamTypes = new ArrayList<>();
        resourceUriParamTypes.addAll(resource.uriParameters());

        for (Resource parentResource = resource.parentResource(); parentResource != null; parentResource = parentResource.parentResource()) {
            resourceUriParamTypes.addAll(parentResource.uriParameters());
        }
        return resourceUriParamTypes;
    }

    protected List<ValidationResult> validateBody(String methodType, String resourcePath, String body) {
        Assert.hasText(methodType, "Method type cannot be blank");
        Assert.hasText(resourcePath, "Resource path cannot be blank");
        Optional<Method> methodOptional = this.stubResourceService.findMethod(methodType, resourcePath);

        if (methodOptional.isPresent()) {
            Method method = methodOptional.get();
            List<ValidationResult> validationResults;
            if (isEmpty(method.body()) && isNotBlank(body)) {
                validationResults = new ArrayList<>();
                validationResults.add(new RamlValidationResult("RAML Does Not expect Body but one is found"));
                return validationResults;
            } else if (!isEmpty(method.body()) && isBlank(body)) {
                validationResults = new ArrayList<>();
                validationResults.add(new RamlValidationResult("RAML expect Body but none is found"));
                return validationResults;
            } else if (isEmpty(method.body())) {
                return new ArrayList<>();
            } else {
                validationResults = new ArrayList<>();
                validationResults.addAll(method.body().get(0).validate(body));
                return validationResults;
            }
        } else {
            throw new IllegalArgumentException(String.format("Failed to fid method for methodType %s and path %s", methodType, resourcePath));
        }
    }

    protected List<ValidationResult> validateQueryParameters(String methodType, String resourcePath, Map<String, String[]> queryParam) {
        Assert.hasText(methodType, "Method type cannot be blank");
        Assert.hasText(resourcePath, "Resource path cannot be blank");
        Optional<Method> methodOptional = this.stubResourceService.findMethod(methodType, resourcePath);

        if (methodOptional.isPresent()) {
            Method method = methodOptional.get();
            if (isEmpty(method.queryParameters())) {
                return new ArrayList<>();
            } else {
                List<ValidationResult> validationResults = new ArrayList<>();
                Iterator<TypeDeclaration> var7 = method.queryParameters().iterator();

                while (var7.hasNext()) {
                    TypeDeclaration typeDeclaration = var7.next();
                    String key = typeDeclaration.name();
                    if (typeDeclaration.required() && (queryParam == null || !queryParam.containsKey(key))) {
                        validationResults.add(new RamlValidationResult("RAML expect Body but none is found"));
                    } else if (!queryParam.containsKey(key)) {
                        // optional not found
                    } else {
                        validationResults.addAll(typeDeclaration.validate(queryParam.get(key)[0]));
                    }
                }
                return validationResults;
            }
        } else {
            throw new IllegalArgumentException(String.format("Failed to fid method for methodType %s and path %s", methodType, resourcePath));
        }
    }

    public List<ValidationResult> validateRequest(String methodType, String resourcePath, Map<String, String[]> queryParam, String body) {
        Assert.hasText(methodType, "Method type cannot be blank");
        Assert.hasText(resourcePath, "Resource path cannot be blank");
        List<String> ignoredEndPoints = new ArrayList<>();
        ignoredEndPoints.add("/healthcheck");
        ignoredEndPoints.add("/health");
        List<ValidationResult> validationResults = new ArrayList<>();
        if ((resourcePath != null) && !ignoredEndPoints.contains(resourcePath)) {
            validationResults.addAll(this.validateBody(methodType, resourcePath, body));
            validationResults.addAll(this.validateUriParameters(resourcePath));
            validationResults.addAll(this.validateQueryParameters(methodType, resourcePath, queryParam));
        }
        return validationResults;
    }

    private List<ValidationResult> validateUriParameters(String resourcePath) {
        Assert.hasText(resourcePath, "Resource path cannot be blank");
        Optional<Resource> resourceOptional = this.stubResourceService.findResource(resourcePath);

        if (resourceOptional.isPresent()) {
            Resource resource = resourceOptional.get();
            if (!expectsUriParameters(resource.resourcePath())) {
                return new ArrayList<>();
            } else {
                Map<String, String> uriParameters = extractUriParameters(resourcePath, resource.resourcePath());

                List<ValidationResult> validationResults = new ArrayList<>();
                Iterator<TypeDeclaration> var7 = this.extractAllUriParams(resource).iterator();

                while (var7.hasNext()) {
                    TypeDeclaration typeDeclaration = var7.next();
                    String key = typeDeclaration.name();

                    validationResults.addAll(typeDeclaration.validate(uriParameters.get(key)));
                }
                return validationResults;
            }
        } else {
            throw new IllegalArgumentException(String.format("Failed to find  path %s", resourcePath));

        }
    }
}
