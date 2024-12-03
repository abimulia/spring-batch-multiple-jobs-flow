package com.abimulia.batch.batch_process.exception;

public class OrderProcessingException extends Exception {

    public OrderProcessingException(){}

    public OrderProcessingException(String message){
        super(message);
    }

    public OrderProcessingException(String message, Exception e){
        super(message, e);
    }

}
