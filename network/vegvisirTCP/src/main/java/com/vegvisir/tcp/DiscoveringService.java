package com.vegvisir.tcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingDeque;
import com.vegvisir.network.datatype.proto.UDPAdvertisingMessage;

public class DiscoveringService {


    private MulticastSocket socket;

    private Config config;

    private InetAddress multicastGroupAddress;

    private boolean discovering = false;

    private final LinkedBlockingDeque<UDPAdvertisingMessage> receivedMessage = new LinkedBlockingDeque<>();

    private byte[] buf = new byte[1024];

    public DiscoveringService(Config config) throws IOException {
        this.config = config;
        socket = new MulticastSocket(config.getUdpPort());
        multicastGroupAddress = InetAddress.getByName(config.getMulticastGroupName());
    }

    public UDPAdvertisingMessage waitingAdvertising() throws InterruptedException {
        return receivedMessage.take();
    }


    public void startDiscovering() {
        receivedMessage.clear();
        new Thread(() -> {
            synchronized (receivedMessage) {
                discovering = true;
            }
            try {
                socket.joinGroup(multicastGroupAddress);
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            while (discovering) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                    receivedMessage.add(UDPAdvertisingMessage.parseFrom(Arrays.copyOfRange(packet.getData(), packet.getOffset(), packet.getLength())));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            try {
                socket.leaveGroup(multicastGroupAddress);
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void stopDiscovering() {
        synchronized (receivedMessage) {
            discovering = true;
            receivedMessage.clear();
        }
    }
}
