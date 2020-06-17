package com.bhadouriya.raml.template;

public class ServiceCallback {

    public <T> T callWithTemplate(ServiceTemplate<T> serviceTemplate, Class<T> type) {
        T resp = null;
        final Object o = serviceTemplate.firstMethod();
        resp = serviceTemplate.callService(type, o);
        serviceTemplate.lastMethod(resp);
        return resp;
    }

}
