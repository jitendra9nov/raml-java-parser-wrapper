package com.bhadouriya.raml.efficacies;

import com.bhadouriya.raml.artifacts.*;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.isEmpty;

public class JsonGenerator {
    private static final String fs = File.separator;
    private static final Logger LOGGER = Logger.getLogger(JsonGenerator.class.getName());
    private static final Map<String, Class<?>> NAME_TYPE_MAP = new HashMap<>();
    private static final Map<String, String> PREDEFINED_CHARACTER_CLASSES;
    private static final Random random = new Random(1234567890);
    private static final int count = 1;
    private static final String defaultPattern = "[a-zA-Z0-9-. /'&90@#=\"_]";
    private static final String PATH = "..\\" + fs + "resources";
    private static final int maxLength = 18;
    private static int minLength;
    private static boolean isMaxList;
    private static boolean isRandom;

    static {
        NAME_TYPE_MAP.put("boolean", Boolean.class);
        NAME_TYPE_MAP.put("char", Character.class);
        NAME_TYPE_MAP.put("byte", Byte.class);
        NAME_TYPE_MAP.put("short", Short.class);
        NAME_TYPE_MAP.put("int", Integer.class);
        NAME_TYPE_MAP.put("integer", Integer.class);
        NAME_TYPE_MAP.put("number", BigDecimal.class);
        NAME_TYPE_MAP.put("long", Long.class);
        NAME_TYPE_MAP.put("float", Float.class);
        NAME_TYPE_MAP.put("double", Double.class);

        NAME_TYPE_MAP.put("Boolean", Boolean.class);
        NAME_TYPE_MAP.put("Character", Character.class);
        NAME_TYPE_MAP.put("Byte", Byte.class);
        NAME_TYPE_MAP.put("Short", Short.class);
        NAME_TYPE_MAP.put("Integer", Integer.class);
        NAME_TYPE_MAP.put("Long", Long.class);
        NAME_TYPE_MAP.put("Float", Float.class);
        NAME_TYPE_MAP.put("Double", Double.class);

        NAME_TYPE_MAP.put("date-only", Date.class);//yyyy-mm-dd e.g. 2015-05-23
        NAME_TYPE_MAP.put("time-only", Date.class);//hh:,,:ss[.ff...] e.g. 12:30:00
        NAME_TYPE_MAP.put("datetime-only", Date.class);//yyyy-mm-ddThh:mm:ss[.ff...] e.g. 2015-05-23T21:00:00
        NAME_TYPE_MAP.put("datetime", Date.class);//015-05-23T21:00:090Z for rfc3339 and Sun, 28 feb 2016 16:41:41 //GMT for rfc2616

        NAME_TYPE_MAP.put("object", Object.class);
    }

    static {
        Map<String, String> characterClasse = new HashMap<>();
        characterClasse.put("\\\\d", "0-9");
        characterClasse.put("\\\\D", "^0-9");
        characterClasse.put("\\\\s", " \t\n\f\r");
        characterClasse.put("\\\\S", "^ \t\n\f\r");
        characterClasse.put("\\\\w", "a-zA-Z_0-9");
        characterClasse.put("\\\\W", "a-zA-Z_0-9");
        characterClasse.put(".*[^ ].*", defaultPattern);
        PREDEFINED_CHARACTER_CLASSES = Collections.unmodifiableMap(characterClasse);
    }

