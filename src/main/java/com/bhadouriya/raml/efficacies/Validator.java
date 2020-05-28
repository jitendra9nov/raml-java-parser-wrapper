package com.bhadouriya.raml.efficacies;

import com.bhadouriya.raml.artifacts.*;
import com.bhadouriya.raml.service.CacheServiceImpl;
import com.bhadouriya.raml.service.StubCacheEntity;
import com.bhadouriya.raml.service.StubValidationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.hamcrest.Matcher;
import org.raml.v2.api.model.common.ValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Optional.of;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.*;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

public class Validator {

    private static final Logger LOGGER = Logger.getLogger(Validator.class.getName());
    private static final ObjectMapper mapper = new ObjectMapper();

    public static ResponseEntity<Object> doRuleValidation(final Object requestBody,
                                                          final String resourcePath,
                                                          final String methodType, final WrapperApi wrapperApi) throws IOException {
        ResponseEntity<Object> responseEntity = null;
        Optional<WrapperMethod> methodOptional = findMethod(methodType, resourcePath, wrapperApi.getWrapperResources());
        if (!methodOptional.isPresent()) {
            LOGGER.log(Level.SEVERE, String.format("Failed to find Rule for method type'%s' and resource path '%s'", methodType, resourcePath));
            throw new IllegalArgumentException(String.format("Failed to find Rule for method type'%s' and resource path '%s'", methodType, resourcePath));
        } else {
            final WrapperMethod wrapperMethod = methodOptional.get();
            final HttpStatus responseCode = HttpStatus.valueOf(wrapperMethod.getSuccessCode());
            String defaultResp = null;
            if (!isEmpty(wrapperMethod.getResponseSamples()) && !isEmpty(wrapperMethod.getWrapperResponses())) {
                defaultResp = wrapperMethod.getResponseSamples().get(wrapperMethod.getSuccessCode());
            }
            responseEntity = validateRules(wrapperMethod, wrapperMethod.getRuleSet(), responseCode, defaultResp, requestBody);
        }
        return responseEntity;
    }

    public static String extractResourcePath(final HttpServletRequest request) {
        String resourcePath = request.getServletPath();
        if (!hasText(resourcePath)) {
            resourcePath = request.getRequestURI();
        }
        return resourcePath;
    }

    private static Optional<WrapperResource> findCustomMatch(final String resourcePath, final List<WrapperResource> wrapperResources, Optional<WrapperResource> findResource, final String sanitisedResourcePath) {

        if (!findResource.isPresent()) {
            for (final WrapperResource resource : wrapperResources
            ) {
                if (findExactMatch(resourcePath, resource.getPath())) {
                    LOGGER.log(Level.INFO, String.format("Custom Match found for resource path '%s'", sanitisedResourcePath));
                    findResource = of(resource);
                    break;
                }

            }
        }
        return findResource;
    }

    private static boolean findExactMatch(final String uri, final String pattern) {
        final int uCount = StringUtils.countMatches(uri, "/");
        final int pCount = StringUtils.countMatches(pattern, "/");
        if (uCount == pCount) {
            final String[] s1 = uri.split("/");
            final String[] s2 = pattern.split("/");
            if (s1.length == s2.length) {
                for (int i = 0; i < s1.length; i++) {
                    if (s1[i].equals(s2[i]) || (s2[i].contains("{") && s2[i].contains("}"))) {
                        if ((i > 0 && s1[i].isEmpty())) {
                            return false;
                        }
                    } else {
                        return false;
                    }

                }
                return true;
            }
        }

        return false;
    }

    private static Optional<WrapperResource> findHardMatch(final List<WrapperResource> wrapperResources, Optional<WrapperResource> findResource, final String sanitisedResourcePath) {
        for (final WrapperResource resource : wrapperResources
        ) {
            if (sanitisedResourcePath.equalsIgnoreCase(resource.getPath())) {
                LOGGER.log(Level.INFO, String.format("Hard Match found for resource path '%s'", sanitisedResourcePath));
                findResource = of(resource);
                break;
            }
        }
        return findResource;
    }


