package com.bhadouriya.raml.efficacies;

import com.bhadouriya.raml.artifacts.*;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.squareup.javapoet.*;
import org.apache.commons.lang.WordUtils;
import org.hamcrest.Matchers;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

import static com.bhadouriya.raml.efficacies.Efficacy.*;
import static com.squareup.javapoet.TypeName.*;
import static java.io.File.separator;
import static javax.lang.model.element.Modifier.*;
import static org.apache.commons.lang.StringUtils.*;
import static org.apache.commons.lang.math.NumberUtils.toInt;
import static org.springframework.http.HttpStatus.valueOf;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.isEmpty;

public class Instigator {

    private static final Logger LOGGER = Logger.getLogger(Instigator.class.getName());
    private static final String AUTHER_SINCE = "\n@author $L\n@since $L\n@version 1.0\n";
    private static final Map<String, TypeName> NAME_TYPE_MAP = new HashMap<String, TypeName>();
    private static final String JAVA_PKG = separator + "src" + separator + "main" + separator + "java" + separator;
    private static final Map<String, String> MATCHER_MAP = new HashMap<>();

    static {
        NAME_TYPE_MAP.put("boolean", BOOLEAN);
        NAME_TYPE_MAP.put("char", CHAR);
        NAME_TYPE_MAP.put("byte", BYTE);
        NAME_TYPE_MAP.put("short", SHORT);
        NAME_TYPE_MAP.put("int", INT);
        NAME_TYPE_MAP.put("integer", INT);
        NAME_TYPE_MAP.put("number", DOUBLE);
        NAME_TYPE_MAP.put("long", LONG);
        NAME_TYPE_MAP.put("float", FLOAT);
        NAME_TYPE_MAP.put("double", DOUBLE);

        NAME_TYPE_MAP.put("Boolean", BOOLEAN.box());
        NAME_TYPE_MAP.put("Character", CHAR.box());
        NAME_TYPE_MAP.put("Byte", BYTE.box());
        NAME_TYPE_MAP.put("Short", SHORT.box());
        NAME_TYPE_MAP.put("Integer", INT.box());
        NAME_TYPE_MAP.put("Long", LONG.box());
        NAME_TYPE_MAP.put("Float", FLOAT.box());
        NAME_TYPE_MAP.put("Double", DOUBLE.box());

        NAME_TYPE_MAP.put("date-only", get(Date.class));//yyyy-mm-dd e.g. 2015-05-23
        NAME_TYPE_MAP.put("time-only", get(Date.class));//hh:,,:ss[.ff...] e.g. 12:30:00
        NAME_TYPE_MAP.put("datetime-only", get(Date.class));//yyyy-mm-ddThh:mm:ss[.ff...] e.g. 2015-05-23T21:00:00
        NAME_TYPE_MAP.put("datetime", get(Date.class));//015-05-23T21:00:090Z for rfc3339 and Sun, 28 feb 2016 16:41:41 //GMT for rfc2616

        NAME_TYPE_MAP.put("object", OBJECT);
    }

    static {
        MATCHER_MAP.put("null", "is(nullValue())");
        MATCHER_MAP.put("notNull", "not(nullValue())");
        MATCHER_MAP.put("containsString", "containsString($L)");
        MATCHER_MAP.put("start", "startsWith($L)");
        MATCHER_MAP.put("ends", "endsWith($L)");
        MATCHER_MAP.put("eqic", "equalToIgnoreCase($L)");
        MATCHER_MAP.put("=", "is($L)");
        MATCHER_MAP.put("!=", "is(not($L))");
        MATCHER_MAP.put("in", "isIn(asList($L))");
        MATCHER_MAP.put("notIn", "is(not(isIn(asList($L))))");
        MATCHER_MAP.put(">", "greaterThan($L)");
        MATCHER_MAP.put(">=", "greaterThanEqualTo($L)");
        MATCHER_MAP.put("<", "greaterThan($L)");
        MATCHER_MAP.put("<=", "lessThanEqualTo($L)");
    }

