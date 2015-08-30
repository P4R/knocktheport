package com.albujer.pere.knocktheport;

import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;


public class PortKnocking {


    public Boolean knockTCP(InetAddress address, int port) {
        Boolean res = true;
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), 100);
            socket.close();
        } catch (SocketTimeoutException e) {
            // Conexión correcta puerto cerrado o DROP
        } catch (ConnectException e) {
            if (e.getMessage().contains("ENETUNREACH")) {
                e.printStackTrace();
                res=false;
            }
            // Conexión correcta Firewall REJECT
        } catch (UnknownHostException e) {
            e.printStackTrace();
            res=false;
        } catch (IOException e) {
            e.printStackTrace();
            res=false;
        }
        return res;
    }

    public Boolean knockUDP(InetAddress address, int port) {
        Boolean res=true;
        try {
            DatagramSocket socket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(new byte[]{1}, 1, address, port);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
            res=false;
        }
        return res;
    }
}