    private static Optional<WrapperMethod> findMethod(final String methodType, final String resourcePath, final List<WrapperResource> wrapperResources) {
        Optional<WrapperMethod> findMethod = Optional.empty();
        final Optional<WrapperResource> wrapperResourceOptional = findResource(resourcePath, wrapperResources);

        if (wrapperResourceOptional.isPresent()) {
            for (final WrapperMethod method : wrapperResourceOptional.get().getWrapperMethods()
            ) {
                if (StringUtils.isBlank(methodType) && methodType.equalsIgnoreCase(method.getType())) {
                    LOGGER.log(Level.INFO, String.format("Hard Match found for method type '%s' at path '%s", methodType, resourcePath));
                    findMethod = of(method);
                    break;
                }
            }
        }
        if (!findMethod.isPresent()) {
            LOGGER.log(Level.INFO, String.format("Failed to find Rule for Method type '%s' at path '%s", methodType, resourcePath));

        }
        return findMethod;
    }

    private static Optional<WrapperResource> findResource(final String resourcePath, final List<WrapperResource> wrapperResources) {
        Optional<WrapperResource> findResource = Optional.empty();

        if (!StringUtils.isBlank(resourcePath)) {
            final String sanitisedResourcePath = stripTrailingForwardSlash(resourcePath);
            findResource = findHardMatch(wrapperResources, findResource, sanitisedResourcePath);
            findResource = findCustomMatch(resourcePath, wrapperResources, findResource, sanitisedResourcePath);
        }

        if (!findResource.isPresent()) {
            LOGGER.log(Level.INFO, String.format("Failed to find Rule for for path '%s", resourcePath));

        }
        return findResource;
    }

    private static Matcher<?> formRule(final Rule rule, final String combinator, final Map<String, RamlAttribute> requestJsonPath, final RuleValidator ruleValidator) {

        //This is Rule
        final String field = rule.getField();
        final String value = (null != rule.getValue() ? rule.getValue() : "");

        final String operator = rule.getOperator();

        final RamlAttribute attribute = requestJsonPath.get(field);

        Matcher<?> returnMatch = null;

        final boolean isNumeric = (null != attribute) && StringUtils.containsIgnoreCase(attribute.getType(), "number");
        switch (operator) {
            case "null":
                returnMatch = is(nullValue());
                break;
            case "notNull":
                returnMatch = not(nullValue());
                break;
            case "containsString":
                returnMatch = containsString(value);
                break;
            case "starts":
                returnMatch = startsWith(value);
                break;
            case "ends":
                returnMatch = endsWith(value);
                break;
            case "eqic":
                returnMatch = equalToIgnoringCase(value);
                break;
            case "=": {
                if (isNumeric) {
                    returnMatch = is(parseInt(value));
                } else {
                    returnMatch = is(value);
                }
            }
            break;
            case "!=": {
                if (isNumeric) {
                    returnMatch = is(not(parseInt(value)));
                } else {
                    returnMatch = is(not(value));
                }
            }
            break;
            case "in": {
                if (isNumeric) {

                    returnMatch = isIn(asList(value.split(",")).stream().map(s -> parseInt(s)).collect(toList()));
                } else {
                    returnMatch = isIn(asList(value.split(",")));
                }
            }
            break;
            case "noiIn": {
                if (isNumeric) {

                    returnMatch = is(not(isIn(asList(value.split(",")).stream().map(s -> parseInt(s)).collect(toList()))));
                } else {
                    returnMatch = is(not(isIn(asList(value.split(",")))));
                }
            }
            break;
            case ">": {
                if (isNumeric) {
                    returnMatch = greaterThan(parseInt(value));
                } else {
                    returnMatch = greaterThan(value);
                }
            }
            break;
            case ">=": {
                if (isNumeric) {
                    returnMatch = greaterThanOrEqualTo(parseInt(value));
                } else {
                    returnMatch = greaterThanOrEqualTo(value);
                }
            }
            break;
            case "<": {
                if (isNumeric) {
                    returnMatch = lessThan(parseInt(value));
                } else {
                    returnMatch = lessThan(value);
                }
            }
            break;

            case "<=": {
                if (isNumeric) {
                    returnMatch = lessThanOrEqualTo(parseInt(value));
                } else {
                    returnMatch = lessThanOrEqualTo(value);
                }
            }
            break;

        }
        return returnMatch;
    }

    public static void returnErrorResponse(final HttpServletResponse resp, final Exception e) throws IOException {
        resp.setStatus(400);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(mapper.writeValueAsString(e.getMessage()));
    }

    private static String stripTrailingForwardSlash(final String resourcePath) {
        return resourcePath.replaceAll("/$", "");
    }