    private static TypeName convertToJavaClass(final String type, final String format) {
        String arrayName = type;
        if (isEmpty(arrayName)) {
            return OBJECT;
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
        TypeName clazz = NAME_TYPE_MAP.get(arrayName);

        if (clazz == null) {
            //No primitive, try to load it from given classloader
            clazz = ClassName.get("java.lang.", capitalize(arrayName));
        }
        if (clazz == null) {
            //No primitive, try to load it from given classloader
            clazz = ClassName.get("java.util.", capitalize(arrayName));
        }
        if (clazz == null) {
            clazz = OBJECT;
        }
        return clazz;
    }

    private static void application(final Artifacts artifacts) throws IOException {
        final String app = artifacts.getDirectories().get(lowerCase(artifacts.getPackageName())).getCanonicalPath();
        final List<MethodSpec> methods = new ArrayList<>();
        methods.add(MethodSpec.methodBuilder("configure").addModifiers(PUBLIC)
                .returns(SpringApplicationBuilder.class).addParameter(
                        ParameterSpec.builder(SpringApplicationBuilder.class, "application").addModifiers(FINAL).build()
                ).addStatement("return application.source($L.class)", "Application").addAnnotation(Override.class).build());

        methods.add(MethodSpec.methodBuilder("main").addModifiers(PUBLIC, STATIC)
                .returns(void.class).addParameter(String[].class, "args").addStatement("$T.run($L.class,args)", SpringApplication.class, "Application").build());

        TypeSpec clazz = TypeSpec.classBuilder("Application")
                .addModifiers(PUBLIC).superclass(SpringBootServletInitializer.class)
                .addAnnotation((AnnotationSpec.builder(SpringBootApplication.class)
                        .addMember("scanBasePackage", "$S", "com.bhadouriya").build()))
                .addMethods(methods)
                .addJavadoc("This is startup class used for $L($L)\n" + AUTHER_SINCE,
                        artifacts.getApiName(), artifacts.getPackageName(), System.getProperty("usr.name"),
                        getCurrentDateInSpecificFormat(Calendar.getInstance()))
                .build();
        writeFile(artifacts, app, buildJavaFile(artifacts, false, app, clazz));
    }

    private static void writeFile(Artifacts artifacts, String dir, JavaFile javaFile) {

        try {
            PrintWriter writer = new PrintWriter(dir + separator + javaFile.typeSpec.name + ".java", "UTF=8");
            writer.println("/*");
            writer.println(" * Copyright (c) " + Calendar.getInstance().get(Calendar.YEAR) + " Bhadouriya, All Right Reserved.");
            writer.println(" * ");
            writer.println(" * Write Something");
            writer.println("*/");
            javaFile.writeTo(writer);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();//TODO
        }
    }

    private static JavaFile buildJavaFile(Artifacts artifacts, boolean isController, String dir, TypeSpec typeSpec) throws IOException {
        JavaFile.Builder javaFileBuilder =
                JavaFile.builder(dir.replace(new File(artifacts.getBaseDirectory()).getCanonicalPath() + JAVA_PKG, "").replace(separator, "."), typeSpec);
        if (isController) {
            javaFileBuilder.addStaticImport(Matchers.class, "*")
                    .addStaticImport(RuleValidator.class, "with");
        }
        javaFileBuilder.skipJavaLangImports(true)/*.addFileComment()*/;
        return javaFileBuilder.build();
    }

    private static String getCurrentDateInSpecificFormat(Calendar instance) {
        String dayNumberSuff = getDayOfMonthSuffix(instance.get(Calendar.DAY_OF_MONTH));
        DateFormat dateFormat = new SimpleDateFormat(" d'" + dayNumberSuff + "' MMMM yyyy");
        return dateFormat.format(instance.getTime());
    }

    private static String getDayOfMonthSuffix(int n) {
        String suffix = "";
        Preconditions.checkArgument((n >= 1) && (n <= 31), "illegal day of month: " + n);
        if ((n >= 11) && (n <= 13)) {
            suffix = "th";
        }
        switch (n % 10) {
            case 1:
                suffix = "st";
                break;
            case 2:
                suffix = "nd";
                break;
            case 3:
                suffix = "rd";
                break;
            default:
                suffix = "th";
                break;
        }
        return "<sup>" + suffix + "</sup>";
    }

    private static void classBuild(Artifacts artifacts, boolean isClass, String name, boolean isLive) throws IOException {
        boolean isController = "controller".endsWith(name);
        final String directory = artifacts.getDirectories().get(lowerCase(name)).getCanonicalPath();

        for (WrapperResource wr : artifacts.getWrapperApi().getWrapperResources()
        ) {
            List<MethodSpec> methodSpecs = new ArrayList<>();
            FieldSpec mapper = null;
            if (isController) {
                mapper = FieldSpec.builder(ObjectMapper.class, WordUtils.uncapitalize(ObjectMapper.class.getSimpleName()))
                        .addModifiers(PRIVATE, STATIC, FINAL)
                        .initializer("new $T()", ObjectMapper.class).build();
            }
            classMethodBuilder(artifacts, isClass, isController, methodSpecs, wr, wr.getName());
            for (WrapperResource child : wr.getChildWrapperResources()
            ) {
                classMethodBuilder(artifacts, isClass, isController, methodSpecs, child, wr.getName());
            }
            ClassName interfaze = ClassName.get(directory.replace(new File(artifacts.getBaseDirectory()).getCanonicalPath() + JAVA_PKG, "").replace(separator, "."), createClasName(wr.getName(), name));

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(createClasName(wr.getName(), name) + ((isController) ? "" : "Impl"))
                    .addModifiers(PUBLIC).addMethods(methodSpecs);

            if (isController) {
                classBuilder.addAnnotation(RestController.class).addField(mapper);
            } else {
                classBuilder.addSuperinterface(interfaze);
            }
            writeFile(artifacts, directory, buildJavaFile(artifacts, isController, directory, classBuilder.build()));
        }
    }

    private static void classMethodBuilder(Artifacts artifacts, boolean isClass, boolean isController, List<MethodSpec> methodSpecs, WrapperResource res, String name) throws IOException {

        for (WrapperMethod wm : res.getWrapperMethods()
        ) {
            TypeName responseEntity = ParameterizedTypeName.get(ResponseEntity.class, Object.class);
            MethodSpec.Builder methBuilder = MethodSpec.methodBuilder(uncapitalize(res.getName())).addModifiers(PUBLIC).returns(responseEntity);

            List<ParameterSpec> parameterSpecList = new ArrayList<>();
            ParameterSpec headerPar = requestHeaderBuilder(artifacts, methBuilder, wm, (isController && isClass));

            if (null != headerPar) {
                parameterSpecList.add(headerPar);
            }
            parameterSpecList.addAll(requestBodyBuilder(artifacts, methBuilder, wm, (isController && isClass), uncapitalize(name)));
            parameterSpecList.addAll(requestUriBuilder(methBuilder, res, (isController && isClass)));

            if (isController) {
                methBuilder.addJavadoc(res.getDescription() + "\n").addJavadoc(wm.getDescription() + "\n");
                methBuilder.addAnnotation(requestMappingBuilder(res, wm)).addException(Exception.class);

                String resCode = "OK";
                String path = "";

                if (!isEmpty(wm.getWrapperResponses())) {
                    for (WrapperResponse resp : wm.getWrapperResponses()
                    ) {
                        if (valueOf(toInt(resp.getCode(), 500)).is2xxSuccessful()) {
                            resCode = getConstantName(HttpStatus.class, valueOf(toInt(resp.getCode(), 500)).name(), true);
                            if (null != resp.getBeanObject()) {
                                path = schemaName(resp.getBeanObject().getName()) + ".json";
                            }
                            break;
                        }
                    }
                }
                formRules(methBuilder, wm, wm.getRuleSet(), artifacts, resCode, path);
            } else {
                methBuilder.addAnnotation(Override.class).addComment("TODO").addStatement("return null");
            }
            methodSpecs.add(methBuilder.build());
        }

    }

    private static void controller(Artifacts artifacts) throws IOException {
        classBuild(artifacts, true, "controller", true);
    }

    public static void instigate(Artifacts artifacts) {
        try {
            application(artifacts);
            bean(artifacts);
            controller(artifacts);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static List<ParameterSpec> requestBodyBuilder(Artifacts artifacts, MethodSpec.Builder methBuilder, WrapperMethod wm, boolean isClass, String name) throws IOException {

        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        if (null != wm.getWrapperRequest()) {
            final String directory = artifacts.getDirectories().get(lowerCase(name)).getCanonicalPath().replace(new File(artifacts.getBaseDirectory()).getCanonicalPath() + JAVA_PKG, "").replace(separator, ".");

        /*ClassName bean=ClassName.get(directory,wm.getWrapperRequest().getRequestBeanName());
        ParameterSpec.Builder builder=ParameterSpec.builder(bean,uncapitalize(wm.getWrapperRequest().getRequestBeanName()));*/

            ParameterSpec.Builder builder = ParameterSpec.builder(Object.class, uncapitalize("request"));//It can be bean name
            if (isClass) {
                builder.addAnnotation(RequestBody.class);
            }
            ParameterSpec param = builder.build();
            parameterSpecs.add(param);
            methBuilder.addParameter(param);
        }
        if (!isEmpty(wm.getWrapperQueryParams())) {

            for (WrapperType qPar : wm.getWrapperQueryParams()
            ) {
                Property prop = qPar.getProperty();
                TypeName cl = convertToJavaClass(prop.getType(), prop.getFormat());
                ParameterSpec.Builder qBuilder = ParameterSpec.builder(cl, prop.getName());

                if (isClass) {
                    qBuilder.addAnnotation((AnnotationSpec.builder(RequestParam.class)
                            .addMember("name", "$S", prop.getName())
                            .addMember("required", "$L", prop.isRequired()).build()));
                }

                ParameterSpec param = qBuilder.build();
                parameterSpecs.add(param);
                methBuilder.addParameter(param);

            }
        }

        return parameterSpecs;
    }

    private static ParameterSpec requestHeaderBuilder(Artifacts artifacts, MethodSpec.Builder methBuilder, WrapperMethod wm, boolean isClass) {
        ParameterSpec param = null;
        if (!isEmpty(wm.getWrapperHeaders())) {
            TypeName header = ParameterizedTypeName.get(Map.class, String.class, String.class);
            ParameterSpec.Builder builder = ParameterSpec.builder(header, "headers");
            if (isClass) {
                builder.addAnnotation(RequestHeader.class);
            }
            param = builder.build();
            methBuilder.addParameter(param);
        }
        return param;
    }

    private static AnnotationSpec requestMappingBuilder(WrapperResource res, WrapperMethod wm) {
        AnnotationSpec.Builder builder = AnnotationSpec.builder(RequestMapping.class)
                .addMember("method", "$T.$L", RequestMethod.class, getConstantName(RequestMethod.class, wm.getType(), true))
                .addMember("value", "$S", res.getPath());

        if (null != wm.getWrapperRequest() && null != wm.getWrapperRequest().getMediaType()) {
            builder.addMember("consumes", "$T.$L", MediaType.class, getConstantName(MediaType.class, wm.getWrapperRequest().getMediaType(), false));

        }
        if (!isEmpty(wm.getWrapperResponses())) {
            for (WrapperResponse resp : wm.getWrapperResponses()
            ) {
                if (valueOf(toInt(resp.getCode(), 500)).is2xxSuccessful()) {
                    builder.addMember("produces", "$T.$L", MediaType.class, getConstantName(MediaType.class, wm.getWrapperRequest().getMediaType(), false));
                    break;
                }
            }
        }
        return builder.build();
    }

    private static List<ParameterSpec> requestUriBuilder(MethodSpec.Builder methBuilder, WrapperResource res, boolean isClass) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        if (!isEmpty(res.getWrapperUriParams())) {
            for (WrapperType uriParam : res.getWrapperUriParams()
            ) {
                Property prop = uriParam.getProperty();
                TypeName cl = convertToJavaClass(prop.getType(), prop.getFormat());
                ParameterSpec.Builder uBuilder = ParameterSpec.builder(cl, prop.getName());

                if (isClass) {
                    uBuilder.addAnnotation((AnnotationSpec.builder(PathVariable.class)
                            .addMember("value", "$S", prop.getName()).build()));
                }

                ParameterSpec param = uBuilder.build();
                parameterSpecs.add(param);
                methBuilder.addParameter(param);
            }

        }
        return parameterSpecs;
    }

    private static void bean(Artifacts artifacts) throws IOException {
        for (WrapperResource res : artifacts.getWrapperApi().getWrapperResources()
        ) {
            buildRequestBeans(artifacts, res, res.getName());
            buildResponseBeans(artifacts, res, res.getName());
            for (WrapperResource child : res.getChildWrapperResources()
            ) {
                buildRequestBeans(artifacts, child, res.getName());
                buildResponseBeans(artifacts, child, res.getName());
            }
        }
    }

    private static void buildRequestBeans(Artifacts artifacts, WrapperResource res, String name) throws IOException {
        for (WrapperMethod method : res.getWrapperMethods()
        ) {
            if (null != method.getWrapperRequest() && null != method.getWrapperRequest().getBeanObject()) {
                createBean(artifacts, method.getWrapperRequest().getBeanObject(), lowerCase(name));
            }
        }
    }

    private static void buildResponseBeans(Artifacts artifacts, WrapperResource res, String name) throws IOException {
        for (WrapperMethod wm : res.getWrapperMethods()
        ) {
            if (!isEmpty(wm.getWrapperResponses())) {
                for (WrapperResponse resp : wm.getWrapperResponses()
                ) {
                    if (valueOf(toInt(resp.getCode(), 500)).is2xxSuccessful() && null != resp.getBeanObject()) {
                        createBean(artifacts, resp.getBeanObject(), lowerCase(name));
                        break;
                    }
                }
            }

        }
    }

    private static void createBean(Artifacts artifacts, BeanObject parent, String beanType) throws IOException {
        final String directory = artifacts.getDirectories().get(lowerCase(beanType)).getCanonicalPath();
        String pkg = directory.replace(new File(artifacts.getBaseDirectory()).getCanonicalPath() + JAVA_PKG, "").replace(separator, ".");

        List<Property> properties = parent.getProperties();

        List<FieldSpec> fieldSpecs = new ArrayList<>();

        for (Property prop : properties
        ) {
            String type = prop.getType();
            TypeName fieldType = OBJECT;
            switch (type) {
                case "object": {
                    BeanObject child = prop.getBeanObject();
                    fieldType = ClassName.get(pkg, child.getName());
                    createBean(artifacts, child, beanType);
                    break;
                }
                case "array": {
                    BeanObject child = prop.getBeanObject();
                    ClassName list = ClassName.get(List.class);
                    if (null != child) {
                        if (child.getProperties().isEmpty()) {
                            fieldType = ParameterizedTypeName.get(list, convertToJavaClass(child.getName(), null));
                        } else {
                            fieldType = ParameterizedTypeName.get(list, ClassName.get(pkg, child.getName()));
                        }
                        createBean(artifacts, child, beanType);
                    }

                    break;
                }
                default: {
                    fieldType = convertToJavaClass(type, null);
                    break;
                }
            }

            FieldSpec.Builder builder = FieldSpec.builder(fieldType, prop.getName()).addModifiers(PRIVATE);

            if (artifacts.isAnnotation()) {
                if ("String".equalsIgnoreCase(type)) {
                    if (prop.isRequired()) {
                        builder.addAnnotation(NotBlank.class);
                    }
                    if (null != prop.getMin() || null != prop.getMax()) {
                        AnnotationSpec.Builder annBuider = AnnotationSpec.builder(Size.class);
                        if (null != prop.getMin()) {
                            annBuider.addMember("min", "$L", prop.getMin());
                        }
                        if (null != prop.getMax()) {
                            annBuider.addMember("max", "$L", prop.getMax());
                        }
                        String mess = prop.getName() + (
                                ((null != prop.getMin() && null != prop.getMax()) ? (" should be of length between " + prop.getMin()
                                        + " and " + prop.getMax())
                                        : (null != prop.getMin()) ? " should be greater than " + prop.getMin() : " should be less than " + prop.getMax())
                        );
                        annBuider.addMember("message", "$S", mess);
                    }
                    if (null != prop.getFormat()) {
                        builder.addAnnotation(AnnotationSpec.builder(Pattern.class)
                                .addMember("regexp", "$S", prop.getFormat())
                                .addMember("message", "$S", prop.getName() + " does not match the valid pattern").build());

                    }
                }
                builder.addAnnotation(AnnotationSpec.builder(JsonProperty.class).addMember("value", "$S", prop.getName()).build());
            }
            fieldSpecs.add(builder.build());

        }
        if (parent.isAdditionalProperties()) {
            TypeName mapType = ParameterizedTypeName.get(Map.class, String.class, Object.class);

            FieldSpec.Builder builderFa = FieldSpec.builder(mapType, "additionalproperties").addModifiers(PRIVATE)
                    .initializer("new $T<>()", HashMap.class);

            if (artifacts.isAnnotation()) {
                builderFa.addAnnotation(AnnotationSpec.builder(JsonIgnore.class).build());
            }
            fieldSpecs.add(builderFa.build());
        }
        List<MethodSpec> methodSpecs = new ArrayList<>();


        for (FieldSpec fieldSpec : fieldSpecs) {
            methodSpecs.add(getter(fieldSpec.name, fieldSpec.type, artifacts.isAnnotation()));
            methodSpecs.add(setter(fieldSpec.name, fieldSpec.type, artifacts.isAnnotation()));
        }
        TypeSpec.Builder clzBuiler = TypeSpec.classBuilder(parent.getName()).addModifiers(PUBLIC).addFields(fieldSpecs)
                .addMethods(methodSpecs);
        if (artifacts.isAnnotation()) {
            clzBuiler.addAnnotation((AnnotationSpec.builder(JsonInclude.class).addMember("value", "$T.NON_NULL", JsonInclude.Include.class)).build());

        }
        TypeSpec clazz = clzBuiler.addJavadoc("This is " + beanType + " bean required for $L($L) API.\n" + AUTHER_SINCE,
                artifacts.getApiName(), artifacts.getPackageName(), System.getProperty("user.name"),
                getCurrentDateInSpecificFormat(Calendar.getInstance())).build();
        writeFile(artifacts, directory, buildJavaFile(artifacts, false, directory, clazz));
    }

    private static MethodSpec getter(String name, TypeName type, boolean annotation) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder((type.equals(BOOLEAN) ? "is" : "get") + capitalize(name))
                .returns(type).addModifiers(PUBLIC).addStatement("return $N", name);
        if (annotation) {
            if ("additionalProperties".endsWith(name)) {
                builder.addAnnotation(AnnotationSpec.builder(JsonAnyGetter.class).build());
            } else {
                builder.addAnnotation(AnnotationSpec.builder(JsonProperty.class).addMember("value", "$S", name).build());
            }
        }
        return builder.build();
    }

    private static MethodSpec setter(String name, TypeName type, boolean annotation) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("set" + capitalize(name))
                .returns(void.class).addModifiers(PUBLIC);

        if ("additionalProperties".endsWith(name)) {
            builder.addParameter(String.class, "name", FINAL)
                    .addParameter(Object.class, "value", FINAL)
                    .addStatement("additionalProperties.put(name,value)");

        } else {
            builder.addParameter(type, name, FINAL).addStatement("this.$N=$N", name, name);
        }
        if (annotation) {
            if ("additionalProperties".endsWith(name)) {
                builder.addAnnotation(AnnotationSpec.builder(JsonAnyGetter.class).build());
            } else {
                builder.addAnnotation(AnnotationSpec.builder(JsonProperty.class).addMember("value", "$S", name).build());
            }
        }

        return builder.build();
    }

