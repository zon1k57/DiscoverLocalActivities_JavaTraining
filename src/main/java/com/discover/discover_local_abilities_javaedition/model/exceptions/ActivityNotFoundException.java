package com.discover.discover_local_abilities_javaedition.model.exceptions;

public class ActivityNotFoundException extends RuntimeException {
    public ActivityNotFoundException(String message,Long id) {

        super(message+id);
    }
}
