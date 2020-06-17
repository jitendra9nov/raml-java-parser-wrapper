package com.bhadouriya.raml.template;

public class TemplateUse {
    public static void main(String[] args) {

        new ServiceCallback().callWithTemplate(new ServiceTemplate<Object>() {
            @Override
            public Object callService(Class<Object> returnType, Object o) {
                return null;
            }
        }, Object.class);
    }
}
