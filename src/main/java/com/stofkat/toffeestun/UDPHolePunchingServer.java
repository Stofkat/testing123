/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stofkat.toffeestun;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Dirk
 */
public class UDPHolePunchingServer {

    public static final char kHeartbeat = 'b';
    public static final char kData = 'd';
    public static final char kMediaList = 'l';
    public static final char kPlay = 'p';
    public static final char kSearch = 's';
    public static final char kDisconnect = 'x';
    public static final char kServerRegister = 'R';
    public static final char kServerReconnect = 'r';
    public static final char kP2PConnect = 'c';
    public static final char kServerConnectionRequest = 'C';
    
    DatagramSocket serverSocket;
    HashMap<String, UDPPeer> connectionList = new HashMap();
    Thread cleanupThread;
    int numberOfConnections = 0;

    char[] UIDTable = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',};

    // Implementing Fisher–Yates shuffle
    static void shuffleArray(char[] ar) {
        // If running on Java 6 or older, use `new Random()` on RHS here
        Random rnd = ThreadLocalRandom.current();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            char a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private String generateGUID() {

        String UUIDString = UUID.randomUUID().toString();
        UUIDString = UUIDString.substring((int) (UUIDString.length() * 0.7)).replace("-", "");
        String digits = "";
        int remaining = numberOfConnections;
        do {
            digits += "" + UIDTable[remaining % UIDTable.length];
            remaining = remaining / UIDTable.length;

        } while (remaining >= 1);
        System.out.println(digits);
        numberOfConnections++;
        return UUIDString + digits;
    }

    public UDPHolePunchingServer() {
        try {

            //shuffleArray(UIDTable);
            serverSocket = new DatagramSocket(Integer.valueOf(System.getenv("PORT")));
            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, 1024);

            startCleanupService();
            System.out.println("Started server..");
            while (true) {
                serverSocket.receive(dp);

                UDPPeer newPeer = new UDPPeer(dp);
                String message;
                DatagramPacket sendPacket;
                switch (newPeer.content.charAt(0)) {

                    case kServerRegister:
                        newPeer.guid = generateGUID();
                        connectionList.put(newPeer.guid, newPeer);
                        message = kServerRegister + newPeer.guid;
                        sendPacket = new DatagramPacket(message.getBytes(), message.length(), dp.getAddress(), dp.getPort());
                        serverSocket.send(sendPacket);
                        break;
                    case kServerReconnect:
                        UDPPeer serverPeer = connectionList.get(newPeer.content.substring(1));
                        if (serverPeer != null) {
                            serverPeer.ipAddress = dp.getAddress().toString().substring(1);
                            serverPeer.port = dp.getPort();
                            message = kServerReconnect + serverPeer.guid;
                            sendPacket = new DatagramPacket(message.getBytes(), message.length(), dp.getAddress(), dp.getPort());
                            serverSocket.send(sendPacket);
                        }else {
                        newPeer.guid = generateGUID();
                        connectionList.put(newPeer.guid, newPeer);
                        message = kServerRegister + newPeer.guid;
                        sendPacket = new DatagramPacket(message.getBytes(), message.length(), dp.getAddress(), dp.getPort());
                        serverSocket.send(sendPacket);
                        }
                        break;
                    case kServerConnectionRequest:
                        newPeer.guid = newPeer.content.substring(1);
                        UDPPeer oldPeer = connectionList.get(newPeer.guid);
                        if (oldPeer != null) {
                            String peer1 = kServerConnectionRequest + oldPeer.ipAddress + ":" + oldPeer.port + ":";
                            String peer2 = kServerConnectionRequest + newPeer.ipAddress + ":" + newPeer.port + ":";
                            DatagramPacket serverPacket = new DatagramPacket(peer1.getBytes(), peer1.length(), dp.getAddress(), dp.getPort());
                            serverSocket.send(serverPacket);

                            DatagramPacket clientPacket = new DatagramPacket(peer2.getBytes(), peer2.length(), InetAddress.getByName(oldPeer.ipAddress), oldPeer.port);
                            serverSocket.send(clientPacket);
                            break;

                        }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void startCleanupService() {
        cleanupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(120000);
                    for (Entry<String, UDPPeer> entry : connectionList.entrySet()) {
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        cleanupThread.start();
    }

    public static void main(String args[]) {

        new UDPHolePunchingServer();
    }
}