    private static RuleValidator validateGroup(final Rule group, final String combinator, final Boolean not, final RuleValidator ruleValidator, final Map<String, RamlAttribute> requestJsonPath) {
        final List<RuleValidator> validatorList = new ArrayList<>();

        final RuleValidator ruleValidatorGrp = ruleValidator.fresh();
        RuleValidator ruleValidatorRule = ruleValidatorGrp;

        boolean isFirstRule = true;

        for (final Rule rule : group.getRules()
        ) {

            if (!isEmpty(rule.getRules())) {
                //This is group
                validatorList.add(validateGroup(rule, rule.getCombinator(), rule.getNot(), ruleValidatorGrp, requestJsonPath));
            } else {
                //This is Rule
                if (isFirstRule) {
                    ruleValidatorRule = ruleValidatorGrp.fresh();
                }
                isFirstRule = false;

                //This is Rule
                final String field = rule.getField();

                if ("and".equalsIgnoreCase(combinator)) {
                    ruleValidatorRule.and(field, formRule(rule, combinator, requestJsonPath, ruleValidatorRule));
                } else {
                    ruleValidatorRule.or(field, formRule(rule, combinator, requestJsonPath, ruleValidatorRule));
                }
            }
        }
        validatorList.add(ruleValidatorRule);
        RuleValidator[] ruleValidatorArray = new RuleValidator[validatorList.size()];
        ruleValidatorArray = validatorList.toArray(ruleValidatorArray);

        if ("and".equalsIgnoreCase(combinator)) {
            ruleValidatorGrp.and(ruleValidatorArray);
        } else {
            ruleValidatorGrp.or(ruleValidatorArray);
        }

        return ruleValidatorGrp.negate(not);

    }

    private static ResponseEntity<Object> validateRules(final WrapperMethod wrapperMethod, final List<RuleSet> ruleSets, final HttpStatus responseCode, final String defaultResp, final Object requestBody) throws IOException {
        ResponseEntity<Object> responseEntity = null;

        ruleSets.sort(
                (RuleSet o1, RuleSet o2) -> {
                    if (null == o2.getOrder() && null == o1.getOrder()) {
                        return 0;
                    }
                    if (null == o1.getOrder()) {
                        return 1;
                    }
                    if (null == o2.getOrder()) {
                        return -1;
                    }
                    return o1.getOrder().compareTo(o2.getOrder());
                }
        );

        final RuleValidator ruleValidator = RuleValidator.with(mapper.writeValueAsString(requestBody));

        boolean isRules = false;

        for (final RuleSet ruleSet : ruleSets
        ) {
            //This is Action
            final HttpStatus httpCode = HttpStatus.valueOf(NumberUtils.toInt(ruleSet.getCode(), 500));

            final String message = ruleSet.getMessage();

            final Rule group = ruleSet.getRuleQuery();
            final String combinator = group.getCombinator();
            final Boolean not = group.getNot();
            if (validateGroup(group, combinator, not, ruleValidator, wrapperMethod.getRequestJsonPath()).found()) {
                responseEntity = new ResponseEntity<>(message, httpCode);
                isRules = true;
                break;
            }
        }
        if (!isRules) {
            final Object payload = mapper.readValue(defaultResp, Object.class);
            responseEntity = new ResponseEntity<>(payload, responseCode);
        }
        return responseEntity;
    }

    private ResponseEntity<Object> stubProcess(String stubId, Object requestBody, final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        ResponseEntity<Object> responseEntity = null;

        try {
            final String stubResourcePath = extractResourcePath(request);
            final String methodType = request.getMethod();
            final Map<String, String[]> queryParam = request.getParameterMap();
            if (null != requestBody) {
                requestBody = request.getReader().lines().collect(joining());
            }
            StubCacheEntity stubCacheEntity = new CacheServiceImpl().getStubById(stubId);

            this.doRamlValidation(requestBody, stubResourcePath, methodType, queryParam, stubCacheEntity.getStubValidationService());
            responseEntity = doRuleValidation(requestBody, stubResourcePath, methodType, stubCacheEntity.getWrapperApi());
        } catch (final Exception e) {
            returnErrorResponse(response, e);
        }
        return responseEntity;
    }

    private void doRamlValidation(Object requestBody, String stubResourcePath, String methodType, Map<String, String[]> queryParam, StubValidationService stubValidationService) throws JsonProcessingException {
        List<ValidationResult> validationResults = stubValidationService.validateRequest(methodType, stubResourcePath, queryParam, mapper.writeValueAsString(requestBody));
        if (!isEmpty(validationResults)) {
            throw new IllegalArgumentException(validationResults.toString());
        }
    }
}
