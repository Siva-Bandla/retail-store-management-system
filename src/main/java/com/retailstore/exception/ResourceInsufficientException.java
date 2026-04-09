package com.retailstore.exception;

public class ResourceInsufficientException extends RuntimeException{

    public ResourceInsufficientException(String message){
        super(message);
    }
}
