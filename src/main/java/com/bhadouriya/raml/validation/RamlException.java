package com.bhadouriya.raml.validation;

import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

public abstract class RamlException extends RuntimeException {

    private final List<ErrorMessage> messages = new ArrayList<>();

    protected RamlException(ErrorMessage message) {
        super(message.getTitle());
        this.messages.add(message);
    }

    protected RamlException(List<ErrorMessage> messages) {
        super(messages.get(0).getTitle());
        this.messages.addAll(messages);
    }

    public abstract HttpStatus getStatus();

    public List<ErrorMessage> getMessages() {
        return messages;
    }
}
