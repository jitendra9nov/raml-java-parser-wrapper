package com.bhadouriya.raml.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.raml.v2.api.model.common.ValidationResult;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.hasText;

public class RequestValidationFilter extends OncePerRequestFilter {

    private final ValidationService validationService;

    public RequestValidationFilter(ValidationService validationService) {
        this.validationService = validationService;
    }

    public static String extractResourcePath(final HttpServletRequest request) {
        String resourcePath = request.getServletPath();
        if (!hasText(resourcePath)) {
            resourcePath = request.getRequestURI();
        }
        return resourcePath;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            final CustomHttpServletRequestWrapper requestWrapper = new CustomHttpServletRequestWrapper(request);
            doRequestValidation(requestWrapper);
            filterChain.doFilter(requestWrapper, response);
        } catch (RamlException e) {
            returnErrorResponse(response, e);
        }
    }

    private void returnErrorResponse(HttpServletResponse response, RamlException e) throws IOException {
        response.setStatus(e.getStatus().value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ErrorResponse errorResponse = new ErrorResponse(e.getMessages());
        response.getWriter().write(new ObjectMapper().writeValueAsString(e.getMessage()));
    }

    private void doRequestValidation(HttpServletRequest request) throws IOException {
        final String methodType = request.getMethod();
        final String resourcePath = extractResourcePath(request);
        final Map<String, String[]> queryParams = request.getParameterMap();
        String body = request.getReader().lines().collect(Collectors.joining());

        List<ValidationResult> validationResultList = validationService.valideRequest(methodType, resourcePath, queryParams, body);
        if (!CollectionUtils.isEmpty(validationResultList)) {
            throw RamlValidationException.create(validationResultList);
        }
    }
}
