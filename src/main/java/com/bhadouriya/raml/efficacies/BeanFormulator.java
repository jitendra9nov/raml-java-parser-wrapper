package com.bhadouriya.raml.efficacies;

import com.bhadouriya.raml.artifacts.BeanObject;
import com.bhadouriya.raml.artifacts.Property;
import org.raml.v2.api.model.v10.datamodel.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static org.springframework.util.CollectionUtils.isEmpty;

public class BeanFormulator {


    private static Property handleAnyType(final AnyTypeDeclaration td) {
        return new Property(td.name(), td.type());
    }

    private static Property handleArrayType(final ArrayTypeDeclaration td) {
        String type = td.type();
        if (type.endsWith("[]")) {
            type = "array";
        }
        final Property prop = new Property(td.name(), td.type());
        if (null != td.items()) {
            final Property items = handleTypes(td.items(), td.name());
            prop.setBean(items.getBeanObject());
            prop.setItems(items);

            if (null == items.getExample()) {
                setExample(td, items);
            }
            if (isEmpty(items.getExamples()) && !isEmpty(td.examples())) {
                final List<String> examples = new ArrayList<>();
                td.examples().forEach(ex -> {
                    examples.add(ex.value());
                });
                items.setExamples(examples);
            }
        }

        prop.setUniqueItems(Boolean.TRUE.equals(td.uniqueItems()));
        prop.setMax(td.maxItems());
        prop.setMin(td.minItems());
        return prop;
    }

    private static Property handleBooleanType(BooleanTypeDeclaration td) {
        final Property prop = new Property(td.name(), td.type());
        prop.setEnumValues(td.enumValues());
        return prop;
    }

    private static Property handleDateTimeOnlyType(DateTimeOnlyTypeDeclaration td) {
        return new Property(td.name(), td.type());
    }

    private static Property handleDateTimeType(DateTimeTypeDeclaration td) {
        final Property prop = new Property(td.name(), td.type());
        prop.setFormat(td.format());
        return prop;
    }

    private static Property handleDateType(DateTypeDeclaration td) {
        return new Property(td.name(), td.type());
    }

    private static Property handleExternalType(ExternalTypeDeclaration td) {
        final Property prop = new Property(td.name(), td.type());
        prop.setSchemeContnt(td.schemaContent());
        return prop;
    }

    private static Property handleFileType(FileTypeDeclaration td) {
        final Property prop = new Property(td.name(), td.type());
        prop.setMax(td.maxLength());
        prop.setMin(td.minLength());
        return prop;
    }

    private static Property handleNullType(final NullTypeDeclaration td) {
        return new Property(td.name(), td.type());
    }

    private static Property handleNumberType(NumberTypeDeclaration td) {
        final Property prop = new Property(td.name(), td.type());
        prop.setMax(td.maximum());
        prop.setMin(td.minimum());
        prop.setEnumValues(td.enumValues());
        prop.setFormat(td.format());
        prop.setMultipleOf(td.multipleOf());
        return prop;
    }

    private static Property handleObjectType(ObjectTypeDeclaration td, BeanObject bn) {
        bn.setAdditionalProperties(td.additionalProperties());
        bn.setDiscriminator(td.discriminator());
        bn.setDefaultValue(td.discriminatorValue());
        bn.setMaxProperties(td.maxProperties());
        bn.setMinProperties(td.minProperties());

        bn.setDescription(isNull(td.description()) ? null : td.description().value());
        bn.setDefaultValue(td.defaultValue());
        bn.setRequired(Boolean.TRUE.equals(td.required()));

        if (null != td.properties()) {
            td.properties().forEach(tdc -> {
                bn.putProperty(handleTypes(tdc, tdc.name()));
            });
        }
        final Property prop = new Property(td.name(), td.type());
        prop.setBean(bn);
        return prop;

    }

    private static Property handlePFileType(org.raml.v2.api.model.v10.parameters.FileTypeDeclaration td) {
        final Property prop = new Property(td.name(), td.type());
        prop.setMax(td.maxLength());
        prop.setMin(td.minLength());
        prop.setEnumValues(td.fileTypes());
        return prop;
    }