    private static void formRules(MethodSpec.Builder methBuilder, WrapperMethod wm, List<RuleSet> ruleSets, Artifacts artifacts, String resCode, String path) {
        ClassName ruleValClass = ClassName.get(RuleValidator.class);
        String ruleValVar = uncapitalize(ruleValClass.simpleName());

        ParameterizedTypeName resEntity = ParameterizedTypeName.get(ResponseEntity.class, Object.class);

        String resEntVar = uncapitalize(ResponseEntity.class.getSimpleName());

        methBuilder.addStatement("$T $L=null", resEntity, resEntVar);

        methBuilder.beginControlFlow("try", "").addStatement("$T $N =null", Object.class, "payload");

        if (null != wm.getWrapperRequest()) {
            methBuilder.addStatement("$T $N=$L($L.writeValueAsString($L))", ruleValClass, ruleValVar, "with", uncapitalize(ObjectMapper.class.getSimpleName()), "request");

        }
        boolean isRule = false;

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

        for (final RuleSet ruleSet : ruleSets
        ) {
            //This is Action
            final String code = getConstantName(HttpStatus.class, valueOf(toInt(ruleSet.getCode(), 500)).name(), true);

            final String message = ruleSet.getMessage();

            final Rule group = ruleSet.getRuleQuery();
            final String combinator = group.getCombinator();
            final Boolean not = group.getNot();
            if (isRule) {
                methBuilder.nextControlFlow("else if ($L.found())", formGroup(group, combinator, not, ruleValClass, ruleValVar, wm.getRequestJsonPath()));
                methBuilder.addComment("$L", ruleSet.getQuery());
                methBuilder.addComment("Rule Name:  $L with Order: $L", ruleSet.getName(), ruleSet.getOrder());
            } else {
                methBuilder.addComment("$L", ruleSet.getQuery());
                methBuilder.addComment("Rule Name:  $L with Order: $L", ruleSet.getName(), ruleSet.getOrder());
                methBuilder.beginControlFlow("if ($L.found())", formGroup(group, combinator, not, ruleValClass, ruleValVar, wm.getRequestJsonPath()));

            }
            methBuilder.addStatement("$L =new $T(create($S,$S,$S), $L)", resEntVar, resEntity, ruleSet.getCode(), code, message, code);

            isRule = true;
        }
        if (isRule) {
            methBuilder.nextControlFlow("else)", "");
            methBuilder.addStatement("payload = $L.readValue(new %$($T.class.getResourceAsStream($S)), $T.class)",
                    uncapitalize(ObjectMapper.class.getSimpleName()),
                    InputStreamReader.class, TypeReference.class, "/jsons/" + path, Object.class
            )
                    .addStatement("$L= new $T($N,$L)", resEntVar, resEntity, "payload", resCode)
                    .endControlFlow()
                    .nextControlFlow("catch ($T e", IOException.class)
                    .addStatement("e.peintStackTrace()", "").addStatement("throw e", "").endControlFlow()
                    .addStatement("return $L", resEntVar);
        } else {

            methBuilder.addStatement("payload = $L.readValue(new %$($T.class.getResourceAsStream($S)), $T.class)",
                    uncapitalize(ObjectMapper.class.getSimpleName()),
                    InputStreamReader.class, TypeReference.class, "/jsons/" + path, Object.class
            )
                    .addStatement("$L= new $T($N,$L)", resEntVar, resEntity, "payload", resCode)
                    .endControlFlow()
                    .nextControlFlow("catch ($T e", IOException.class)
                    .addStatement("e.peintStackTrace()", "").addStatement("throw e", "").endControlFlow()
                    .addStatement("return $L", resEntVar);
        }
    }

