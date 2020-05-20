package com.vegvisir.vegvisir_lower_level.network;

import com.vegvisir.vegvisir_lower_level.network.Exceptions.HandlerNotRegisteredException;
import com.vegvisir.network.datatype.proto.Payload;

import java.util.HashMap;
import java.util.Map;

public class Dispatcher {

    Map<String, PayloadHandler> dispatcher;

    public Dispatcher() {
        dispatcher = new HashMap<>();
    }

    /**
     * Register a handler for @type of payload.
     * @param type a identifier to be used in dispatch table.
     * @param handler a function takes payload as input.
     * @return true if registered success; otherwise, false for duplicate register. Please redo
     * with overwrite method.
     */
    public boolean registerHandler(String type, PayloadHandler handler) {
        boolean succ = dispatcher.putIfAbsent(type, handler) == null;
        if (succ) {
            new Thread(handler).start();
        }
        return succ;
    }

    /**
     * Calling handler on payload's type.
     * @param payload new arrived payload
     * @throws HandlerNotRegisteredException if no type for this payload has been registered.
     */
    public void dispatch(String remoteId, Payload payload) throws HandlerNotRegisteredException {
        String messageType = payload.getType();
        if (dispatcher.containsKey(messageType)) {
            dispatcher.get(messageType).onNewPayload(remoteId, payload);
        } else {
            throw new HandlerNotRegisteredException(messageType);
        }
    }
}