    private static Property handleStringType(StringTypeDeclaration td) {
        final Property prop = new Property(td.name(), "string");
        prop.setMax(td.maxLength());
        prop.setMin(td.minLength());
        prop.setEnumValues(td.enumValues());
        prop.setFormat(td.pattern());
        return prop;
    }

    private static Property handleTimeOnlyType(TimeOnlyTypeDeclaration td) {
        return new Property(td.name(), td.type());
    }

    private static Property handleUnionType(UnionTypeDeclaration td) {
        return new Property(td.name(), td.type());
    }

    private static Property handleTypes(TypeDeclaration td, String name) {
        Property prop = null;
        if (td instanceof ObjectTypeDeclaration) {
            String beanName = name;
            if (!"object".equals(td.type())) {
                beanName = td.type();
            }
            prop = handleObjectType((ObjectTypeDeclaration) td, new BeanObject(beanName));
            prop.setName(name);
            prop.setType("object");
        } else if (td instanceof NumberTypeDeclaration) {
            prop = handleNumberType((NumberTypeDeclaration) td);
        } else if (td instanceof BooleanTypeDeclaration) {
            prop = handleBooleanType((BooleanTypeDeclaration) td);
        } else if (td instanceof AnyTypeDeclaration) {
            prop = handleAnyType((AnyTypeDeclaration) td);
        } else if (td instanceof ArrayTypeDeclaration) {
            prop = handleArrayType((ArrayTypeDeclaration) td);
        } else if (td instanceof DateTimeTypeDeclaration) {
            prop = handleDateTimeType((DateTimeTypeDeclaration) td);
        } else if (td instanceof DateTimeOnlyTypeDeclaration) {
            prop = handleDateTimeOnlyType((DateTimeOnlyTypeDeclaration) td);
        } else if (td instanceof DateTypeDeclaration) {
            prop = handleDateType((DateTypeDeclaration) td);
        } else if (td instanceof TimeOnlyTypeDeclaration) {
            prop = handleTimeOnlyType((TimeOnlyTypeDeclaration) td);
        } else if (td instanceof ExternalTypeDeclaration) {
            prop = handleExternalType((ExternalTypeDeclaration) td);
        } else if (td instanceof FileTypeDeclaration) {
            prop = handleFileType((FileTypeDeclaration) td);
        } else if (td instanceof org.raml.v2.api.model.v10.parameters.FileTypeDeclaration) {
            prop = handlePFileType((org.raml.v2.api.model.v10.parameters.FileTypeDeclaration) td);
        } else if (td instanceof StringTypeDeclaration) {
            prop = handleStringType((StringTypeDeclaration) td);
        } else if (td instanceof UnionTypeDeclaration) {
            prop = handleUnionType((UnionTypeDeclaration) td);
        } else if (td instanceof NullTypeDeclaration) {
            prop = handleNullType((NullTypeDeclaration) td);
        }
        if (null != prop) {
            prop.setDescription(isNull(td.description()) ? null : td.description().value());
            prop.setDefaultValue(td.defaultValue());
            prop.setRequired(Boolean.TRUE.equals(td.required()));

            setExample(td, prop);
            final List<String> examples = new ArrayList<>();
            td.examples().forEach(ex -> {
                examples.add(ex.value());
            });
            prop.setExamples(examples);
        }
        return prop;
    }

    private static void setExample(TypeDeclaration td, Property prop) {
        String example = isNull(td.example()) ? null : td.example().value();
        if (!isNull(example) && example.startsWith("[") && example.endsWith("]")) {
            example = example.substring(1, example.length() - 1).replaceAll("\\s+", "");
            example = example.replaceAll("\"", "").split(",")[0];
        }
        prop.setExample(example);
    }

    public static BeanObject toBean(final TypeDeclaration td) {
        final BeanObject bn = new BeanObject(td.type());
        bn.setDescription(isNull(td.description()) ? null : td.description().value());
        bn.setDefaultValue(td.defaultValue());
        bn.setRequired(Boolean.TRUE.equals(td.required()));
        if (td instanceof ObjectTypeDeclaration) {
            handleObjectType((ObjectTypeDeclaration) td, bn);
        }
        return bn;
    }


}
