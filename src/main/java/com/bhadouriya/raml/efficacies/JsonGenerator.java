package com.bhadouriya.raml.efficacies;

import com.bhadouriya.raml.artifacts.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mifmif.common.regex.Generex;
import org.apache.commons.lang.math.NumberUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.http.HttpStatus;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.apache.commons.lang.StringUtils.*;
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
    private static final int maxLength = 18;
    private static String PATH = "..\\" + fs + "resources";
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
        final Map<String, String> characterClasse = new HashMap<>();
        characterClasse.put("\\\\d", "0-9");
        characterClasse.put("\\\\D", "^0-9");
        characterClasse.put("\\\\s", " \t\n\f\r");
        characterClasse.put("\\\\S", "^ \t\n\f\r");
        characterClasse.put("\\\\w", "a-zA-Z_0-9");
        characterClasse.put("\\\\W", "a-zA-Z_0-9");
        characterClasse.put(".*[^ ].*", defaultPattern);
        PREDEFINED_CHARACTER_CLASSES = Collections.unmodifiableMap(characterClasse);
    }

    private static void addHeaders(final WrapperMethod wm) {
        if (!isEmpty(wm.getWrapperHeaders())) {
            getSeperationAttribute(wm, "---REQUEST HEADERS---");
            wm.getWrapperHeaders().forEach(header -> {
                final Property prop = header.getProperty();
                final String type = prop.getType();
                final String jsonPathLocal = prop.getName();

                final Class<?> fieldType = convertToJavaClass(type, prop.getFormat());

                final RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, type, prop.isRequired());
                ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
                ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(),
                        ramlAttribute.getInput().getChildType(), prop.isRequired()));
                wm.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
            });
        }
    }

    private static void addHSecurityHeaders(WrapperApi wrapperApi, final WrapperMethod wm) {
        if (!isEmpty(wrapperApi.getWrapperSecureBy())) {
            getSeperationAttribute(wm, "---REQUEST HEADERS---");
            wrapperApi.getWrapperSecureBy().forEach(secure -> {
                List<WrapperType> headers = secure.getWrapperHeaders();

                if (!isEmpty(headers)) {
                    headers.forEach(header -> {
                        final Property prop = header.getProperty();
                        final String type = prop.getType();
                        final String jsonPathLocal = prop.getName();

                        final Class<?> fieldType = convertToJavaClass(type, prop.getFormat());

                        final RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, type, prop.isRequired());
                        ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
                        ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(),
                                ramlAttribute.getInput().getChildType(), prop.isRequired()));
                        wm.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
                    });
                }

            });
        }
    }

    private static void addQuryParams(final WrapperMethod wm) {
        if (!isEmpty(wm.getWrapperQueryParams())) {
            getSeperationAttribute(wm, "---QUERY PARAMETERS---");
            wm.getWrapperQueryParams().forEach(params -> {
                final Property prop = params.getProperty();
                final String type = prop.getType();
                final String jsonPathLocal = prop.getName();

                final Class<?> fieldType = convertToJavaClass(type, prop.getFormat());

                final RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, type, prop.isRequired());
                ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
                ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(),
                        ramlAttribute.getInput().getChildType(), prop.isRequired()));
                wm.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
            });
        }
    }

    private static void addUrlParams(WrapperResource wres, final WrapperMethod wm) {
        if (!isEmpty(wres.getWrapperUriParams())) {
            getSeperationAttribute(wm, "---QUERY PARAMETERS---");
            wres.getWrapperUriParams().forEach(params -> {
                final Property prop = params.getProperty();
                final String type = prop.getType();
                final String jsonPathLocal = prop.getName();

                final Class<?> fieldType = convertToJavaClass(type, prop.getFormat());

                final RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, type, prop.isRequired());
                ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
                ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(),
                        ramlAttribute.getInput().getChildType(), prop.isRequired()));
                wm.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
            });
        }
    }

    private static void addRequestBody(final WrapperApi wapi, WrapperMethod wm, boolean isPrint) {
        if (null != wm.getWrapperRequest() && null != wm.getWrapperRequest().getBeanObject()) {
            getSeperationAttribute(wm, "---REQUEST ATTRIBUTES---");
            try {
                for (int i = 0; i < count; i++) {
                    final int ind = i;
                    final BeanObject parent = wm.getWrapperRequest().getBeanObject();
                    String examples = !isEmpty(wm.getWrapperRequest().getExamples()) ? wm.getWrapperRequest().getExamples().get(0) : null;
                    String example = !isEmpty(wm.getWrapperRequest().getExample()) ? wm.getWrapperRequest().getExample() : null;
                    final String requestSample = forAllFields(wapi, parent, wm, ind, true, example, isPrint);
                    wm.setRequestSample(requestSample);
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Exception while generating request", e);
            }
        }
    }

    private static void buildJsonResponse(final WrapperApi wapi, WrapperMethod wm, WrapperResponse wrsp, int httpStatus, boolean isPrint) {
        if (HttpStatus.valueOf(httpStatus).is2xxSuccessful()) {
            for (int i = 0; i < count; i++) {
                final int ind = i;
                String examples = !isEmpty(wrsp.getExamples()) ? wrsp.getExamples().get(0) : null;
                String example = !isEmpty(wrsp.getExample()) ? wrsp.getExample() : null;
                final String responseSample = forAllFields(wapi, wrsp.getBeanObject(), wm, ind, false, example, isPrint);
                wm.getResponseSamples().put(httpStatus, responseSample);
            }

        }
    }

    private static void buildRequestJson(final WrapperApi wapi, WrapperResource wres, boolean isPrint) {
        wres.getWrapperMethods().forEach(wm -> {
            addHSecurityHeaders(wapi, wm);
            addHeaders(wm);
            addUrlParams(wres, wm);
            addQuryParams(wm);
            addRequestBody(wapi, wm, isPrint);
        });
    }

    private static void buildResponseJson(final WrapperApi wapi, WrapperResource wres, boolean isPrint) {
        wres.getWrapperMethods().forEach(wm -> {
            try {
                wm.getWrapperResponses().forEach(rspns -> {
                    final int httpStatus = NumberUtils.toInt(rspns.getCode(), 500);
                    buildJsonResponse(wapi, wm, rspns, httpStatus, isPrint);
                    wm.getResponseCodes().put(httpStatus, rspns.getCode() + "-" + HttpStatus.valueOf(httpStatus).getReasonPhrase());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static String byteValue(final Property prop) {
        final String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            byte min = null != prop.getMin() ? prop.getMin().byteValue() : Byte.MIN_VALUE;
            byte max = null != prop.getMax() ? prop.getMax().byteValue() : Byte.MAX_VALUE;
            double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            int range = (int) ((max - min) / multipleOf);

            final byte values = (byte) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static Class<?> convertToJavaClass(String type, String format) {
        String arrayName = type;
        if (isEmpty(arrayName)) {
            return Object.class;
        }
        if (!isEmpty(format) && ("number").equals(arrayName) || "integer".equals(arrayName)) {
            switch (format.toLowerCase()) {
                case "int8":
                    arrayName = "byte";
                    break;
                case "int16":
                    arrayName = "short";
                    break;
                case "int32":
                    arrayName = "int";
                    break;
                case "int64":
                    arrayName = "long";
                    break;
                default:
                    arrayName = format.toLowerCase();
                    break;
            }
        }
        while (arrayName.endsWith("[]")) {
            arrayName = arrayName.substring(0, arrayName.length() - 2);
        }
        //check for primitive type
        Class<?> clazz = NAME_TYPE_MAP.get(arrayName);

        if (clazz == null) {
            //No primitive, try to load it from given classloader
            try {
                clazz = Class.forName("java.lang." + capitalize(arrayName));
            } catch (final ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error While Generating Json", e);
            }
        }
        if (clazz == null) {
            //No primitive, try to load it from given classloader
            try {
                clazz = Class.forName("java.util." + capitalize(arrayName));
            } catch (final ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error While Generating Json", e);
            }
        }
        if (clazz == null) {
            clazz = Object.class;
        }
        return clazz;
    }

    private static void createJon(final WrapperApi wrapperApi, final BeanObject parent, final JSONObject jsonObject, final boolean isMandatory, final String jsonPathRoot, final WrapperMethod wrapperMethod) {
        final List<Property> properties = parent.getProperties();

        for (final Property prop : properties
        ) {
            if (isMandatory && !prop.isRequired()) {
                continue;
            }
            final String type = prop.getType();
            final String jsonPathLocal = jsonPathRoot;
            final String propName = prop.getName();
            switch (type) {
                case "array":
                    handleArray(wrapperApi, jsonObject, isMandatory, wrapperMethod, prop, jsonPathLocal, propName);
                    break;
                case "object":
                    handleObject(wrapperApi, jsonObject, isMandatory, wrapperMethod, prop, jsonPathLocal, propName);
                    break;
                default:
                    handleType(jsonObject, wrapperMethod, prop, jsonPathLocal, propName);
                    break;
            }

        }
    }

    private static void createJsonSchemas(final WrapperApi wrapperApi, final boolean isPrint) {
        wrapperApi.getWrapperResources().forEach(res -> {
            buildRequestJson(wrapperApi, res, isPrint);
            buildResponseJson(wrapperApi, res, isPrint);

            res.getChildWrapperResources().forEach(child -> {
                buildRequestJson(wrapperApi, child, isPrint);
                buildResponseJson(wrapperApi, child, isPrint);
            });
        });
        if (isPrint) {
            LOGGER.log(Level.INFO, "\n\n\tWe have generated files at <" + PATH + "> path.");
        }
    }

    private static void createJsonSchemas(final WrapperApi wrapperApi, final boolean isRandomArg, final boolean isMaxListArg, final String resources, final boolean isPrint) {

        isMaxList = isMaxListArg;
        isRandom = isRandomArg;
        PATH = resources;

        try {
            createJsonSchemas(wrapperApi, isPrint);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while generating Json", e);
        }
    }

    private static String dateValue(final Property prop) {
        String value;
        String format = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'";
        switch (prop.getType()) {
            case "date-only":
                format = "yyyy-MM-dd";
                break;
            case "time-only":
                format = "hh:mm:ss";
                break;
            case "datetime-only":
                format = "yyyy-MM-dd'T'hh:mm:ss";
                break;
            case "datetime":
                format = "rfc2616".equalsIgnoreCase(prop.getFormat()) ? null : format;
                break;
            default:
                break;
        }
        return getCurrentDate(format);
    }

    private static String doubleValue(final Property prop) {
        final String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            double min = null != prop.getMin() ? prop.getMin().doubleValue() : Double.MIN_VALUE;
            double max = null != prop.getMax() ? prop.getMax().doubleValue() : Double.MAX_VALUE;
            double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            int range = (int) ((max - min) / multipleOf);

            final double values = (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static String floatValue(final Property prop) {
        final String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            float min = null != prop.getMin() ? prop.getMin().floatValue() : Float.MIN_VALUE;
            float max = null != prop.getMax() ? prop.getMax().floatValue() : Float.MAX_VALUE;
            double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            int range = (int) ((max - min) / multipleOf);

            final float values = (float) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static String forAllFields(WrapperApi wapi, BeanObject parent, WrapperMethod wm, int ind, boolean isJsonPath, String example, boolean isPrint) {
        String writeJsonSchema = null;
        if (null != parent) {
            final JSONObject jsonRootObj = new JSONObject();
            final String jsonRootPath = isJsonPath ? "$" : null;
            createJon(wapi, parent, jsonRootObj, false, jsonRootPath, wm);
            final String jsonString = isEmpty(example) ? jsonRootObj.toJSONString() : example;
            writeJsonSchema = writeJsonSchema(parent.getName() + (ind == 0 ? "" : ind), jsonString, PATH + "\\jsons", isPrint);
        }
        return writeJsonSchema;
    }

    private static String getCurrentDate(final String format) {
        final Calendar cal = Calendar.getInstance();
        if (null != format) {
            final SimpleDateFormat sd = new SimpleDateFormat(format);
            return sd.format(cal.getTime());
        }
        return cal.getTime().toString();
    }

    private static Input getInput(Property prop, Class<?> clazz, String name) {
        final String dataType = clazz.getSimpleName().toLowerCase();
        String type = "text";
        String format = prop.getFormat();
        String errMsg = null;
        String childType = null;
        final List<Config> options = new ArrayList<>();

        if (String.class == clazz) {
            processEnum(prop, options);
            errMsg = "Not acceptable pattern.";
        }
        if (Boolean.class == clazz) {
            type = "checkbox";//TODO
            processEnum(prop, options);
            errMsg = "Not acceptable pattern.";
        } else if ((Byte.class == clazz) || (Short.class == clazz) || (Integer.class == clazz)
                || (Double.class == clazz) || (BigDecimal.class == clazz) || (Float.class == clazz) || (Long.class == clazz)) {
            childType = "number";
            processEnum(prop, options);
            errMsg = "Not acceptable number pattern.";
        } else if (Date.class == clazz) {
            format = "yyyy-MM-dd'T'hh:mm:ss.SSS'Z'";
            switch (prop.getType()) {
                case "date-only":
                    format = "yyyy-MM-dd";
                    childType = "date";
                    break;
                case "time-only":
                    format = "hh:mm:ss";
                    childType = "time";
                    break;
                case "datetime-only":
                    format = "yyyy-MM-dd'T'hh:mm:ss";
                    childType = "datetime-local";
                    break;
                case "datetime":
                    format = "rfc2616".equalsIgnoreCase(prop.getFormat()) ? null : format;
                    childType = "datetime-local";
                    break;
                default:
                    break;
            }
            errMsg = "Not acceptable date pattern.";
        }
        final Input input = new Input(dataType, type, errMsg, format, childType);
        if (!options.isEmpty()) {
            input.setOptions(options);
        }
        return input;
    }

    private static void processEnum(final Property prop, final List<Config> options) {
        final String format;
        final String type;
        if (!isEmpty(prop.getEnumValues())) {
            final StringBuilder format2 = new StringBuilder("(");
            options.add(new Config(null, "--SELECT--"));
            prop.getEnumValues().forEach(enm -> {
                format2.append(enm + "|");
                options.add(new Config(String.valueOf(enm), String.valueOf(enm)));
            });
            final int lastIndexOf = format2.lastIndexOf("|");
            format = format2.replace(lastIndexOf, lastIndexOf + 1, "").append(")").toString();
            type = "select";
        }
    }

    private static List<Config> getOperators(String dataType, String childType, boolean isMandatory) {
        final List<Config> operators = new ArrayList<>();

        final boolean isString = "string".equalsIgnoreCase(dataType);
        final boolean isNumeric = "number".equalsIgnoreCase(childType);
        final boolean isDateTime = (null != childType && (childType.toLowerCase().contains("date") || childType.toLowerCase().contains("time")));

        if (!isMandatory) {
            operators.add(new Config("null", "Is Null"));
            operators.add(new Config("notNull", "Is Not Null"));
        }
        operators.add(new Config("notNull", "Is Not Null"));
        operators.add(new Config("notNull", "Is Not Null"));

        if (isNumeric || isDateTime) {
            operators.add(new Config(">", "Greater than"));
            operators.add(new Config(">=", "Greater than or Equal to"));
            operators.add(new Config("<", "Less than"));
            operators.add(new Config("<=", "Less than or Equal to"));
        }
        if (isString) {
            operators.add(new Config("empty", "Is Empty"));
            operators.add(new Config("contains", "Contains"));
            operators.add(new Config("notContains", "Does not Contains"));
            operators.add(new Config("starts", "Starts with"));
            operators.add(new Config("notStarts", "Does not starts with"));
            operators.add(new Config("ends", "Ends with"));
            operators.add(new Config("notEnds", "Does not Ends with"));
            operators.add(new Config("eqic", "Equals Ignore case"));
            operators.add(new Config("leq", "Length is not equal to"));
            operators.add(new Config("lneq", "Length is not equal to"));
            operators.add(new Config("gt", "Greater than"));
            operators.add(new Config("gte", "Greater than or Equal to"));
            operators.add(new Config("lt", "Less than"));
            operators.add(new Config("lte", "Less than or Equal to"));
        }
        if (isString || isNumeric) {
            operators.add(new Config("in", "Is In (Comma separated values)"));
            operators.add(new Config("notIn", "Is Not In (Comma separated values)"));
        }
        if (isNumeric) {
            operators.add(new Config("range", "Range (Hyphen sepedated)"));
        }
        return operators;
    }

    private static String getRandomValue(final Property prop, final Class<?> clazz, String value) {
        if (String.class == clazz) {
            value = stringValue(prop);
        } else if (Boolean.class == clazz) {
            value = String.valueOf(random.nextBoolean());
        } else if (Byte.class == clazz) {
            value = byteValue(prop);
        } else if (Short.class == clazz) {
            value = shortValue(prop);
        } else if (Integer.class == clazz) {
            value = intValue(prop);
        } else if (Double.class == clazz) {
            value = doubleValue(prop);
        } else if (Float.class == clazz) {
            value = floatValue(prop);
        } else if (Long.class == clazz) {
            value = longValue(prop);
        } else if (Date.class == clazz) {
            value = dateValue(prop);
        }
        return value;
    }

    private static void getSeperationAttribute(WrapperMethod wm, String name) {
        final RamlAttribute seperationAttr = new RamlAttribute(name, name, null, false);
        seperationAttr.setInput(new Input());
        final List<Config> operators = new ArrayList<>();
        operators.add(new Config("null", null));
        seperationAttr.setOperators(operators);
        wm.getRequestJsonPath().put(name, seperationAttr);
    }

    private static String getStringRegEx(final Property prop) {
        String value;
        String pattern = prop.getFormat();

        pattern = null != pattern ? removeEnd(removeStart(pattern, "^"), "$") : null;

        final boolean isWhiteSpaceStart = startsWith(pattern, "(?!\\s)");
        final boolean isWhiteSpaceEnd = endsWith(pattern, "(?<!\\s)");

        pattern = null != pattern ? removeEnd(removeStart(pattern, "(?!\\s)"), "(?<!\\s)") : null;
        if (null == pattern) {
            final long minLenFinal = null != prop.getMin() ? prop.getMin().longValue() : minLength;
            final long maxLenFinal = null != prop.getMax() ? prop.getMax().longValue() : maxLength;
            pattern = "[a-zA-Z0-9-. /'&()@#=\\\"_]{" + minLenFinal + "," + maxLenFinal + "}";
        }
        for (final Map.Entry<String, String> charClass : PREDEFINED_CHARACTER_CLASSES.entrySet()
        ) {
            if (startsWith(pattern, charClass.getKey().replace("\\\\", "\\"))) {
                pattern = pattern.replaceFirst(charClass.getKey(), "[" + charClass.getValue() + "]");
            }
            pattern = pattern.replaceAll(charClass.getKey(), charClass.getValue());
        }

        final Generex generex = new Generex(pattern);
        value = generex.random(minLength, maxLength);

        if (isWhiteSpaceStart) {
            value = value.replaceAll("^\\s+", "");
        }
        if (isWhiteSpaceEnd) {
            value = value.replaceAll("^\\s+$", "");
        }
        return value;
    }

    private static Object getValue(final Property property, final Class<?> clazz) {
        String value = clazz.getName();
        if (isRandom) {
            value = getRandomValue(property, clazz, value);
        } else {
            if (isNotEmpty(property.getDefaultValue())) {
                value = property.getDefaultValue();
            } else if (isNotEmpty(property.getValue())) {
                value = property.getValue();
            } else if (isNotEmpty(property.getExample())) {
                value = property.getExample();
            } else if (!isEmpty(property.getExamples())) {
                int index = property.getExamples().size() - 1;
                index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
                value = property.getExamples().get(index);
            } else if (isEmpty(property.getEnumValues())) {
                int index = property.getEnumValues().size() - 1;
                index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
                value = property.getEnumValues().get(index).toString();
            } else {
                value = getRandomValue(property, clazz, value);
            }
        }
        final Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(value);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.warning("Error while generating value");
        }
        return value;
    }

    private static void handleArray(final WrapperApi wrapperApi, final JSONObject jsonObject, final boolean isMandatory, final WrapperMethod wrapperMethod, final Property prop, String jsonPathLocal, final String propName) {
        Class<?> fieldType;
        if (null != jsonPathLocal) {
            jsonPathLocal += "." + propName + "[*]";
        }
        final BeanObject childArray = prop.getBeanObject();
        final long locMin = prop.getMin().longValue() != 0 ? prop.getMin().longValue() : 1;
        final long min = null != prop.getMin() ? locMin : 1;
        final long locMax = isMaxList ? prop.getMax().longValue() : min;
        final long max = null != prop.getMax() ? locMin : min;
        final JSONArray list = new JSONArray();

        for (long i = 0; i < max; i++) {
            if (null != childArray) {
                final JSONObject jsonChildObj = new JSONObject();
                createJon(wrapperApi, childArray, jsonChildObj, isMandatory, jsonPathLocal, wrapperMethod);
                list.add(jsonChildObj);
            } else {
                final Property items = prop.getItems();
                fieldType = convertToJavaClass(items.getType(), items.getFormat());
                list.add(getValue(items, fieldType));

                if (null != jsonPathLocal) {
                    final RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, prop.getType(), prop.isRequired());
                    ramlAttribute.setInput(getInput(items, fieldType, fieldType.getName()));
                    ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(), ramlAttribute.getInput().getChildType(), prop.isRequired()));
                    wrapperMethod.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
                }
            }
        }

    }

    private static String handleObject(final WrapperApi wrapperApi, final JSONObject jsonObject, final boolean isMandatory, final WrapperMethod wrapperMethod, final Property prop, String jsonPathLocal, final String propName) {
        if (null != jsonPathLocal) {
            jsonPathLocal += "." + propName;
        }
        final BeanObject childObj = prop.getBeanObject();
        final JSONObject jsonChildObj = new JSONObject();
        createJon(wrapperApi, childObj, jsonChildObj, isMandatory, jsonPathLocal, wrapperMethod);
        jsonObject.put(propName, jsonChildObj);
        return jsonPathLocal;

    }

    private static void handleType(final JSONObject jsonObject, final WrapperMethod wrapperMethod, final Property prop, String jsonPathLocal, final String propName) {
        final Class<?> fieldType;
        if (null != jsonPathLocal) {
            jsonPathLocal += "." + propName;
        }
        fieldType = convertToJavaClass(prop.getType(), prop.getFormat());
        jsonObject.put(propName, getValue(prop, fieldType));

        if (null != jsonPathLocal) {
            final RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, prop.getType(), prop.isRequired());
            ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
            ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(), ramlAttribute.getInput().getChildType(), prop.isRequired()));
            wrapperMethod.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
        }

    }

    private static String intValue(final Property prop) {
        final String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            int min = null != prop.getMin() ? prop.getMin().intValue() : Integer.MIN_VALUE;
            int max = null != prop.getMax() ? prop.getMax().intValue() : Integer.MAX_VALUE;
            double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            int range = (int) ((max - min) / multipleOf);

            final int values = (int) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static String longValue(final Property prop) {
        final String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            long min = null != prop.getMin() ? prop.getMin().longValue() : Long.MIN_VALUE;
            long max = null != prop.getMax() ? prop.getMax().longValue() : Long.MAX_VALUE;
            double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            int range = (int) ((max - min) / multipleOf);

            final long values = (long) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }


    private static String shortValue(final Property prop) {
        final String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            short min = null != prop.getMin() ? prop.getMin().shortValue() : Short.MIN_VALUE;
            short max = null != prop.getMax() ? prop.getMax().shortValue() : Short.MAX_VALUE;
            double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            int range = (int) ((max - min) / multipleOf);

            final short values = (short) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static String stringValue(final Property prop) {
        final String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {

            value = getStringRegEx(prop);
        }
        return value;
    }

    private static String writeJsonSchema(final String name, final String jsonString, final String path, final boolean isPrint) {
        String prettyJso = null;

        final ObjectMapper mapper = new ObjectMapper();
        final Object json;
        try {
            json = mapper.readValue(jsonString, Object.class);
            prettyJso = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Error while writing Json", e);
            throw new IllegalArgumentException("Unable to generate json schemas");
        }
        if (isPrint) {
            final File directory = new File(path);
            LOGGER.log(Level.INFO, "Generating Path>>" + path);
            if (!directory.exists()) {
                LOGGER.log(Level.INFO, "Generating Directories>>" + directory.mkdirs());
            } else {
                LOGGER.log(Level.INFO, "Existing Directories>>");
            }

            try (final FileWriter fileWriter = new FileWriter(directory + File.separator + Efficacy.schemaName(name) + ".json")) {

                fileWriter.write(prettyJso);
                fileWriter.flush();
            } catch (final Exception e) {
                LOGGER.log(Level.SEVERE, "Error while Generating Json scheme", e);
                throw new IllegalArgumentException("Unable to generate json schemas");
            }
        }
        return prettyJso;
    }

}
