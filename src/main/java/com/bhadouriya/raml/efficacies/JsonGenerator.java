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
    private static String PATH = "..\\" + fs + "resources";
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

    private static String byteValue(Property prop) {
        String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            final byte min = null != prop.getMin() ? prop.getMin().byteValue() : Byte.MIN_VALUE;
            final byte max = null != prop.getMax() ? prop.getMax().byteValue() : Byte.MAX_VALUE;
            final double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            final int range = (int) ((max - min) / multipleOf);

            byte values = (byte) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static Class<?> convertToJavaClass(final String type, final String format) {
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
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error While Generating Json", e);
            }
        }
        if (clazz == null) {
            //No primitive, try to load it from given classloader
            try {
                clazz = Class.forName("java.util." + capitalize(arrayName));
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE, "Error While Generating Json", e);
            }
        }
        if (clazz == null) {
            clazz = Object.class;
        }
        return clazz;
    }

    private static void createJon(WrapperApi wrapperApi, BeanObject parent, JSONObject jsonObject, boolean isMandatory, String jsonPathRoot, WrapperMethod wrapperMethod) {
        List<Property> properties = parent.getProperties();

        for (Property prop : properties
        ) {
            if (isMandatory && !prop.isRequired()) {
                continue;
            }
            String type = prop.getType();
            String jsonPathLocal = jsonPathRoot;
            String propName = prop.getName();
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

    private static void createJsonSchemas(WrapperApi wrapperApi, boolean isPrint) {
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

    private static void createJsonSchemas(WrapperApi wrapperApi, boolean isRandomArg, boolean isMaxListArg, String resources, boolean isPrint) {

        isMaxList = isMaxListArg;
        isRandom = isRandomArg;
        PATH = resources;

        try {
            createJsonSchemas(wrapperApi, isPrint);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while generating Json", e);
        }
    }

    private static String dateValue(Property prop) {
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

    private static String doubleValue(Property prop) {
        String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            final double min = null != prop.getMin() ? prop.getMin().doubleValue() : Double.MIN_VALUE;
            final double max = null != prop.getMax() ? prop.getMax().doubleValue() : Double.MAX_VALUE;
            final double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            final int range = (int) ((max - min) / multipleOf);

            double values = (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static String floatValue(Property prop) {
        String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            final float min = null != prop.getMin() ? prop.getMin().floatValue() : Float.MIN_VALUE;
            final float max = null != prop.getMax() ? prop.getMax().floatValue() : Float.MAX_VALUE;
            final double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            final int range = (int) ((max - min) / multipleOf);

            float values = (float) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static String forAllFields(final WrapperApi wapi, final BeanObject parent, final WrapperMethod wm, final int ind, final boolean isJsonPath, final String example, final boolean isPrint) {
        String writeJsonSchema = null;
        if (null != parent) {
            JSONObject jsonRootObj = new JSONObject();
            String jsonRootPath = isJsonPath ? "$" : null;
            createJon(wapi, parent, jsonRootObj, false, jsonRootPath, wm);
            String jsonString = isEmpty(example) ? jsonRootObj.toJSONString() : example;
            writeJsonSchema = writeJsonSchema(parent.getName() + (ind == 0 ? "" : ind), jsonString, PATH + "\\jsons", isPrint);
        }
        return writeJsonSchema;
    }

    private static String getCurrentDate(String format) {
        Calendar cal = Calendar.getInstance();
        if (null != format) {
            SimpleDateFormat sd = new SimpleDateFormat(format);
            return sd.format(cal.getTime());
        }
        return cal.getTime().toString();
    }

    private static Input getInput(final Property prop, final Class<?> clazz, final String name) {
        String dataType = clazz.getSimpleName().toLowerCase();
        String type = "text";
        String format = prop.getFormat();
        String errMsg = null;
        String childType = null;
        List<Config> options = new ArrayList<>();

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
        Input input = new Input(dataType, type, errMsg, format, childType);
        if (!options.isEmpty()) {
            input.setOptions(options);
        }
        return input;
    }

    private static void processEnum(Property prop, List<Config> options) {
        String format;
        String type;
        if (!isEmpty(prop.getEnumValues())) {
            StringBuilder format2 = new StringBuilder("(");
            options.add(new Config(null, "--SELECT--"));
            prop.getEnumValues().forEach(enm -> {
                format2.append(enm + "|");
                options.add(new Config(String.valueOf(enm), String.valueOf(enm)));
            });
            int lastIndexOf = format2.lastIndexOf("|");
            format = format2.replace(lastIndexOf, lastIndexOf + 1, "").append(")").toString();
            type = "select";
        }
    }

    private static List<Config> getOperators(final String dataType, final String childType, final boolean isMandatory) {
        List<Config> operators = new ArrayList<>();

        boolean isString = "string".equalsIgnoreCase(dataType);
        boolean isNumeric = "number".equalsIgnoreCase(childType);
        boolean isDateTime = (null != childType && (childType.toLowerCase().contains("date") || childType.toLowerCase().contains("time")));

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

    private static String getRandomValue(Property prop, Class<?> clazz, String value) {
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

    private static void getSeperationAttribute(final WrapperMethod wm, final String name) {
        RamlAttribute seperationAttr = new RamlAttribute(name, name, null, false);
        seperationAttr.setInput(new Input());
        List<Config> operators = new ArrayList<>();
        operators.add(new Config("null", null));
        seperationAttr.setOperators(operators);
        wm.getRequestJsonPath().put(name, seperationAttr);
    }

    private static String getStringRegEx(Property prop) {
        String value;
        String pattern = prop.getFormat();

        pattern = null != pattern ? removeEnd(removeStart(pattern, "^"), "$") : null;

        boolean isWhiteSpaceStart = startsWith(pattern, "(?!\\s)");
        boolean isWhiteSpaceEnd = endsWith(pattern, "(?<!\\s)");

        pattern = null != pattern ? removeEnd(removeStart(pattern, "(?!\\s)"), "(?<!\\s)") : null;
        if (null == pattern) {
            long minLenFinal = null != prop.getMin() ? prop.getMin().longValue() : minLength;
            long maxLenFinal = null != prop.getMax() ? prop.getMax().longValue() : maxLength;
            pattern = "[a-zA-Z0-9-. /'&()@#=\\\"_]{" + minLenFinal + "," + maxLenFinal + "}";
        }
        for (Map.Entry<String, String> charClass : PREDEFINED_CHARACTER_CLASSES.entrySet()
        ) {
            if (startsWith(pattern, charClass.getKey().replace("\\\\", "\\"))) {
                pattern = pattern.replaceFirst(charClass.getKey(), "[" + charClass.getValue() + "]");
            }
            pattern = pattern.replaceAll(charClass.getKey(), charClass.getValue());
        }

        Generex generex = new Generex(pattern);
        value = generex.random(minLength, maxLength);

        if (isWhiteSpaceStart) {
            value = value.replaceAll("^\\s+", "");
        }
        if (isWhiteSpaceEnd) {
            value = value.replaceAll("^\\s+$", "");
        }
        return value;
    }

    private static Object getValue(Property property, Class<?> clazz) {
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
        Constructor<?> constructor;
        try {
            constructor = clazz.getConstructor(String.class);
            return constructor.newInstance(value);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.warning("Error while generating value");
        }
        return value;
    }

    private static void handleArray(WrapperApi wrapperApi, JSONObject jsonObject, boolean isMandatory, WrapperMethod wrapperMethod, Property prop, String jsonPathLocal, String propName) {
        Class<?> fieldType;
        if (null != jsonPathLocal) {
            jsonPathLocal += "." + propName + "[*]";
        }
        BeanObject childArray = prop.getBeanObject();
        long locMin = prop.getMin().longValue() != 0 ? prop.getMin().longValue() : 1;
        long min = null != prop.getMin() ? locMin : 1;
        long locMax = isMaxList ? prop.getMax().longValue() : min;
        long max = null != prop.getMax() ? locMin : min;
        JSONArray list = new JSONArray();

        for (long i = 0; i < max; i++) {
            if (null != childArray) {
                JSONObject jsonChildObj = new JSONObject();
                createJon(wrapperApi, childArray, jsonChildObj, isMandatory, jsonPathLocal, wrapperMethod);
                list.add(jsonChildObj);
            } else {
                Property items = prop.getItems();
                fieldType = convertToJavaClass(items.getType(), items.getFormat());
                list.add(getValue(items, fieldType));

                if (null != jsonPathLocal) {
                    RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, prop.getType(), prop.isRequired());
                    ramlAttribute.setInput(getInput(items, fieldType, fieldType.getName()));
                    ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(), ramlAttribute.getInput().getChildType(), prop.isRequired()));
                    wrapperMethod.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
                }
            }
        }

    }

    private static String handleObject(WrapperApi wrapperApi, JSONObject jsonObject, boolean isMandatory, WrapperMethod wrapperMethod, Property prop, String jsonPathLocal, String propName) {
        if (null != jsonPathLocal) {
            jsonPathLocal += "." + propName;
        }
        BeanObject childObj = prop.getBeanObject();
        JSONObject jsonChildObj = new JSONObject();
        createJon(wrapperApi, childObj, jsonChildObj, isMandatory, jsonPathLocal, wrapperMethod);
        jsonObject.put(propName, jsonChildObj);
        return jsonPathLocal;

    }

    private static void handleType(JSONObject jsonObject, WrapperMethod wrapperMethod, Property prop, String jsonPathLocal, String propName) {
        Class<?> fieldType;
        if (null != jsonPathLocal) {
            jsonPathLocal += "." + propName;
        }
        fieldType = convertToJavaClass(prop.getType(), prop.getFormat());
        jsonObject.put(propName, getValue(prop, fieldType));

        if (null != jsonPathLocal) {
            RamlAttribute ramlAttribute = new RamlAttribute(jsonPathLocal, jsonPathLocal, prop.getType(), prop.isRequired());
            ramlAttribute.setInput(getInput(prop, fieldType, fieldType.getName()));
            ramlAttribute.setOperators(getOperators(ramlAttribute.getInput().getDataType(), ramlAttribute.getInput().getChildType(), prop.isRequired()));
            wrapperMethod.getRequestJsonPath().put(jsonPathLocal, ramlAttribute);
        }

    }

    private static String intValue(Property prop) {
        String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            final int min = null != prop.getMin() ? prop.getMin().intValue() : Integer.MIN_VALUE;
            final int max = null != prop.getMax() ? prop.getMax().intValue() : Integer.MAX_VALUE;
            final double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            final int range = (int) ((max - min) / multipleOf);

            int values = (int) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static String longValue(Property prop) {
        String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            final long min = null != prop.getMin() ? prop.getMin().longValue() : Long.MIN_VALUE;
            final long max = null != prop.getMax() ? prop.getMax().longValue() : Long.MAX_VALUE;
            final double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            final int range = (int) ((max - min) / multipleOf);

            long values = (long) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }


    private static String shortValue(Property prop) {
        String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {
            final short min = null != prop.getMin() ? prop.getMin().shortValue() : Short.MIN_VALUE;
            final short max = null != prop.getMax() ? prop.getMax().shortValue() : Short.MAX_VALUE;
            final double multipleOf = null != prop.getMultipleOf() ? prop.getMultipleOf().doubleValue() : 1;

            final int range = (int) ((max - min) / multipleOf);

            short values = (short) (min + (random.nextInt(range) * multipleOf));
            value = String.valueOf(values);
        }
        return value;
    }

    private static String stringValue(Property prop) {
        String value;
        if (!isEmpty(prop.getEnumValues())) {
            int index = prop.getEnumValues().size() - 1;
            index = (int) ((Math.random() * ((index - 0) + 1)) + 0);
            value = prop.getEnumValues().get(index).toString();
        } else {

            value = getStringRegEx(prop);
        }
        return value;
    }

    private static String writeJsonSchema(String name, String jsonString, String path, boolean isPrint) {
        String prettyJso = null;

        ObjectMapper mapper = new ObjectMapper();
        Object json;
        try {
            json = mapper.readValue(jsonString, Object.class);
            prettyJso = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error while writing Json", e);
            throw new IllegalArgumentException("Unable to generate json schemas");
        }
        if (isPrint) {
            File directory = new File(path);
            LOGGER.log(Level.INFO, "Generating Path>>" + path);
            if (!directory.exists()) {
                LOGGER.log(Level.INFO, "Generating Directories>>" + directory.mkdirs());
            } else {
                LOGGER.log(Level.INFO, "Existing Directories>>");
            }

            try (FileWriter fileWriter = new FileWriter(directory + File.separator + Efficacy.schemaName(name) + ".json")) {

                fileWriter.write(prettyJso);
                fileWriter.flush();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error while Generating Json scheme", e);
                throw new IllegalArgumentException("Unable to generate json schemas");
            }
        }
        return prettyJso;
    }

}
