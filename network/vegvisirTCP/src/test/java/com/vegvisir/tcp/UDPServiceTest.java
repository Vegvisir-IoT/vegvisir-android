package com.vegvisir.tcp;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class UDPServiceTest {


    @Test(timeout = 5000)
    public void serviceTest() {
        Config config = new Config("Device", 10012, "230.0.0.1");
        try {
            AdvertisingService advertisingService = new AdvertisingService(config);
            DiscoveringService dIscoveringService = new DiscoveringService(config);
            TCPService tcpService = new TCPService(config);
            tcpService.startTCPServer();
            advertisingService.startAdvertising();
            dIscoveringService.startDiscovering();
            com.vegvisir.network.datatype.proto.UDPAdvertisingMessage message = dIscoveringService.waitingAdvertising();
            advertisingService.stopAdvertising();
            dIscoveringService.stopDiscovering();
            Assert.assertEquals(config.getTcpPort(), message.getTcpPort());
            Assert.assertEquals(config.getAddress(), message.getIpAddress());
            Assert.assertEquals(config.getDeviceID(), message.getDeviceId());
            System.out.println("Prepare TEST PASSED");
            tcpService.startTCPClient(message);
            String id = tcpService.waitingConnections();
            Assert.assertEquals(message.getDeviceId(), id);
            System.out.println("ALL TEST PASSED");
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }
}
