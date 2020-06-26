package com.bhadouriya.raml.oauth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.*;

public class AddHeaderHttpServletRequest extends HttpServletRequestWrapper {

    private HashMap<String,String> customHeaders;

    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public AddHeaderHttpServletRequest(HttpServletRequest request){
        super(request);
        this.customHeaders = new HashMap<>();
    }

    @Override
    public String getHeader(String name) {
        if(customHeaders.containsKey(name)){
            return customHeaders.get(name);
        }
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {

        Set<String> headerNames=new HashSet<>();

        Enumeration<String> originalHeaderNames=super.getHeaderNames();

        if(null!=originalHeaderNames){
            while (originalHeaderNames.hasMoreElements()){
                headerNames.add(originalHeaderNames.nextElement());
            }
        }
        headerNames.addAll(customHeaders.keySet());
        return Collections.enumeration(headerNames);
    }

    public void addHeader(String key, String value){
        customHeaders.put(key,value);
    }
}
