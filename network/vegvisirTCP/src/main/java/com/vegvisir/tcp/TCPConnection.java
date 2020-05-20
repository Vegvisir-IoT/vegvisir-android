package com.vegvisir.tcp;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import com.vegvisir.network.datatype.proto.Payload;

public class TCPConnection {

    private static ExecutorService pool = Executors.newCachedThreadPool();

    private Socket socket;
    private String remoteID;
    private LinkedBlockingDeque<Pair<String, Payload>> payloads;

    public TCPConnection(String remoteID, Socket socket, LinkedBlockingDeque<Pair<String, Payload>> payloadBuffer) {
        this.remoteID = remoteID;
        this.socket = socket;
        this.payloads = payloadBuffer;
        pool.submit(this::run);
    }

    private void run() {
        while (true) {
            try {
                Payload payload = Payload.parseDelimitedFrom(socket.getInputStream());
                payloads.add(new Pair<>(remoteID, payload));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    public void write(Payload payload) throws IOException {
        payload.writeDelimitedTo(socket.getOutputStream());
        socket.getOutputStream().flush();
    }

    /**
     * ONLY write ONCE by clients upon connection established.
     * @param message
     * @throws IOException
     */
    public void write(com.vegvisir.network.datatype.proto.UDPAdvertisingMessage message) throws IOException {
        message.writeDelimitedTo(socket.getOutputStream());
        socket.getOutputStream().flush();
    }

    public boolean isSocketAvailable() {
        return socket.isConnected();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
