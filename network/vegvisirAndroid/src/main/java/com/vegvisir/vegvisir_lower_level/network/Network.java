package com.vegvisir.vegvisir_lower_level.network;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import com.vegvisir.network.datatype.proto.Payload;
import com.vegvisir.vegvisir_lower_level.network.Exceptions.ConnectionNotAvailableException;
import com.vegvisir.vegvisir_lower_level.network.Exceptions.HandlerNotRegisteredException;

import java.util.HashMap;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * expose lower network layer APIs to upper layers.
 */
public class Network {

    private HashMap<String, EndPointConnection> endPoints;
    private ByteStream byteStream;
    private Dispatcher dispatcher;
    private Thread pollingThread;
    private BlockingDeque<String> activeConnection;


    public Network(Context context, String advisingID) {
        endPoints = new HashMap<>();
        byteStream = new ByteStream(context, advisingID);
        dispatcher = new Dispatcher();
        activeConnection = new LinkedBlockingDeque<>(1);
        byteStream.start();
    }

    /**
     * Start dispatching arrived payload by running a separate polling thread whose job is
     * keeping blocking reading input from current connection.
     */
    @Deprecated
    private void startDispatcher() {
        pollingThread = new Thread(() -> {
            for (;;) {
                String remoteId = waitingConnection();
                activeConnection.add(remoteId);
                EndPointConnection connection = byteStream.getConnectionByID(remoteId);
                while (connection.isConnected()) {
                    try {
                        Payload payload = connection.blockingRecv();
                        if (payload.getType() == null)
                            break;
                        else
                            dispatcher.dispatch(remoteId, payload);
                    } catch (InterruptedException ex) {

                    } catch (HandlerNotRegisteredException ex) {
                        Log.e(ex.getLocalizedMessage(), ex.getMessage());
                    }
                }
            }
        });
        pollingThread.start();
    }

    /**
     * [BLOCKING] waiting until get new data from remote side.
     * @return a pair of <remote id, payload>
     */
    public Pair<String, Payload> waitingData() {
        return byteStream.blockingRecv();
    }

    public String waitingConnection() {
        return byteStream.establishConnection().getRemoteID();
    }

    public void send(String id, Payload payload) throws ConnectionNotAvailableException {
        byteStream.getConnectionByID(id).send(payload);
    }

    /**
     * Ignore connections from device with @id for @timeout milliseconds.
     * @param id connection/device id
     * @param timeout in milliseconds
     */
    public void ignore(String id, Long timeout) {
        EndPointConnection connection = byteStream.getConnectionByID(id);
        if (connection != null) {
            connection.ignore(timeout);
        }
    }

    /**
     * Whether this device is connecting to another device at this moment.
     * @return True connected.
     */
    public boolean isConnected() {
        return byteStream.isConnected();
    }

    public String getActiveRemoteID() {
        return byteStream.getActiveEndPoint();
    }

    public void disconnect(String remoteID) {
        byteStream.disconnect(remoteID);
    }

    public String getDisconnectedId() throws InterruptedException {
        return byteStream.getDisconnectedId().take();
    }
}
