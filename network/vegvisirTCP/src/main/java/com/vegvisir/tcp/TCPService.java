package com.vegvisir.tcp;

import com.vegvisir.network.datatype.proto.Payload;
import com.vegvisir.network.datatype.proto.UDPAdvertisingMessage;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class TCPService {

    private ServerSocket server;

    private LinkedBlockingDeque<String> establishedConnections;
    private LinkedBlockingDeque<String> disconnectedConnections;

    private Map<String, TCPConnection> connections;
    private Map<String, TCPConnection> clientConnections;

    private LinkedBlockingDeque<Pair<String, Payload>> payloads;

    private Config config;

    ExecutorService pool;


    public TCPService(Config config) {
        this.config = config;
        this.connections = new ConcurrentHashMap<>();
        this.clientConnections = new ConcurrentHashMap<>();
        pool = Executors.newCachedThreadPool();
        establishedConnections = new LinkedBlockingDeque<>();
        disconnectedConnections = new LinkedBlockingDeque<>();
        payloads = new LinkedBlockingDeque<>();
    }


    public void startTCPServer() {
        pool.submit(() -> {
            try {
                server = new ServerSocket(0);
                config.setTcpPort(server.getLocalPort());
            } catch (IOException ex) {
                ex.printStackTrace();
                return;
            }
            while (true) {
                try {
                    Socket socket = server.accept();
                    UDPAdvertisingMessage message = UDPAdvertisingMessage.parseDelimitedFrom(socket.getInputStream());
                    if (isConnected(message.getDeviceId())) {
                        socket.close();
                        continue;
                    }
                    connections.put(message.getDeviceId(), new TCPConnection(message.getDeviceId(), socket, payloads));
                    establishedConnections.add(message.getDeviceId());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public void startTCPClient(UDPAdvertisingMessage message) {
        if (isConnected(message.getDeviceId()))
            return;
        pool.submit(() -> {
            try {
                Socket socket = new Socket(message.getIpAddress(), message.getTcpPort());
                clientConnections.put(message.getDeviceId(), new TCPConnection(message.getDeviceId(), socket, payloads));
                TCPConnection connection = clientConnections.get(message.getDeviceId());
                connection.write(message);
                establishedConnections.add(message.getDeviceId());
            } catch (IOException ex) {

            }
        });
    }

    /**
     * [BLOCKING]
     * @return
     */
    public String waitingConnections() throws InterruptedException {
        return establishedConnections.take();
    }

    public void send(String remoteID, Payload payload) throws IOException {
        if (connections.containsKey(remoteID)) {
            connections.get(remoteID).write(payload);
        } else {
            throw new IOException("No such connection with id " + remoteID);
        }
    }

    private synchronized boolean isConnected(String remoteID) {
        if (connections.containsKey(remoteID)) {
            if (!connections.get(remoteID).isSocketAvailable()) {
                connections.remove(remoteID);
            }
        }

        if (clientConnections.containsKey(remoteID)) {
            if (!clientConnections.get(remoteID).isSocketAvailable()) {
                clientConnections.remove(remoteID);
            }
        }
        if (clientConnections.containsKey(remoteID) || connections.containsKey(remoteID)) {
            return true;
        } else {
            disconnectedConnections.add(remoteID);
            return false;
        }
    }

    public Pair<String, Payload> blockingRecvPayload() throws InterruptedException {
            return payloads.take();
    }

    public void disconnect(String id) {
        if (connections.containsKey(id)) {
            connections.get(id).close();
        }

        if (clientConnections.containsKey(id)) {
            clientConnections.get(id).close();
        }
        isConnected(id);
    }

    public String waitingDisconnectedEvent() throws InterruptedException {
        return disconnectedConnections.take();
    }
}