    private static void addHeaders(WrapperMethod wm) {
        if (!isEmpty(wm.getWrapperHeaders())) {
            getSeperationAttribute(wm, "---REQUEST HEADERS---");
            wm.getWrapperHeaders().forEach(header -> {
                Property prop = header.getProperty();
                String type = prop.getType();
                String jsonPathLocal = prop.getName();

                Class<?> fieldType = convertToJavaClass(type, prop.getFormat());

                RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, type, prop.isRequired());
                ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
                ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(),
                        ramlAttribute.getInput().getChildType(), prop.isRequired()));
                wm.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
            });
        }
    }

    private static void addHSecurityHeaders(final WrapperApi wrapperApi, WrapperMethod wm) {
        if (!isEmpty(wrapperApi.getWrapperSecureBy())) {
            getSeperationAttribute(wm, "---REQUEST HEADERS---");
            wrapperApi.getWrapperSecureBy().forEach(secure -> {
                final List<WrapperType> headers = secure.getWrapperHeaders();

                if (!isEmpty(headers)) {
                    headers.forEach(header -> {
                        Property prop = header.getProperty();
                        String type = prop.getType();
                        String jsonPathLocal = prop.getName();

                        Class<?> fieldType = convertToJavaClass(type, prop.getFormat());

                        RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, type, prop.isRequired());
                        ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
                        ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(),
                                ramlAttribute.getInput().getChildType(), prop.isRequired()));
                        wm.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
                    });
                }

            });
        }
    }

    private static void addQuryParams(WrapperMethod wm) {
        if (!isEmpty(wm.getWrapperQueryParams())) {
            getSeperationAttribute(wm, "---QUERY PARAMETERS---");
            wm.getWrapperQueryParams().forEach(params -> {
                Property prop = params.getProperty();
                String type = prop.getType();
                String jsonPathLocal = prop.getName();

                Class<?> fieldType = convertToJavaClass(type, prop.getFormat());

                RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, type, prop.isRequired());
                ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
                ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(),
                        ramlAttribute.getInput().getChildType(), prop.isRequired()));
                wm.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
            });
        }
    }

    private static void addUrlParams(final WrapperResource wres, WrapperMethod wm) {
        if (!isEmpty(wres.getWrapperUriParams())) {
            getSeperationAttribute(wm, "---QUERY PARAMETERS---");
            wres.getWrapperUriParams().forEach(params -> {
                Property prop = params.getProperty();
                String type = prop.getType();
                String jsonPathLocal = prop.getName();

                Class<?> fieldType = convertToJavaClass(type, prop.getFormat());

                RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, type, prop.isRequired());
                ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
                ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(),
                        ramlAttribute.getInput().getChildType(), prop.isRequired()));
                wm.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
            });
        }
    }

    private static void addRequestBody(WrapperApi wapi, final WrapperMethod wm, final boolean isPrint) {
        if (null != wm.getWrapperRequest() && null != wm.getWrapperRequest().getBeanObject()) {
            getSeperationAttribute(wm, "---REQUEST ATTRIBUTES---");
            try {
                for (int i = 0; i < count; i++) {
                    int ind = i;
                    BeanObject parent = wm.getWrapperRequest().getBeanObject();
                    final String examples = !isEmpty(wm.getWrapperRequest().getExamples()) ? wm.getWrapperRequest().getExamples().get(0) : null;
                    final String example = !isEmpty(wm.getWrapperRequest().getExample()) ? wm.getWrapperRequest().getExample() : null;
                    String requestSample = forAllFields(wapi, parent, wm, ind, true, example, isPrint);
                    wm.setRequestSample(requestSample);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Exception while generating request", e);
            }
        }
    }

    private static void buildJsonResponse(WrapperApi wapi, final WrapperMethod wm, final WrapperResponse wrsp, final int httpStatus, final boolean isPrint) {
        if (HttpStatus.valueOf(httpStatus).is2xxSuccessful()) {
            for (int i = 0; i < count; i++) {
                int ind = i;
                final String examples = !isEmpty(wrsp.getExamples()) ? wrsp.getExamples().get(0) : null;
                final String example = !isEmpty(wrsp.getExample()) ? wrsp.getExample() : null;
                String responseSample = forAllFields(wapi, wrsp.getBeanObject(), wm, ind, false, example, isPrint);
                wm.getResponseSamples().put(httpStatus, responseSample);
            }

        }
    }

    private static void buildRequestJson(WrapperApi wapi, final WrapperResource wres, final boolean isPrint) {
        wres.getWrapperMethods().forEach(wm -> {
            addHSecurityHeaders(wapi, wm);
            addHeaders(wm);
            addUrlParams(wres, wm);
            addQuryParams(wm);
            addRequestBody(wapi, wm, isPrint);
        });
    }

    private static void buildResponseJson(WrapperApi wapi, final WrapperResource wres, final boolean isPrint) {
        wres.getWrapperMethods().forEach(wm -> {
            try {
                wm.getWrapperResponses().forEach(rspns -> {
                    int httpStatus = NumberUtils.toInt(rspns.getCode(), 500);
                    buildJsonResponse(wapi, wm, rspns, httpStatus, isPrint);
                    wm.getResponseCodes().put(httpStatus, rspns.getCode() + "-" + HttpStatus.valueOf(httpStatus).getReasonPhrase());
                });
            } catch (final Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String forAllFields(final WrapperApi wapi, final BeanObject parent, final WrapperMethod wm, final int ind, final boolean b, final String example, final boolean isPrint) {
        return null;
    }

    private static List<Config> getOperators(final String dataType, final String childType, final boolean required) {
        return null;
    }

    private static Input getInput(final Property prop, final Class<?> fieldType, final String name) {
        return null;
    }

    private static Class<?> convertToJavaClass(final String type, final String format) {
        return null;
    }

    private static void getSeperationAttribute(final WrapperMethod wm, final String s) {
    }
}
