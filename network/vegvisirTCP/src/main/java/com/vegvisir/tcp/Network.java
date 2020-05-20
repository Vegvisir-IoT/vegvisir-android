package com.vegvisir.tcp;

import java.io.IOException;
import java.util.function.Consumer;

import com.vegvisir.network.datatype.proto.Payload;

public class Network {

    private AdvertisingService advertisingService;

    private DiscoveringService discoveringService;

    private TCPService tcpService;

    private Config config;

    private Consumer<String> lostConnectionHandler;

    private boolean running = true;

    private static String multicastAddress = "230.0.0.1";

    public Network(String deviceID, int udpPort, String multicastAddress) throws IOException {
        config = new Config(deviceID, udpPort, multicastAddress);
        advertisingService = new AdvertisingService(config);
        discoveringService = new DiscoveringService(config);
        tcpService = new TCPService(config);
        new Thread(this::run).start();
    }

    public Network(String deviceID, int udpPort) throws IOException {
        this(deviceID, udpPort, multicastAddress);
    }

    private void run() {
        tcpService.startTCPServer();
        advertisingService.startAdvertising();
        discoveringService.startDiscovering();
        while(running) {
            try {
                tcpService.startTCPClient(discoveringService.waitingAdvertising());
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
        discoveringService.stopDiscovering();
        advertisingService.stopAdvertising();
    }

    public void send(String remoteID, Payload payload) throws IOException {
        tcpService.send(remoteID, payload);
    }

    public String waitingConnection() throws InterruptedException {
        return tcpService.waitingConnections();
    }

    public Pair<String, Payload> blockingRecvData() throws InterruptedException {
        return tcpService.blockingRecvPayload();
    }

    public void stop() {
        running = false;
    }

    public void disconnect(String id) {
       tcpService.disconnect(id);
    }

    public void onConnectionLost(Consumer<String> handler) {
        lostConnectionHandler = handler;
    }

    private void connectionCheck() {
        while (running) {
            try {
                String id = tcpService.waitingDisconnectedEvent();
                if (lostConnectionHandler != null)
                    lostConnectionHandler.accept(id);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}
