package com.bhadouriya.raml.efficacies.demo;

public class SubResponse extends Response {


    public static Response SUCCESS = new SubResponse(1, "Success");

    public static Response WARNING = new SubResponse(2, "warning", Attribute.WARNING);

    public static Response FAILURE = new SubResponse(3, "Failure", Attribute.WARNING, Attribute.INPUT);

    private SubResponse(int code, String description, Attribute... attributes) {
        super(code, description, attributes);
    }
}
