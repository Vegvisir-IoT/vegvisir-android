package com.vegvisir.vegvisir_lower_level.network.Exceptions;

public class HandlerNotRegisteredException extends Exception {

    public HandlerNotRegisteredException(String messageType) {
        super(messageType);
    }

}
