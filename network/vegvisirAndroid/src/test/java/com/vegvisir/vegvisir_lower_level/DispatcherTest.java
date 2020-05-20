package com.vegvisir.vegvisir_lower_level;

import com.vegvisir.vegvisir_lower_level.network.Dispatcher;
import com.vegvisir.vegvisir_lower_level.network.Exceptions.HandlerNotRegisteredException;
import com.vegvisir.vegvisir_lower_level.network.PayloadHandler;
import com.vegvisir.lower.datatype.proto.Payload;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class DispatcherTest {

    private Dispatcher dispatcher;
    private PayloadHandler payloadHandler;
    private PayloadHandler.Handler handler;
    private PayloadHandler.Handler secondHandler;
    private String remoteId = "testConn";
    private String info = "hello";
    private String info2 = "world";
    private BlockingDeque<Integer> waiting;

    @Before
    public void init() {
        dispatcher = new Dispatcher();
        waiting = new LinkedBlockingDeque<>();

        handler = (data) -> {
            Assert.assertEquals(remoteId, data.first);
            Payload p = data.second;
            Assert.assertEquals("abc", p.getType());
            Assert.assertEquals(info, p.getInfo());
            waiting.add(1);
        };

        secondHandler = (data) -> {
            Assert.assertEquals(info2, data.second.getInfo());
            waiting.add(1);
        };
    }

    @Test(timeout = 1000)
    public void registerHandler() {
        Payload payload = Payload.newBuilder().setType("abc").setInfo(info).build();
        payloadHandler = new PayloadHandler(handler);
        dispatcher.registerHandler("abc",  payloadHandler);
        try {
            dispatcher.dispatch(remoteId, payload);
            waiting.take();
        } catch (HandlerNotRegisteredException ex) {
            Assert.fail("Register handler failed");
        } catch (InterruptedException ex) {

        }
    }

    @Test(timeout = 1000)
    public void updateHandler() {
        payloadHandler = new PayloadHandler(handler);
        dispatcher.registerHandler("abc", payloadHandler);
        Payload payload = Payload.newBuilder().setType("abc").setInfo(info2).build();
        boolean res = dispatcher.registerHandler("abc", new PayloadHandler(secondHandler));
        Assert.assertEquals(false, res);
        payloadHandler.setRecvHandler(secondHandler);
        try {
            dispatcher.dispatch(remoteId, payload);
            waiting.take();
        } catch (HandlerNotRegisteredException ex) {
            Assert.fail("Register handler failed");
        } catch (InterruptedException ex) {

        }
    }

    @Test
    public void removeHandler() {

    }
}
