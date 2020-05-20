package com.vegvisir.tcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import com.vegvisir.network.datatype.proto.UDPAdvertisingMessage;

public class AdvertisingService {


    Config config;

    private DatagramSocket socket;

    private InetAddress multicastGroup;

    private boolean advertising = false;

    public AdvertisingService(Config config) throws IOException  {
        this.config = config;
        socket = new DatagramSocket(0);
        multicastGroup = InetAddress.getByName(config.getMulticastGroupName());
    }

    private byte[] getAdvertisingMessage() {
         return UDPAdvertisingMessage.newBuilder()
                 .setDeviceId(config.getDeviceID())
                 .setProtocolSecret("VEGVISIR-IOT")
                 .setTcpPort(config.getTcpPort())
                 .setIpAddress(config.getAddress())
                 .build().toByteArray();
    }

    public void startAdvertising() {
        new Thread(() -> {
            byte[] message = getAdvertisingMessage();
            advertising = true;
            while (advertising) {
                try {
                    socket.send(new DatagramPacket(message, message.length, multicastGroup, config.getUdpPort()));
                    Thread.sleep(5000);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.err.println("Start Advertising failed due to " + ex.getLocalizedMessage());
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }).start();

    }

    public void stopAdvertising() {
        advertising = false;
    }

}
