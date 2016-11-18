/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package holepunchingserver;

import java.net.DatagramPacket;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Dirk
 */
public class UDPPeer {

    
    public String ipAddress;
    public String guid;
    public int port;
    public String clientKey;
    public String content;
    public boolean isServer;


    public UDPPeer(DatagramPacket packet) {

        ipAddress = packet.getAddress().toString().substring(1);
        port = packet.getPort();
        content = new String(packet.getData(), 0, packet.getLength());
        System.out.println(content + ":" + ipAddress + ":" + port);
        
    }

}