    private static Object formGroup(Rule group, String combinator, Boolean not, ClassName ruleValClass, String ruleValVar, Map<String, RamlAttribute> requestJsonPath) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.add("$L.fresh().$L(insert(0, new$T[0],", ruleValVar, combinator, ruleValClass);

        boolean isFirstRule = true;
        boolean isFirst = true;

        for (Rule rule : group.getRules()) {

            if (!isEmpty(rule.getRules())) {
                //This is Group
                if (!isFirst) {
                    builder.add(",");
                }
                builder.add("$L", formGroup(rule, rule.getCombinator(), rule.getNot(), ruleValClass, ruleValVar, requestJsonPath));
            } else {
                //This is Rule
                if (isFirstRule) {
                    builder.add("$L.fresh()", ruleValVar);
                }
                isFirstRule = false;
                builder.add(".$L($L)", combinator, formRule(rule, combinator, requestJsonPath));
            }
            isFirst = false;
        }
        builder.add(")).negate($L)", not);
        return builder.build();
    }

    private static CodeBlock formRule(Rule rule, String combinator, Map<String, RamlAttribute> requestJsonPath) {
        //This is Rule
        CodeBlock.Builder builder = CodeBlock.builder();

        String field = rule.getField();
        String value = rule.getValue();
        String operator = rule.getOperator();

        RamlAttribute attribute = requestJsonPath.get(field);
        if (null != attribute && (containsIgnoreCase(attribute.getType(), "string") || containsIgnoreCase(attribute.getType(), "date"))) {
            value = "\"" + value + "\"";
        }

        builder.add("$S," + MATCHER_MAP.get(operator), field, (null != value ? value : ""));
        return builder.build();

    }


}
