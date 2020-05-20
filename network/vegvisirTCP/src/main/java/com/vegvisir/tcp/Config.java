package com.vegvisir.tcp;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Config {

    private int udpPort;

    private int tcpPort;

    private String deviceID;

    private InetAddress address;

    private String multicastGroupName;

    private final Object waitLock = new Object();


    public Config(String deviceID, int udpPort, String multicastGroupName) {
        this.udpPort = udpPort;
        this.deviceID = deviceID;
        this.multicastGroupName = multicastGroupName;
        this.tcpPort = 0;
        try {
            this.address = Inet4Address.getLocalHost();
        } catch (UnknownHostException ex) {
            ex.printStackTrace();;
        }
    }

    public int getTcpPort() {
        synchronized (waitLock) {
            while (tcpPort == 0) {
                try{
                    waitLock.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return tcpPort;
    }

    public void setTcpPort(int port) {
        synchronized (waitLock) {
            tcpPort = port;
            waitLock.notifyAll();
        }
    }

    public int getUdpPort() {
        return udpPort;
    }

    public String getMulticastGroupName() {
        return multicastGroupName;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public String getAddress() {
        return address.getHostAddress();
    }
}
